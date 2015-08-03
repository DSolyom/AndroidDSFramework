package ds.framework.v4.app;

import android.content.Context;
import android.view.View;

public interface DSFragmentInterface {
	
	String getFragmentId();
	void setFragmentId(String name);
	
	Context getContext();
	
	void setActive(boolean active);
	boolean isActive();
	
	View getRootView();
	int getDataState();
	void loadData(boolean subfragmentsToo);
	void display(boolean subfragmentsToo);
	void invalidateData(boolean subfragmentsToo);
	void invalidateDisplay();
	void reset();
	
	void attachSubFragmentsInner();
	
	void onTransport(Object data);
	void onActivityResult(Object data);
	
	boolean onBackPressed();

	boolean onMenuItemSelected(int itemId);

}
