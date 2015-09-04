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
package ds.framework.v4.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import ds.framework.v4.Global;
import ds.framework.v4.Settings;
import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.widget.MiniListView;

public class Common {
	
	/**
	 * 
	 * @param context
	 * @param url
	 */
	public static void openInBrowser(Context context, String url) {
		try {
			if (url.contains("mailto:")){
			    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
			  	context.startActivity(intent);
			  	return;
			}
		} catch(Throwable e) {
			Debug.logException(e);
		}
		Uri uri = createHttpUri(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(intent);
	}
	
	/**
	 * 
	 * @param activity
	 * @param url
	 * @param requestCode
	 */
	public static void openInBrowser(Activity activity, String url, int requestCode) {
		Uri uri = createHttpUri(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		activity.startActivityForResult(intent, requestCode);
	}
	
    /**
     * 
     * @param url
     * @return
     */
	public static boolean isDocument(String url) {
		try {
			if (!url.contains("docs.google.com")) {
				url = url.split("\\?")[0];
		    	if (url.endsWith(".pdf") || url.endsWith(".PDF") || url.endsWith(".doc") || url.endsWith(".DOC") ||
		    			url.endsWith(".xls") || url.endsWith(".XLS")) {
		    		return true;
		    	}
			}
		} catch(Throwable e) {
			;
		}
		return false;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static Uri createHttpUri(String url) {
		if (url.length() < 8 || !"http://".equals(url.substring(0, 7)) && !"https://".equals(url.substring(0, 8))) {
			url = "http://" + url;
		}
		return Uri.parse(url);
	}
	
	/**
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Intent getAppOpenIntent(Context context, String packageName) {
		return getAppOpenIntent(context, packageName, -1);
	}

	/**
	 * 
	 * @param context
	 * @param packageName
	 * @param minVersionCode
	 * @return
	 */
	public static Intent getAppOpenIntent(Context context, String packageName, int minVersionCode) {
		PackageManager pm = context.getPackageManager();
		
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		for(ResolveInfo ri : pm.queryIntentActivities(intent, 0)) {
			if (ri.activityInfo.packageName.equals(packageName)) {
				try {
					if (minVersionCode != -1 && pm.getPackageInfo(packageName, 0).versionCode < minVersionCode) { 
						return null;
					}
				} catch (NameNotFoundException e) {
					// wont happen but still;
					return null;
				}
				
					// create intent
				intent = new Intent("android.intent.action.MAIN");
				intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
				intent.addCategory("android.intent.category.LAUNCHER");
				return intent;
			}
		}
		return null;
	}
	
	/**
	 * open market at app's details screen
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void openMarket(Context context, String packageName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + packageName));
		context.startActivity(intent);
	}

	/**
	 * open market at app's details screen
	 * 
	 * @param context
	 * @param marketUrl
	 * @param dummy
	 */
	public static void openMarket(Context context, String marketUrl, boolean dummy) {
		Intent intent = new Intent(Intent.ACTION_VIEW);

		intent.setData(Uri.parse(marketUrl));
		context.startActivity(intent);
	}
	
	/**
	 * open app or market at app's details screen if app not installed
	 * 
	 * @param context
	 * @param packageName
	 * @param minVersion
	 */
	public static void openAppOrMarket(Context context, String packageName, int minVersion) {
		Intent i = getAppOpenIntent(context, packageName, minVersion);
		if (i != null) {
			context.startActivity(i);
		} else {
			openMarket(context, packageName);
		}
	}
	
	/**
	 * open app or market at app's details screen if app not installed
	 * 
	 * @param context
	 * @param packageName
	 * @param minVersion
	 */
	public static void openAppOrMarket(Context context, Intent i, String packageName, int minVersion) {
		if (i != null) {
			context.startActivity(i);
		} else {
			openMarket(context, packageName);
		}
	}
	
	abstract public static class AppResultCallback implements Runnable {

		protected Object mResult;
		
		public void setResult(int result) {
			mResult = result;
		}
		
	}
	
	/**
	 * start phone
	 * 
	 * @param context
	 * @param phoneNumber
	 */
	public static void startPhoneCallingDialog(Context context, String phoneNumber) {
		try {
			context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
		} catch(Throwable e) {
			Debug.logException(e);
		}
	}
	
	/**
	 * start email
	 * 
	 * @param context
	 * @param emails
	 * @param subject
	 * @param defaultMessage
	 */
	public static void startSendingEmail(Context context, String[] emails, 
			String subject, String defaultMessage, File... attachments) {
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emails);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, defaultMessage);

		if (attachments != null) {
            for(File attachment : attachments) {
                emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(attachment));
            }
        }

		context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	
	/**
	 * to fix tile mode bug
	 *  
	 * @param view
	 */
	public static void fixTiledBackground(View view) {
		try {
			fixTiledDrawable((BitmapDrawable) view.getBackground());
		} catch(ClassCastException e) {
			;
		}
	}
	
	/**
	 * to fix tile mode bug
	 * 
	 * @param tiled
	 */
	public static void fixTiledDrawable(BitmapDrawable tiled) {
		if (tiled == null) {
			return;
		}
		tiled.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
	}

	/**
	 * validate email
	 * 
	 * @param string
	 * @return
	 */
	public static boolean checkEmail(String string) {
		return string.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$");
	}

	/**
	 * copy from assets
	 * 
	 * @param context
	 * @param assetDirOrFile
	 * @param targetPath
	 * @throws IOException
	 */
	public static void copyFromAssets(Context context, String assetDirOrFile,
			String targetPath) throws IOException {

	    AssetManager assetManager = context.getAssets();
	    String assets[] = null;
	    
        assets = assetManager.list(assetDirOrFile);
        if (assets.length == 0) {
            copyFileFromAsset(context, assetManager, assetDirOrFile, targetPath);
        } else {
        	if (targetPath != null && targetPath.length() > 0) {
	        	File fTargetPath = new File(targetPath);
	            if (!fTargetPath.exists()) {
	            	fTargetPath.mkdir();
	            }	
        	}
            for (int i = 0; i < assets.length; ++i) {
                copyFromAssets(context, assetDirOrFile + "/" + assets[i],
                		targetPath + "/" + assets[i]);
            }
        }
	}
	
	public static void createDirStructureFromAssets(Context context, String assetDir,
			String targetPath) throws IOException {

	    AssetManager assetManager = context.getAssets();
	    String assets[] = null;
	    
        assets = assetManager.list(assetDir);
        if (assets.length != 0) {
        	if (targetPath != null && targetPath.length() > 0) {
	        	File fTargetPath = new File(targetPath);
	            if (!fTargetPath.exists()) {
	            	fTargetPath.mkdir();
	            }	
        	}
            for (int i = 0; i < assets.length; ++i) {
            	createDirStructureFromAssets(context, assetDir + "/" + assets[i],
                		targetPath + "/" + assets[i]);
            }
        }
	}

	public static void copyFileFromAsset(Context context, AssetManager assetManager, String assetFile,
			String targetFilePath) throws IOException {

	    InputStream in = null;
	    OutputStream out = null;

        in = assetManager.open(assetFile);
        out = new FileOutputStream(targetFilePath);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;
	}

	public static void deleteDirRec(String dirPath) {
		File fDirPath = new File(dirPath);
        if (!fDirPath.exists()) {
        	return;
        }
        String[] fileOrDirs = fDirPath.list();
        if (fileOrDirs.length > 0) {
        	for(int i = 0; i < fileOrDirs.length; ++i) {
        		deleteDirRec(dirPath + "/" + fileOrDirs[i]);
        	}
        }
        fDirPath.delete();
	}

	/**
	 * remove every view from a viewgroup
	 * 
	 * @param vg
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void removeAllViewsRec(ViewGroup vg) {
		final int cnt = vg.getChildCount();
		for(int i = 0; i < cnt; ++i) {
			final View child = vg.getChildAt(i);
			child.setBackgroundDrawable(null);
			if (child instanceof ViewGroup) {
				removeAllViewsRec((ViewGroup) child);
			} else if (child instanceof ImageView) {
				((ImageView) child).setImageDrawable(null);
			}
			if (child instanceof MiniListView) {
				((MiniListView) child).setAdapter(null);
			}
			if (child instanceof AdapterView) {
				((AdapterView) child).setAdapter(null);
			}
		}
		vg.removeAllViewsInLayout();
	}

    /**
     *
     * @param a
     * @param editResource
     */
	public static void showKeyboard(ActivityInterface a, int editResource) {
		View edit = a.findViewById(editResource);
		InputMethodManager mgr = (InputMethodManager) ((Context) a).getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
	}

    /**
     *
     * @param a
     * @param editResource
     */
	public static void hideKeyboard(ActivityInterface a, int editResource) {
		hideKeyboard(a, a.findViewById(editResource));
	}

    /**
     *
     * @param a
     * @param v
     */
	public static void hideKeyboard(ActivityInterface a, View v) {
		try {
			InputMethodManager mgr = (InputMethodManager) ((Context) a).getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
		} catch(Exception e) {
			;
		}
	}
	
	/**
	 * 
	 * @param a
	 */
	public static void hideKeyboard(Activity a) {
		InputMethodManager im = (InputMethodManager) a.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(a.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public static void postFromThread(final Runnable runnable) {
		final Handler handler = new Handler();
		
		new Thread() {
			
			@Override
			public void run() {
				handler.post(runnable);
			}
		}.start();
	}
	
	/**
	 * post message from thread
	 * 
	 * @param context
	 * @param handler
	 * @param messageRes
	 */
	public static void postMessageFromThread(final Context context, Handler handler, int messageRes) {
		postMessageFromThread(context, handler, context.getString(messageRes));
	}
	
	/**
	 * post message from thread
	 * 
	 * @param context
	 * @param handler
	 * @param message
	 */
	public static void postMessageFromThread(final Context context, Handler handler, final String message) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					toastMessage(context, message);
				} catch(Exception e) {
					
					// just closed app?
					;
				}
			}
		});
	}

	/**
	 * open soft keyboard
	 * 
	 * @param context
	 * @param view
	 */
	public static void openKeyboard(Context context, View view) {
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}	
	
	/**
	 * close soft keyboard
	 * 
	 * @param context
	 * @param view
	 */
	public static void closeKeyboard(Context context, View view) {
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	/**
	 * 
	 * @param number
	 * @param currency
	 * @return
	 */
	public static String formatByCurrency(float number, String currency) {
		try {
			Currency cur = Currency.getInstance(currency);
			NumberFormat format = NumberFormat.getCurrencyInstance(Settings.getLocale());
			format.setCurrency(cur);
			final int digits = cur.getDefaultFractionDigits();
			format.setMinimumFractionDigits(digits);
			format.setMaximumFractionDigits(digits);

			final String ret = format.format(number);
			
			return ret;
		} catch(Throwable e) {
			return String.valueOf(number) + currency;
		}
	}
	
	/**
	 * format number by currency
	 * 
	 * @param number
	 * @param currency
	 * @return
	 */
	public static String formatNumberByCurrency(Object number, String currency) {
		try {
			return formatNumberByCurrency(number, Currency.getInstance(currency));
		} catch(Exception e) {
			return number.toString();
		}
	}
	
	/**
	 * 
	 * @param number
	 * @param currency
	 * @param fractionDigits
	 * @return
	 */
	public static String formatNumberByCurrency(Object number, String currency, int fractionDigits) {
		try {
			return formatNumberByCurrency(number, Currency.getInstance(currency), fractionDigits);
		} catch(Exception e) {
			return number.toString();
		}
	}
	
	/**
	 * format number by currency
	 * 
	 * @param number
	 * @param currency
	 * @return
	 */
	public static String formatNumberByCurrency(Object number, Currency currency) {
		return formatNumberByCurrency(number, currency, currency.getDefaultFractionDigits());
	}
	
	/**
	 * 
	 * @param number
	 * @param currency
	 * @param fractionDigits
	 * @return
	 */
	public static String formatNumberByCurrency(Object number, Currency currency, int fractionDigits) {
		NumberFormat nf;
		nf = NumberFormat.getInstance();
		try {
			nf.setCurrency(currency);
			nf.setMinimumFractionDigits(fractionDigits);
			nf.setMaximumFractionDigits(fractionDigits);
			return nf.format(number);
		} catch(Exception e) {
			return number.toString();
		}
	}
	
	/**
	 * 
	 * @param number
	 * @param fractionDigits
	 * @return
	 */
	public static String formatNumber(Object number, int fractionDigits) {
		NumberFormat nf;
		nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(fractionDigits);
		nf.setMaximumFractionDigits(fractionDigits);
		return nf.format(number);
	}
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @param locale
	 * @param caseSensitive
	 * @return
	 */
	public static int compareLettersAccentSame(String left, String right, Locale locale, boolean caseSensitive) {
		Collator collator = Collator.getInstance(locale);
		if (!caseSensitive) {
			left = left.toUpperCase(locale);
			right = right.toUpperCase(locale);
		}
		collator.setStrength(Collator.PRIMARY);
		return collator.compare(left, right);
	}

	/**
	 * 
	 * @param context
	 * @param msg
	 */
	public static void toastMessage(Context context, String msg) {
		try {
			Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
			((TextView) ((ViewGroup) t.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
			t.show();
		} catch (Exception e) {
			Debug.logException(e);	// closed app just before this
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param msgResId
	 */
	public static void toastMessage(Context context, int msgResId) {
		try {
			Toast t = Toast.makeText(context, msgResId, Toast.LENGTH_LONG);
			((TextView) ((ViewGroup) t.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
			t.show();
		} catch (Exception e) {
			Debug.logException(e);	// closed app just before this
		}
	}
	
	/**
	 * 
	 * @param root
	 * @param font
	 */
	public static void setTypefaceRec(View root, Typeface font) {
		if (root instanceof ViewGroup) {
			for(int i = 0; i < ((ViewGroup) root).getChildCount(); ++i) {
				setTypefaceRec(((ViewGroup) root).getChildAt(i), font);
			}
		} else if (root instanceof TextView) {
			((TextView) root).setTypeface(font);
		}
	}
	
	/**
	 * serialize object
	 * 
	 * @param o
	 * @return
	 */
	public static byte[] serializeObject(Object o) { 
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
	 
	    try { 
	      ObjectOutput out = new ObjectOutputStream(bos); 
	      out.writeObject(o); 
	      out.close(); 
	 
	      // Get the bytes of the serialized object 
	      byte[] buf = bos.toByteArray(); 
	 
	      return buf; 
	    } catch(IOException e) { 
	      e.printStackTrace();  
	      return null; 
	    } 
	}
	
	/**
	 * deserialize object
	 * 
	 * @param b
	 * @return
	 */
	public static Object deserializeObject(byte[] b) { 
	    try { 
	      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b)); 
	      Object object = in.readObject(); 
	      in.close(); 
	 
	      return object; 
	    } catch(ClassNotFoundException e) { 
	      e.printStackTrace();
	      return null; 
	    } catch(IOException e) { 
	      e.printStackTrace(); 
	      return null; 
	    } 
	}
	
	public static boolean hasExternalStorage() {
		String state = Environment.getExternalStorageState();
		return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
	}
	
	/**
	 * read a file
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFile(String file) throws IOException {
        // Open file
		RandomAccessFile f = null;
		byte[] data = null;
		try {
        	f = new RandomAccessFile(new File(file), "r");

            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) throw new IOException("File too big");

            // Read file and return data
            data = new byte[length];
            f.readFully(data);
            return data;
        } catch(Exception e) {
        	e.printStackTrace();
        }

    	if (f != null) {
    		f.close();
    	}

		return data;
    }
	
	/**
	 * for locations
	 * 
	 * @return
	 */
	public static long getCalendarTimeInMillisUTC() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		return calendar.getTimeInMillis();
	}
	
	/**
	 * calendar base uri - different from among phones
	 *
	 * @return
	 */
	public static String getCalendarUriBase() {
		final Activity activity = Global.getCurrentActivity();
		
		String calendarUriBase = null;
		Uri calendars = Uri.parse("content://calendar/calendars");
		Cursor managedCursor = null;
		try {
			managedCursor = activity.managedQuery(calendars, null, null, null, null);
		} catch (Exception e) {
			try {
				calendars = Uri.parse("content://calendarEx/calendars");
				managedCursor = activity.managedQuery(calendars, null, null, null, null);
			} catch (Exception ex) {}
		}
		if (managedCursor != null) {
			calendarUriBase = calendars.toString().replace("/calendars", "");
		} else {
			calendars = Uri.parse("content://com.android.calendar/calendars");
			try {
				managedCursor = activity.managedQuery(calendars, null, null, null, null);
			} catch (Exception e) {}
			if (managedCursor != null) {
				calendarUriBase = "content://com.android.calendar/";
			}
		}

		return calendarUriBase;
	}

    /**
     * put event to default calendar
     *
     * @param context
     * @param title
     * @param description
     * @param startInMillis
     * @param endInMillis
     * @return
     */
	public static Long putIntoDefaultCalendar(Context context, String title, String description, 
			long startInMillis, long endInMillis) {
		ContentValues event = new ContentValues();

		try {
			final String cBase = getCalendarUriBase();
			
			ContentResolver cr = context.getContentResolver();
			Cursor cursor = cr.query(Uri.parse(cBase + "calendars"), 
					(new String[] { "_id" }), null, null, null);
			cursor.moveToLast();
			final int cid = Integer.parseInt(cursor.getString(0));
			
			event.put("calendar_id", cid);
			event.put("title", title);
			event.put("description", description);
			event.put("dtstart", String.valueOf(startInMillis));
			event.put("dtend", String.valueOf(endInMillis));
			event.put("eventTimezone", IDate.getDefaultTimeZone().getID());
	
			Uri eventUri = cr.insert(Uri.parse(cBase + "events"), event);
			return Long.parseLong(eventUri.getLastPathSegment());
		} catch(Throwable e) {
			return null;
		}
	}
	
	/**
	 * remove from calendar by id
	 * 
	 * @param context
	 * @param calId
	 * @return
	 */
	public static Integer removeFromDefaultCalendar(Context context, Long calId) {
		try {
			final String cBase = getCalendarUriBase();
			ContentResolver cr = context.getContentResolver();
			
			int iNumRowsDeleted = 0;

			try {
				Uri eventUri = ContentUris.withAppendedId(Uri.parse(cBase + "events"), calId);
				iNumRowsDeleted = cr.delete(eventUri, null, null);
			} catch(Throwable e) {
				;
			}

	        if (iNumRowsDeleted == 0) {
	        	
	        	// maybe another method?
	        	final Cursor cursor = cr.query(Uri.parse(cBase + "events"), new String[]{ "_id" },     "calendar_id=" + calId, null, null);
	            do {
	                long eventId = cursor.getLong(cursor.getColumnIndex("_id"));
	                cr.delete(ContentUris.withAppendedId(Uri.parse(cBase + "events"), eventId), null, null);
	                ++iNumRowsDeleted;
	            } while(cursor.moveToNext());
	            cursor.close();
	        }
	        
	        return iNumRowsDeleted;
		} catch(Throwable e) {
			return null;
		}
	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public static void enableListViewInScrollView(View v) {
		final ScrollView parent = findScrollViewParent(v);
		if (parent == null) {
			return;
		}
		v.setOnTouchListener(new OnTouchListener() {
			
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	parent.requestDisallowInterceptTouchEvent(true);
		    	return false;
		    }
		});
	}

	/**
	 * 
	 * @param v
	 * @return
	 */
	public static ScrollView findScrollViewParent(View v) {
		final ViewParent parent = v.getParent();
		if (parent == null || !(parent instanceof View)) {
			return null;
		}
		if (parent instanceof ScrollView) {
			return (ScrollView) parent;
		}

		return findScrollViewParent((View) parent);
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getActionBarHeight(Context context) {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
					true))
				actionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, context.getResources().getDisplayMetrics());
		} else {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
					context.getResources().getDisplayMetrics());
		}
		return actionBarHeight;	
	}
}
