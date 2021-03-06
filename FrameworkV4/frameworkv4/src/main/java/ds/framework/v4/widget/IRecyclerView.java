package ds.framework.v4.widget;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by solyom on 11/08/15.
 */
public class IRecyclerView extends RecyclerView {

    protected View mHeaderView;
    protected View mFooterView;
    private Parcelable mStateSave;

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
        if (adapter == getAdapter()) {
            return;
        }

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

    /**
     *
     */
    public void saveState() {
        mStateSave = getLayoutManager().onSaveInstanceState();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mStateSave != null) {
            final Parcelable stateSave = mStateSave;
            mStateSave = null;
            getLayoutManager().onRestoreInstanceState(stateSave);
        }
        super.onLayout(changed, l ,t, r, b);
    }

}
