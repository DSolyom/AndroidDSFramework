package ds.framework.v4.widget;

public interface DSAdapterInterface {

	void notifyDataSetChanged();
	boolean isEmpty();
	int getCount();
	Object getItem(int position);
}
