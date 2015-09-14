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
package ds.framework.v4.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;
import ds.framework.v4.R;

public class DSEditText extends EditText {

	private int mCounterResource;
	private TextView mVCounter;
	protected Integer mMaxLength = -1;
	protected Activity mIn;
	
	public DSEditText(Context context) {
		super(context);
	}
	
	public DSEditText(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public DSEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mIn = (Activity) getContext();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DsView, defStyle, 0);
		
		mCounterResource = a.getResourceId(R.styleable.DsView_countIn, -1);

		mMaxLength = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "maxLength", -1);

		addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (mMaxLength != -1 && mVCounter != null) {
					mVCounter.setText(String.valueOf(mMaxLength - s.length()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if (mVCounter == null) {
					mVCounter = (TextView) mIn.findViewById(mCounterResource);
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				;
			}
			
		});
		
		a.recycle();
	}
	
	/**
	 * set max length programmatically
	 * 
	 * @param limit
	 */
	public void setMaxLength(int limit) {
		mMaxLength = limit;
		
		InputFilter[] filters = getFilters();
		int sF = filters.length;
		int i;

		for(i = 0; i < sF; ++i) {
			if (filters[i] instanceof InputFilter.LengthFilter) {
				if (mMaxLength != -1) {
					filters[i] = new InputFilter.LengthFilter(mMaxLength);
				} else {
					InputFilter[] newFilters = new InputFilter[sF - 1];
					if (i > 0) {
						System.arraycopy(filters, 0, newFilters, 0, i);
					}
					if (i < sF - 1) {
						System.arraycopy(filters, i + 1, newFilters, i, sF - 1 - i);
					}
					filters = newFilters;
				}
				break;
			}
		}
		if (mMaxLength != -1 && i == sF) {
			InputFilter[] newFilters = new InputFilter[sF + 1];
			System.arraycopy(filters, 0, newFilters, 0, sF);
			newFilters[sF] = new InputFilter.LengthFilter(mMaxLength);
			filters = newFilters;
		}
		setFilters(filters);
	}
	
	public void removeMaxLength() {
		setMaxLength(-1);
	}
}
