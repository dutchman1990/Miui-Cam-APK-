package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import java.util.ArrayList;

public class V6RelativeLayout extends RelativeLayout implements Rotatable, V6FunctionUI {
    protected ArrayList<View> mChildren = new ArrayList();

    public V6RelativeLayout(Context context) {
        super(context);
    }

    public V6RelativeLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void enableControls(boolean z) {
        for (View view : this.mChildren) {
            if (view instanceof V6FunctionUI) {
                ((V6FunctionUI) view).enableControls(z);
            }
        }
    }

    public View findChildrenById(int i) {
        View findViewById = super.findViewById(i);
        if (findViewById != null) {
            this.mChildren.add(findViewById);
        }
        return findViewById;
    }

    public void onCameraOpen() {
        for (View view : this.mChildren) {
            if (view instanceof V6FunctionUI) {
                ((V6FunctionUI) view).onCameraOpen();
            }
        }
    }

    public void onCreate() {
        for (View view : this.mChildren) {
            if (view instanceof V6FunctionUI) {
                ((V6FunctionUI) view).onCreate();
            }
        }
    }

    public void onPause() {
        for (View view : this.mChildren) {
            if (view instanceof V6FunctionUI) {
                ((V6FunctionUI) view).onPause();
            }
        }
    }

    public void onResume() {
        for (View view : this.mChildren) {
            if (view instanceof V6FunctionUI) {
                ((V6FunctionUI) view).onResume();
            }
        }
    }

    public void setMessageDispacher(MessageDispacher messageDispacher) {
        for (View view : this.mChildren) {
            if (view instanceof V6FunctionUI) {
                ((V6FunctionUI) view).setMessageDispacher(messageDispacher);
            }
        }
    }

    public void setOrientation(int i, boolean z) {
        for (View view : this.mChildren) {
            if (view instanceof Rotatable) {
                ((Rotatable) view).setOrientation(i, z);
            }
        }
    }
}
