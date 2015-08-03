package ds.framework.v4.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import ds.framework.v4.app.ActivityInterface;

public abstract class FixedRatioTemplateAdapter<T> extends TemplateListAdapter<T> {
	
	private int mItemWidth;
	
	/**
	 * ratio of width / height
	 */
	private float sizeRatio = 1;
	
	public FixedRatioTemplateAdapter(ActivityInterface in, int itemWidth, int rowLayoutId) {
		super(in, rowLayoutId);

		mItemWidth = itemWidth;
	}
		
	@Override
	protected View inflateConvertView(int rowRes, ViewGroup viewParent) {
		final View view = super.inflateConvertView(rowRes, viewParent);
		final LayoutParams lp = view.getLayoutParams();
		lp.width = mItemWidth;
		lp.height = (int) ((float) mItemWidth / sizeRatio);

		return view;
	}
}
