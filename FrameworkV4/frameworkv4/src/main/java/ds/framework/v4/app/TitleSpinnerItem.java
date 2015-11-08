package ds.framework.v4.app;

@Deprecated
public class TitleSpinnerItem {
	int mId;
	String mTitle;
	int mOrder;
	
	public TitleSpinnerItem(int id, String title, int order) {
		mId = id;
		mTitle = title;
		mOrder = order;
	}
	
	public int getItemId() {
		return mId;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public int getOrder() {
		return mOrder;
	}
}
