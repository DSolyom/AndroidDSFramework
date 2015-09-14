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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ds.framework.v4.R;

import android.content.Context;

public class IDate {
	Calendar mC;
	
	public final static int T_MINUTE_LENGTH = 60;
	public final static int T_HOUR_LENGTH = T_MINUTE_LENGTH * 60;
	public final static int T_DAY_LENGTH = T_HOUR_LENGTH * 24;
	public final static int T_WEEK_LENGTH = T_DAY_LENGTH * 7;
	
	public final static long MINUTE_LENGTH = 1000 * 60;
	public final static long HOUR_LENGTH = MINUTE_LENGTH * 60;
	public final static long DAY_LENGTH = HOUR_LENGTH * 24;
	public final static long WEEK_LENGTH = DAY_LENGTH * 7;
	
	String mFormat = "yyyy-MM-dd HH:mm:ss";

	private static TimeZone defaultTimeZone;

	public IDate() {
		mC = Calendar.getInstance();
		if (defaultTimeZone != null) {
			setTimezone(defaultTimeZone);
		}
	}
	
	public IDate(int timestamp) {
		this();
		setTimestamp(timestamp);
	}
	
	public Calendar getCalendar() {
		return mC;
	}
	
	public void setTimezone(TimeZone timezone) {
		mC.setTimeZone(timezone);
	}
	
	public void setFormat(String format) {
		mFormat = format;
	}
	
	public IDate setTimestamp(long timestamp) {
		mC.setTimeInMillis(timestamp * 1000);
		return this;
	}
	

	public int getTimestamp() {
		return (int) (mC.getTimeInMillis() / 1000);
	}
	
	public int timeOffset(TimeZone timezone) {
		final long time = System.currentTimeMillis();
		final int offset = mC.getTimeZone().getOffset(time);
		return offset - timezone.getOffset(time);
	}
	
	/**
	 * 
	 * @param timezone
	 * @param time
	 * @return
	 */
	public int timeOffset(TimeZone timezone, long time) {
		final int offset = mC.getTimeZone().getOffset(time);
		return offset - timezone.getOffset(time);
	}
	
	public void setDateTime(String datetime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(mFormat);
		sdf.setTimeZone(mC.getTimeZone());
		mC.setTime(sdf.parse(datetime));
	}
	
	public void setDateTime(String datetime, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(mC.getTimeZone());
		mC.setTime(sdf.parse(datetime));
	}
	
	public int[] getCurrentArray() {
		int[] array = new int[6];
		
		array[0] = mC.get(Calendar.YEAR);
		array[1] = mC.get(Calendar.MONTH) + 1;
		array[2] = mC.get(Calendar.DAY_OF_MONTH);
		array[3] = mC.get(Calendar.HOUR_OF_DAY);
		array[4] = mC.get(Calendar.MINUTE);
		array[5] = mC.get(Calendar.SECOND);
		
		return array;
	}
	
	/**
	 * minute of hour starting from 0 
	 * @return
	 */
	public int minuteOfHour() {
		return mC.get(Calendar.MINUTE);
	}
	
	/**
	 * hour of day starting from 0
	 * @return
	 */
	public int hourOfDay() {
		return mC.get(Calendar.HOUR_OF_DAY);
	}
	
	/**
	 * day of week starting from Sunday = 0
	 * 
	 * @param timestamp
	 * @return
	 */
	public int dayOfWeek() {
		return mC.get(Calendar.DAY_OF_WEEK) - 1;
	}
	
	/**
	 * month starting from January = 0
	 * 
	 * @return
	 */
	public int monthOfYear() {
		return mC.get(Calendar.MONTH);
	}
	
	/**
	 * day of month starting from 1
	 * 
	 * @return
	 */
	public int dayOfMonth() {
		return mC.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * get formatted date string from timestamp
	 * 
	 * @param timestamp
	 * @param format
	 * @return
	 */
	public static String timestampToString(int timestamp, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis((long) timestamp * 1000);
		return sdf.format(c.getTime());
	}
	
	public String getDateTimeString() {
		return getDateTimeString(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
	}
	
	public String getTimeString() {
		return getTimeString(SimpleDateFormat.SHORT);
	}
		
	public String getDateTimeString(int dateStyle, int timeStyle) {
		DateFormat df = SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle);
		df.setCalendar(mC);
		return df.format(new Date(mC.getTimeInMillis()));
	}
	
	public String getTimeString(int timeStyle) {
		DateFormat df = SimpleDateFormat.getTimeInstance(timeStyle);
		df.setCalendar(mC);
		return df.format(new Date(mC.getTimeInMillis()));
	}

	
	public String getDateString() {
		return getDateString(SimpleDateFormat.MEDIUM);
	}
	
	public String getDateString(int style) {
		DateFormat df = SimpleDateFormat.getDateInstance(style);
		df.setCalendar(mC);
		return df.format(new Date(mC.getTimeInMillis()));
	}
	
	public int[] getDateArray(String dbDate) {
		int[] array = new int[6];
		
		array[0] = Integer.parseInt(dbDate.substring(0, 4));
		array[1] = Integer.parseInt(dbDate.substring(5, 7));
		array[2] = Integer.parseInt(dbDate.substring(8, 10));
		
		try {
			array[3] = Integer.parseInt(dbDate.substring(11, 13));
			array[4] = Integer.parseInt(dbDate.substring(14, 16));
			array[5] = Integer.parseInt(dbDate.substring(17));
		} catch(Exception e) {
			array[3] = 0;
			array[4] = 0;
			array[5] = 0;
		}
		
		return array;
	}
	
	public long getTimeInMillis() {
		return mC.getTimeInMillis();
	}
	
	public long getTimeInMillis(String dbDate) {
		int[] dArray = getDateArray(dbDate);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeZone(mC.getTimeZone());
		gc.set(dArray[0], dArray[1] - 1, dArray[2], dArray[3], dArray[4], dArray[5]);
		
		return gc.getTimeInMillis();
	}
	
	public long differenceTillNow(String dbDate) {
		return mC.getTimeInMillis() - getTimeInMillis(dbDate);
	}
	
	public long differenceTillNow(IDate date) {
		return mC.getTimeInMillis() - date.getTimeInMillis();
	}
	
	public long startOfThisDay() {
		final Calendar mc = Calendar.getInstance();
		mc.setTimeZone(mC.getTimeZone());
		mc.set(mC.get(Calendar.YEAR), 
				mC.get(Calendar.MONTH),  
				mC.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		return mc.getTimeInMillis();
	}
	
	public int startOfThisDayTimestamp() {
		return (int) (startOfThisDay() / 1000);
	}
	
	/**
	 * returns start of this week (the closest past Sunday)
	 * 
	 * @return
	 */
	public long startOfThisWeek() {
		final Calendar mc = Calendar.getInstance();
		mc.setTimeZone(mC.getTimeZone());
		int d = mC.get(Calendar.DAY_OF_WEEK);
		mc.set(mC.get(Calendar.YEAR), 
				mC.get(Calendar.MONTH),  
				mC.get(Calendar.DAY_OF_MONTH) - d + 1, 0, 0, 0);
		return mc.getTimeInMillis();
	}
	
	public boolean isToday(String dbDate) {
		int[] dArray = getDateArray(dbDate);
		int[] cArray = getCurrentArray();
		
		return (dArray[0] == cArray[0] && dArray[1] == cArray[1] && dArray[2] == cArray[2]);
	}

	public boolean isThisWeek(String dbDate) {
		return startOfThisWeek() <= getTimeInMillis(dbDate);
	}
	
	public boolean isThisMonth(String dbDate) {
		int[] dArray = getDateArray(dbDate);
		int[] cArray = getCurrentArray();
		
		return (dArray[0] == cArray[0] && dArray[1] == cArray[1]);
	}
	
	public boolean isThisYear(String dbDate) {
		int[] dArray = getDateArray(dbDate);
		int[] cArray = getCurrentArray();
		
		return (dArray[0] == cArray[0]);
	}

	public static void setDefaultTimeZone(TimeZone timeZone) {
		defaultTimeZone = timeZone;
	}
	
	public static TimeZone getDefaultTimeZone() {
		return defaultTimeZone;
	}

	public boolean isToday() {
		final Calendar cToday = Calendar.getInstance();

		return mC.get(Calendar.DAY_OF_YEAR) == cToday.get(Calendar.DAY_OF_YEAR);
	}
	
	public boolean isTomorrow() {
		final Calendar cToday = Calendar.getInstance();
		cToday.add(Calendar.DAY_OF_YEAR, 1);
		
		return mC.get(Calendar.DAY_OF_YEAR) == cToday.get(Calendar.DAY_OF_YEAR);
	}
	
	public boolean isThisWeek() {
		final Calendar cToday = Calendar.getInstance();
		
		return mC.get(Calendar.WEEK_OF_YEAR) == cToday.get(Calendar.WEEK_OF_YEAR);
	}
	
	public boolean isThisMonth() {
		final Calendar cToday = Calendar.getInstance();
		
		return mC.get(Calendar.MONTH) == cToday.get(Calendar.MONTH);
	}
	
	public boolean isThisYear() {
		final Calendar cToday = Calendar.getInstance();
		
		return mC.get(Calendar.YEAR) == cToday.get(Calendar.YEAR);
	}
	
	public boolean isBeforeNow() {
		final Calendar cToday = Calendar.getInstance();
		
		return mC.before(cToday);
	}
	
	/**
	 * 
	 * @param context
	 * @param date
	 * @return
	 */
	public static String getDateInString(Context context, Date date) {
		final Date current = new Date();
		final long timeDiff = current.getTime() - date.getTime();
		
		if (timeDiff <= 1000 * 60) {
			return context.getString(R.string.x_idate_currently);
		}
		if (timeDiff < 1000 * 60 * 60) {
			return context.getString(R.string.x_idate_within_an_hour).replace("%D", String.valueOf(timeDiff / 1000 / 60));
		}
		
		final long dayDiff = daysBetween(date, current);
		if (dayDiff < 1) {
			return context.getString(R.string.x_idate_within_the_day).replace("%D", String.valueOf(timeDiff / 1000 / 60 / 60));
		}
		
		if (dayDiff == 1) {
			return context.getString(R.string.x_idate_yesterday);
		}
		if (dayDiff < 7) {
			return context.getString(R.string.x_idate_within_the_week).replace("%D", String.valueOf(dayDiff));
		}
		
		return SimpleDateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
	}
	
// from stackoverflow.com
	
	public static Calendar getDatePart(Date date){
	    Calendar cal = Calendar.getInstance();       // get calendar instance
	    cal.setTime(date);      
	    cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
	    cal.set(Calendar.MINUTE, 0);                 // set minute in hour
	    cal.set(Calendar.SECOND, 0);                 // set second in minute
	    cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

	    return cal;                                  // return the date part
	}

	/**
	 * This method also assumes endDate >= startDate
	**/
	public static long daysBetween(Date startDate, Date endDate) {
	  Calendar sDate = getDatePart(startDate);
	  Calendar eDate = getDatePart(endDate);

	  long daysBetween = 0;
	  while (sDate.before(eDate)) {
	      sDate.add(Calendar.DAY_OF_MONTH, 1);
	      daysBetween++;
	  }
	  return daysBetween;
	}
//
}
