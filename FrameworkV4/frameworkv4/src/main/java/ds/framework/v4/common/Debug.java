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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

import ds.framework.v4.Global;

public class Debug {
	
	/**
	 * 
	 */
	static public void logNativeHeapAllocatedSize() {
		logE("Debug", "NativeHeapSize: " + android.os.Debug.getNativeHeapAllocatedSize());
	}

	/**
	 * print stack trace of an exception in debug mode
	 * 
	 * @param e
	 */
	public static void logException(Throwable e) {
		if (e != null && Global.isDEBUG()) {
			e.printStackTrace();
		}
	}
	
	/**
	 * log to file
	 * 
	 * @param filename
	 * @param text
	 */
	public static void logToFile(String filename, String text) {
		File dir = new File(Environment.getExternalStorageDirectory() + "/log");
		dir.mkdirs();
		File logFile = new File(dir, filename);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * log error in infp mode
	 * 
	 * @param title
	 * @param error
	 */
	public static void logI(String title, String error) {
		if (Global.isDEBUG()) {
			Log.i(title, error);
		}
	}

	/**
	 * log error in debug mode
	 * 
	 * @param title
	 * @param error
	 */
	public static void logE(String title, String error) {
		if (Global.isDEBUG()) {
			Log.e(title, error);
		}
	}
	
	/**
	 * log warning in debug mode
	 * 
	 * @param title
	 * @param warning
	 */
	public static void logW(String title, String warning) {
		if (Global.isDEBUG()) {
			Log.w(title, warning);
		}
	}
	
	/**
	 * log debug in debug mode
	 * 
	 * @param title
	 * @param warning
	 */
	public static void logD(String title, String warning) {
		if (Global.isDEBUG()) {
			Log.d(title, warning);
		}
	}
	
	/**
	 * log error
	 * 
	 * @param title
	 * @param error
	 */
	public static void logEA(String title, String error) {
		Log.e(title, error);
	}

}
