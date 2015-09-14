/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Modified by DS 2013
 */

package ds.framework.v4.widget;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import ds.framework.v4.R;
import ds.framework.v4.common.Debug;
import ds.framework.v4.common.IDate;

/**
 * This class is a calendar widget for displaying and selecting dates. The range
 * of dates supported by this calendar is configurable. A user can select a date
 * by taping on it and can scroll and fling the calendar to a desired date.
 *
 * @attr ref android.R.styleable#CalendarView_showWeekNumber
 * @attr ref android.R.styleable#CalendarView_firstDayOfWeek
 * @attr ref android.R.styleable#CalendarView_minDate
 * @attr ref android.R.styleable#CalendarView_maxDate
 * @attr ref android.R.styleable#CalendarView_shownWeekCount
 * @attr ref android.R.styleable#CalendarView_selectedWeekBackgroundColor
 * @attr ref android.R.styleable#CalendarView_focusedMonthDateColor
 * @attr ref android.R.styleable#CalendarView_unfocusedMonthDateColor
 * @attr ref android.R.styleable#CalendarView_weekNumberColor
 * @attr ref android.R.styleable#CalendarView_weekSeparatorLineColor
 * @attr ref android.R.styleable#CalendarView_selectedDateVerticalBar
 * @attr ref android.R.styleable#CalendarView_weekDayTextAppearance
 * @attr ref android.R.styleable#CalendarView_dateTextAppearance
 */
// most of the above references have been removed by DS @see me_views.xml

public class CalendarView extends FrameLayout {

    /**
     * Tag for logging.
     */
    private static final String LOG_TAG = CalendarView.class.getSimpleName();

    /**
     * The number of milliseconds in a day.e
     */
    private static final long MILLIS_IN_DAY = 86400000L;

    /**
     * The number of day in a week.
     */
    private static final int DAYS_PER_WEEK = 7;

    /**
     * The number of milliseconds in a week.
     */
    private static final long MILLIS_IN_WEEK = DAYS_PER_WEEK * MILLIS_IN_DAY;

    /**
     * Affects when the month selection will change while scrolling upe
     */
    private static final int SCROLL_HYST_WEEKS = 2;

    /**
     * How long the GoTo fling animation should last.
     */
    private static final int GOTO_SCROLL_DURATION = 1000;

    /**
     * The duration of the adjustment upon a user scroll in milliseconds.
     */
    private static final int ADJUSTMENT_SCROLL_DURATION = 500;

    /**
     * How long to wait after receiving an onScrollStateChanged notification
     * before acting on it.
     */
    private static final int SCROLL_CHANGE_DELAY = 40;

    /**
     * String for formatting the month name in the title text view.
     */
    private static final String FORMAT_MONTH_NAME = "MMMM, yyyy";

    /**
     * String for parsing dates.
     */
    private static final String DATE_FORMAT = "MM/dd/yyyy";

    /**
     * The default minimal date.
     */
    private static final String DEFAULT_MIN_DATE = "01/01/1900";

    /**
     * The default maximal date.
     */
    private static final String DEFAULT_MAX_DATE = "01/01/2100";

    private static final int DEFAULT_SHOWN_WEEK_COUNT = 6;

    private static final int DEFAULT_DATE_TEXT_SIZE = 14;

    private static final int UNSCALED_WEEK_MIN_VISIBLE_HEIGHT = 12;

    private static final int UNSCALED_LIST_SCROLL_TOP_OFFSET = 2;

    private static final int UNSCALED_BOTTOM_BUFFER = 20;

    private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 1;

    private static final int DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID = -1;

    private final int mWeekSeperatorLineWidth;

    private final int mDateTextSize;

    private final int mSelectedDayBackgroundColor;
    
    private final int mEventDayBackgroundColor;

    private final int mFocusedMonthDateColor;

    private final int mUnfocusedMonthDateColor;
    
    private final int mSelectedDayColor;
    
    private final int mEventDayColor;
    
    private final int mUnfocusedEventDayColor;

    private final int mDaySeparatorLineColor;
    
    private final int mTodayBackgroundColor;
    
    private final int mTodayColor; 
    
    /**
     * if set can only select days with event on it
     */
    private boolean mCanOnlySelectEvents = true;

    /**
     * The top offset of the weeks list.
     */
    private int mListScrollTopOffset = 2;

    /**
     * The visible height of a week view.
     */
    private int mWeekMinVisibleHeight = 12;

    /**
     * The visible height of a week view.
     */
    private int mBottomBuffer = 20;

    /**
     * The number of shown weeks.
     */
    private int mShownWeekCount;

    /**
     * The friction of the week list while flinging.
     */
    private float mFriction = .05f;

    /**
     * Scale for adjusting velocity of the week list while flinging.
     */
    private float mVelocityScale = 0.333f;

    /**
     * The adapter for the weeks list.
     */
    private WeeksAdapter mAdapter;

    /**
     * The weeks list.
     */
    private ListView mListView;

    /**
     * The name of the month to display.
     */
    private TextView mMonthName;

    /**
     * The header with week day names.
     */
    private ViewGroup mDayNamesHeader;

    /**
     * Cached labels for the week names header.
     */
    private String[] mDayLabels;

    /**
     * The first day of the week.
     */
    private int mFirstDayOfWeek;

    /**
     * Which month should be displayed/highlighted [0-11].
     */
    private int mCurrentMonthDisplayed;

    /**
     * Used for tracking during a scroll.
     */
    private long mPreviousScrollPosition;

    /**
     * Used for tracking which direction the view is scrolling.
     */
    private boolean mIsScrollingUp = false;

    /**
     * The previous scroll state of the weeks ListView.
     */
    private int mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * The current scroll state of the weeks ListView.
     */
    private int mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * Listener for changes in the selected day.
     */
    private OnDateChangeListener mOnDateChangeListener;

    /**
     * Command for adjusting the position after a scroll/fling.
     */
    private ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();

    /**
     * Temporary instance to avoid multiple instantiations.
     */
    private Calendar mTempDate;

    /**
     * The first day of the focused month.
     */
    private Calendar mFirstDayOfMonth;

    /**
     * The start date of the range supported by this picker.
     */
    private Calendar mMinDate;

    /**
     * The end date of the range supported by this picker.
     */
    private Calendar mMaxDate;

    /**
     * Date format for parsing dates.
     */
    private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

    /**
     * The current locale.
     */
    private Locale mCurrentLocale;
    
    private HashMap<Integer, boolean[]> mEventDatesByWeeks = new HashMap<Integer, boolean[]>();
    
    protected Context mContext;

    /**
     * The callback used to indicate the user changes the date.
     */
    public interface OnDateChangeListener {

        /**
         * Called upon change of the selected day.
         *
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param month The month that was set [0-11].
         * @param dayOfMonth The day of the month that was set.
         */
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth);
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, 0);

        mContext = context;
        
        final Calendar calendar = new IDate().getCalendar();

        // initialization based on locale
        setCurrentLocale(mContext.getResources().getConfiguration().locale);

        mFirstDayOfWeek = calendar.getFirstDayOfWeek();
        parseDate(DEFAULT_MIN_DATE, mMinDate);
        parseDate(DEFAULT_MAX_DATE, mMaxDate);

        mShownWeekCount = DEFAULT_SHOWN_WEEK_COUNT;
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, 0, 0);
        
        mSelectedDayBackgroundColor = a.getColor(
                R.styleable.CalendarView_selectedDayBackgroundColor, 0);
        mEventDayBackgroundColor = a.getColor(
                R.styleable.CalendarView_eventDayBackgroundColor, 0);
        mTodayBackgroundColor = a.getColor(
                R.styleable.CalendarView_todayBackgroundColor, 0);
        mFocusedMonthDateColor = a.getColor(
                R.styleable.CalendarView_focusedMonthDateColor, 0);
        mUnfocusedMonthDateColor = a.getColor(
                R.styleable.CalendarView_unfocusedMonthDateColor, 0);
        mSelectedDayColor = a.getColor(
                R.styleable.CalendarView_selectedDayColor, 0);
        mEventDayColor = a.getColor(
                R.styleable.CalendarView_eventDayColor, 0);
        mTodayColor = a.getColor(
                R.styleable.CalendarView_todayColor, 0);
        mUnfocusedEventDayColor = a.getColor(
                R.styleable.CalendarView_unfocusedEventDayColor, 0);
        mDaySeparatorLineColor = a.getColor(
                R.styleable.CalendarView_daySeparatorLineColor, 0);
        
        mCanOnlySelectEvents = a.getBoolean(
        		R.styleable.CalendarView_canOnlySelectEvents, mCanOnlySelectEvents);

        mDateTextSize = a.getDimensionPixelSize(
                R.styleable.CalendarView_calendarDateTextSize, DEFAULT_DATE_TEXT_SIZE);

        int weekDayTextAppearanceResId = a.getResourceId(
                R.styleable.CalendarView_weekDayTextAppearance, DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID);
        a.recycle();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mWeekMinVisibleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_WEEK_MIN_VISIBLE_HEIGHT, displayMetrics);
        mListScrollTopOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_LIST_SCROLL_TOP_OFFSET, displayMetrics);
        mBottomBuffer = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_BOTTOM_BUFFER, displayMetrics);
        mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, displayMetrics);

        LayoutInflater layoutInflater = (LayoutInflater) mContext
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View content = layoutInflater.inflate(R.layout.x_calendar_view, null, false);
        addView(content);

        mListView = (ListView) findViewById(R.id.list);
        mDayNamesHeader = (ViewGroup) content.findViewById(R.id.day_names);
        mMonthName = (TextView) content.findViewById(R.id.month_name);
        mMonthName.setTextAppearance(context, weekDayTextAppearanceResId);

        setUpHeader(weekDayTextAppearanceResId);
        setUpListView();
        setUpAdapter();

        // go to today or whichever is close to today min or max date
        mTempDate.setTimeInMillis(System.currentTimeMillis());
        if (mTempDate.before(mMinDate)) {
            goTo(mMinDate, false, true, true);
        } else if (mMaxDate.before(mTempDate)) {
            goTo(mMaxDate, false, true, true);
        } else {
            goTo(mTempDate, false, true, true);
        }

        invalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mListView.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return mListView.isEnabled();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Gets the minimal date supported by this {@link CalendarView} in milliseconds
     * since January 1, 1970 00:00:00 in {@link TimeZone#getDefault()} time
     * zone.
     * <p>
     * Note: The default minimal date is 01/01/1900.
     * <p>
     *
     * @return The minimal supported date.
     */
    public long getMinDate() {
        return mMinDate.getTimeInMillis();
    }

    /**
     * Sets the minimal date supported by this {@link CalendarView} in milliseconds
     * since January 1, 1970 00:00:00 in {@link TimeZone#getDefault()} time
     * zone.
     *
     * @param minDate The minimal supported date.
     */
    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (isSameDate(mTempDate, mMinDate)) {
            return;
        }
        mMinDate.setTimeInMillis(minDate);
        // make sure the current date is not earlier than
        // the new min date since the latter is used for
        // calculating the indices in the adapter thus
        // avoiding out of bounds error
        Calendar date = mAdapter.mSelectedDate;
        if (date.before(mMinDate)) {
            mAdapter.setSelectedDay(mMinDate);
        }
        // reinitialize the adapter since its range depends on min date
        mAdapter.init();
        if (date.before(mMinDate)) {
            setDate(mTempDate.getTimeInMillis());
        } else {
            // we go to the current date to force the ListView to query its
            // adapter for the shown views since we have changed the adapter
            // range and the base from which the later calculates item indices
            // note that calling setDate will not work since the date is the same
            goTo(date, false, true, false);
        }
    }

    /**
     * Gets the maximal date supported by this {@link CalendarView} in milliseconds
     * since January 1, 1970 00:00:00 in {@link TimeZone#getDefault()} time
     * zone.
     * <p>
     * Note: The default maximal date is 01/01/2100.
     * <p>
     *
     * @return The maximal supported date.
     */
    public long getMaxDate() {
        return mMaxDate.getTimeInMillis();
    }

    /**
     * Sets the maximal date supported by this {@link CalendarView} in milliseconds
     * since January 1, 1970 00:00:00 in {@link TimeZone#getDefault()} time
     * zone.
     *
     * @param maxDate The maximal supported date.
     */
    public void setMaxDate(long maxDate) {
        mTempDate.setTimeInMillis(maxDate);
        if (isSameDate(mTempDate, mMaxDate)) {
            return;
        }
        mMaxDate.setTimeInMillis(maxDate);
        // reinitialize the adapter since its range depends on max date
        mAdapter.init();
        Calendar date = mAdapter.mSelectedDate;
        if (date.after(mMaxDate)) {
            setDate(mMaxDate.getTimeInMillis());
        } else {
            // we go to the current date to force the ListView to query its
            // adapter for the shown views since we have changed the adapter
            // range and the base from which the later calculates item indices
            // note that calling setDate will not work since the date is the same
            goTo(date, false, true, false);
        }
    }
    
    /**
     * set events<br/>
     * !note: this calls setMinDate and setMaxDate if eventTimestamps is not empty
     * 
     * @param eventTimestamps
     */
    public void setEvents(ArrayList<Integer> eventTimestamps) {
    	mEventDatesByWeeks.clear();
    	
    	IDate iDate = new IDate();
    	Calendar date = iDate.getCalendar();
    	long todayInMillis = mAdapter.mTodayDate.getTimeInMillis();
    	
    	if (eventTimestamps.isEmpty()) {
    		setMinDate(todayInMillis);
    		setMaxDate(todayInMillis);
    		return;
    	}
    	
    	iDate.setTimestamp(eventTimestamps.get(0));
    	date.set(Calendar.DAY_OF_MONTH, 1);
    	date.add(Calendar.WEEK_OF_YEAR, -1);
    	setMinDate(Math.min(todayInMillis, date.getTimeInMillis()));
    	
    	iDate.setTimestamp(eventTimestamps.get(eventTimestamps.size() - 1));
    	date.set(Calendar.DAY_OF_MONTH, 1);
    	date.add(Calendar.MONTH, 1);
    	date.add(Calendar.WEEK_OF_YEAR, 1);
    	setMaxDate(Math.max(todayInMillis, date.getTimeInMillis()));
    	
    	for(Integer event : eventTimestamps) {
    		iDate.setTimestamp(event);
    		date = iDate.getCalendar();

    		final int week = getWeeksSinceMinDate(date);
    		boolean[] eventByWeek = mEventDatesByWeeks.get(week);
    		if (eventByWeek == null) {
    			eventByWeek = new boolean[DAYS_PER_WEEK];
    			mEventDatesByWeeks.put(week, eventByWeek);
    		}
    		int eventsDayOfWeek = date.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
    		if (eventsDayOfWeek < 0) {
    			eventsDayOfWeek += DAYS_PER_WEEK;
    		}
    		eventByWeek[eventsDayOfWeek] = true;
    	}
 
    	setDate((long) eventTimestamps.get(0) * 1000);
    }

    /**
     * Gets the first day of week.
     *
     * @return The first day of the week conforming to the {@link CalendarView}
     *         APIs.
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     * @see Calendar#SUNDAY
     */
    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    /**
     * Sets the first day of week.
     *
     * @param firstDayOfWeek The first day of the week conforming to the
     *            {@link CalendarView} APIs.
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     * @see Calendar#SUNDAY
     */
    public void setFirstDayOfWeek(int firstDayOfWeek) {
        if (mFirstDayOfWeek == firstDayOfWeek) {
            return;
        }
        mFirstDayOfWeek = firstDayOfWeek;
        mAdapter.init();
        mAdapter.notifyDataSetChanged();
        setUpHeader(DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID);
    }

    /**
     * Sets the listener to be notified upon selected date change.
     *
     * @param listener The listener to be notified.
     */
    public void setOnDateChangeListener(OnDateChangeListener listener) {
        mOnDateChangeListener = listener;
    }

    /**
     * Gets the selected date in milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @return The selected date.
     */
    public long getDate() {
        return mAdapter.mSelectedDate.getTimeInMillis();
    }

    /**
     * Sets the selected date in milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param date The selected date.
     *
     * @throws IllegalArgumentException of the provided date is before the
     *        minimal or after the maximal date.
     *
     * @see #setDate(long, boolean, boolean, boolean)
     * @see #setMinDate(long)
     * @see #setMaxDate(long)
     */
    public void setDate(long date) {
        setDate(date, false, false, false);
    }

    /**
     * Sets the selected date in milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param date The date.
     * @param animate Whether to animate the scroll to the current date.
     * @param center Whether to center the current date even if it is already visible.
     *
     * @throws IllegalArgumentException of the provided date is before the
     *        minimal or after the maximal date.
     *
     * @see #setMinDate(long)
     * @see #setMaxDate(long)
     */
    public void setDate(long date, boolean animate, boolean center, boolean forced) {
        mTempDate.setTimeInMillis(date);
        if (!forced && isSameDate(mTempDate, mAdapter.mSelectedDate)) {
            return;
        }
        goTo(mTempDate, animate, true, center);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }

        mCurrentLocale = locale;

        mTempDate = getCalendarForLocale(mTempDate, locale);
        mFirstDayOfMonth = getCalendarForLocale(mFirstDayOfMonth, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale The locale.
     */
    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }

    /**
     * @return True if the <code>firstDate</code> is the same as the <code>
     * secondDate</code>.
     */
    private boolean isSameDate(Calendar firstDate, Calendar secondDate) {
        return (firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR)
                && firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR));
    }

    /**
     * Creates a new adapter if necessary and sets up its parameters.
     */
    private void setUpAdapter() {
        if (mAdapter == null) {
            mAdapter = new WeeksAdapter(getContext());
            mListView.setAdapter(mAdapter);
        }

        // refresh the view with the new parameters
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Sets up the strings to be used by the header.
     */
    private void setUpHeader(int weekDayTextAppearanceResId) {
    	
    	// changed to be able to use own current locale (by DS)
    	mDayLabels = new String[DAYS_PER_WEEK];
    	DateFormatSymbols symbols = new DateFormatSymbols(mCurrentLocale);
    	final String[] tmpLabels = symbols.getShortWeekdays();
        
        for (int i = 0; i < DAYS_PER_WEEK; i++) {
            int calendarDay = i + mFirstDayOfWeek;
            if (calendarDay > DAYS_PER_WEEK) {
            	calendarDay -= DAYS_PER_WEEK;
            }
            mDayLabels[i] = tmpLabels[calendarDay];
        }

        TextView label = (TextView) mDayNamesHeader.getChildAt(0);
        label.setVisibility(View.GONE);
        
        for (int i = 1, count = mDayNamesHeader.getChildCount(); i < count; i++) {
            label = (TextView) mDayNamesHeader.getChildAt(i);
            if (weekDayTextAppearanceResId > -1) {
                label.setTextAppearance(mContext, weekDayTextAppearanceResId);
            }
            if (i < DAYS_PER_WEEK + 1) {
                label.setText(mDayLabels[i - 1]);
                label.setVisibility(View.VISIBLE);
            } else {
                label.setVisibility(View.GONE);
            }
        }
        mDayNamesHeader.invalidate();
    }

    /**
     * Sets all the required fields for the list view.
     */
    private void setUpListView() {
        // Configure the listview
        mListView.setDivider(null);
        mListView.setItemsCanFocus(true);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                CalendarView.this.onScrollStateChanged(view, scrollState);
            }

            public void onScroll(
                    AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                CalendarView.this.onScroll(view, firstVisibleItem, visibleItemCount,
                        totalItemCount);
            }
        });
    }

    /**
     * This moves to the specified time in the view. If the time is not already
     * in range it will move the list so that the first of the month containing
     * the time is at the top of the view. If the new time is already in view
     * the list will not be scrolled unless forceScroll is true. This time may
     * optionally be highlighted as selected as well.
     *
     * @param date The time to move to.
     * @param animate Whether to scroll to the given time or just redraw at the
     *            new location.
     * @param setSelected Whether to set the given time as selected.
     * @param forceScroll Whether to recenter even if the time is already
     *            visible.
     *
     * @throws IllegalArgumentException of the provided date is before the
     *        range start of after the range end.
     */
    private void goTo(Calendar date, boolean animate, boolean setSelected, boolean forceScroll) {
        if (date.before(mMinDate) || date.after(mMaxDate)) {
            
        	// invalid date - go to the min date if forceScroll is true
        	if (!forceScroll) {
        		
        		// do nothing
        		return;
        	}
        	date = mMinDate;
        }

        // Find the first and last entirely visible weeks
        int firstFullyVisiblePosition = mListView.getFirstVisiblePosition();
        View firstChild = mListView.getChildAt(0);
        if (firstChild != null && firstChild.getTop() < 0) {
            firstFullyVisiblePosition++;
        }
        int lastFullyVisiblePosition = firstFullyVisiblePosition + mShownWeekCount - 1;
        if (firstChild != null && firstChild.getTop() > mBottomBuffer) {
            lastFullyVisiblePosition--;
        }
        if (setSelected) {
            mAdapter.setSelectedDay(date);
        }
        // Get the week we're going to
        int position = getWeeksSinceMinDate(date);

        // Check if the selected day is now outside of our visible range
        // and if so scroll to the month that contains it
        if (position < firstFullyVisiblePosition || position > lastFullyVisiblePosition
                || forceScroll) {
            mFirstDayOfMonth.setTimeInMillis(date.getTimeInMillis());
            mFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

            setMonthDisplayed(mFirstDayOfMonth);

            // the earliest time we can scroll to is the min date
            if (mFirstDayOfMonth.before(mMinDate)) {
                position = 0;
            } else {
                position = getWeeksSinceMinDate(mFirstDayOfMonth);
            }

            mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING;

            mListView.setSelectionFromTop(position, mListScrollTopOffset);
            // Perform any after scroll operations that are needed
            onScrollStateChanged(mListView, OnScrollListener.SCROLL_STATE_IDLE);
        } else if (setSelected) {
            // Otherwise just set the selection
            setMonthDisplayed(date);
        }
    }

    /**
     * Parses the given <code>date</code> and in case of success sets
     * the result to the <code>outDate</code>.
     *
     * @return True if the date was parsed.
     */
    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    /**
     * Called when a <code>view</code> transitions to a new <code>scrollState
     * </code>.
     */
    private void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
    }

    /**
     * Updates the title and selected month if the <code>view</code> has moved to a new
     * month.
     */
    private void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        WeekView child = (WeekView) view.getChildAt(0);
        if (child == null) {
            return;
        }

        // Figure out where we are
        long currScroll = view.getFirstVisiblePosition() * child.getHeight() - child.getBottom();

        // If we have moved since our last call update the direction
        if (currScroll < mPreviousScrollPosition) {
            mIsScrollingUp = true;
        } else if (currScroll > mPreviousScrollPosition) {
            mIsScrollingUp = false;
        } else {
            return;
        }

        // Use some hysteresis for checking which month to highlight. This
        // causes the month to transition when two full weeks of a month are
        // visible when scrolling up, and when the first day in a month reaches
        // the top of the screen when scrolling down.
        int offset = child.getBottom() < mWeekMinVisibleHeight ? 1 : 0;
        if (mIsScrollingUp) {
            child = (WeekView) view.getChildAt(SCROLL_HYST_WEEKS + offset);
        } else if (offset != 0) {
            child = (WeekView) view.getChildAt(offset);
        }

        // Find out which month we're moving into
        int month;
        if (mIsScrollingUp) {
            month = child.getMonthOfFirstWeekDay();
        } else {
            month = child.getMonthOfLastWeekDay();
        }

        // And how it relates to our current highlighted month
        int monthDiff;
        if (mCurrentMonthDisplayed == 11 && month == 0) {
            monthDiff = 1;
        } else if (mCurrentMonthDisplayed == 0 && month == 11) {
            monthDiff = -1;
        } else {
            monthDiff = month - mCurrentMonthDisplayed;
        }

        // Only switch months if we're scrolling away from the currently
        // selected month
        if ((!mIsScrollingUp && monthDiff > 0) || (mIsScrollingUp && monthDiff < 0)) {
            Calendar firstDay = child.getFirstDay();
            if (mIsScrollingUp) {
                firstDay.add(Calendar.DAY_OF_MONTH, -DAYS_PER_WEEK);
            } else {
                firstDay.add(Calendar.DAY_OF_MONTH, DAYS_PER_WEEK);
            }
            setMonthDisplayed(firstDay);
        }
        mPreviousScrollPosition = currScroll;
        mPreviousScrollState = mCurrentScrollState;
    }

    /**
     * Sets the month displayed at the top of this view based on time. Override
     * to add custom events when the title is changed.
     *
     * @param calendar A day in the new focus month.
     */
    private void setMonthDisplayed(Calendar calendar) {
    	
    	// changed to be able to use own current locale (by DS)
    	final SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL, mCurrentLocale);
    	df.applyLocalizedPattern(df.toLocalizedPattern().replaceAll(".?[,EeDd].?", ""));
        mMonthName.setText(df.format(calendar.getTime()));
        mMonthName.invalidate();
        mCurrentMonthDisplayed = calendar.get(Calendar.MONTH);
        mAdapter.setFocusMonth(mCurrentMonthDisplayed);
        // TODO Send Accessibility Event
    }

    /**
     * @return Returns the number of weeks between the current <code>date</code>
     *         and the <code>mMinDate</code>.
     */
    private int getWeeksSinceMinDate(Calendar date) {
        long startTimeMillis = mMinDate.getTimeInMillis()
                + mMinDate.getTimeZone().getOffset(mMinDate.getTimeInMillis());
        long endTimeMillis = 0;
        if (date.before(mMinDate)) {

            Debug.logW("CalendarView", "date does not precede minDate");

            endTimeMillis = startTimeMillis;
        } else {
            endTimeMillis = date.getTimeInMillis()
                    + date.getTimeZone().getOffset(date.getTimeInMillis());
        }
        long dayOffsetMillis = (mMinDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
                * MILLIS_IN_DAY;
        return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
    }

    /**
     * Command responsible for acting upon scroll state changes.
     */
    private class ScrollStateRunnable implements Runnable {
        private AbsListView mView;

        private int mNewState;

        /**
         * Sets up the runnable with a short delay in case the scroll state
         * immediately changes again.
         *
         * @param view The list view that changed state
         * @param scrollState The new state it changed to
         */
        public void doScrollStateChange(AbsListView view, int scrollState) {
            mView = view;
            mNewState = scrollState;
            removeCallbacks(this);
            postDelayed(this, SCROLL_CHANGE_DELAY);
        }

        public void run() {
            mCurrentScrollState = mNewState;
            // Fix the position after a scroll or a fling ends
            if (mNewState == OnScrollListener.SCROLL_STATE_IDLE
                    && mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                View child = mView.getChildAt(0);
                if (child == null) {
                    // The view is no longer visible, just return
                    return;
                }
                int dist = child.getBottom() - mListScrollTopOffset;
                if (dist > mListScrollTopOffset) {
                    if (mIsScrollingUp) {
                        mView.smoothScrollBy(dist - child.getHeight(), ADJUSTMENT_SCROLL_DURATION);
                    } else {
                        mView.smoothScrollBy(dist, ADJUSTMENT_SCROLL_DURATION);
                    }
                }
            }
            mPreviousScrollState = mNewState;
        }
    }

    /**
     * <p>
     * This is a specialized adapter for creating a list of weeks with
     * selectable days. It can be configured to display the week number, start
     * the week on a given day, show a reduced number of days, or display an
     * arbitrary number of weeks at a time.
     * </p>
     */
    private class WeeksAdapter extends BaseAdapter implements OnTouchListener {

        private int mSelectedWeek;
        private int mTodayWeek;

        private GestureDetector mGestureDetector;

        private int mFocusedMonth;

        private final Calendar mSelectedDate = Calendar.getInstance();
        
        private final Calendar mTodayDate = Calendar.getInstance();

        private int mTotalWeekCount;

        public WeeksAdapter(Context context) {
            mContext = context;
            mGestureDetector = new GestureDetector(mContext, new CalendarGestureListener());
            init();
        }

        /**
         * Set up the gesture detector and selected time
         */
        private void init() {
            mTotalWeekCount = getWeeksSinceMinDate(mMaxDate);
            int missing = 2 * SCROLL_HYST_WEEKS + 1 - mTotalWeekCount;
            if (missing > 0) {
                int missingEnd = missing / 2;
                int missingStart = missing - missingEnd;
                mMinDate.add(Calendar.WEEK_OF_MONTH, -missingStart);
                mMaxDate.add(Calendar.WEEK_OF_MONTH, missingEnd);

                mTotalWeekCount = getWeeksSinceMinDate(mMaxDate);
            }

            mTodayWeek = getWeeksSinceMinDate(mTodayDate);
            mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);

            if (mMinDate.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek
                || mMaxDate.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
                mTotalWeekCount++;
            }
        }

        /**
         * Updates the selected day and related parameters.
         *
         * @param selectedDay The time to highlight
         */
        public void setSelectedDay(Calendar selectedDay) {
            if (selectedDay.get(Calendar.DAY_OF_YEAR) == mSelectedDate.get(Calendar.DAY_OF_YEAR)
                    && selectedDay.get(Calendar.YEAR) == mSelectedDate.get(Calendar.YEAR)) {
                return;
            }
            mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
            mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);
            mFocusedMonth = mSelectedDate.get(Calendar.MONTH);
            
            notifyDataSetChanged();
        }

        /**
         * @return The selected day of month.
         */
        public Calendar getSelectedDay() {
            return mSelectedDate;
        }

        @Override
        public int getCount() {
            return mTotalWeekCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WeekView weekView = null;
            if (convertView != null) {
                weekView = (WeekView) convertView;
            } else {
                weekView = new WeekView(mContext);
                android.widget.AbsListView.LayoutParams params =
                    new android.widget.AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
                weekView.setLayoutParams(params);
                weekView.setClickable(true);
                weekView.setOnTouchListener(this);
            }

            int selectedWeekDay;
            int todayWeekDay;
            if (mSelectedWeek == position) {
            	selectedWeekDay = mSelectedDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
                if (selectedWeekDay < 0) {
                	selectedWeekDay += DAYS_PER_WEEK;
                }
            } else {
            	selectedWeekDay = -1;
            }
            if (mTodayWeek == position) {
            	todayWeekDay = mTodayDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
                if (todayWeekDay < 0) {
                	todayWeekDay += DAYS_PER_WEEK;
                }
            } else {
            	todayWeekDay = -1;
            }
            weekView.init(position, todayWeekDay, selectedWeekDay, mFocusedMonth, mEventDatesByWeeks.get(position));

            return weekView;
        }

        /**
         * Changes which month is in focus and updates the view.
         *
         * @param month The month to show as in focus [0-11]
         */
        public void setFocusMonth(int month) {
            if (mFocusedMonth == month) {
                return;
            }
            mFocusedMonth = month;
            notifyDataSetChanged();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mListView.isEnabled() && mGestureDetector.onTouchEvent(event)) {
                WeekView weekView = (WeekView) v;
                // if we cannot find a day for the given location we are done
                if (!weekView.getDayFromLocation(event.getX(), mTempDate)) {
                    return true;
                }
                // it is possible that the touched day is outside the valid range
                // we draw whole weeks but range end can fall not on the week end
                if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
                    return true;
                }
                onDateTapped(mTempDate);
                return true;
            }
            return false;
        }

        /**
         * Maintains the same hour/min/sec but moves the day to the tapped day.
         *
         * @param day The day that was tapped
         */
        private void onDateTapped(Calendar day) {
            setSelectedDay(day);
            setMonthDisplayed(day);
            
            if (mOnDateChangeListener != null) {
                mOnDateChangeListener.onSelectedDayChange(CalendarView.this,
                		mSelectedDate.get(Calendar.YEAR),
                		mSelectedDate.get(Calendar.MONTH),
                		mSelectedDate.get(Calendar.DAY_OF_MONTH));
            }
        }

        /**
         * This is here so we can identify single tap events and set the
         * selected day correctly
         */
        class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        }
    }

    /**
     * <p>
     * This is a dynamic view for drawing a single week. It can be configured to
     * display the week number, start the week on a given day, or show a reduced
     * number of days. It is intended for use as a single view within a
     * ListView. See {@link WeeksAdapter} for usage.
     * </p>
     */
    private class WeekView extends View {

        private final Rect mTempRect = new Rect();

        private final Paint mDrawPaint = new Paint();

        private final Paint mMonthNumDrawPaint = new Paint();

        // Cache the number strings so we don't have to recompute them each time
        private String[] mDayNumbers;

        // Quick lookup for checking which days are in the focus month
        private boolean[] mFocusDay;

        // The first day displayed by this item
        private Calendar mFirstDay;

        // The month of the first day in this week
        private int mMonthOfFirstWeekDay = -1;

        // The month of the last day in this week
        private int mLastWeekDayMonth = -1;

        // The position of this week, equivalent to weeks since the week of Jan
        // 1st, 1900
        private int mWeek = -1;

        // Quick reference to the width of this view, matches parent
        private int mWidth;

        // The height this view should draw at in pixels, set by height param
        private int mHeight;
        
        // Which day is today [0-6] or -1 if no day is today
        private int mToday = -1;

        // Which day is selected [0-6] or -1 if no day is selected
        private int mSelectedDay = -1;

        // The number of days + a spot for week number if it is displayed
        private int mNumCells;
        
        private boolean[] mDaysWEvents;

        public WeekView(Context context) {
            super(context);

            mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView
                    .getPaddingBottom()) / mShownWeekCount;

            // Sets up any standard paints that will be used
            setPaintProperties();
        }

        /**
         * Initializes this week view.
         *
         * @param weekNumber The number of the week this view represents. The
         *            week number is a zero based index of the weeks since
         *            {@link CalendarView#getMinDate()}.
         * @param todayWeekDay The day of the week from 0 to 6
         * @param selectedWeekDay The selected day of the week from 0 to 6, -1 if no
         *            selected day.
         * @param focusedMonth The month that is currently in focus i.e.
         *            highlighted.
         */
        public void init(int weekNumber, int todayWeekDay, int selectedWeekDay, int focusedMonth,
        		boolean[] daysWEvents) {
        	mDaysWEvents = daysWEvents;
        	
        	mToday = todayWeekDay;
            mSelectedDay = selectedWeekDay;
            mNumCells = DAYS_PER_WEEK;
            mWeek = weekNumber;
            mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());

            mTempDate.add(Calendar.WEEK_OF_YEAR, mWeek);
            mTempDate.setFirstDayOfWeek(mFirstDayOfWeek);

            // Allocate space for caching the day numbers and focus values
            mDayNumbers = new String[mNumCells];
            mFocusDay = new boolean[mNumCells];

            // If we're showing the week number calculate it based on Monday
            int i = 0;

            // Now adjust our starting day based on the start day of the week
            int diff = mFirstDayOfWeek - mTempDate.get(Calendar.DAY_OF_WEEK);
            mTempDate.add(Calendar.DAY_OF_MONTH, diff);

            mFirstDay = (Calendar) mTempDate.clone();
            mMonthOfFirstWeekDay = mTempDate.get(Calendar.MONTH);

            for (; i < mNumCells; i++) {
                mFocusDay[i] = (mTempDate.get(Calendar.MONTH) == focusedMonth);
                // do not draw dates outside the valid range to avoid user confusion
                if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
                    mDayNumbers[i] = "";
                } else {
                    mDayNumbers[i] = Integer.toString(mTempDate.get(Calendar.DAY_OF_MONTH));
                }
                mTempDate.add(Calendar.DAY_OF_MONTH, 1);
            }
            // We do one extra add at the end of the loop, if that pushed us to
            // new month undo it
            if (mTempDate.get(Calendar.DAY_OF_MONTH) == 1) {
                mTempDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            mLastWeekDayMonth = mTempDate.get(Calendar.MONTH);
        }

        /**
         * Sets up the text and style properties for painting.
         */
        private void setPaintProperties() {
            mDrawPaint.setFakeBoldText(false);
            mDrawPaint.setAntiAlias(true);
            mDrawPaint.setTextSize(mDateTextSize);
            mDrawPaint.setStyle(Style.FILL);

            mMonthNumDrawPaint.setFakeBoldText(true);
            mMonthNumDrawPaint.setAntiAlias(true);
            mMonthNumDrawPaint.setTextSize(mDateTextSize);
            mMonthNumDrawPaint.setColor(mFocusedMonthDateColor);
            mMonthNumDrawPaint.setStyle(Style.FILL);
            mMonthNumDrawPaint.setTextAlign(Align.CENTER);
        }

        /**
         * Returns the month of the first day in this week.
         *
         * @return The month the first day of this view is in.
         */
        public int getMonthOfFirstWeekDay() {
            return mMonthOfFirstWeekDay;
        }

        /**
         * Returns the month of the last day in this week
         *
         * @return The month the last day of this view is in
         */
        public int getMonthOfLastWeekDay() {
            return mLastWeekDayMonth;
        }

        /**
         * Returns the first day in this view.
         *
         * @return The first day in the view.
         */
        public Calendar getFirstDay() {
            return mFirstDay;
        }

        /**
         * Calculates the day that the given x position is in, accounting for
         * week number.
         *
         * @param x The x position of the touch event.
         * @return True if a day was found for the given location.
         */
        public boolean getDayFromLocation(float x, Calendar outCalendar) {
            int dayStart = 0;
            if (x < dayStart || x > mWidth) {
                outCalendar.clear();
                return false;
            }
            // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
            int dayPosition = (int) ((x - dayStart) * DAYS_PER_WEEK
                    / (mWidth - dayStart));
            
        	if (mCanOnlySelectEvents && (mDaysWEvents == null || !mDaysWEvents[dayPosition])) {
        		
        		// if this day has no event it does not exists as far as we are concerned
        		return false;
        	}
            
            outCalendar.setTimeInMillis(mFirstDay.getTimeInMillis());
            outCalendar.add(Calendar.DAY_OF_MONTH, dayPosition);
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            drawWeekSeparators(canvas);
            drawWeekNumbers(canvas);
        }

        /**
         * Draws the week and month day numbers and backgrounds for this week, 
         * also draws horizontal lines after days except for the last day
         *
         * @param canvas The canvas to draw on
         */
        private void drawWeekNumbers(Canvas canvas) {
            float textHeight = mDrawPaint.getTextSize();
            int y = (int) ((mHeight + textHeight) / 2) - mWeekSeperatorLineWidth;

            mDrawPaint.setTextAlign(Align.CENTER);
            int i = 0;

            for (; i < mNumCells; i++) {
            	
            	int x = i * mWidth / mNumCells;
            	
            	// draw background of day if day is in events
            	final boolean isEvent = mDaysWEvents != null && mDaysWEvents[i];
            	final boolean isSelected = mSelectedDay == i && (!mCanOnlySelectEvents || isEvent);
            	if (isSelected || isEvent || mToday == i) {
            		mDrawPaint.setColor(isSelected ? mSelectedDayBackgroundColor : 
            			mToday != i ? mEventDayBackgroundColor : mTodayBackgroundColor);
            		mTempRect.top = mWeekSeperatorLineWidth;
            		mTempRect.bottom = mHeight;
            		mTempRect.left = x;
            		mTempRect.right = x + mWidth / mNumCells;
            		canvas.drawRect(mTempRect, mDrawPaint);
            	}
            	
                // draw vertical separator
            	mDrawPaint.setColor(mDaySeparatorLineColor);
                if (i != 0) {
                	canvas.drawRect(x, 0, x + mWeekSeperatorLineWidth - 1, mHeight, mDrawPaint);
                }
            	
            	// draw days number
                x = (2 * i + 1) * mWidth / mNumCells / 2;
                mMonthNumDrawPaint.setColor(isSelected ? mSelectedDayColor :
                	(isEvent ? (mFocusDay[i] ? mEventDayColor : mUnfocusedEventDayColor) :
                		(mToday == i ?
                				mTodayColor : (mFocusDay[i] ? mFocusedMonthDateColor : mUnfocusedMonthDateColor)
                		)
                	)
                );
                canvas.drawText(mDayNumbers[i], x, y, mMonthNumDrawPaint);
            }
        }

        /**
         * Draws a horizontal line for separating the weeks.
         *
         * @param canvas The canvas to draw on.
         */
        private void drawWeekSeparators(Canvas canvas) {
            mDrawPaint.setColor(mDaySeparatorLineColor);
            mDrawPaint.setStrokeWidth(mWeekSeperatorLineWidth);
            float x = 0;
            canvas.drawLine(x, 0, mWidth, 0, mDrawPaint);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mWidth = w;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
        }
    }
}