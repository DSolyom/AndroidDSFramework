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

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class AnimationStarter {
	
	public static void start(final ImageView view) {
		final AnimationDrawable aDrawable = (AnimationDrawable) view.getDrawable();
		
		if (aDrawable != null) {
			
			// stupid stupid way - but no animation otherwise
			final Handler handler = new Handler();
			
			new Thread() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (view.getVisibility() == View.VISIBLE) {
								aDrawable.start();
							}
						}
					});
				}
			}.start();
		}
	}
	
	public static void start(final AnimationDrawable animation) {
		
		// stupid stupid way - but no animation otherwise
		final Handler handler = new Handler();
		
		new Thread() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						animation.start();
					}
				});
			}
		}.start();
	}
}
