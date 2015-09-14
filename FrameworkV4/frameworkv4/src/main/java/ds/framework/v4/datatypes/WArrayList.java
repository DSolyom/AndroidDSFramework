package ds.framework.v4.datatypes;

import java.util.ArrayList;
import java.util.Arrays;

public class WArrayList<T> extends Datatype<ArrayList<T>> {
	
	private static final long serialVersionUID = -3754923965692993943L;

	public WArrayList() {
		mType = ARRAY;
		mValue = new ArrayList<T>();
	}
	
	public WArrayList(ArrayList<T> value) {
		mType = ARRAY;
		mValue = new ArrayList<T>();
		mValue.addAll(value);
	}
	
	public WArrayList(T[] value) {
		mType = ARRAY;
		mValue = new ArrayList<T>();
		mValue.addAll(Arrays.asList(value));
	}
	
	@Override
	public void set(ArrayList<T> value) {
		mValue.clear();
		mValue.addAll(value);
	}
	
	public void set(T[] value) {
		mValue.clear();
		mValue.addAll(Arrays.asList(value));
	}
	
	@Override
	public void reset() {
		mValue.clear();
	}
}
