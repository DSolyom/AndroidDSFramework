/*
	Copyright 2011 Dániel Sólyom

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

import java.util.HashMap;

import ds.framework.v4.common.Debug;

import android.os.Handler;

public abstract class BackgroundThread {

	public static final int EMPTY = 0;
	public static final int RUNNING = 1;
	public static final int FINISHING = 2;
	public static final int FINISHED = 4;
	public static final int INTERRUPTED = 8;
	
	static private HashMap<String, BackgroundThread> sThreads = new HashMap<String, BackgroundThread>();
		
	private String mTag;
	
	private Result mResult;
	
	/**
	 * 
	 * @param tag
	 * @return
	 */
	static public BackgroundThread getThreadByTag(String tag) {
		return sThreads.get(tag);
	}
	
	/**
	 * !note: call from the same thread where start were called from
	 * 
	 * @param tag
	 * @return
	 */
	synchronized static public Result getResult(String tag, boolean remove) {
		if (tag == null) {
			return null;
		}
		final BackgroundThread thread = sThreads.get(tag);
		if (thread == null) {
			return null;
		}
		if (remove && thread.mResult != null) {
			sThreads.remove(tag);
		}

		return thread.mResult;
	}
	
	private WorkingThread mThread;
	protected Handler mHandler;
	
	private long mLoadInterval;
	
	private int mPriority = Thread.NORM_PRIORITY;
	protected boolean mContinuous;
	
	private boolean mSuccessful;
	private boolean mFirstRun;
	private boolean mAfterWait;
	private boolean mAfterReset;
	
	public BackgroundThread() {
		this(false);
	}
	
	public BackgroundThread(boolean continuous) {
		reset();
		mContinuous = continuous;
		mHandler = null;
	}
	
	public BackgroundThread(boolean continuous, Handler handler) {
		reset();
		
		mContinuous = continuous;
		mHandler = handler;
	}
	
	synchronized public void reset() {
		if (getState() == RUNNING) {
			requestInterrupt();
		}
		mFirstRun = true;
		mSuccessful = false;
		mAfterReset = true;
	}
	
	synchronized public void start(String tag) throws IllegalThreadStateException {
		if (mHandler == null) {
			mHandler = new Handler();
		}
		if (getState() != RUNNING) {
			
			if (tag != null) {
				
				// save thread in the static sThreads to be recoverable
				mResult = null;
				if (!tag.equals(mTag)) {
					sThreads.remove(mTag);
				}
				mTag = tag;
				sThreads.put(tag, this);
			} else {
				if (mTag != null) {
					sThreads.remove(mTag);
				}
				mTag = null;
			}
			
			if (mThread != null && getState() == INTERRUPTED) {
				
				// requested interrupt but not yet set to FINISHED
				mThread.setRunState(FINISHED);
			}
			mThread = new WorkingThread();
			mThread.setRunState(RUNNING);
			if (mFirstRun) {
				mAfterWait = true;
			}
			mThread.start();
		} else {
			throw(new IllegalThreadStateException("BackgroundThread already started!"));
		}
	}
	
	/**
	 * get the state of the loader thread<br/>
	 * <b>!note!</b>: synchronize with this thread when acquiring the state because 
	 * it can change state before using the value
	 * 
	 * @return
	 */
	public int getState() {
		if (mThread != null) {
			return mThread.getRunState();
		}
		return EMPTY;
	}
	
	public void finishAfterCycle() {
		mContinuous = false;
	}
	
	/**
	 * set priority of the working thread(s)
	 * 
	 * @param priority
	 */
	public void setPriority(int priority) {
		mPriority = priority;
	}
	
	/**
	 * returns the current threads priority or the will be priority if thread is not running
	 * 
	 * @return
	 */
	synchronized public int getPriority() {
		if (getState() == RUNNING) {
			return mThread.getPriority();
		}
		return mPriority;
	}
	
	/**
	 * get interval between cycles
	 * 
	 * @return
	 */
	public long getLoadInterval() {
		return mLoadInterval;
	}
	
	/**
	 * set interval between cycles
	 * 
	 * @param time
	 */
	public void setLoadInterval(long time) {
		mLoadInterval = time;
	}
	
	public void requestStartAgain() {
		mFirstRun = true;
	}

	/**
	 * request pause (an interrupt on the thread)
	 */
	synchronized public void pause() {
		if (mThread != null) {
			mThread.mRemoveWhenFinished = (getState() != RUNNING);
		}
		if (getState() == RUNNING) {
			mThread.setRunState(INTERRUPTED);
			mThread.interrupt();
		}
	}
	
	/**
	 * request an interrupt on the thread
	 */
	synchronized public void requestInterrupt() {
		if (getState() == RUNNING) {
			mThread.setRunState(INTERRUPTED);			
			mThread.interrupt();
			onInterrupt();
		}
	}
	
	/**
	 * interrupt current thread and restart
	 */
	synchronized public void restart() throws IllegalThreadStateException {
		reset();
		start(mTag);
	}
	

	synchronized public void resume() {
		try {
			start(mTag);
		} catch(IllegalThreadStateException e) {
			;
		}
	}

	/**
	 * called to create the result of the thread just before it posts onFinished or onFailure<br/>
	 * can be used to create results which (may) survives the destruction of the activity
	 * 
	 * @param success
	 * @return
	 */
	protected Object createResult(boolean success) {
		return null;
	}
	
	/**
	 * return preset result - used when no tag given for the thread
	 * 
	 * @return
	 */
	protected Result getResult() {
		return mResult;
	}

	/**
	 * act on interrupt
	 */
	protected void onInterrupt() {
		if (mTag != null) {
			
			// remove thread from saved
			sThreads.remove(mTag);
		}
	}
	
	/**
	 * act on finished loading everything
	 */
	protected void onFinished() {
	}
	
	/**
	 * act on loading failure
	 */
	protected void onFailure() {
	}
	
	/**
	 * act on successfully finishing a cycle<br/>
	 * <b>Note:</b> this is still inside the working thread
	 * 
	 * @return
	 */
	protected boolean onCycleFinished() {
		return true;
	}
	
	/**
	 * act on finishing a cycle with an error
	 * 
	 * @return
	 */
	protected boolean onCycleFailure() {
		return false;
	}
	
	/**
	 * post from onCycleFinished when needed
	 * 
	 * @param postMe
	 */
	public void post(Runnable postMe) {
		mHandler.post(postMe);
	}

	class WorkingThread extends Thread {
		
		boolean mRemoveWhenFinished = true;
		private int mState = RUNNING;
		private boolean cycleSuccess;
		
		@Override
		public void run() {
			synchronized(BackgroundThread.this) {
				if (interrupted()) {
					if (mState != INTERRUPTED) {
						
						// onInterrupt was not called yet
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								onInterrupt();
							}
						});
					}
					mState = INTERRUPTED;
				}
				if (mState == INTERRUPTED) {
					interrupt();
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mState = FINISHED;
						}
						
					});
	    			return;
				}
			}

			mThread = this;
			
			setPriority(mPriority);

			if (!mAfterReset) {
				mFirstRun = !mSuccessful;
				cycleSuccess = mSuccessful;
			} else {
				mAfterReset = false;
			}
			while(true) {
				synchronized(BackgroundThread.this) {

					if (isInterrupted() || mState == INTERRUPTED) {
						if (mState != INTERRUPTED) {

							// onInterrupt was not called yet
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {
									onInterrupt();
								}
							});
						}
						interrupt();
						mState = FINISHED;
		    			break;
					}
					if (!mFirstRun) {
						mSuccessful = cycleSuccess = cycleSuccess ? onCycleFinished() : onCycleFailure();
						if (!mContinuous) {
							
							mResult = new Result(createResult(mSuccessful), mSuccessful);
							
							mHandler.post(cycleSuccess ? 
								new Runnable() {
				    				
				    				@Override
				    				public void run() {
				    					if (mState == INTERRUPTED) {
				    						
				    						// interrupted at the last moment
				    						mState = FINISHED;
				    						return;
				    					}
				    					if (mState == FINISHED || mState == FINISHING) {
				    						
				    						// probably interrupted and already started another thread
				    						return;
				    					}
										mState = FINISHING;
				    					onFinished();
                                        mState = FINISHED;
				    				} 		
				    	    	} : new Runnable() {
				    				
				    				@Override
				    				public void run() {
				    					if (mState == INTERRUPTED) {
				    						
				    						// interrupted at the last moment
				    						mState = FINISHED;
				    						return;
				    					}
				    					if (mState == FINISHING || mState == FINISHED) {
				    						
				    						// probably interrupted and already started another thread
				    						return;
				    					}
										mState = FINISHING;
				    					onFailure();
                                        mState = FINISHED;
				    				}
				    	    	}
			    	    	);
							break;
						}
					}
					mFirstRun = false;
				}
				
	    		// sleep if load interval is set
	    		if (!mAfterWait) {
	    			mAfterWait = true;
	    			try {
	    				sleep(mLoadInterval);
	    			} catch(InterruptedException e) {
	    				continue;
	    			}
	    			continue;
	    		}
	    		if (mLoadInterval > 0) {
	    			mAfterWait = false;
	    		}
	    		cycleSuccess = mSuccessful = false;

	    		// and now the real job
	    		cycleSuccess = runCycle(this);
			}
		}
		
		public void setRunState(int state) {
			mState = state;
		}

		public int getRunState() {
			return mState;
		}
	}
	
	/**
	 * @class Result
	 */
	public class Result {
		public Object data;
		public boolean success;
		
		public Result(Object data, boolean success) {
			this.data = data;
			this.success = success;
		}
	}
	
	abstract protected boolean runCycle(Thread in);


}
