package ds.framework.v4.widget;

public interface DSAdapterInterface {

	void notifyDataSetChanged();
	void notifyDataSetInvalidated();
	boolean isEmpty();
	int getCount();
	Object getItem(int position);
}
