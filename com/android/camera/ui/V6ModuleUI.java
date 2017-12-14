package com.android.camera.ui;

import android.view.View;
import com.android.camera.Camera;
import java.util.ArrayList;

public class V6ModuleUI implements Rotatable, V6FunctionUI {
    protected Camera mActivity;
    protected ArrayList<View> mChildren = new ArrayList();

    public V6ModuleUI(Camera camera) {
        this.mActivity = camera;
    }

    public void enableControls(boolean z) {
        for (View view : this.mChildren) {
            if (view instanceof V6FunctionUI) {
                ((V6FunctionUI) view).enableControls(z);
            }
        }
    }

    public View findViewById(int i) {
        View findViewById = this.mActivity.findViewById(i);
        if (!this.mChildren.contains(findViewById)) {
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
