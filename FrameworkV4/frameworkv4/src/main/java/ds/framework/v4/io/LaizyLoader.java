/*
	Copyright 2012 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package ds.framework.v4.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ds.framework.v4.Global;
import ds.framework.v4.common.Debug;

import android.os.Handler;
import android.util.SparseArray;

abstract public class LaizyLoader<S, T> extends Handler {
	
	private static final int MAX_NUMBER_OF_THREADS = 5;
	private static final int MAX_NUMBER_OF_RETRIES = 3;
	
	final private ArrayList<S> mQueueOrder = new ArrayList<S>();
	final private HashMap<S, QueueItem> mQueue = new LinkedHashMap<S, QueueItem>();
	final private SparseArray<DownloaderThread> mDownloaders = new SparseArray<DownloaderThread>(MAX_NUMBER_OF_THREADS);
	final private HashMap<S, Integer> mRetries = new HashMap<S, Integer>();
	
	private boolean mNeedSleeping = true;
	
	public void load(S key, Callback<S, T> callback) {
		synchronized(mQueue) {
			
			// check if already loading for this key
			QueueItem existing = mQueue.get(key);
			if (existing != null) {
				if (!existing.callbacks.contains(callback)) {
					existing.callbacks.add(callback);
				}

				mQueueOrder.remove(key);
			} else {
				
				// new item or retry
				mQueue.put(key, new QueueItem(key, callback));				
			}
			mQueueOrder.add(0, key);

			// start downloading (or do nothing if downloader is already working)
			synchronized(DownloaderThread.class) {
				for(int i = 0; i < MAX_NUMBER_OF_THREADS; ++i) {
					if (mDownloaders.get(i) == null) {
						mDownloaders.put(i, new DownloaderThread(i));
Debug.logE("starting", mDownloaders.get(i).getId() + "");
						mDownloaders.get(i).start();
						break;
					}
				}
			}
		}
	}
	
	/**
	 * stop loading item with key 'key' with callback 'callback'
	 * 
	 * @param key
	 * @param callback
	 */
	public void stopLoading(S key, Callback<S, T> callback) {
		synchronized(mQueue) {
			QueueItem existing = mQueue.get(key);
			if (existing != null) {
				existing.callbacks.remove(callback);
				if (existing.callbacks.isEmpty()) {
					mQueue.remove(key);
					mQueueOrder.remove(key);
				}
			}
		}
	}
	
	public void needSleeping(boolean need) {
		mNeedSleeping = need;
	}
	
	/**
	 * @class QueueItem
	 */
	public class QueueItem {
		public S item;
		final ArrayList<Callback<S, T>> callbacks = new ArrayList<Callback<S, T>>();
		boolean downloading = false;

		public QueueItem(S item, Callback<S, T> callback) {
			this.item = item;
			callbacks.add(callback);
		}
	}
	
	public interface Callback<S, T> {
		
		/**
		 * called when load is finished - it is unsure if it was successful or not at this point<br/>
		 * !called from a Thread
		 * 
		 * @param item
		 */
		public void onLoadFinished(S item, T result);
		
		/**
		 * called when load is past retry limit - it is surely a failure<br/>
		 * !called from a Thread
		 * 
		 * @param item
		 */
		public void onLoadFailed(S item);
	}

	/**
	 * @class DownloaderThread
	 */
	private class DownloaderThread extends Thread {
		
		int id;
		QueueItem current;
		
		public DownloaderThread(int id) {
			this.id = id;
			setPriority(Thread.MIN_PRIORITY);
		}
		
		@Override
		public void run() {
			while(true) {
				current = null;
				synchronized(mQueue) {
					if (!mQueue.isEmpty()) {
						
						// set first (which is not downloading) from queue for downloading
						for(S key : mQueueOrder) {
							current = mQueue.get(key);
							if (current != null && !current.downloading) {
								break;
							}
						
						}
						if (current == null || current.downloading) {
							current = null;
						} else {
							current.downloading = true;
						}
					}
					if (current == null) {
						
						// everthing is taken care of? this means that this thread is finished
						mDownloaders.remove(id);
Debug.logE("remove thread", getId() + "");
						break;
					}
				}

				Integer retryCnt = 0;
				synchronized(mRetries) {
					retryCnt = mRetries.get(current.item);
					if (retryCnt == null) {
						
						// first here
						mRetries.put(current.item, retryCnt = 0);
					} else if (retryCnt == MAX_NUMBER_OF_RETRIES) {
Debug.logE("laizy loader", "max number of retries exceeded (" + current.item.toString() + ")");
						// max number of retries exceeded - show error
						final S item = current.item;
						
						onLoadFailure(current);

						synchronized(mQueue) {
							mQueue.remove(current.item);
							mQueueOrder.remove(current.item);
						}
						
						continue;
					}
				}

				if (mNeedSleeping) {
					try {
						
						// sleep a bit - more for every retry
						// always sleep mainly for images, because if not
						// there is some kind of os bug preventing some images to show
						// also we use sleep to skip unwanted elements while scrolling a list
						sleep(retryCnt * 75 + 275);
					} catch (InterruptedException e) {
						;
					}
				}

				synchronized(mQueue) {
					if (current.callbacks.isEmpty()) {

						// image not needed anymore
						continue;
					} 
				}
		
				// load in background
				if (loadInBackground(current)) {
					synchronized(mRetries) {
						mRetries.remove(current.item);
					}
				} else {
					
					// add to retry count (unless there is a connection problem)
					boolean hasConnection = true;
					try {
						hasConnection = ConnectionChecker.check(Global.getContext(), false);
					} catch(Throwable e) {
						;
					}
					if (hasConnection) {
						synchronized(mRetries) {
							mRetries.put(current.item, retryCnt + 1);
						}
					}
				}
				
				// ok downloaded (or failed - you have to handle failure outside)
				synchronized(mQueue) {
					mQueue.remove(current.item);
					mQueueOrder.remove(current.item);
				}
			}
		}
	}
	
	protected void onLoadFinished(QueueItem current, T result) {
		synchronized(mQueue) {

			// go through all callback for this item
			for(Callback<S, T> iv : current.callbacks) {
				iv.onLoadFinished(current.item, result);
			}
		}
	}
	
	protected void onLoadFailure(QueueItem current) {
		synchronized(mQueue) {
		
			// go through all callback for this item to notify about failure
			for(Callback<S, T> iv : current.callbacks) {
				iv.onLoadFailed(current.item);
			}
		}
	}
	
	/**
	 * call onLoadFinished(item) if successful<br/>
	 * call onLoadFailure(item) if not
	 * 
	 * @param item
	 * @return false if need to increase the retry count for this item
	 */
	abstract protected boolean loadInBackground(QueueItem item);
}
