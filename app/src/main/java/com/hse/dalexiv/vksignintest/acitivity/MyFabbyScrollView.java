package com.hse.dalexiv.vksignintest.acitivity;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by dalex on 11/23/2015.
 */
public class MyFabbyScrollView extends ScrollView {
    FloatingActionButton fab;
    public MyFabbyScrollView(Context context) {
        super(context);
    }

    public MyFabbyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFabbyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFab(FloatingActionButton fab) {
        this.fab = fab;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged( l, t, oldl, oldt );
        if (t - oldt > 0 && fab.isShown())
            fab.hide();
        else if (t - oldt < 0 && !fab.isShown())
            fab.show();
    }
}
