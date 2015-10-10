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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.util.LruCache;

import ds.framework.v4.Global;
import ds.framework.v4.common.Bitmaps;
import ds.framework.v4.common.Debug;
import ds.framework.v4.common.Files;

public class ImageLoader extends LaizyLoader<ImageLoader.ImageInfo, Bitmap> {

	private static ImageFileCache sFileCache;
    private static LruCache<String, Bitmap> sBitmapCache;
	
	/**
	 * get an ImageLoader singleton<br/>
	 * !please note: cache size is only set when using for the first time
	 * 
	 * @param context
	 * @param capacity
	 * @param imagedir
	 * @param filemaxsize
	 * @return
	 */
	public static ImageLoader getInstance(Context context, int capacity, String imagedir, long filemaxsize) {
		if (sFileCache == null) {
			sFileCache = new ImageFileCache(context, imagedir, filemaxsize);
		}
        if (sBitmapCache == null) {
            sBitmapCache = new LruCache<String, Bitmap>(capacity) {

                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
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
		Bitmap bmp = getFromBitmapCache(info);
        if (bmp == null) {
            bmp = getFromFileCache(info);
        }
        return bmp;
	}

    /**
     *
     * @param info
     * @return
     */
    public static Bitmap getFromBitmapCache(ImageInfo info) {
        final Bitmap bmp = sBitmapCache.get(info.prefix + info.url);
if (bmp != null) {
    Debug.logE("ImageLoader", "loaded from cache: " + info.prefix + " " + info.url);
}
        return bmp;
    }

    /**
     *
     * @param info
     * @return
     */
    public static Bitmap getFromFileCache(ImageInfo info) {
        byte[] bmpBytes = sFileCache.get(info.prefix + info.url);
        if (bmpBytes == null || bmpBytes.length == 0) {
            Point max = new Point();
            ImageLoader.parsePrefix(info.prefix, max);
            bmpBytes = sFileCache.get(info.url, max.x, max.y);

            if (bmpBytes == null || bmpBytes.length == 0) {
                return null;
            }
            sFileCache.put(info.prefix + info.url, bmpBytes);
        }

        Bitmap bmp = Bitmaps.createBitmap(bmpBytes);

		if (bmp != null) {
			sBitmapCache.put(info.prefix + info.url, bmp);
		}

Debug.logE("ImageLoader", "loaded from file: " + info.prefix + " " + info.url);
        return bmp;
    }

	
	/**
	 * get uri for cache file
	 * 
	 * @param info
	 * @return
	 */
	public static Uri getCacheUri(ImageInfo info) {
		if (sFileCache == null) {
			return null;
		}
		return sFileCache.getUri(info.prefix + info.url);
	}
	
	@Override
	public boolean loadInBackground(QueueItem item) {

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
	public static Bitmap loadImage(final ImageInfo info) {
        if (!info.url.substring(0, 4).equals("http")) {
            info.url = "http://" + info.url;
        }

        try {

            synchronized (ImageLoader.class) {

                if (info.cacheType == ImageInfo.CACHE_TYPE_FULL) {

                    // from file cache
                    final Bitmap bmp;
                    bmp = getFromFileCache(info);
                    if (bmp != null) {
                        return bmp;
                    }
                }

                final Bitmap bmp;

                // from url
                Point max = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
                parsePrefix(info.prefix, max);

Debug.logD("ImageLoader", "downloading: " + info.url);

                if (max.x == Integer.MAX_VALUE) {
                    bmp = Bitmaps.downloadImage(new URL(info.url));
                } else {
                    bmp = Bitmaps.getResizedImageFromHttpStream(new URL(info.url), max.x, max.y);
                }
                if (bmp == null) {

                    // not found
                    return null;
                }

                if (info.cacheType != ImageInfo.CACHE_TYPE_NONE) {

                    // put in bitmap cache
                    sBitmapCache.put(info.prefix + info.url, bmp);

                    if (info.cacheType != ImageInfo.CACHE_TYPE_MEMORY) {

                        // put in file cache (in another thread - so we can give this bitmap right away
                        new Thread() {

                            @Override
                            public void run() {
                                sFileCache.put(info.prefix + info.url, Bitmaps.compressBitmapToByteArray(bmp));
                            }
                        }.start();
                    }
                }

                return bmp;
            }

        } catch(OutOfMemoryError e) {
            Debug.logException(e);
        } catch(Throwable e) {
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
	 * 
	 * @param prefix
	 * @param max
	 */
	private static void parsePrefix(String prefix, Point max) {
        if (prefix == null || prefix.length() == 0 || "big".equals(prefix)) {
            max.x = (int) Global.getScreenWidth();
            max.y = (int) Global.getScreenHeight();
            return;
        }
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
	public static class ImageFileCache extends AbsFileCache<byte[]> {

	    public ImageFileCache(Context context, String imagedir, long maxfilesize) {
	    	super(context, imagedir, maxfilesize);
	    }

		/**
		 *
		 * @param key
		 * @param maxWidth
		 * @param maxHeight
		 * @return
		 */
		public byte[] get(String key, int maxWidth, int maxHeight) {
			final byte[] byteArray = super.get(key);
			if (byteArray == null) {
				return null;
			}
			return Bitmaps.createThumbnailByteArray(byteArray, maxWidth, maxHeight);
		}

		@Override
		synchronized protected byte[] getObjectFromStream(FileInputStream inStream) {
			try {
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
