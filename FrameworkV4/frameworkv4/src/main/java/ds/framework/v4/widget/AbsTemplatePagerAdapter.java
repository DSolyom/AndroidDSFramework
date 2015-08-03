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
import ds.framework.v4.app.ActivityInterface;
import ds.framework.v4.template.Template;

public abstract class AbsTemplatePagerAdapter extends PagerAdapter {
		
	protected Template mTemplate;
	
	private int mPageLayoutResID;

	private ActivityInterface mIn;
	
	public AbsTemplatePagerAdapter(ActivityInterface in, int pageLayoutResID) {
		super();
		
		mIn = in;
		mPageLayoutResID = pageLayoutResID;
		mTemplate = new Template(in, null);
	}
	
	@Override
    public Object instantiateItem(ViewGroup container, int position) {
		View item = createItem(container, position);
		
		((ViewGroup) container).addView(item);	
		setItemLayoutParams(item, container);
		mTemplate.setRoot(item);
		fillItem(item, position);
		
		return item;
	}
	
	/**
	 * 
	 * @param viewParent
	 * @param position
	 * @return
	 */
	public View createItem(ViewGroup viewParent, int position) {
		final View convertView = mIn.inflate(mPageLayoutResID, viewParent, false);
		convertView.setTag(mPageLayoutResID);
		
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
     * @return
     */
    public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == (View) object;
	}
	
	abstract public void fillItem(View item, int position);
	
}
