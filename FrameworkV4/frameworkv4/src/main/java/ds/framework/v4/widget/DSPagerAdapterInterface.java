package ds.framework.v4.widget;

import com.google.samples.apps.iosched.ui.widget.AbsSlidingTabLayout;

/**
 * Created by solyom on 28/01/16.
 */
public interface DSPagerAdapterInterface extends AbsSlidingTabLayout.SlidingTabAdapterInterface {

    Object getItem(int position);
}
