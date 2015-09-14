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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import ds.framework.v4.Global;
import ds.framework.v4.common.Bitmaps;
import ds.framework.v4.common.Debug;
import ds.framework.v4.common.Files;
import ds.framework.v4.io.LaizyLoader.Callback;

public class ImageLoader extends LaizyLoader<ImageLoader.ImageInfo, Bitmap> {

	private static LocalImageCache sCache;
	
	/**
	 * get an ImageLoader singleton<br/>
	 * !please note: cache size is only set when using for the first time
	 * 
	 * @param context
	 * @param capacity
	 * @param imagedir
	 * @param filemaxsize
	 * @param useReferenceCount
	 * @return
	 */
	public static ImageLoader getInstance(Context context, long capacity, String imagedir, long filemaxsize) {
		if (sCache == null) {
			sCache = new LocalImageCache(context, capacity, imagedir, filemaxsize);
		}
		return new ImageLoader();
	}
	
	/**
	 * get bitmap from cache
	 * 
	 * @param info
	 * @return
	 */
	public static Bitmap getFromCache(ImageInfo info) {
		if (sCache == null) {
			return null;
		}
		return sCache.getBitmap(info);
	}
	
	/**
	 * get uri for cache file
	 * 
	 * @param info
	 * @return
	 */
	public static Uri getCacheUri(ImageInfo info) {
		if (sCache == null) {
			return null;
		}
		return sCache.getUri(info.prefix + info.url);
	}
	
	@Override
	public boolean loadInBackground(QueueItem item) {
		while (sCache != null && sCache.isFull()) {
Debug.logW("ImageLoader", "!cache full!");	
			
			// tell the cache to remove some
			sCache.onCapacityFull();
		}

		final Bitmap bmp = loadImage(item.item);
		if (bmp != null) {
Debug.logNativeHeapAllocatedSize();
			onLoadFinished(item, bmp);
			return true;
		}

		onLoadFailure(item);
		
		try {
			
			// increase retry count only if we had connection
			return !ConnectionChecker.check(Global.getContext(), false);
		} catch(Throwable e) {
			
			// be safe - do not add increase retry count
			return true;
		}
	}
	
	/**
	 * call from thread to load image (either from file or net) in background<br/>
	 * most steps are synchronized except for the real downloading
	 * 
	 * @param info
	 * @return
	 */
	public static Bitmap loadImage(ImageInfo info) {
		final byte[] byteArray = loadImageByteArray(info);
    	if (byteArray != null) {
    		return Bitmaps.createBitmap(byteArray);
    	}
    	return null;
	}
	
	/**
	 * call from thread to load image as byte array (either from file or net) in background<br/>
	 * most steps are synchronized except for the real downloading 
	 * 
	 * @param info
	 * @return
	 */
	public static byte[] loadImageByteArray(ImageInfo info) {
		if (!info.url.substring(0, 4).equals("http")) {
			info.url = "http://" + info.url;
    	}
		
		try {
			// first try file cache
			byte[] byteArray = null;
	
			synchronized(ImageLoader.class) {
				if (info.cacheType != ImageInfo.CACHE_TYPE_NONE && sCache != null) {
					try{
					
						// try with prefix
						byteArray = sCache.getByteArrayFromFile(info.prefix + info.url);
						
						if (byteArray != null) {
							sCache.put(info.prefix + info.url, byteArray, info.cacheType == ImageInfo.CACHE_TYPE_FULL);
							return byteArray;
						}
						
					} catch(Throwable e){
						Debug.logException(e);
					}
				}
			}
Debug.logD("ImageLoader", "download start: " + info.url);

			// put downloaded image to memory and file cache or only file cache if it is 'big'
			Point max = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);;
      		if (info.prefix == null || info.prefix.length() == 0 || "big".equals(info.prefix)) {
      			max.x = (int) Global.getScreenWidth();
      			max.y = (int) Global.getScreenHeight();
      		} else {	
      			// check if we may need to resize the image	      			
      			if (info.prefix.length() > 0 && info.cacheType != ImageInfo.CACHE_TYPE_NONE && sCache != null) {
      				
      				parsePrefix(info.prefix, max);	      				
      			}
      		}
			if (info.cacheType != ImageInfo.CACHE_TYPE_NONE && sCache != null && max.x != Integer.MAX_VALUE) {
				// we may need to resize the image
				
				// we already tried this to load from file cache
				// so try to load from file without prefix as we haven't tried that
				// resizing while doing it
				byteArray = sCache.getByteArrayFromFile(info.url, max.x, max.y);
			}

      			
  			if (byteArray == null) {
  				
Debug.logD("ImageLoader", "downloading: " + info.url);
  				
				if (max.x == Integer.MAX_VALUE) {
					byteArray = Bitmaps.downloadImageAsByteArray(new URL(info.url));
				} else {
					final Bitmap bmp = Bitmaps.getResizedImageFromHttpStream(new URL(info.url), max.x, max.y);
					byteArray = Bitmaps.compressBitmapToByteArray(bmp, 
							"big".equals(info.prefix) ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, 100);
					bmp.recycle();
				}
  			}
  			
			synchronized(ImageLoader.class) {
				if (info.cacheType != ImageInfo.CACHE_TYPE_NONE && sCache != null) {
					sCache.put(info.prefix + info.url, byteArray, info.cacheType == ImageInfo.CACHE_TYPE_FULL);
				}
			}
      		return byteArray;

		} catch(Throwable e){
			Debug.logException(e);
		}

		return null;
	}
	
	/**
	 * encode url - just change // -s
	 * 
	 * @param url
	 * @return
	 */
	static String encodeUrl(String url) {
		return url.replace("/", "|||");
	}
	
	/**
	 * clear memory cache
	 */
	static public void clearCache() {
		if (sCache == null) {
			return;
		}
		synchronized(sCache) {
			sCache.clear();
		}
	}

	/**
	 * 
	 * @param prefix
	 * @param max
	 */
	private static void parsePrefix(String prefix, Point max) {
		try {
			max.x = max.y = Integer.parseInt(prefix);
		} catch(Throwable e) {
			try {
				final String[] tmp = prefix.split("X");
				max.x = (int) (Integer.parseInt(tmp[0]) * Global.getDipMultiplier() * (Global.isLargeScreen() ? 2 : 1));
				max.y = (int) (Integer.parseInt(tmp[1]) * Global.getDipMultiplier() * (Global.isLargeScreen() ? 2 : 1));
			} catch(Throwable e2) {
				;
			}
		}
	}
	
	/**
	 * class ImageCache
	 */
	public static class LocalImageCache extends AbsFileCache<byte[]> {
		
		private final ArrayList<String> mOrder = new ArrayList<String>();
	    private final HashMap<String, byte[]> mValues = new HashMap<String, byte[]>();
	    
	    long mSize = 0;
		private long mCapacity;

	    public LocalImageCache(Context context, long capacity, String imagedir, long maxfilesize) {
	    	super(context, imagedir, maxfilesize);
	    	
	    	mCapacity = capacity;
	    }
	    
	    /**
	     * 
	     */
	    void onCapacityFull() {
	    	long limit = (mCapacity * 10) / 16;
	    	while(mSize > limit) {
	    		synchronized(mValues) {
	    			mSize -= mValues.remove(mOrder.remove(0)).length;
	    		}
			}
		}

		/**
	     * the 'real' get using ImageInfo
	     * 
	     * @param info
	     * @return
	     */
		public Bitmap getBitmap(ImageInfo info) {
			Bitmap bmp = getBitmap(info.prefix + info.url);
			if (bmp != null || sCache == null || info.cacheType == ImageInfo.CACHE_TYPE_NONE || info.prefix.equals(bmp)) {
				return bmp;
			}
			bmp = getBitmap(info.url);
			if (bmp != null) {
				
				Point max = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
				ImageLoader.parsePrefix(info.prefix, max);
				if (max.x != Integer.MAX_VALUE) {
					bmp = Bitmaps.resizeBitmap(bmp, max.x, max.y);
					put(info.prefix + info.url, Bitmaps.compressBitmapToByteArray(bmp));
				}
			}
			return bmp;
		}

		/**
		 * the 'real' get using key
		 * 
		 * @param key
		 * @return
		 */
	    public Bitmap getBitmap(String key) {
    		Bitmap ret = null;
    		
    		// try to get the compressed byte array and create a bitmap from it
    		// put bitmap in cache
	    	final byte[] byteArray = getByteArrayFromCache(key);
	    	if (byteArray != null) {
	    		ret = Bitmaps.createBitmap(byteArray);
	    	}
    		return ret;
	    }
	    
	    /**
	     * 
	     * @param key
	     * @param value
	     * @return
	     */
	    @Override
	    public byte[] put(String key, byte[] value) {
	    	return putByteArrayInCache(key, value);
	    }
   
    	/**
    	 * 
    	 * @param key
    	 * @param value
    	 * @param useFileCacheToo
    	 * @return
    	 */
	    public byte[] put(String key, byte[] value, boolean useFileCacheToo) {
	    	return putByteArrayInCache(key, value, useFileCacheToo);
	    }
	    
	    /**
	     * 
	     * @param key
	     * @return
	     */
	    public byte[] getByteArrayFromCache(String key) {
	    	synchronized(mValues) {
		    	if (!mOrder.contains(key)) {
		    		return null;
		    	}
		    	mOrder.remove(key);
		    	mOrder.add(key);
		    	
		    	return mValues.get(key);
	    	}
	    }
	    
	    /**
	     * 
	     * @return
	     */
	    public byte[] putByteArrayInCache(String key, byte[] value) {
	    	return putByteArrayInCache(key, value, true);
	    }
	    
	    /**
	     * 
	     * @return
	     */
	    public byte[] putByteArrayInCache(String key, byte[] value, boolean useFileCacheToo) {
	    	if (value == null) {
	    		return null;
	    	}
	    	Context context = Global.getContext();
	    	if (context == null) {
	    		return null;
	    	}
	    	
	    	synchronized(ImageLoader.class) {
	    		synchronized(mValues) {
	    			
		    		if (mOrder.contains(key)) {
		    			
		    			// already got this key
		    			// remove it
		    			mOrder.remove(key);
		    			mSize -= mValues.remove(key).length;
		    		}
	    		
		    		// check memory
		    		ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		    		MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		    		activityManager.getMemoryInfo(memoryInfo);

		    		if (memoryInfo.lowMemory) {
		    			
		    			// low memory
		    			// remove half of the entries
		    			for(int i = mOrder.size() / 2; i > 0; --i) {
		    				mSize -= mValues.remove(mOrder.remove(0)).length;
		    			}
		    		}
		    		
		    		mValues.put(key, value);
		    		mOrder.add(key);
		    		mSize += value.length;
		    		Debug.logD("ImageLoader", "bytearray cache size: " + mSize);
	    		}
	    		
	    		if (useFileCacheToo) {
	    			putInFileCache(key, value);
	    		}
		    	
		    	return value;
	    	}
	    }
	    
	    /**
	     * 
	     * @param key
	     * @param value
	     * @return
	     */
	    public void putInFileCache(final String key, final byte[] value) {
	    	if (!has(key)) {

	    		// put in file
	    		final Thread thread = new Thread() {
	    			
	    			@Override
	    			public void run() {
	    				LocalImageCache.super.put(key, value);	    				
	    			}
	    		};
	    		thread.setPriority(Thread.MIN_PRIORITY);
	    		thread.start();
        	}
	    }
	    
	    /**
	     * 
	     * @return
	     */
	    public boolean isFull() {
	    	return mSize > mCapacity;
	    }
	    
	    /**
	     * 
	     */
	    public void clear() {
	    	synchronized(mValues) {
	    		mOrder.clear();
	    		mValues.clear();
	    		mSize = 0;
	    	}
	    }

	    /**
	     * 
	     * @param url
	     * @return
	     */
		public byte[] getByteArrayFromFile(String key) {
			return super.get(key);
		}
		
		/**
		 * 
		 * @param key
		 * @param maxWidth
		 * @param maxHeight
		 * @return
		 */
		public byte[] getByteArrayFromFile(String key, int maxWidth, int maxHeight) {
			final byte[] byteArray = super.get(key);
			if (byteArray == null) {
				return null;
			}
			return Bitmaps.createThumbnailByteArray(byteArray, maxWidth, maxHeight);
		}

		@Override
		synchronized protected byte[] getObjectFromStream(FileInputStream inStream) {
			try {
Debug.logE("ImageLoader", "from file");
				return Files.getFileAsByteArray(inStream);
			} catch(Throwable e) {
				Debug.logException(e);

				return null;
			}
		}

		@Override
		synchronized protected void putObjectIntoStream(byte[] value, FileOutputStream outStream) {
			if (value == null) {
				return;
			}
			try {
				outStream.write(value);
			} catch(OutOfMemoryError e) {
				Debug.logException(e);
			} catch(Throwable e) {
				Debug.logException(e);
			}
		}
	}
	
	public static class ImageInfo {
		public static final int CACHE_TYPE_NONE = 0;
		public static final int CACHE_TYPE_MEMORY = 1;
		public static final int CACHE_TYPE_FULL = 2;
		
		public String prefix = "";
		public String url;
		public int cacheType = CACHE_TYPE_FULL;
		
		public ImageInfo() {
		}
		
		public ImageInfo(String url) {
			this.url = url;
		}
		
		public ImageInfo(String prefix, String url) {
			this.prefix = prefix;
			this.url = url;
		}
		
		@Deprecated
		public ImageInfo(String prefix, String url, boolean useCache) {
			this.prefix = prefix;
			this.url = url;
			this.cacheType = useCache ? CACHE_TYPE_FULL : CACHE_TYPE_MEMORY;
		}
		
		public ImageInfo(String prefix, String url, int cacheType) {
			this.prefix = prefix;
			this.url = url;
			this.cacheType = cacheType;
		}
		
		@Override
		public boolean equals(Object info) {
			return info != null && ((url == null && ((ImageInfo) info).url == null) || 
						url.equals(((ImageInfo) info).url)) && 
					((prefix == null && ((ImageInfo) info).prefix == null) || prefix.equals(((ImageInfo) info).prefix)
			);
		}
		
		@Override
		public int hashCode() {
			return prefix.hashCode() + url.hashCode();
		}
	}

}
