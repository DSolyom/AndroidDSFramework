package ds.framework.v4.webkit;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class DSWebViewClient extends WebViewClient {
	
	final private WebView mWebView;
	final private ImageView mLoadingAnim;
	
	public DSWebViewClient(WebView v, ImageView loadingAnim) {
		mWebView = v;
		mLoadingAnim = loadingAnim;
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		startLoading();
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		stopLoading();
	}
	
	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		stopLoading();
	}
	
	public void startLoading() {
		mWebView.setVisibility(View.GONE);
		mLoadingAnim.setVisibility(View.VISIBLE);
	}
	
	public void stopLoading() {
		mWebView.setVisibility(View.VISIBLE);
		mLoadingAnim.setVisibility(View.GONE);
	}
}