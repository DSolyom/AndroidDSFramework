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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.LatLng;

public class Poi {

	public int mId;
	public int mIcon;
	public String mTitle;
	public String mDescription;
	public LatLng mLatLng;
    public Drawable marker;
    public Bitmap markerBmp;
    public float markerAnchorX = 0.5f;
    public float markerAnchorY = 1f;
	public String mCoordinates;
	public String mBuildingLevel;

    public Poi(int id, String coordinates, int icon, String title) {
    	mId = id;
    	mIcon = icon;
    	mTitle = title;
    	mDescription = null;
    	setLatLngFromCoordinates(coordinates);
    }
    
    public Poi(int id, LatLng latLng, int icon, String title) {
    	mId = id;
    	mIcon = icon;
    	mTitle = title;
    	mDescription = null;
    	mLatLng = latLng;
    }

    public Poi(int id, String coordinates, int icon, String title, String buildingLevel) {
        mId = id;
        mIcon = icon;
        mTitle = title;
        mDescription = null;
        mCoordinates = coordinates;
        mBuildingLevel = buildingLevel;
        setLatLngFromCoordinates(coordinates);
    }
   
	/**
	 * set LatLng from coordinate string (of format: lat:lng)
	 * 
	 * @param coordinates
	 */
	public void setLatLngFromCoordinates(String coordinates) {
		try {
			String[] aCoords = coordinates.split(":");
			mLatLng = new LatLng(Double.parseDouble(aCoords[0]), 
					Double.parseDouble(aCoords[1]) 
			);
		} catch(Exception e) {
			
			// fake or none
			mLatLng = new LatLng(0, 0);
		}
		return;
	}

	/**
	 * 
	 */
	public void ensureMarker(Context context) {
		if (marker == null) {
			marker = context.getResources().getDrawable(mIcon);
			int w = marker.getMinimumWidth();
			int h = marker.getMinimumHeight();
			marker.setBounds((int) - ((float) w * markerAnchorX), (int) - ((float) h * markerAnchorY), (int) ((float) w * markerAnchorX), 0);
		}
	}

	/**
	 * 
	 * @param context
	 */
	public void ensureMarkerBitmap(Context context) {
		if (markerBmp == null) {
			markerBmp = BitmapFactory.decodeResource(context.getResources(), mIcon);
		}
	}
}