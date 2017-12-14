package com.android.camera.ui;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioSystem;
import android.opengl.GLSurfaceView.Renderer;
import android.support.v4.view.ViewCompat;
import android.support.v7.recyclerview.C0049R;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.camera.ActivityBase;
import com.android.camera.AutoLockManager;
import com.android.camera.CameraDataAnalytics;
import com.android.camera.CameraScreenNail;
import com.android.camera.CameraSettings;
import com.android.camera.Device;
import com.android.camera.aosp_porting.ReflectUtil;
import com.android.camera.effect.EffectController;
import com.android.camera.effect.draw_mode.DrawExtTexAttribute;
import com.android.camera.preferences.IconListPreference;
import com.android.camera.preferences.PreferenceGroup;
import com.android.gallery3d.ui.GLCanvas;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EffectPopup extends V6AbstractSettingPopup implements OnClickListener {
    private final String TAG = "EffectPopup";
    protected int mCurrentIndex = -1;
    private EffectItemAdapter mEffectItemAdapter;
    private List<EffectItemHolder> mEffectItemHolderList = new LinkedList();
    private int mHolderHeight;
    private int mHolderWidth;
    protected boolean mIgnoreSameItemClick = true;
    private LinearLayoutManager mLayoutManager;
    private Recycler mRecycler;
    private RecyclerView mRecyclerView;
    private EffectSelectedOverlay mSelectedOverlay;
    private int mTextureHeight;
    private int mTextureOffsetX;
    private int mTextureOffsetY;
    private int mTextureWidth;
    private int mTotalWidth;

    protected class EffectDivider extends ItemDecoration {
        protected int mFrameWidth;
        protected int mPadding;
        protected Paint mPaint = new Paint(1);
        protected int mPosition;
        protected int mVerticalPadding;
        protected int mWidth;

        public EffectDivider(int i) {
            Resources resources = EffectPopup.this.mContext.getResources();
            this.mPadding = resources.getDimensionPixelSize(C0049R.dimen.effect_item_padding);
            this.mWidth = resources.getDimensionPixelSize(C0049R.dimen.effect_divider_width);
            this.mFrameWidth = resources.getDimensionPixelSize(C0049R.dimen.effect_divider_frame_width);
            this.mVerticalPadding = resources.getDimensionPixelSize(C0049R.dimen.effect_divider_vertical_padding);
            this.mPosition = i;
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setColor(resources.getColor(C0049R.color.effect_divider_color));
            this.mPaint.setStrokeWidth((float) this.mWidth);
        }

        public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, State state) {
            if (recyclerView.getChildPosition(view) == this.mPosition) {
                rect.set(0, 0, this.mFrameWidth - this.mPadding, 0);
            }
        }

        public void onDraw(Canvas canvas, RecyclerView recyclerView, State state) {
            super.onDraw(canvas, recyclerView, state);
            int paddingTop = recyclerView.getPaddingTop() + this.mVerticalPadding;
            int height = (recyclerView.getHeight() - recyclerView.getPaddingBottom()) - this.mVerticalPadding;
            ViewHolder findViewHolderForPosition = recyclerView.findViewHolderForPosition(this.mPosition);
            if (findViewHolderForPosition != null) {
                View view = findViewHolderForPosition.itemView;
                int right = ((view.getRight() + ((LayoutParams) view.getLayoutParams()).rightMargin) + Math.round(ViewCompat.getTranslationX(view))) + (this.mFrameWidth / 2);
                canvas.drawLine((float) right, (float) paddingTop, (float) right, (float) height, this.mPaint);
            }
        }
    }

    protected abstract class EffectItemHolder extends ViewHolder {
        protected int mEffectIndex;
        protected TextView mTextView;

        public EffectItemHolder(View view) {
            super(view);
            this.mTextView = (TextView) view.findViewById(C0049R.id.effect_item_text);
            updateBackground();
        }

        public void bindEffectIndex(int i) {
            this.mEffectIndex = i;
            this.mTextView.setText(EffectPopup.this.mPreference.getEntries()[this.mEffectIndex]);
        }

        public void pause() {
        }

        public void requestRender() {
        }

        public void resume() {
        }

        public void start() {
        }

        public void stop() {
        }

        public void updateBackground() {
            if (((ActivityBase) EffectPopup.this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
                this.mTextView.setBackgroundResource(C0049R.color.effect_item_text_fullscreen_background);
            } else {
                this.mTextView.setBackgroundResource(C0049R.color.effect_item_text_halfscreen_background);
            }
        }
    }

    protected class EffectDynamicItemHolder extends EffectItemHolder {
        protected EffectDynamicItemRender mEffectRender;
        protected GLSurfaceTexture mEffectSurface = new GLSurfaceTexture();
        protected TextureView mTextureView;

        public EffectDynamicItemHolder(View view) {
            super(view);
            this.mTextureView = (TextureView) view.findViewById(C0049R.id.effect_item_texture);
            this.mEffectRender = new EffectDynamicItemRender();
            this.mEffectSurface.setEGLContextClientVersion(2);
            this.mEffectSurface.setRenderer(this.mEffectRender);
            this.mEffectSurface.setPreserveEGLContextOnPause(true);
            this.mEffectSurface.setRenderMode(0);
            this.mEffectSurface.setSize(EffectPopup.this.mHolderWidth, EffectPopup.this.mHolderHeight);
            this.mEffectSurface.startWithShareContext(((ActivityBase) EffectPopup.this.mContext).getUIController().getGLView().getEGLContext());
        }

        public void bindEffectIndex(int i) {
            super.bindEffectIndex(i);
            this.mEffectRender.bindEffectIndex(i);
        }

        public void pause() {
            this.mEffectSurface.pause();
        }

        public void requestRender() {
            this.mEffectSurface.requestRender();
        }

        public void resume() {
            this.mEffectSurface.resume();
            if (this.mTextureView.getSurfaceTexture() != this.mEffectSurface) {
                this.mTextureView.setSurfaceTexture(this.mEffectSurface);
            }
        }

        public void start() {
            if (this.mTextureView.getSurfaceTexture() != this.mEffectSurface) {
                this.mTextureView.setSurfaceTexture(this.mEffectSurface);
            }
            this.mEffectSurface.startWithShareContext(((ActivityBase) EffectPopup.this.mContext).getUIController().getGLView().getEGLContext());
        }

        public void stop() {
            this.mEffectSurface.stop();
        }
    }

    protected class EffectDynamicItemRender implements Renderer {
        int mEffectIndex;
        private DrawExtTexAttribute mExtTexture = new DrawExtTexAttribute(true);
        float[] mTransform = new float[16];

        protected EffectDynamicItemRender() {
        }

        public void bindEffectIndex(int i) {
            this.mEffectIndex = i;
        }

        public void onDrawFrame(GL10 gl10) {
            CameraScreenNail cameraScreenNail = ((ActivityBase) EffectPopup.this.mContext).getCameraScreenNail();
            GLCanvas gLCanvas = ((ActivityBase) EffectPopup.this.mContext).getUIController().getGLView().getGLCanvas();
            if (cameraScreenNail != null && gLCanvas != null && cameraScreenNail.getSurfaceTexture() != null) {
                synchronized (gLCanvas) {
                    gLCanvas.clearBuffer();
                    int width = gLCanvas.getWidth();
                    int height = gLCanvas.getHeight();
                    gLCanvas.getState().pushState();
                    gLCanvas.setSize(EffectPopup.this.mHolderWidth, EffectPopup.this.mHolderHeight);
                    cameraScreenNail.getSurfaceTexture().getTransformMatrix(this.mTransform);
                    EffectController instance = EffectController.getInstance();
                    synchronized (instance) {
                        int effect = instance.getEffect(false);
                        instance.setEffect(this.mEffectIndex);
                        gLCanvas.draw(this.mExtTexture.init(cameraScreenNail.getExtTexture(), this.mTransform, EffectPopup.this.mTextureOffsetX, EffectPopup.this.mTextureOffsetY, EffectPopup.this.mTextureWidth, EffectPopup.this.mTextureHeight));
                        instance.setEffect(effect);
                    }
                    gLCanvas.setSize(width, height);
                    gLCanvas.getState().popState();
                    gLCanvas.recycledResources();
                }
            }
        }

        public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        }

        public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        }
    }

    protected class EffectItemAdapter extends Adapter {
        protected List<Map<String, Object>> mEffectItem;
        protected LayoutInflater mLayoutInflater;

        public EffectItemAdapter(Context context, List<Map<String, Object>> list) {
            this.mEffectItem = list;
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        public int getItemCount() {
            return this.mEffectItem.size();
        }

        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            EffectItemHolder effectItemHolder = (EffectItemHolder) viewHolder;
            effectItemHolder.itemView.setTag(Integer.valueOf(i));
            effectItemHolder.bindEffectIndex(i);
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View inflate;
            ViewHolder effectDynamicItemHolder;
            if (Device.isSupportedDynamicEffectPopup()) {
                inflate = this.mLayoutInflater.inflate(C0049R.layout.effect_dynamic_item, viewGroup, false);
                effectDynamicItemHolder = new EffectDynamicItemHolder(inflate);
            } else {
                inflate = this.mLayoutInflater.inflate(C0049R.layout.effect_still_item, viewGroup, false);
                effectDynamicItemHolder = new EffectStillItemHolder(inflate);
            }
            inflate.setOnClickListener(EffectPopup.this);
            EffectPopup.this.mEffectItemHolderList.add(effectDynamicItemHolder);
            return effectDynamicItemHolder;
        }

        public void onViewAttachedToWindow(ViewHolder viewHolder) {
            ((EffectItemHolder) viewHolder).resume();
            super.onViewAttachedToWindow(viewHolder);
        }

        public void onViewDetachedFromWindow(ViewHolder viewHolder) {
            ((EffectItemHolder) viewHolder).pause();
            super.onViewDetachedFromWindow(viewHolder);
        }
    }

    protected class EffectItemPadding extends ItemDecoration {
        protected int mPadding;

        public EffectItemPadding() {
            this.mPadding = EffectPopup.this.mContext.getResources().getDimensionPixelSize(C0049R.dimen.effect_item_padding);
        }

        public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, State state) {
            int i = 0;
            if (recyclerView.getChildPosition(view) == 0) {
                i = this.mPadding;
            }
            rect.set(i, this.mPadding, this.mPadding, this.mPadding);
        }
    }

    protected class EffectSelectedOverlay extends ItemDecoration {
        protected ObjectAnimator mAnimator;
        protected int mOffsetX;
        protected Drawable mOverlay;
        protected int mPosition;

        public EffectSelectedOverlay() {
            this.mAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(EffectPopup.this.mContext, C0049R.anim.effect_select_slide);
            this.mOverlay = EffectPopup.this.mContext.getResources().getDrawable(C0049R.drawable.effect_item_selected);
            this.mAnimator.setTarget(this);
        }

        private int calcOffsetX(int i, int i2) {
            int left = getLeft(i);
            if (EffectPopup.this.mRecyclerView.findViewHolderForPosition(i) == null) {
                if (i < i2) {
                    left = EffectPopup.this.mRecyclerView.getLeft();
                } else if (i > i2) {
                    left = EffectPopup.this.mRecyclerView.getRight();
                }
            }
            return left - getLeft(i2);
        }

        private int getLeft(int i) {
            ViewHolder findViewHolderForPosition = EffectPopup.this.mRecyclerView.findViewHolderForPosition(i);
            if (findViewHolderForPosition == null) {
                return 0;
            }
            View view = findViewHolderForPosition.itemView;
            return view.getLeft() + Math.round(ViewCompat.getTranslationX(view));
        }

        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, State state) {
            super.onDraw(canvas, recyclerView, state);
            ViewHolder findViewHolderForPosition = EffectPopup.this.mRecyclerView.findViewHolderForPosition(this.mPosition);
            if (findViewHolderForPosition != null) {
                View view = findViewHolderForPosition.itemView;
                this.mOverlay.setBounds(this.mOffsetX + (view.getLeft() + Math.round(ViewCompat.getTranslationX(view))), view.getTop(), this.mOffsetX + (view.getRight() + Math.round(ViewCompat.getTranslationX(view))), view.getBottom());
                this.mOverlay.draw(canvas);
            }
        }

        public void select(int i) {
            this.mAnimator.cancel();
            this.mAnimator.setIntValues(new int[]{calcOffsetX(this.mPosition, i), 0});
            this.mPosition = i;
            this.mAnimator.start();
        }

        public void setOffsetX(int i) {
            this.mOffsetX = i;
            EffectPopup.this.mRecyclerView.postInvalidateOnAnimation();
        }
    }

    protected class EffectStillItemHolder extends EffectItemHolder {
        protected ImageView mImageView;

        public EffectStillItemHolder(View view) {
            super(view);
            this.mImageView = (ImageView) view.findViewById(C0049R.id.effect_item_image);
        }

        public void bindEffectIndex(int i) {
            super.bindEffectIndex(i);
            if (i < EffectPopup.this.mPreference.getIconIds().length) {
                this.mImageView.setImageResource(EffectPopup.this.mPreference.getIconIds()[i]);
            }
        }
    }

    public EffectPopup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public Animation getAnimation(boolean z) {
        return AnimationUtils.loadAnimation(this.mContext, z ? C0049R.anim.effect_popup_slide_up : C0049R.anim.effect_popup_slide_down);
    }

    public void initialize(PreferenceGroup preferenceGroup, IconListPreference iconListPreference, MessageDispacher messageDispacher) {
        super.initialize(preferenceGroup, iconListPreference, messageDispacher);
        Context context = getContext();
        CharSequence[] entries = this.mPreference.getEntries();
        List arrayList = new ArrayList();
        for (CharSequence charSequence : entries) {
            HashMap hashMap = new HashMap();
            hashMap.put("text", charSequence.toString());
            arrayList.add(hashMap);
        }
        this.mRecycler = (Recycler) ReflectUtil.getFieldValue(this.mRecyclerView.getClass(), this.mRecyclerView, "mRecycler", "");
        this.mTotalWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.mHolderWidth = context.getResources().getDimensionPixelSize(C0049R.dimen.effect_item_width);
        this.mHolderHeight = context.getResources().getDimensionPixelSize(C0049R.dimen.effect_item_height);
        this.mEffectItemAdapter = new EffectItemAdapter(context, arrayList);
        this.mLayoutManager = new LinearLayoutManager(context);
        this.mLayoutManager.setOrientation(0);
        this.mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, EffectController.getInstance().getEffectCount());
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mRecyclerView.addItemDecoration(new EffectItemPadding());
        this.mRecyclerView.addItemDecoration(new EffectDivider(EffectController.sDividerIndex));
        this.mSelectedOverlay = new EffectSelectedOverlay();
        this.mRecyclerView.addItemDecoration(this.mSelectedOverlay);
        this.mRecyclerView.setAdapter(this.mEffectItemAdapter);
        reloadPreference();
    }

    protected void notifyToDispatcher(boolean z) {
        if (this.mMessageDispacher != null) {
            this.mMessageDispacher.dispacherMessage(6, 0, 3, this.mPreference.getKey(), this);
        }
    }

    public void onClick(View view) {
        if (this.mRecyclerView.isEnabled()) {
            int intValue = ((Integer) view.getTag()).intValue();
            if (this.mCurrentIndex != intValue || !this.mIgnoreSameItemClick) {
                boolean z = this.mCurrentIndex == intValue;
                this.mCurrentIndex = intValue;
                this.mSelectedOverlay.select(this.mCurrentIndex);
                this.mPreference.setValueIndex(intValue);
                if ("pref_camera_scenemode_key".equals(this.mPreference.getKey())) {
                    CameraSettings.setFocusModeSwitching(true);
                } else if ("pref_audio_focus_key".equals(this.mPreference.getKey()) && ((ActivityBase) this.mContext).getCurrentModule().isVideoRecording()) {
                    AudioSystem.setParameters("camcorder_mode=" + this.mPreference.getValue());
                }
                EffectController.getInstance().setInvertFlag(0);
                ((ActivityBase) this.mContext).getUIController().getEffectCropView().updateVisible(this.mCurrentIndex);
                notifyToDispatcher(z);
                if (this.mCurrentIndex != 0) {
                    CameraDataAnalytics.instance().trackEvent(this.mPreference.getKey());
                }
                AutoLockManager.getInstance(this.mContext).onUserInteraction();
            }
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mRecyclerView = (RecyclerView) findViewById(C0049R.id.effect_list);
    }

    public void reloadPreference() {
        this.mCurrentIndex = this.mPreference.findIndexOfValue(this.mPreference.getValue());
        if (this.mCurrentIndex != -1) {
            if (Device.isNeedForceRecycleEffectPopup() && this.mRecycler != null) {
                this.mLayoutManager.removeAndRecycleAllViews(this.mRecycler);
            }
            setItemInCenter(this.mCurrentIndex);
            this.mSelectedOverlay.select(this.mCurrentIndex);
            return;
        }
        Log.e("EffectPopup", "Invalid preference value.");
        this.mPreference.print();
    }

    public void requestEffectRender() {
        for (int i = 0; i < this.mLayoutManager.getChildCount(); i++) {
            View childAt = this.mLayoutManager.getChildAt(i);
            if (childAt != null) {
                EffectItemHolder effectItemHolder = (EffectItemHolder) this.mRecyclerView.getChildViewHolder(childAt);
                if (effectItemHolder != null) {
                    effectItemHolder.requestRender();
                }
            }
        }
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        if (this.mRecyclerView != null) {
            this.mRecyclerView.setEnabled(z);
        }
    }

    protected void setItemInCenter(int i) {
        this.mLayoutManager.scrollToPositionWithOffset(i, (this.mTotalWidth / 2) - (this.mHolderWidth / 2));
    }

    public void setOrientation(int i, boolean z) {
    }

    public void show(boolean z) {
        super.show(z);
        if ("pref_camera_scenemode_key".equals(this.mPreference.getKey()) && !"auto".equals(this.mPreference.getValue())) {
            CameraSettings.setFocusModeSwitching(true);
        }
    }

    public void startEffectRender() {
        CameraScreenNail cameraScreenNail = ((ActivityBase) this.mContext).getCameraScreenNail();
        int width = cameraScreenNail.getWidth();
        int height = cameraScreenNail.getHeight();
        this.mTextureOffsetX = 0;
        this.mTextureOffsetY = 0;
        this.mTextureWidth = this.mHolderWidth;
        this.mTextureHeight = this.mHolderHeight;
        if (this.mHolderWidth * height > this.mHolderHeight * width) {
            this.mTextureHeight = (this.mHolderWidth * height) / width;
            this.mTextureOffsetY = (-(this.mTextureHeight - this.mHolderHeight)) / 2;
        } else {
            this.mTextureWidth = (this.mHolderHeight * width) / height;
            this.mTextureOffsetX = (-(this.mTextureWidth - this.mHolderWidth)) / 2;
        }
        for (EffectItemHolder start : this.mEffectItemHolderList) {
            start.start();
        }
    }

    public void stopEffectRender() {
        for (EffectItemHolder stop : this.mEffectItemHolderList) {
            stop.stop();
        }
    }

    public void updateBackground() {
        if (((ActivityBase) this.mContext).getUIController().getPreviewFrame().isFullScreen()) {
            this.mRecyclerView.setBackgroundResource(C0049R.color.effect_popup_fullscreen_background);
        } else {
            this.mRecyclerView.setBackgroundResource(C0049R.color.effect_popup_halfscreen_background);
        }
        for (EffectItemHolder updateBackground : this.mEffectItemHolderList) {
            updateBackground.updateBackground();
        }
    }
}
