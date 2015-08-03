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
package ds.framework.v4.app;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.IndoorLevel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import ds.framework.v4.Global;
import ds.framework.v4.R;
import ds.framework.v4.common.Debug;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.data.AbsAsyncData.OnDataLoadListener;
import ds.framework.v4.map.AbsPoiList;
import ds.framework.v4.map.Poi;
import ds.framework.v4.template.Template;

public abstract class DSMapFragment extends MapFragment 
		implements DSFragmentInterface, OnDataLoadListener, OnMarkerClickListener, OnMapClickListener {

	public final static int DATA_INVALID = 0;
	public final static int DATA_LOADING = 1;
	public final static int DATA_LOADED = 2;
	
	public final static int DATA_DISPLAYED = 4;
	
	/**
	 * the current data state<br/>
	 * <br/>
	 * DATA_INVALID - data is invalid and needs to be loaded
	 * DATA_LOADING - data is being loaded atm
	 * DATA_LOADED	- data is loaded
	 * DATA_DISPLAYED - data is loaded and displayed
	 */
	protected int mDataState = DATA_INVALID;
	
	protected Template mTemplate;

	/**
	 * used to identify this fragment (like when saving state)
	 */
	protected String mFragmentId;
	
	/**
	 * the root view in the fragment's view hierarchy
	 */
	protected View mRootView;
	
	/**
	 * flag to tell if the fragment is active when resumed<br/>
	 * set to true as default
	 */
	protected boolean mActiveDefault = true;
	
	protected boolean mActive = false;
	
	/**
	 * are action bar items created
	 */
	private boolean mActionBarItemsCreated;
	
	/**
	 *
	 */
	protected AbsPoiList mPoiList;
	private HashMap<String, Poi> mMarkers = new HashMap<String, Poi>();
	
	/**
	 * minimum size of bounds (in meters) when centering on pois
	 */
	private int mCenterOnPoiMinDistance = 1000;
	
	private int mCenterOnPoiPadding = 24;
	
	/**
	 * 
	 */
	protected View mMapView;
	
	protected ViewGroup mMapContainer;

    private boolean mFocusOnBuildingLevel = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			onRestoreInstanceState(savedInstanceState);
		}
	}
		
	/**
	 * 
	 * @param meter
	 */
	public void setCenterOnPoiMinDistance(int meter) {
		mCenterOnPoiMinDistance = meter;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getCenterOnPoiMinDistance() {
		return mCenterOnPoiMinDistance;
	}

    /**
     * @param focus
     */
    public void setFocusOnBuildingLevel(boolean focus) {
        mFocusOnBuildingLevel = focus;
    }

    /**
     *
     * @return
     */
    public boolean getFocusOnBuildingLevel() {
        return mFocusOnBuildingLevel;
    }
	
	/**
	 * 
	 * @return
	 */
	public DSActivity getDSActivity() {
		return (DSActivity) getActivity();
	}
	
	/**
	 * 
	 * @param rootView
	 */
	public void setRootView(View rootView) {
		mRootView = rootView;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = getRootView(inflater, container);
				
		if (mRootView == null) {
			
			// use map as root
			mMapView = mRootView = super.onCreateView(inflater, container, savedInstanceState);
		} else {
			addMapView(inflater, savedInstanceState);
		}
		
		if (savedInstanceState != null && mFragmentId == null) {
			onRestoreInstanceState(savedInstanceState);
		}

		onViewCreated(mRootView);
		
		if (canLoadData()) {
			loadData();
		}

		if (canDisplay()) {
			display();
		}

		return mRootView;
	}
	
	/**
	 * 
	 * @param inflater
	 * @param savedInstanceState
	 */
	protected void addMapView(LayoutInflater inflater, Bundle savedInstanceState) {
		mMapContainer = (ViewGroup) mRootView.findViewById(R.id.container_map);
		if (mMapContainer == null) {
			
			// use current root as parent for the map
			mMapView = super.onCreateView(inflater, (ViewGroup) mRootView, savedInstanceState);
			((ViewGroup) mRootView).addView(mMapView);
		} else {
			
			// use container in inflated layout as root
			mMapView = super.onCreateView(inflater, (ViewGroup) mMapContainer, savedInstanceState);
			mMapContainer.addView(mMapView);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (mFragmentId == null) {
			mFragmentId = getTag();
		}
		
		mActive = mActiveDefault;
		
		((DSActivity) activity).onFragmentAttached(this);
		
		attachSubFragmentsInner();
	}
	
	/**
	 * 
	 */
	public void onDetach() {
		mActive = false;
		
		super.onDetach();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		final DSActivity activity = getDSActivity();
		if (mActive && activity.getOptionsMenu() != null) {
			
			// only handle options menu if active
			createAndHandleOptionsMenu(activity);
		}
	
		loadDataAndDisplay();
	}
	
	/**
	 * called from onCreateView after root view is created and started data loading but before display
	 */
	protected void onViewCreated(View rootView) {
		mTemplate = createTemplate();
	}
	
	/**
	 * 
	 * @return
	 */
	protected Template createTemplate() {
		return new Template((ActivityInterface) getDSActivity(), mRootView);
	}
	
	/**
	 * 
	 * @return
	 */
	public View getRootView() {
		return mRootView;
	}

	/**
	 * override to create the fragment view tree root<br/>
	 * use the map view as root if null is returned (this is the default behavior)
	 * 
	 * @param inflater
	 * @param container
	 * @return
	 */
	protected View getRootView(LayoutInflater inflater, ViewGroup container) {
		return null;
	}
	
	/**
	 * set the fragment id prior attach
	 * 
	 * @param id
	 */
	public void setFragmentId(String id) {
		mFragmentId = id;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFragmentId() {
		return mFragmentId;
	}
	
	/**
	 * get rootView's or container activity's context
	 * 
	 * @return
	 */
	public Context getContext() {
		if (mRootView != null) {
			return mRootView.getContext();
		}
		return getDSActivity();
	}
	
	/**
	 * get data state
	 * 
	 * @return
	 */
	public int getDataState() {
		return mDataState;
	}
	
	/**
	 * load data and display it, but only if needed<br/>
	 * make sure it is only called after the activities SetContentView()
	 */
	public void loadDataAndDisplay() {
		if (getContext() == null) {
			
			// we may need context for this which we might not have at the moment
			return;
		}
		
		if (mRootView == null) {
			return;
		}

		if (canLoadData()) {
			loadData();
		}

		if (canDisplay()) {
			display();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean canLoadData() {
		return mDataState == DATA_INVALID;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean canDisplay() {
		return mDataState != DATA_DISPLAYED;
	}
	
	/**
	 *
	 */
	protected void loadData() {
		if (mPoiList == null) {
			mPoiList = getPoiList();
		}
		if (mPoiList == null || mPoiList.isValid() || !mPoiList.loadIfNeeded(this, -1)) {
			onDataLoaded();
		}
	}


	/**
	 * invalidate data so it is needed to be loaded again 
	 */
	public void invalidateData() {
		mDataState = DATA_INVALID;
		
		if (mPoiList != null) {
			mPoiList.invalidate();
		}
	}
	
	/**
	 * invalidate and reload data
	 */
	public void reloadData() {
		invalidateData();
		
		if (getContext() == null || mRootView == null) {
			
			// we may need context for this which we might not have at the moment
			return;
		}
		
		if (canLoadData()) {
			loadData();
		}
	}
	
	/**
	 * invalidate, reload and display data
	 */
	public void reloadDataAndDisplay() {
		reloadData();

		if (mRootView != null) {
			if (canDisplay()) {
				display();
			}
		}
	}
	
	/**
	 * invalidate display of data so it needs to be displayed again
	 */
	public void invalidateDisplay() {
		if (mDataState == DATA_DISPLAYED) {
			mDataState = DATA_LOADED;
		}
	}
	
	/**
	 * refresh display
	 */
	public void refresh() {
		invalidateDisplay();

		if (mRootView != null) {
			if (canDisplay()) {
				display();
			}
		}
	}
	
	/**
	 * call this when data is loaded in the background
	 */
	protected void onDataLoaded() {
		mDataState = DATA_LOADED;
		
		invalidateDisplay();
		if (mRootView != null) {
			if (canDisplay()) {
				display();
			}
		}
	}
	
	/**
	 * override to display data
	 */
	public void display() {
		if (mDataState == DATA_LOADED && mMapView != null) {
			final GoogleMap map = getMap();

			if (map == null) {
				return;
			}
			
			if (mPoiList != null && mPoiList.isValid()) {
				
				map.clear();
				mMarkers.clear();
				
				addPoisToMap(map);
				centerOnPois(map);
				
				map.setOnMarkerClickListener(this);
			}
			
			map.setOnMapClickListener(this);
			
			mDataState = DATA_DISPLAYED;
		}
	}
	
	/**
	 * 
	 */
	protected void addPoisToMap(GoogleMap map) {				
		for(Poi poi : mPoiList.getList()) {
			addMarkerToMap(map, poi);
		}
	}
	
	/**
	 * 
	 * @param marker
	 * @return
	 */
	public Poi getPoiForMarker(Marker marker) {
		return mMarkers.get(marker.getId());
	}
	
	/**
	 * 
	 * @param poi
	 */
	protected void addMarkerToMap(GoogleMap map, Poi poi) {
		poi.ensureMarkerBitmap(getContext());
		
		Marker marker = map.addMarker(getPoiMarkerOptions(poi));
		mMarkers.put(marker.getId(), poi);
	}
	
	protected MarkerOptions getPoiMarkerOptions(Poi poi) {
		return new MarkerOptions()
			.position(poi.mLatLng)
			.icon(BitmapDescriptorFactory.fromBitmap(poi.markerBmp))
			.anchor(poi.markerAnchorX, poi.markerAnchorY);
	}
	
	/**
	 * 
	 */
	protected void centerOnPois(GoogleMap map) {
        if (map == null) {
            return;
        }

		if (mPoiList.size() > 0) {
			final LatLngBounds.Builder builder = new LatLngBounds.Builder();
			
			for(Poi poi : mPoiList.getList()) {
				builder.include(poi.mLatLng);
			}
			
			LatLngBounds bounds = builder.build();
			
			if (mCenterOnPoiMinDistance != 0) {
				LatLng center = bounds.getCenter();
				LatLng northEast = moveLatLng(center, mCenterOnPoiMinDistance, mCenterOnPoiMinDistance);
			    LatLng southWest = moveLatLng(center, -mCenterOnPoiMinDistance, -mCenterOnPoiMinDistance);
			    builder.include(southWest);
			    builder.include(northEast);

			    bounds = builder.build();
			}

			if (mFocusOnBuildingLevel && mPoiList.getList().size() == 1) {

				// may need to move to a building level
                final Poi poi = mPoiList.getList().get(0);
                final String buildingLevel = poi.mBuildingLevel;
                if (buildingLevel != null) {
                    map.setOnIndoorStateChangeListener(new GoogleMap.OnIndoorStateChangeListener() {

                        private boolean changedLevel;

                        @Override
                        public void onIndoorBuildingFocused() {
                            final GoogleMap map = getMap();
                            if (map == null) {
                                return;
                            }
                            if (!changedLevel) {
                                try {
                                    for (IndoorLevel iLevel : map.getFocusedBuilding().getLevels()) {

                                        if (buildingLevel.equals(iLevel.getShortName())) {
                                            iLevel.activate();
                                            break;
                                        }
                                    }
                                    changedLevel = true;
                                } catch(Throwable e) {
                                    Debug.logException(e);
                                }
                            }
                        }

                        @Override
                        public void onIndoorLevelActivated(IndoorBuilding indoorBuilding) {

                        }
                    });
                }
			}
			
			centerMap(map, bounds);
		}
	}
	
	private void centerMap(GoogleMap map, final LatLngBounds bounds) {
		try {
			CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, (int) (Global.getDipMultiplier() * mCenterOnPoiPadding));
			map.moveCamera(cu);
		} catch(IllegalStateException e) {
			mRootView.post(new Runnable() {

				@Override
				public void run() {
					centerMap(getMap(), bounds);
				}
			});
		} catch(Throwable e) {
			;
		}
	}
	
	/**
	 * get data state to save in onSaveInstanceState
	 * 
	 * @return
	 */
	protected int getDataStateToSave() {
		return DATA_INVALID;
	}

	/**
	 * restart all data
	 */
	public void reset() {
		invalidateData();
	}
	
	/**
	 * use this if any activation required (like when using a view pager)
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		mActiveDefault = active;
		
		if (isAdded()) {
			mActive = mActiveDefault;
		}
		
		final DSActivity activity = getDSActivity();
		
		if (activity == null) {
			return;
		}

		if (activity.getOptionsMenu() != null) {
			createAndHandleOptionsMenu(activity);
		}
	}
	
	/**
	 * use this if any activation required (like when using a view pager)
	 * 
	 * @return
	 */
	public boolean isActive() {
		return mActive;
	}	
	
	/**
	 * 
	 * @param activity
	 */
	void createAndHandleOptionsMenu(DSActivity activity) {
		if (!mActionBarItemsCreated) {
			mActionBarItemsCreated = true;
			createActionBarItems(activity.getOptionsMenu());
		}
		
		handleActionBarItems(mActive);
	}
	
	/**
	 * 
	 * @param menu
	 */
	protected void createActionBarItems(Menu menu) {
		
	}
	
	/**
	 * 
	 * @param active
	 */
	protected void handleActionBarItems(boolean active) {
		
	}
	
	/**
	 * save fragment state<br/>
	 * all ids put in the outState should be unique in the application
	 * 
	 * @param outState
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("fragment-id", mFragmentId);
		outState.putInt(mFragmentId + "-data-state", getDataStateToSave());
	}
	
	/**
	 * restore saved instance state
	 * 
	 * @param savedInstanceState
	 */
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mFragmentId = savedInstanceState.getString("fragment-id");
		mDataState = savedInstanceState.getInt(mFragmentId + "-data-state");
	}

	@Override
	public void loadData(boolean subfragmentsToo) {
		if (canLoadData()) {
			loadData();
		}
	}
	
	@Override
	public void display(boolean subfragmentsToo) {
		if (canDisplay()) {
			display();
		}
	}

	@Override
	public void invalidateData(boolean subfragmentsToo) {
		invalidateData();
	}

	@Override
	public void attachSubFragmentsInner() {
		;
	}

	@Override
	public void onTransport(Object data) {
		;
	}

	@Override
	public void onActivityResult(Object data) {
		;
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onDataLoadStart(AbsAsyncData data, int loadId) {
		mDataState = DATA_LOADING;
	}

	@Override
	public void onDataLoaded(AbsAsyncData data, int loadId) {
		onDataLoaded();
	}

	@Override
	public void onDataLoadFailed(AbsAsyncData data, int loadId) {
		;
	}

	@Override
	public void onDataLoadInterrupted(AbsAsyncData data, int loadId) {
		;
	}
	
	@Override
	public boolean onMenuItemSelected(int itemId) {
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	abstract protected AbsPoiList getPoiList();
	
// implementing OnMapClickListener and OnMarkerClickListener
	
	@Override
	public void onMapClick(LatLng latLng) {
		
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		return false;
	}
	
// -----------------------------------------------
// code below is from http://stackoverflow.com/questions/20571951/set-a-max-zoom-level-on-latlngbounds-builder
// by user2808624
	
	private static final double EARTHRADIUS = 6366198;
	
	/**
	 * Create a new LatLng which lies toNorth meters north and toEast meters
	 * east of startLL
	 */
	private LatLng moveLatLng(LatLng startLL, double toNorth, double toEast) {
	    double lonDiff = meterToLongitude(toEast, startLL.latitude);
	    double latDiff = meterToLatitude(toNorth);
	    return new LatLng(startLL.latitude + latDiff, startLL.longitude
	            + lonDiff);
	}

	private double meterToLongitude(double meterToEast, double latitude) {
	    double latArc = Math.toRadians(latitude);
	    double radius = Math.cos(latArc) * EARTHRADIUS;
	    double rad = meterToEast / radius;
	    return Math.toDegrees(rad);
	}


	private double meterToLatitude(double meterToNorth) {
	    double rad = meterToNorth / EARTHRADIUS;
	    return Math.toDegrees(rad);
	}
}
