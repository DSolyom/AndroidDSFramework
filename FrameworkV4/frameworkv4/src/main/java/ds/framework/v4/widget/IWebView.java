package ds.framework.v4.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import ds.framework.v4.R;
import ds.framework.v4.common.AnimationStarter;
import ds.framework.v4.common.Common;
import ds.framework.v4.common.Debug;
import ds.framework.v4.template.Template;

/**
 * WebView with some stuff like loading animation - must have a container_loading in the layout
 */
public class IWebView extends WebView {
	
	private View mParentRootView;

	public IWebView(Context context) {
		super(context);
	}
	
	public IWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setParentRootView(View parentRootView) {
		mParentRootView = parentRootView;
		setWebViewClient(new IWebViewClient());
	}
	
	@Override
	public void loadUrl(String url) {
		if (Common.isDocument(url)) {
	    	url = "http://docs.google.com/gview?embedded=true&url=" + url;
		}
		
		setVisibility(View.GONE);
		
		super.loadUrl(url);
	}
	
	/**
	 * call in activity onBackPressed()<br/>
	 * returns true if press is handled by the web view
	 * 
	 * @return
	 */
	public boolean onBackPressed() {
    	try {
    		if (copyBackForwardList().getCurrentIndex() > 0) {
    			goBack();
    			return true;
    		}
    	} catch(Throwable e) {
    		Debug.logException(e);
    	}
        	
		return false;
	}

	/**
     * @class MWebViewClient
     */
    private class IWebViewClient extends WebViewClient {

    	private boolean mInDocViewer = false;
    	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		super.onPageFinished(view, url);
    		
    		if (url.contains("docs.google.com")) {
    			mInDocViewer = true;
    		}

    		try {	
				mParentRootView.findViewById(R.id.container_loading).setVisibility(Template.GONE);	
    		} catch(Throwable e) {
    			;
    		}

    		view.setVisibility(View.VISIBLE);
    	}
	    
	    @Override
	    public void onPageStarted(WebView view, String url, Bitmap favicon) {
			try {
				mParentRootView.findViewById(R.id.container_loading).setVisibility(Template.VISIBLE);
			} catch(Throwable e) {
				;
			}

	    	super.onPageStarted(view, url, favicon);
	    }
	    
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if (Common.isDocument(url)) {
	    		if (!mInDocViewer) {
	    			
	    			// first open in-app with google doc viewer
	    			view.loadUrl("http://docs.google.com/gview?embedded=true&url=" + url);
    			} else {
    				
	    			// download with browser
    				Common.openInBrowser(getContext(), url);
    			}
	    		return true;
	    	}
	    	return false;
	    }
    }
}
