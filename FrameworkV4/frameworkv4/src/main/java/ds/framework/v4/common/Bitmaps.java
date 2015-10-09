/*
	Copyright 2013 Dániel Sólyom

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
package ds.framework.v4.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;

public class Bitmaps {
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws OutOfMemoryError
	 */
	public static Bitmap downloadImage(URL url) throws IOException, OutOfMemoryError {
		InputStream stream = null;
		try {
			stream = getFlushedHttpStream(url);
			return BitmapFactory.decodeStream(stream);
		}  catch(IOException e) {
			throw(e);
		} catch(OutOfMemoryError e) {
			throw(e);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws OutOfMemoryError
	 */
	public static byte[] downloadImageAsByteArray(URL url) throws IOException, OutOfMemoryError {
		InputStream stream = null;
		try {
			stream = getFlushedHttpStream(url);
			return Files.getFileAsByteArray(stream);
		} catch(IOException e) {
			throw(e);
		} catch(OutOfMemoryError e) {
			throw(e);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public static Bitmap createBitmap(byte[] bytes) {
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}
	
	/**
	 * create a thumbnail (png, 100 quality) from byte array<br/>
	 * this first creates a bitmap from the byte array, then scales that bitmap<br/>
	 * !please note: this does not checks if the input byteArray is 'small' enough to create the bitmap
	 * 
	 * @param byteArray
	 * @param width
	 * @param height
	 * @throws OutOfMemoryError
	 * @return
	 */
	public static Bitmap createThumbnail(byte[] byteArray, int width, int height) throws OutOfMemoryError {
		final Bitmap tmp = createBitmap(byteArray);
		final Bitmap result = Bitmap.createScaledBitmap(tmp, width, height, false);
		tmp.recycle();
		return result;
	}
	
	/**
	 * create a thumbnail's byte array (png, 100 quality)<br/>
	 * this first creates a bitmap from the byte array, then scales that bitmap and converts that to byte array<br/>
	 * !please note: this does not checks if the input byteArray is 'small' enough to create the bitmap(s)
	 * 
	 * @param byteArray
	 * @param maxWidth
	 * @param maxHeight
	 * @throws OutOfMemoryError
	 * @return
	 */
	public static byte[] createThumbnailByteArray(byte[] byteArray, int maxWidth, int maxHeight) throws OutOfMemoryError {
		return createThumbnailByteArray(byteArray, maxWidth, maxHeight, Bitmap.CompressFormat.PNG, 100);
	}
	
	/**
	 * create a thumbnail's byte array<br/>
	 * this first creates a bitmap from the byte array, then scales that bitmap and converts that to byte array<br/>
	 * !please note: this does not checks if the input byteArray is 'small' enough to create the bitmap(s)
	 * 
	 * @param byteArray
	 * @param maxWidth
	 * @param maxHeight
	 * @throws OutOfMemoryError
	 * @return
	 */
	public static byte[] createThumbnailByteArray(byte[] byteArray, int maxWidth, int maxHeight, 
			Bitmap.CompressFormat format, int quality) throws OutOfMemoryError {
		final Point point = findNewBitmapSize(byteArray, maxWidth, maxHeight);
		
		if (point == null) {
			return byteArray;
		}
		
		final Bitmap tmp = createThumbnail(byteArray, point.x, point.y);
		byteArray = compressBitmapToByteArray(tmp, format, quality);
		tmp.recycle();
		return byteArray;
	}
	
	/**
	 * gives back another bitmap resized so size is inside maxWidth and maxHeight<br/>
	 * !note: does not recycle the input bitmap and may return the original bitmap (if no scaling was necessary)
	 * 
	 * @param bmp
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public static Bitmap resizeBitmap(Bitmap bmp, int maxWidth, int maxHeight) {
		final Point point = findNewBitmapSize(bmp.getWidth(), bmp.getHeight(), maxWidth, maxHeight);
		return Bitmap.createScaledBitmap(bmp, point.x, point.y, true);
	}
	
	/**
	 * gives back another bitmap resized so size is inside maxWidth and maxHeight<br/>
	 * !note: may return the original bitmap (if no scaling was necessary)
	 * 
	 * @param bmp
	 * @param maxWidth
	 * @param maxHeight
	 * @param recycle
	 * @return
	 */
	public static Bitmap resizeBitmap(Bitmap bmp, int maxWidth, int maxHeight, boolean recycle) {
		final int bw = bmp.getWidth();
		final int bh = bmp.getHeight();
		final Point point = findNewBitmapSize(bw, bh, maxWidth, maxHeight);
		if (bw == point.x && bh == point.y) {
			return bmp;
		}
		final Bitmap ret = Bitmap.createScaledBitmap(bmp, point.x, point.y, true);
		if (recycle) {
			bmp.recycle();
		}
		return ret;
	}
	
	/**
	 * 
	 * @param byteArray
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public static Point findNewBitmapSize(byte[] byteArray, int maxWidth, int maxHeight) {
		
		//Decode image size from byte array
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, o);
        
        return findNewBitmapSize(o.outWidth, o.outHeight, maxWidth, maxHeight);
	}
	
	/**
	 * 
	 * @param oWidth
	 * @param oHeight
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public static Point findNewBitmapSize(int oWidth, int oHeight, int maxWidth, int maxHeight) {
		
        // use image size ratio to find out real width and height
		if (oWidth > maxWidth || oHeight > maxHeight) {
			int nw = maxWidth;
			int nh = maxHeight;
			double rw = (double) maxWidth / oWidth;
			double rh = (double) maxHeight / oHeight;
			if (rw > rh) {
				nw = (int) (oWidth * rh);
			} else {
				nh = (int) (oHeight * rw);
			}
			return new Point(nw, nh);
		} else {
			return maxWidth > maxHeight ? new Point(maxHeight, maxHeight) : new Point(maxWidth, maxWidth);
		}
	}
	
	/**
	 * 
	 * @param byteArray
	 * @param approxWidth
	 * @param approxHeight
	 * @return
	 */
	public static Bitmap createThumbnailApprox(byte[] byteArray, int approxWidth, int approxHeight) {
		try {
			//Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;

	        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, o);
	        
	        Bitmap bmp = null;
	
	        int scale = 1;
	        if (o.outHeight > approxHeight || o.outWidth > approxWidth) {
	            scale = Math.max((int) Math.pow(2, (int) Math.ceil(Math.log((double) approxHeight / (double) o.outHeight) / Math.log(0.5))),
	            		(int) Math.pow(2, (int) Math.ceil(Math.log((double) approxWidth / (double) o.outWidth) / Math.log(0.5))));
	        }
	
	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, o2);
	
			return bmp;
		} catch(Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @param bmp
	 * @return
	 */
	public static byte[] compressBitmapToByteArray(Bitmap bmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}
	
	/**
	 * 
	 * @param bmp
	 * @param format
	 * @param quality
	 * @return
	 */
	public static byte[] compressBitmapToByteArray(Bitmap bmp, Bitmap.CompressFormat format, int quality) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(format, quality, stream);
		return stream.toByteArray();
	}
	
	/**
	 * 
	 * @param context
	 * @param uri
	 * @param approxWidth
	 * @param approxHeight
	 * @return
	 */
	public static Bitmap getThumbnailInternal(Context context, Uri uri, int approxWidth, int approxHeight) {
		try {
			InputStream is = context.getContentResolver().openInputStream(uri);
			
			//Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        
	        BitmapFactory.decodeStream(is, null, o);
	        is.close();
	        
	        Bitmap bmp = null;
	
	        int scale = 1;
	        if (o.outHeight > approxHeight || o.outWidth > approxWidth) {
	            scale = Math.max((int) Math.pow(2, (int) Math.ceil(Math.log((double) approxHeight / (double) o.outHeight) / Math.log(0.5))),
	            		(int) Math.pow(2, (int) Math.ceil(Math.log((double) approxWidth / (double) o.outWidth) / Math.log(0.5))));
	        }
	
	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        is = context.getContentResolver().openInputStream(uri);
	        bmp = BitmapFactory.decodeStream(is, null, o2);
	        is.close();

			return bmp;
		} catch(Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @param imageFileName
	 * @param approxWidth
	 * @param approxHeight
	 * @return
	 */
	public static Bitmap getThumbnail(String imageFileName, int approxWidth, int approxHeight) {
		final File f = new File(imageFileName);
		if (!f.exists()) {
			return null;
		}
		
		try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
			
			FileInputStream fis = new FileInputStream(f);
	        BitmapFactory.decodeStream(fis, null, o);
	        fis.close();
	        
	        Bitmap bmp = null;
	
	        int scale = 1;
	        if (o.outHeight > approxHeight || o.outWidth > approxWidth) {
	            scale = Math.max((int) Math.pow(2, (int) Math.floor(Math.log((double) approxHeight / (double) o.outHeight) / Math.log(0.5))),
	            		(int) Math.pow(2, (int) Math.floor(Math.log((double) approxWidth / (double) o.outWidth) / Math.log(0.5))));
	        }
	
	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        fis = new FileInputStream(f);
	        bmp = BitmapFactory.decodeStream(fis, null, o2);
	        fis.close();
	
			return bmp;
		} catch(Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param approxWidth
	 * @param approxHeight
	 * @return
	 */
	public static Bitmap getResizedImageFromHttpStream(URL url, int approxWidth, int approxHeight) throws IOException {
		InputStream stream = null;
		try {
			//Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        
	        o.inJustDecodeBounds = true;
	        stream = getFlushedHttpStream(url);
	        BitmapFactory.decodeStream(stream, null, o);
Debug.logD("Bitmaps", "original size: "+ o.outWidth + "x" + o.outHeight + " ");
	        Bitmap bmp = null;
	
	        int scale = 1;
	        if (o.outHeight > approxHeight || o.outWidth > approxWidth) {
	            scale = Math.max((int) Math.pow(2, (int) Math.ceil(Math.log((double) approxHeight / (double) o.outHeight) / Math.log(0.5))),
	            		(int) Math.pow(2, (int) Math.ceil(Math.log((double) approxWidth / (double) o.outWidth) / Math.log(0.5))));
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        stream = getFlushedHttpStream(url);
	        bmp = BitmapFactory.decodeStream(stream ,null, o2);
Debug.logD("Bitmaps", "new size: "+ bmp.getWidth() + "x" + bmp.getHeight() + " ");	
			return bmp;
		} catch(IOException e) {
			e.printStackTrace();
			throw(e);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	/**
	 * 
	 * @param activity
	 * @param uri
	 * @return
	 */
	public static String getImageFileNameFromUri(Activity activity, Uri uri) {
        String ret;
        try {
        	String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = activity.managedQuery( uri, proj, null, null, null );
        	int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        	cursor.moveToFirst();

        	ret = cursor.getString(index);
        	cursor.close();
        } catch(Exception e) {
        	ret = null;
        }
        return ret;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static FlushedInputStream getFlushedHttpStream(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.connect();

		InputStream stream = conn.getInputStream();
		try {
      		stream.reset();
      	} catch(Throwable e) {
      		;
      	}
		
      	return new FlushedInputStream(stream);
	}
	
// manipulations
	
	public static Bitmap fastBlur(Bitmap sentBitmap, int radius, int fromX, int fromY,
		    int width, int height, float brightnessMod, float scale) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
		
		// added brightness modification by DS (january 7th, 2013)

		Bitmap bitmap;
		
		if (scale == 1.0f) {
			bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		} else {
			bitmap = Bitmap.createScaledBitmap(sentBitmap, 
					(int) (sentBitmap.getWidth() * scale), (int) (sentBitmap.getHeight() * scale), false);
		}

		if (radius < 1) {
		    return (null);
		}

		int w = (int) (width * scale);
		int h = (int) (height * scale);

		int[] pix = new int[w * h];

		bitmap.getPixels(pix, 0, w, fromX, fromY, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
		    dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		int originRadius = radius;
		for (y = 0; y < h; y++) {
		    rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
		    for (i = -radius; i <= radius; i++) {
		        p = pix[yi + Math.min(wm, Math.max(i, 0))];
		        sir = stack[i + radius];
		        sir[0] = (p & 0xff0000) >> 16;
		        sir[1] = (p & 0x00ff00) >> 8;
		        sir[2] = (p & 0x0000ff);
		        rbs = r1 - Math.abs(i);
		        rsum += sir[0] * rbs;
		        gsum += sir[1] * rbs;
		        bsum += sir[2] * rbs;
		        if (i > 0) {
		            rinsum += sir[0];
		            ginsum += sir[1];
		            binsum += sir[2];
		        } else {
		            routsum += sir[0];
		            goutsum += sir[1];
		            boutsum += sir[2];
		        }
		    }
		    stackpointer = radius;

		    for (x = 0; x < w; x++) {

		        r[yi] = dv[rsum];
		        g[yi] = dv[gsum];
		        b[yi] = dv[bsum];

		        rsum -= routsum;
		        gsum -= goutsum;
		        bsum -= boutsum;

		        stackstart = stackpointer - radius + div;
		        sir = stack[stackstart % div];

		        routsum -= sir[0];
		        goutsum -= sir[1];
		        boutsum -= sir[2];

		        if (y == 0) {
		            vmin[x] = Math.min(x + radius + 1, wm);
		        }
		        p = pix[yw + vmin[x]];

		        sir[0] = (p & 0xff0000) >> 16;
		        sir[1] = (p & 0x00ff00) >> 8;
		        sir[2] = (p & 0x0000ff);

		        rinsum += sir[0];
		        ginsum += sir[1];
		        binsum += sir[2];

		        rsum += rinsum;
		        gsum += ginsum;
		        bsum += binsum;

		        stackpointer = (stackpointer + 1) % div;
		        sir = stack[(stackpointer) % div];

		        routsum += sir[0];
		        goutsum += sir[1];
		        boutsum += sir[2];

		        rinsum -= sir[0];
		        ginsum -= sir[1];
		        binsum -= sir[2];

		        yi++;
		    }
		    yw += w;
		}

		radius = originRadius;

		for (x = 0; x < w; x++) {
		    rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
		    yp = -radius * w;
		    for (i = -radius; i <= radius; i++) {
		        yi = Math.max(0, yp) + x;

		        sir = stack[i + radius];

		        sir[0] = r[yi];
		        sir[1] = g[yi];
		        sir[2] = b[yi];

		        rbs = r1 - Math.abs(i);

		        rsum += r[yi] * rbs;
		        gsum += g[yi] * rbs;
		        bsum += b[yi] * rbs;

		        if (i > 0) {
		            rinsum += sir[0];
		            ginsum += sir[1];
		            binsum += sir[2];
		        } else {
		            routsum += sir[0];
		            goutsum += sir[1];
		            boutsum += sir[2];
		        }

		        if (i < hm) {
		            yp += w;
		        }
		    }
		    yi = x;
		    stackpointer = radius;
		    for (y = 0; y < h; y++) {
		        pix[yi] = 0xff000000 | ((int) (dv[rsum] * brightnessMod) << 16) | ((int) (dv[gsum] * brightnessMod) << 8)
		                | (int) (dv[bsum] * brightnessMod);
		        
		        rsum -= routsum;
		        gsum -= goutsum;
		        bsum -= boutsum;

		        stackstart = stackpointer - radius + div;
		        sir = stack[stackstart % div];

		        routsum -= sir[0];
		        goutsum -= sir[1];
		        boutsum -= sir[2];

		        if (x == 0) {
		            vmin[y] = Math.min(y + r1, hm) * w;
		        }
		        p = x + vmin[y];

		        sir[0] = r[p];
		        sir[1] = g[p];
		        sir[2] = b[p];

		        rinsum += sir[0];
		        ginsum += sir[1];
		        binsum += sir[2];

		        rsum += rinsum;
		        gsum += ginsum;
		        bsum += binsum;

		        stackpointer = (stackpointer + 1) % div;
		        sir = stack[stackpointer];

		        routsum += sir[0];
		        goutsum += sir[1];
		        boutsum += sir[2];

		        rinsum -= sir[0];
		        ginsum -= sir[1];
		        binsum -= sir[2];

		        yi += w;
		    }
		}

		bitmap.setPixels(pix, 0, w, fromX, fromY, w, h);

		return (bitmap);
	}
	
	// for jpegs
	public static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                      int byteSkipped = read();
                      if (byteSkipped < 0) {
                          break;  // we reached EOF
                      } else {
                          bytesSkipped = 1; // we read one byte
                      }
               }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
