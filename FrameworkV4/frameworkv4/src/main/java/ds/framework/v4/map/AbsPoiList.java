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
package ds.framework.v4.map;

import java.util.ArrayList;

import ds.framework.v4.data.AbsAsyncData;

abstract public class AbsPoiList extends AbsAsyncData {

	protected ArrayList<Poi> mList = new ArrayList<Poi>();
	
	public AbsPoiList() {
		super();
	}
	
	public AbsPoiList(String loaderTag) {
		super(loaderTag);
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Poi> getList() {
		return mList;
	}
	
	/**
	 * load pois and add them to mPois<br/>
	 * !note: do not forget to clear mPois if needed
	 */
	abstract protected void loadPoisInto(ArrayList<Poi> list, Thread in);
	
	@Override
	protected LoaderThread createLoader() {
		return new PoiListLoader();
	}

	public int size() {
		return mList.size();
	};
	
	public void invalidate(boolean forced) {
		super.invalidate(forced);
		
		mList.clear();
	}
	
	/**
	 * PoiListLoader
	 */
	protected class PoiListLoader extends LoaderThread {
		
		ArrayList<Poi> mPois = new ArrayList<Poi>();

		@Override
		protected boolean runCycle(Thread in) {
			try {
				loadPoisInto(mPois, in);
			} catch(Throwable e) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void onFinished() {
			mList = mPois;
			super.onFinished();
		}
	}
}