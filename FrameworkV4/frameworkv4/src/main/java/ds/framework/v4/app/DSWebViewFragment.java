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

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import ds.framework.v4.R;
import ds.framework.v4.webkit.DSWebViewClient;

abstract public class DSWebViewFragment extends DSFragment {

	private WebView mWebView;
	private ImageView mLoadingAnim;
	private String mUrl;
	
	public DSWebViewFragment() {
		;
	}
	
	public DSWebViewFragment(String url) {
		mUrl = url;
	}
	
	@Override
	public void display() {
		if (mRootView == null) {
			return;
		}
		
		mWebView = (WebView) mRootView.findViewById(R.id.x_webview);
		mLoadingAnim = (ImageView) mRootView.findViewById(R.id.container_loading);
		mWebView.setWebViewClient(getWebViewClient());
		
		super.display();
		
		if (mUrl != null) {
			mWebView.loadUrl(mUrl);
		}
	}
	
	/**
	 * override to return different web view client
	 */
	public WebViewClient getWebViewClient() {
		return new DSWebViewClient(mWebView, mLoadingAnim);
	}
}
