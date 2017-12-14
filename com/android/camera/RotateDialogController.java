package com.android.camera;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.recyclerview.C0049R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotateLayout;

public class RotateDialogController implements Rotatable {
    private Activity mActivity;
    private View mDialogRootLayout;
    private Animation mFadeInAnim;
    private Animation mFadeOutAnim;
    private int mLayoutResourceID;
    private RotateLayout mRotateDialog;
    private TextView mRotateDialogButton1;
    private TextView mRotateDialogButton2;
    private View mRotateDialogButtonLayout;
    private ProgressBar mRotateDialogSpinner;
    private TextView mRotateDialogText;
    private TextView mRotateDialogTitle;
    private View mRotateDialogTitleLayout;

    private void inflateDialogLayout() {
        if (this.mDialogRootLayout == null) {
            View inflate = this.mActivity.getLayoutInflater().inflate(this.mLayoutResourceID, (ViewGroup) this.mActivity.getWindow().getDecorView());
            this.mDialogRootLayout = inflate.findViewById(C0049R.id.rotate_dialog_root_layout);
            this.mRotateDialog = (RotateLayout) inflate.findViewById(C0049R.id.rotate_dialog_layout);
            this.mRotateDialogTitleLayout = inflate.findViewById(C0049R.id.rotate_dialog_title_layout);
            this.mRotateDialogButtonLayout = inflate.findViewById(C0049R.id.rotate_dialog_button_layout);
            this.mRotateDialogTitle = (TextView) inflate.findViewById(C0049R.id.rotate_dialog_title);
            this.mRotateDialogSpinner = (ProgressBar) inflate.findViewById(C0049R.id.rotate_dialog_spinner);
            this.mRotateDialogText = (TextView) inflate.findViewById(C0049R.id.rotate_dialog_text);
            this.mRotateDialogButton1 = (Button) inflate.findViewById(C0049R.id.rotate_dialog_button1);
            this.mRotateDialogButton2 = (Button) inflate.findViewById(C0049R.id.rotate_dialog_button2);
            this.mFadeInAnim = AnimationUtils.loadAnimation(this.mActivity, 17432576);
            this.mFadeOutAnim = AnimationUtils.loadAnimation(this.mActivity, 17432577);
            this.mFadeInAnim.setDuration(150);
            this.mFadeOutAnim.setDuration(150);
        }
    }

    public static void showSystemAlertDialog(Context context, String str, String str2, String str3, final Runnable runnable, String str4, final Runnable runnable2) {
        Builder builder = new Builder(context);
        builder.setTitle(str);
        builder.setMessage(str2);
        builder.setCancelable(false);
        if (str3 != null) {
            builder.setPositiveButton(str3, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
        if (str4 != null) {
            builder.setNegativeButton(str4, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (runnable2 != null) {
                        runnable2.run();
                    }
                }
            });
        }
        builder.create().show();
    }

    public static void showSystemChoiceDialog(Context context, String str, String str2, String str3, String str4, final Runnable runnable, final Runnable runnable2) {
        Builder builder = new Builder(context);
        View inflate = LayoutInflater.from(context).inflate(C0049R.layout.v6_choice_alertdialog, null);
        ((TextView) inflate.findViewById(C0049R.id.alert_declaration)).setText(str2);
        final CheckBox checkBox = (CheckBox) inflate.findViewById(C0049R.id.alert_declaration_checkbox);
        checkBox.setText(str3);
        builder.setTitle(str);
        builder.setCancelable(false);
        builder.setView(inflate);
        if (str4 != null) {
            builder.setPositiveButton(str4, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (checkBox.isChecked()) {
                        if (runnable != null) {
                            runnable.run();
                        }
                    } else if (runnable2 != null) {
                        runnable2.run();
                    }
                }
            });
        }
        builder.create().show();
    }

    public void setOrientation(int i, boolean z) {
        inflateDialogLayout();
        this.mRotateDialog.setOrientation(i, z);
    }
}
