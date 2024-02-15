package code.common;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import code.activity.MapActivity;


public class TouchableWrapper extends FrameLayout {

    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                  MapActivity.isMapTouched = true;
                  break;

            case MotionEvent.ACTION_UP:
                MapActivity.isMapTouched = false;
                  break;
        }
        return super.dispatchTouchEvent(event);
    }
}