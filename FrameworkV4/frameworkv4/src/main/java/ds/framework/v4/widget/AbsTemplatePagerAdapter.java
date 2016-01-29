/*
	Copyright 2015 Dániel Sólyom

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

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPagerModByDS;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.template.Template;

public abstract class AbsTemplatePagerAdapter extends PagerAdapter implements AbsAsyncData.OnDataLoadListener, DSAdapterInterface {
		
	protected Template mTemplate;

    /**
     * layout resource ids for pages
     */
    private HashMap<Integer, Integer> mPageLayoutResIDs = new HashMap<>();

	protected ActivityInterface mIn;

    protected AbsAsyncData mPagerData;

    public AbsTemplatePagerAdapter(ActivityInterface in, int pageLayoutResID) {
		super();
		
		mIn = in;
        mPageLayoutResIDs.put(-1, pageLayoutResID); // -1 = for all page without set layout
		mTemplate = new Template(in, null);
	}

    public ActivityInterface getIn() {
        return mIn;
    }
	
	@Override
    public Object instantiateItem(ViewGroup container, int position) {
		View item = inflatePage(container, position);
		
		((ViewGroup) container).addView(item);	
		setItemLayoutParams(item, container);
		mTemplate.setRoot(item);
		fillItem(item, position);
		
		return item;
	}

    /**
     *
     * @param position
     * @param resID
     */
    public void setPageLayoutResID(int position, int resID) {
        mPageLayoutResIDs.put(position, resID);
    }
	
	/**
	 * 
	 * @param viewParent
	 * @param position
	 * @return
	 */
	public View inflatePage(ViewGroup viewParent, int position) {
        final int pageLayoutResID;
        if (mPageLayoutResIDs.containsKey(position)) {
            pageLayoutResID = mPageLayoutResIDs.get(position);
        } else {
            pageLayoutResID = mPageLayoutResIDs.get(-1);
        }
		final View convertView = mIn.inflate(pageLayoutResID, viewParent, false);
		convertView.setTag(pageLayoutResID);
		
		return convertView;
	}

    /**
     *
     * @param item
     * @param container
     */
	protected void setItemLayoutParams(View item, View container) {
		ViewGroup.LayoutParams lp = item.getLayoutParams();
    	lp.width = container.getMeasuredWidth();
    	lp.height = container.getMeasuredHeight();
	}
	
    @Override
    synchronized public void destroyItem(View container, int position, Object object) {
        ((ViewPagerModByDS) container).removeView((View) object);
    }

    /**
     *
     * @param data
     * @return
     */
    public boolean hasData(AbsAsyncData data) {
        return data == mPagerData;
    };

    /**
     *
     * @return
     */
    public AbsAsyncData getPagerData() {
        return mPagerData;
    }

    /**
     *
     * @param data
     */
    public void setPagerData(AbsAsyncData data) {
        mPagerData = data;
    }
    
    /**
     * 
     * @return
     */
    public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == (View) object;
	}

    /**
     *
     */
    public void invalidate() {
        mPagerData.invalidate();

        notifyDataSetChanged();
    }

// implements AbsAsyncData.OnDataLoadListener

    public void onDataLoadStart(AbsAsyncData data, int loadId) {

    }

    public void onDataLoadFailed(AbsAsyncData data, int loadId) {

    }

    public void onDataLoadInterrupted(AbsAsyncData data, int loadId) {

    }
	
	abstract public void fillItem(View item, int position);
}
