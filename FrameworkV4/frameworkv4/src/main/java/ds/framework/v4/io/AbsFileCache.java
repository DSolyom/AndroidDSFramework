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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import ds.framework.v4.common.Debug;

abstract public class AbsFileCache<T> implements InterfaceCache<String, T> {

	private Thread mCT;
	private Thread mDT;
	
	String mDir;
	long mMaxSize;
	long mSize;
	
	public AbsFileCache(Context context, String dir, Long maxSize) {
		if (context.getExternalCacheDir() == null) {
			mDir = null;
			return;
		}
		mDir = context.getExternalCacheDir() + "/" + dir;
		File mFD = new File(mDir);
		mFD.mkdirs();
		if (mFD.exists()) {
			mSize = 0l;
			countCurrentSizeInThread(mFD);
		} else {
			mDir = null;
		}
		mMaxSize = maxSize;
	}
	
	public String getCacheDir() {
		return mDir;
	}
	
	/**
	 * get the maximum size of the cache
	 * 
	 * @return
	 */
	public Long getMaxSize() {
		return mMaxSize;
	}
	
	/**
	 * change the maximum size of the cache
	 * 
	 * @param newMaxSize
	 */
	public void changeMaxSize(Long newMaxSize) {
		mMaxSize = newMaxSize;
		if (mMaxSize != -1 && mSize > mMaxSize) {
			deleteAboveMax(new File(mDir));
		}
	}
	
	/**
	 * 
	 */
	public boolean has(String filename) {
		if (mMaxSize == 0 || mDir == null || !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return false;
		}

		synchronized(AbsFileCache.class) {
			return new File(mDir, getRealFilename(filename)).exists();
		}
	}

	/**
	 * get file content from file cache
	 * 
	 * @param filename
	 * @return
	 */
	public T get(String filename) {
		synchronized(AbsFileCache.class) {
			if (mMaxSize == 0 || mDir == null || !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				return null;
			}

			FileInputStream inStream = null;
			T result = null;
			try {
				inStream = new FileInputStream(new File(mDir, getRealFilename(filename)));
				result = getObjectFromStream(inStream);
			} catch (FileNotFoundException e) {
				return null;
			} catch (OutOfMemoryError e) {
				return null;
			} finally {
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			if (result == null) {
				
				// file exists but could not get from filesystem - remove that file
				new File(mDir, filename).delete();
			}
			
			return result;
		}
	}

	/**
	 * put content into file cache
	 */
	public T put(String filename, T value) {
		synchronized(AbsFileCache.class) {
			File file = getOutFile(filename);
			if (file == null) {
				return value;
			}
	
			if (file.exists()) {
				
				// only return value if file already exists - need to delete this to overwrite
				return value;
			}
			try {
				FileOutputStream outStream = new FileOutputStream(file);
	
				putObjectIntoStream(value, outStream);
	
				outStream.flush();
				outStream.close();
			} catch (IOException e) {
				return null;
			}
			mSize += file.length();
			if (mSize > getCurrentMaxSize()) {
				
				Debug.logW("file cache max size exceeded", mSize + "");
				
				if (mDT != null) {
					
					// already doing this
					return value;
				}
				mDT = new Thread() {
					
					@Override
					public void run() {
						deleteAboveMax(new File(mDir));
						mDT = null;
					}
				};
				mDT.start();
			}
	
			return value;
		}
	}
	
	/**
	 * 
	 * @param filename
	 * @return
	 */
	public Uri getUri(String filename) {
		if (mMaxSize == 0 || mDir == null || !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return null;
		}
		return Uri.fromFile(new File(mDir + getRealFilename(filename)));
	}
	
	/**
	 * 
	 * @return
	 */
	private Long getCurrentMaxSize() {
		long maxSize = mMaxSize;
		try {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			long sdAvailSize = (long) stat.getAvailableBlocks()
		                   	* (long) stat.getBlockSize();
			if (maxSize == -1 || maxSize > sdAvailSize / 10) {
				maxSize = sdAvailSize / 10;
			}
			return maxSize;
		} catch(Throwable e) {
			return 1024l * 1024l;
		}
	}

	/**
	 * 
	 * @param filename
	 * @return
	 */
	public File getOutFile(String filename) {
		if (mMaxSize == 0 || mDir == null || !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return null;
		}

		filename = getRealFilename(filename);
		new File(mDir, filename.substring(0, filename.lastIndexOf("/"))).mkdirs();
		return new File(mDir, filename);
	}

	/**
	 * get object from given stream
	 * 
	 * @param inStream
	 * @return
	 */
	abstract protected T getObjectFromStream(FileInputStream inStream);
	
	/**
	 * put object to given stream
	 * 
	 * @param value
	 * @param outStream
	 */
	abstract protected void putObjectIntoStream(T value, FileOutputStream outStream);

	/**
	 * count size on 'disc' in thread
	 * 
	 * @param dir
	 */
	private void countCurrentSizeInThread(final File dir) {
		if (mCT != null) {
			
			// already doing this
			return;
		}
		mCT = new Thread() {
			
			@Override
			public void run() {
				synchronized(this) {
					final long size = countAllSizeRec(dir);
					mSize = size;
					
					Debug.logW("file cache current size", mSize + "");
					
					if (mMaxSize != -1 && size > mMaxSize) {
						Debug.logW("file cache max size exceeded", mSize + "");
						deleteAboveMax(dir);
					}
					mCT = null;
				}
			}
		};
		mCT.start();
	}
	
	/**
	 * size on 'disc'
	 * 
	 * @return
	 */
	private long countAllSizeRec(File dir) {
        Long size = 0l;

        final String[] list = dir.list();

        for (String file : list) {
        	final File fFile = new File(dir, file);
        	if (fFile.isDirectory()) { 
        		size += countAllSizeRec(fFile);
        	} else {
        		size += fFile.length();
        	}
        }
        return size;
    }
	
	@Override
	public void remove(String filename) {
		new File(getRealFilename(filename)).delete();
	}
	
	/**
	 * delete oldest files till size is below or equal to max size
	 */
	synchronized private void deleteAboveMax(File dir) {
        final String[] l = dir.list();
        final long maxSize = (getCurrentMaxSize() * 3) / 4;
        
        final FileModifiedInfo[] list = new FileModifiedInfo[l.length];
        final int s = l.length;
        for(int i = 0; i < s; ++i) {
        	list[i] = new FileModifiedInfo();
        	list[i].file = new File(dir, l[i]);
        	list[i].modified = list[i].file.lastModified(); 
        }

        Arrays.sort(list, new Comparator<FileModifiedInfo>() {
            public int compare(FileModifiedInfo f1, FileModifiedInfo f2) {
                return Long.valueOf(f1.modified).compareTo(
                        f2.modified);
            }
        });
        for (FileModifiedInfo info : list) {
        	final File fFile = info.file;
        	if (fFile.isDirectory()) {
        		deleteAboveMax(fFile);
        		if (fFile.list().length == 0) {
        			fFile.delete();
        		}
        	} else {
	        	mSize -= fFile.length();
	            fFile.delete();
        	}
            if (mSize <= maxSize) {
            	break;
            }
        }
        Debug.logW("file cache above max deleted - new size", mSize + "");
	}

	public String getRealFilename(String filename) {
		return filename.replace("\\\\", "/").replace(":", "___");
	}
	
	private static class FileModifiedInfo {
		long modified;
		File file;
	}
}
