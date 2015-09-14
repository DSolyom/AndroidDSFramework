/**
 * @class LinkWithoutScrollingMovementMethod
 * 
 * code mostly coming from LinkMovementMethod
 * which is:
 */
/* Copyright (C) 2006 The Android Open Source Project
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

package ds.framework.v4.widget;

import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkWithoutScrollingMovementMethod implements MovementMethod {

	private static LinkWithoutScrollingMovementMethod sInstance;
	private static Object FROM_BELOW = new NoCopySpan.Concrete();
	
	@Override
	public boolean canSelectArbitrarily() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize(TextView widget, Spannable text) {
		Selection.removeSelection(text);
		text.removeSpan(FROM_BELOW);
	}

	@Override
	public boolean onKeyDown(TextView widget, Spannable text, int keyCode,
			KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				if (event.getRepeatCount() == 0) {
					if (action(widget, text)) {
						return true;
					}
				}
				
				// no break intended
				
			default:
				return false;
		}
	}

	@Override
	public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onKeyUp(TextView widget, Spannable text, int keyCode,
			KeyEvent event) {
		return false;
	}

	@Override
	public void onTakeFocus(TextView widget, Spannable text, int direction) {
	}

	@Override
	public boolean onTouchEvent(TextView widget, Spannable text,
			MotionEvent event) {
		int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = text.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                	try {
                		link[0].onClick(widget);
                	} catch(Throwable e) {
                		return false;
                	}
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(text,
                                           text.getSpanStart(link[0]),
                                           text.getSpanEnd(link[0]));
                }

                return true;
            } else {
                Selection.removeSelection(text);
            }
        }

        return false;
	}

	@Override
	public boolean onTrackballEvent(TextView widget, Spannable text,
			MotionEvent event) {
		return false;
	}
	
	private boolean action(TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();

        int padding = widget.getTotalPaddingTop() +
                      widget.getTotalPaddingBottom();
        int areatop = widget.getScrollY();
        int areabot = areatop + widget.getHeight() - padding;

        int linetop = layout.getLineForVertical(areatop);
        int linebot = layout.getLineForVertical(areabot);

        int first = layout.getLineStart(linetop);
        int last = layout.getLineEnd(linebot);

        int a = Selection.getSelectionStart(buffer);
        int b = Selection.getSelectionEnd(buffer);

        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);

        if (selStart < 0) {
            if (buffer.getSpanStart(FROM_BELOW) >= 0) {
                selStart = selEnd = buffer.length();
            }
        }

        if (selStart > last) {
            selStart = selEnd = Integer.MAX_VALUE;
        }
        if (selEnd < first) {
            selStart = selEnd = -1;

            if (selStart == selEnd) {
                return false;
            }

            ClickableSpan[] link = buffer.getSpans(selStart, selEnd, ClickableSpan.class);

            if (link.length != 1) {
                return false;
            }

            try {
            	link[0].onClick(widget);
            } catch(Throwable e) {
            	return false;
            }
            return true;
        }

        return false;
    }

	public static LinkWithoutScrollingMovementMethod getInstance() {
		if (sInstance == null) {
			sInstance = new LinkWithoutScrollingMovementMethod();
		}
		return sInstance;
	}

	public boolean onGenericMotionEvent(TextView arg0, Spannable arg1,
			MotionEvent arg2) {
		return false;
	}
}
