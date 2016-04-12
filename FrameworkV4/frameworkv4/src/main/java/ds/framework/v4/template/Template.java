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

package ds.framework.v4.template;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPagerModByDS;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import java.util.ArrayList;

import ds.framework.v4.Global;
import ds.framework.v4.R;
import ds.framework.v4.app.DSActivity;
import ds.framework.v4.common.AnimationStarter;
import ds.framework.v4.common.Common;
import ds.framework.v4.common.Debug;
import ds.framework.v4.datatypes.Datatype;
import ds.framework.v4.datatypes.Transport;
import ds.framework.v4.datatypes.WInteger;
import ds.framework.v4.datatypes.WString;
import ds.framework.v4.widget.BothScrollView;
import ds.framework.v4.widget.HtmlTextView;
import ds.framework.v4.widget.IRecyclerView;
import ds.framework.v4.widget.LaizyImageFlipAnimationLayout;
import ds.framework.v4.widget.LaizyImageView;
import ds.framework.v4.widget.LaizyImageView.LaizyImageViewInfo;
import ds.framework.v4.widget.MiniListView;
import ds.framework.v4.widget.RecyclerViewHeaderedAdapter;
import ds.framework.v4.widget.TemplateRecyclerViewAdapter;

public class Template {

    public static final int VIEW_NONE = -1;

	public static final int DETECT = 0;
	
	public static final int TEXT = 1;
	public static final int TEXT_RESOURCE = 2;
	public static final int FROM_HTML = 3;
	public static final int FROM_HTML_W_LINK = 4;
	public static final int TEXT_COLOR = 5;
	public static final int TEXT_UNDERLINE = 6;

	public static final int BACKGROUND = 10;
	public static final int BACKGROUND_COLOR = 11;
	public static final int BACKGROUND_DRAWABLE = 12;
	public static final int BACKGROUND_ANIMATION = 13;

	public static final int IMAGE = 20;
	public static final int IMAGE_DRAWABLE = 21;
	public static final int IMAGE_LAIZY = 22;
	public static final int IMAGE_ANIMATION_START = 23;
	public static final int IMAGE_TINT = 24;
	
	public static final int GONE = 30;
	public static final int INVISIBLE = 31;
	public static final int VISIBLE = 32;
	
	public static final int ENABLED = 35;
	
	/**
	 * any template list - (like TemplateListView or ContinuousListView)
	 */
	@Deprecated
	public static final int LIST = 40;
	@Deprecated
	public static final int LIST_HEADER = 41;
	@Deprecated
	public static final int LIST_FOOTER = 42;

	public static final int RECYCLER_VIEW = LIST;
	public static final int RECYCLER_HEADER = LIST_HEADER;
	public static final int RECYCLER_FOOTER = LIST_FOOTER;

	public static final int ADAPTER = 45;
	
	public static final int CHECKBOX_CHECKED = 51;

	public static final int FORMFIELD = 60;
	public static final int FIELD_EDITTEXT = 61;
	public static final int ONKEY_LISTENER = 62;
	
	public static final int FIELD_SPINNER = FORMFIELD;
	public static final int FIELD_MULTICHOICESPINNER = FORMFIELD;
	public static final int FIELD_RADIOBUTTON = FORMFIELD;
	public static final int FIELD_RADIOGROUP = 70;
	public static final int FIELD_CHECKBOX = 71;
	public static final int FIELD_SLIDER = 72;
	
	public static final int ONCLICK_TRANSPORT = 100;
	// public static final int ONITEMCLICK_TRANSPORT = 101; removed due to framework changes and recyclerview
	public static final int ONCLICK_FORWARD = 102;
	public static final int ONCLICK_FORWARD_AND_CLEAR = 103;
	public static final int ONCLICK_DIALOG = 104;
	public static final int ONCLICK_DIALOG_CLOSE = 105;
	public static final int ONCLICK_RUNNABLE = 106;
	public static final int ONCLICK_GOBACK = 107;
	public static final int ONCLICK_GOBACKTO = 108;

	public static final int ONCLICK_LISTENER = 200;
	public static final int ONLONGCLICK_LISTENER = 201;
	public static final int ONLONGCLICK_DIALOG = 202;
	public static final int ONITEMSELECTED_LISTENER = 203;
	public static final int ONITEMCLICK_LISTENER = 204;
	public static final int ONFOCUSCHANGE_LISTENER = 205;
	public static final int ONTOUCH_LISTENER = 206;	
	public static final int ONPOSITIONCHANGED_LISTENER = 207;
	public static final int ONSCROLL_LISTENER = 208;
	public static final int ONCHECKEDCHANGE_LISTENER = 209;
	public static final int ONBUTTONSELECT_LISTENER = 210;

	public static final int ONCLICK_SEARCH = 300;
	
	public static final int SELECTED_STATE = 500;
	public static final int TAG = 501;
	
	public static final int PADDING = 600;
	public static final int PADDING_LEFT = 601;
	public static final int PADDING_RIGHT = 602;
	public static final int PADDING_BOTTOM = 603;
	public static final int PADDING_TOP = 604;
	
	public static final int FIX_TILE_MODE = 10000;

	private DSActivity mOwner;
	private View mRootView;
    private ArrayList<View> mOtherRoots;
	
	public Template(DSActivity owner, View rootView) {
		mOwner = owner;
		mRootView = rootView;
	}
	
	/**
	 * 
	 * @return
	 */
	public Context getContext() {
		if (mRootView == null) {
			return null;
		}
		return mRootView.getContext();
	}
	
	/**
	 * 
	 * @return
	 */
	public DSActivity getOwner() {
		return mOwner;
	}
	
	/**
	 * 
	 * @param viewResID
	 * @return
	 */
	public View findViewById(int viewResID) {
		if (viewResID ==  VIEW_NONE) {
			return null;
		}
		if (viewResID == 0 || mRootView.getId() == viewResID) {
			return mRootView;
		}
		View v = mRootView.findViewById(viewResID);
        if (v == null && mOtherRoots != null) {
            for(View root : mOtherRoots) {
                v = root.findViewById(viewResID);
                if (v != null) {
                    break;
                }
            }
        }
        return v;
	}
	
	public Object fill(int viewRes, Object value) {
		return fill(viewRes, value, DETECT, null);
	}
	
	public Object fill(int viewRes, Object value, int type) {
		return fill(viewRes, value, type, null);
	}
	
	/**
	 * 
	 * @param viewRes
	 * @param value
	 * @param type
	 * @param id
	 * @return
	 */
	public Object fill(int viewRes, Object value, int type, String id) {
		View view = findViewById(viewRes);

		if (view == null) {
			
			// this is just a warning
			// this way it's safe to create general template filling methods in data objects 
			// even when the layout does not support some part of the data
			Debug.logI("template", "View (" + Integer.toHexString(viewRes) + ") not found for (" + id + ")!");
			return null;
		}

		if (type == DETECT) {
			type = getTypeByViewOrValue(view, value);
		}
		
		return fill(view, value, type, id);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object fill(View view, Object value, final int type, String id) {
		try {
			switch(type) {
				case TEXT:
					((TextView) view).setText("");
					try {
						if (value != null) {
							((TextView) view).setText(String.valueOf(value));
						}
					} catch(ClassCastException e) {
						;
					}
					break;
					
				case TEXT_RESOURCE:
					
					// if not set - show nothing
					
					try {
						if (((Integer) value) != 0) {
							((TextView) view).setText((Integer) value);
						} else {
							((TextView) view).setText("");
						}
					} catch(ClassCastException e) {
						if (!((Datatype<Integer>) value).isEmpty()) {
							((TextView) view).setText(((Datatype<Integer>) value).get());
						} else {
							((TextView) view).setText("");
						}
					} catch(NullPointerException e) {
						((TextView) view).setText("");
					}
					break;
					
				case FROM_HTML_W_LINK:
					((TextView) view).setMovementMethod(LinkMovementMethod.getInstance());
					// no break intended 
					
				case FROM_HTML:
					try {
						String text;
						try {
							text = Global.getContext().getString((Integer) value);
						} catch(ClassCastException e) {
							try {
								text = Global.getContext().getString(((Datatype<Integer>) value).get());
							} catch(ClassCastException e2) {
								text = String.valueOf(value);
							}
						}
						if (view instanceof HtmlTextView) {
							((HtmlTextView) view).setHtmlText(text, R.drawable.x_empty);
						} else {
							((TextView) view).setText(Html.fromHtml(text));
						}
					} catch(NullPointerException e) {
						((TextView) view).setText("");
					}
					break;
					
				case TEXT_COLOR:
					try {
						((TextView) view).setTextColor(Color.parseColor(String.valueOf(value)));
					} catch(IllegalArgumentException e) {
						((TextView) view).setTextColor((Integer) value);
					}
					break;
					
				case TEXT_UNDERLINE:
					CharSequence text = ((TextView) view).getText();
					if (text.length() == 0) {
						return null;
					}
					SpannableString sText = new SpannableString(text);
					sText.setSpan(new UnderlineSpan(), 0, text.length(), 0);
				    ((TextView) view).setText(sText, BufferType.SPANNABLE);

					break;
				
				case BACKGROUND:
					try {
						view.setBackgroundResource((Integer) value);
					} catch(ClassCastException e) {
						view.setBackgroundResource(((Datatype<Integer>) value).get());
					}
					break;
					
				case BACKGROUND_COLOR:
					view.setBackgroundColor(getIntegerValue(value));
					break;
					
				case BACKGROUND_DRAWABLE:
					try {
						view.setBackgroundDrawable((Drawable) value);
					} catch(ClassCastException e) {
						view.setBackgroundDrawable((Drawable) ((Datatype<Drawable>) value).get());
					}
					break;
					
				case BACKGROUND_ANIMATION:
					
					view.setBackgroundResource(getIntegerValue(value));
					final AnimationDrawable animation = (AnimationDrawable)
							view.getBackground();
					
					AnimationStarter.start(animation);
					break;
						
				case IMAGE:
					Integer resource = getIntegerValue(value);
					
					if (resource != null) {
						((ImageView) view).setImageResource(resource);
					} else {
						((ImageView) view).setImageResource(R.drawable.x_empty);
					}
					
					break;
					
				case IMAGE_DRAWABLE:
					
					try {
						((ImageView) view).setImageDrawable(((Datatype<Drawable>) value).get());
					} catch (ClassCastException e) {
						((ImageView) view).setImageDrawable((Drawable) value);
					} catch (NullPointerException e) {
						((ImageView) view).setImageResource(R.drawable.x_empty);
					}
					break;
					
				case IMAGE_LAIZY:
					
					try {
						if (view instanceof LaizyImageView) {
							((LaizyImageView) view).load((LaizyImageViewInfo) value);
							break;
						} else {
							((LaizyImageFlipAnimationLayout) view).loadImage((LaizyImageViewInfo) value);
						}
					} catch(Throwable e) {
						
						// hide image instead
						((ImageView) view).setImageResource(R.drawable.x_empty);
					}
					break;
					
				case IMAGE_ANIMATION_START:
					
					if (!isTrue(value)) {
						return null;
					}
					
					final AnimationDrawable drawable = (AnimationDrawable)
							((ImageView) view).getDrawable();
					
					AnimationStarter.start(drawable);
					break;
					
				case IMAGE_TINT:
					((ImageView) view).setColorFilter(Global.getContext().getResources().getColor((Integer) value));
					break;
					
				case VISIBLE:
					
					view.setVisibility(isTrue(value) ? View.VISIBLE : View.GONE);
					break;
					
				case INVISIBLE:
					
					view.setVisibility(isTrue(value) ? View.INVISIBLE : View.VISIBLE);
					break;
					
				case GONE:
					
					view.setVisibility(isTrue(value) ? View.GONE : View.VISIBLE);
					break;
					
				case ENABLED:
					view.setEnabled(isTrue(value));
					break;

				case ADAPTER:
				case RECYCLER_VIEW:

                    if (view instanceof RecyclerView) {
                        ((RecyclerView) view).setAdapter((RecyclerView.Adapter) value);
                    } else if (view instanceof AdapterView) {
						Adapter adapter = ((AdapterView) view).getAdapter();
						if (adapter != null && adapter instanceof HeaderViewListAdapter) {
							adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
						}
						if (adapter != (Adapter) value) {
							((AdapterView) view).setAdapter((Adapter) value);
						
							if (value instanceof TemplateRecyclerViewAdapter) {
								((TemplateRecyclerViewAdapter) value).setAdapterView((AdapterView) view);
							}
						} else if (value instanceof BaseAdapter) {
							((BaseAdapter) value).notifyDataSetChanged();
						}
					} else if (view instanceof MiniListView) {
						((MiniListView) view).setAdapter((RecyclerViewHeaderedAdapter) value);
					} else if (view instanceof ViewPagerModByDS) {
						((ViewPagerModByDS) view).setAdapter((PagerAdapter) value);
					}
					break;
					
				case RECYCLER_HEADER:

                    if (view instanceof IRecyclerView) {
                        ((IRecyclerView) view).setHeaderView((View) value);
                        return value;
                    }

					if (value == null) {
						return null;
					}

                    if (((ListView) view).getHeaderViewsCount() == 0 && value instanceof Integer) {
						final View headerView = mOwner.inflate((Integer) value, (ListView) view, false);
						((ListView) view).addHeaderView(headerView);
						return headerView;
					} else if (value instanceof View) {
						((ListView) view).removeHeaderView((View) value);
						return null;
					}
					break;
					
				case RECYCLER_FOOTER:

                    if (view instanceof IRecyclerView) {
                        ((IRecyclerView) view).setFooterView((View) value);
                        return value;
                    }

					if (value == null) {
						return null;
					}
					if (((ListView) view).getFooterViewsCount() == 0 && value instanceof Integer) {
						final View footerView = mOwner.inflate((Integer) value, (ListView) view, false);
						((ListView) view).addFooterView(footerView);
						return footerView;
					} else if (value instanceof View) {
						((ListView) view).removeFooterView((View) value);
						return null;
					}
					break;
					
				case CHECKBOX_CHECKED:
					((CheckBox) view).setChecked(isTrue(value));
					break;
					
				case ONKEY_LISTENER:
					
					view.setOnKeyListener((OnKeyListener) value);
					break;
					
				case ONCLICK_TRANSPORT:
				case ONCLICK_FORWARD:
				case ONCLICK_FORWARD_AND_CLEAR:
				case ONCLICK_GOBACKTO:

					final Transport transport = ((Transport) value);
					view.setTag(R.integer.tag_click_id, mOwner.getUniqueId());
					
					// show another screen on click - sending data along the way
					view.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								final DSActivity activity = (DSActivity) Global.getCurrentActivity();
								if (activity == null || !activity.getUniqueId().equals(v.getTag(R.integer.tag_click_id))) {
									return;
								}

								if (activity.isRunning()) {
									if (type == ONCLICK_TRANSPORT) {
										activity.transport(transport.to, transport.sharedViews, transport.data);
									} else if (type == ONCLICK_FORWARD) {
										activity.forward(transport.to, transport.data);
									} else if (type == ONCLICK_FORWARD_AND_CLEAR) {
										activity.forwardAndClear(transport.to, transport.data);
									} else {
										activity.goBackTo(transport.to, transport.data);
									}
								}
								
							} catch(Throwable e) {
								Debug.logException(e);
							}
						}

					});
					break;
				
				case ONCLICK_GOBACK:
					
					view.setTag(R.integer.tag_click_id, mOwner.getUniqueId());

					final Object result = value;
					
					view.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								final DSActivity activity = (DSActivity) Global.getCurrentActivity();
								if (activity == null || !activity.getUniqueId().equals(v.getTag(R.integer.tag_click_id))) {
									return;
								}

								if (activity.isRunning()) {
									((DSActivity) Global.getCurrentActivity()).goBack(result);
								}
								
							} catch(Throwable e) {
								Debug.logException(e);
							}
						}
						
					});
					break;

				case ONCLICK_DIALOG:
					
					// TODO
					break;
					
				case ONLONGCLICK_DIALOG:
					
					// TODO
					break;

				case ONCLICK_DIALOG_CLOSE:
					
					// TODO
					break;
					
				case ONCLICK_RUNNABLE:
					
					final Runnable runnable = (Runnable) value;
					
					view.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							try {
								runnable.run();
							} catch(Throwable e) {
								Debug.logException(e);
							}
						}
					});
					break;
			/* removed due to framework changes and recyclerview		
				case ONITEMCLICK_TRANSPORT:
					
					final Transport transportb = ((Transport) value);
					view.setTag(R.integer.tag_click_id, mOwner.getUniqueId());

					/*if (a instanceof AbsScreenFragment) {
						transportb.fromFragment = (AbsScreenFragment) a;
					} else {
						transportb.fromFragment = null;
					}*
					
					// show another screen on click - sending data along the way
					((AdapterView) view).setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							try {
								final DSActivity activity = (DSActivity) Global.getCurrentActivity();
								if (activity == null || !activity.getUniqueId().equals(parent.getTag(R.integer.tag_click_id))) {
									return;
								}
								/*if (transportb.fromFragment != null) {
									if (!transportb.fromFragment.isResumed()) {
										return;
									}
								}*

								// fix for list headers
								if (parent instanceof ListView) { 
									position -= ((ListView) parent).getHeaderViewsCount();
								}
								if (position < 0) {
									return;
								}
								
								Object data = null;
								
								try {
									final AbsTemplateAdapter adapter = (AbsTemplateAdapter<?>) transportb.data;
									
									if (adapter instanceof ContinuousListAdapter) {
										position -= ((ContinuousListAdapter) adapter).hasTopLoadingRow() ? 1 : 0;
									}
									
									if (position >= adapter.getCount()) {
										return;
									}
									data = ((AbsTemplateAdapter) transportb.data).getItemTransportValue(position);
								} catch (ClassCastException e1) {
									try {
										data = ((ArrayList<?>) transportb.data).get(position);
									} catch (ClassCastException e2) {
										data = ((Object[]) transportb.data)[position];
									}
								}
								if (data == null) {
									
									// selected row is null or footer -> do nothing
									return;
								}
								
								/* if (transportb.fromFragment != null) {
									transportb.fromFragment.transport(transportb.to, transportb.data);
									transportb.fromFragment = null;
								} else { *
								if (activity.isRunning()) {
									((DSActivity) Global.getCurrentActivity()).transport(transportb.to, data);
								}
								
							} catch(Throwable e) {
								Debug.logException(e);
							}
						}

					});
					break;
*/
				case ONCLICK_LISTENER:
					view.setOnClickListener((OnClickListener) value);
					if (value == null) {
						view.setClickable(false);
					}
					break;

				case ONLONGCLICK_LISTENER:
					view.setOnLongClickListener((OnLongClickListener) value);
					break;
					
				case ONITEMCLICK_LISTENER:
					if (view instanceof AdapterView) {
						((AdapterView) view).setOnItemClickListener((OnItemClickListener) value);
					}
					break;
					
				case ONITEMSELECTED_LISTENER:
					if (view instanceof AdapterView) {
						((AdapterView) view).setOnItemSelectedListener((OnItemSelectedListener) value);
					}
					break;
					
				case ONFOCUSCHANGE_LISTENER:
					view.setOnFocusChangeListener((OnFocusChangeListener) value);
					break;
					
				case ONTOUCH_LISTENER:
					view.setOnTouchListener((OnTouchListener) value);
					break;
					
				case ONSCROLL_LISTENER:
					if (view instanceof AbsListView) {
						((AbsListView) view).setOnScrollListener((AbsListView.OnScrollListener) value);
					} else if (view instanceof BothScrollView) {
						((BothScrollView) view).setOnScrollListener((BothScrollView.OnScrollListener) value);
					}
					break;
					
				case ONCHECKEDCHANGE_LISTENER:
					((CompoundButton) view).setOnCheckedChangeListener((OnCheckedChangeListener) value);
					break;
					
				case ONCLICK_SEARCH:
					
					// TODO
					break;
					
				case SELECTED_STATE:
					view.setSelected(isTrue(value));
					break;
					
				case TAG:
					view.setTag(value);
					break;
					
				case FIX_TILE_MODE:
					if (isTrue(value)) {
						Common.fixTiledBackground(view);
					}
					break;
					
				case PADDING:
					final int padding = getIntegerValue(value);
					view.setPadding(padding, padding, padding, padding);
					break;
					
				case PADDING_LEFT:
					view.setPadding(getIntegerValue(value), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
					break;
					
				case PADDING_RIGHT:
					view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), getIntegerValue(value), view.getPaddingBottom());
					break;
					
				case PADDING_TOP:
					view.setPadding(view.getPaddingLeft(), getIntegerValue(value), view.getPaddingRight(), view.getPaddingBottom());
					break;
					
				case PADDING_BOTTOM:
					view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), getIntegerValue(value));
					break;
			}
		} catch(Throwable e) {
			Debug.logException(e);
		}
		
		return null;
	}
		
	@SuppressWarnings("unchecked")
	static private boolean isTrue(Object value) {
		if (value == null) {
			return false;
		}
		
		boolean positive = false;
		
		try {
			if (((Datatype<?>) value).isEmpty()) {
				return false;
			}
			try {
				positive = ((Datatype<Integer>) value).get() > 0;
			} catch(ClassCastException e1) {
				try {
					positive = ((Datatype<Boolean>) value).get();
				} catch(ClassCastException e2) {
					return true;
				}
			}
		} catch(ClassCastException e0) {
			try {
				positive = (Boolean) value;
			} catch(ClassCastException e1) {
				if (value instanceof String) {
					return ((String) value).length() > 0;
				} else {
					return ((Integer) value) > 0;
				}
			}
		}

		return positive;
	}
	
	@SuppressWarnings("unchecked")
	static private Integer getIntegerValue(Object value) {
		if (value == null) {
			return 0;
		}
		try {
			return ((Datatype<Integer>) value).get();
		} catch(ClassCastException e) {
			return (Integer) value;
		}
	}
		
	static private int getTypeByViewOrValue(View v, Object value) {
		if (value instanceof String || value instanceof WString) {
			return TEXT;
		} else if (value instanceof Integer || value instanceof WInteger) {
			if (v instanceof ImageView) {
				return IMAGE;
			} else {
				return TEXT;
			}
		/*} else if ((v instanceof AdapterView || v instanceof MiniListView) && value instanceof Transport) {
			return ONITEMCLICK_TRANSPORT;
		} */
		} else if (value instanceof LaizyImageViewInfo) {
			return IMAGE_LAIZY;
		} else if (value instanceof Transport) {
			return ONCLICK_TRANSPORT;
		} else if (value instanceof OnClickListener) {
			return ONCLICK_LISTENER;
		} else if (value instanceof OnItemClickListener) {
			return ONITEMCLICK_LISTENER;
		} else if (value instanceof OnLongClickListener) {
			return ONLONGCLICK_LISTENER;
		} else if (value instanceof Runnable) {
			return ONCLICK_RUNNABLE;
		} else if (v instanceof AdapterView || v instanceof MiniListView) {
			return LIST;
		}
		return TEXT;
	}

    /**
     *
     * @param view
     */
	public void setRoot(View view) {
		mRootView = view;
	}

    /**
     *
     */
    public void clearOtherRoots() {
        mOtherRoots = null;
    }

    /**
     *
     */
    public void addOtherRoot(View rootView) {
        if (mOtherRoots == null) {
            mOtherRoots = new ArrayList();
        }
        mOtherRoots.add(rootView);
    }
}
