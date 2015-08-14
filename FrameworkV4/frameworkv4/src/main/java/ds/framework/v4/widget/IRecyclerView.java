package ds.framework.v4.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by solyom on 11/08/15.
 */
public class IRecyclerView extends RecyclerView {

    protected View mHeaderView;
    protected View mFooterView;

    public IRecyclerView(Context context) {
        super(context);
    }

    public IRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter instanceof RecyclerViewHeaderedAdapter) {
            ((RecyclerViewHeaderedAdapter) adapter).setHeaderView(mHeaderView);
            ((RecyclerViewHeaderedAdapter) adapter).setFooterView(mFooterView);
        }

        super.setAdapter(adapter);
    }

    /**
     *
     * @param headerView
     */
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        final RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null && adapter instanceof RecyclerViewHeaderedAdapter) {
            ((RecyclerViewHeaderedAdapter) adapter).setHeaderView(mHeaderView);
        }
    }

    /**
     *
     * @return
     */
    public View getHeaderView() {
        return mHeaderView;
    }

    /**
     *
     * @param footerView
     */
    public void setFooterView(View footerView) {
        mFooterView = footerView;
        final RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null && adapter instanceof RecyclerViewHeaderedAdapter) {
            ((RecyclerViewHeaderedAdapter) adapter).setFooterView(mFooterView);
        }
    }

    /**
     *
     * @return
     */
    public View getFooterView() {
        return mFooterView;
    }
}
