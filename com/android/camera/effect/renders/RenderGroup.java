package com.android.camera.effect.renders;

import android.opengl.GLES20;
import com.android.camera.effect.EffectController;
import com.android.camera.effect.EffectController.EffectRectAttribute;
import com.android.camera.effect.FrameBuffer;
import com.android.camera.effect.draw_mode.DrawAttribute;
import com.android.camera.ui.V6ModulePicker;
import com.android.gallery3d.ui.GLCanvas;
import java.util.ArrayList;

public class RenderGroup extends Render {
    protected int mParentFrameBufferIdOld;
    private ArrayList<Render> mPartRenders = new ArrayList();
    protected ArrayList<Render> mRenders = new ArrayList();

    public RenderGroup(GLCanvas gLCanvas) {
        super(gLCanvas);
    }

    public RenderGroup(GLCanvas gLCanvas, int i) {
        super(gLCanvas);
        for (int i2 = 0; i2 < i; i2++) {
            addRender(null);
        }
    }

    private void setSize(Render render) {
        if (render != null) {
            if (!(this.mPreviewWidth == 0 && this.mPreviewHeight == 0)) {
                render.setPreviewSize(this.mPreviewWidth, this.mPreviewHeight);
            }
            if (this.mViewportWidth != 0 || this.mViewportHeight != 0) {
                render.setViewportSize(this.mViewportWidth, this.mViewportHeight);
            }
        }
    }

    public void addPartRender(Render render) {
        this.mPartRenders.add(render);
    }

    public void addRender(Render render) {
        this.mRenders.add(render);
        setSize(render);
    }

    public void beginBindFrameBuffer(FrameBuffer frameBuffer) {
        GLES20.glBindFramebuffer(36160, frameBuffer.getId());
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, frameBuffer.getTexture().getId(), 0);
        this.mGLCanvas.getState().pushState();
        this.mGLCanvas.getState().indentityAllM();
        this.mOldViewportWidth = this.mViewportWidth;
        this.mOldViewportHeight = this.mViewportHeight;
        this.mParentFrameBufferIdOld = this.mParentFrameBufferId;
        setParentFrameBufferId(frameBuffer.getId());
        setViewportSize(frameBuffer.getWidth(), frameBuffer.getHeight());
    }

    public void clearPartRenders() {
        this.mPartRenders.clear();
    }

    public boolean draw(DrawAttribute drawAttribute) {
        if (this.mRenders.isEmpty()) {
            return false;
        }
        for (Render draw : this.mRenders) {
            if (draw.draw(drawAttribute)) {
                return true;
            }
        }
        return false;
    }

    public void endBindFrameBuffer() {
        this.mGLCanvas.getState().popState();
        GLES20.glBindFramebuffer(36160, this.mParentFrameBufferIdOld);
        setViewportSize(this.mOldViewportWidth, this.mOldViewportHeight);
        setParentFrameBufferId(this.mParentFrameBufferIdOld);
    }

    public Render getPartRender(int i) {
        return (i < 0 || i >= this.mPartRenders.size()) ? null : (Render) this.mPartRenders.get(i);
    }

    public Render getRender(int i) {
        return (i < 0 || i >= this.mRenders.size()) ? null : (Render) this.mRenders.get(i);
    }

    public boolean isNeedInit(int i) {
        boolean z = true;
        if (i > -1) {
            if (i == 0) {
                z = false;
            } else if (this.mRenders.size() > i && this.mRenders.get(i) != null) {
                z = false;
            }
            return z;
        }
        int i2 = 0;
        while (i2 < this.mRenders.size()) {
            if (this.mRenders.get(i2) == null && i2 != 0 && (V6ModulePicker.isCameraModule() || i2 == EffectController.sBackgroundBlurIndex)) {
                return true;
            }
            i2++;
        }
        return false;
    }

    public boolean isPartComplete(int i) {
        return this.mPartRenders.size() == i;
    }

    public void setEffectRangeAttribute(EffectRectAttribute effectRectAttribute) {
        super.setEffectRangeAttribute(effectRectAttribute);
        if (!this.mRenders.isEmpty()) {
            for (Render render : this.mRenders) {
                if (render != null) {
                    render.setEffectRangeAttribute(effectRectAttribute);
                }
            }
        }
    }

    public void setJpegOrientation(int i) {
        if (this.mJpegOrientation != i) {
            super.setJpegOrientation(i);
            if (!this.mRenders.isEmpty()) {
                for (Render render : this.mRenders) {
                    if (render != null) {
                        render.setJpegOrientation(i);
                    }
                }
            }
        }
    }

    public void setMirror(boolean z) {
        super.setMirror(z);
        if (!this.mRenders.isEmpty()) {
            for (Render render : this.mRenders) {
                if (render != null) {
                    render.setMirror(z);
                }
            }
        }
    }

    public void setOrientation(int i) {
        if (this.mOrientation != i) {
            super.setOrientation(i);
            if (!this.mRenders.isEmpty()) {
                for (Render render : this.mRenders) {
                    if (render != null) {
                        render.setOrientation(i);
                    }
                }
            }
        }
    }

    protected void setParentFrameBufferId(int i) {
        super.setParentFrameBufferId(i);
        if (!this.mRenders.isEmpty()) {
            for (Render render : this.mRenders) {
                if (render != null) {
                    render.setParentFrameBufferId(i);
                }
            }
        }
    }

    public void setPreviewSize(int i, int i2) {
        super.setPreviewSize(i, i2);
        if (!this.mRenders.isEmpty()) {
            for (Render render : this.mRenders) {
                if (render != null) {
                    render.setPreviewSize(i, i2);
                }
            }
        }
    }

    public void setRender(Render render, int i) {
        this.mRenders.set(i, render);
        setSize(render);
    }

    public void setViewportSize(int i, int i2) {
        super.setViewportSize(i, i2);
        if (!this.mRenders.isEmpty()) {
            for (Render render : this.mRenders) {
                if (render != null) {
                    render.setViewportSize(i, i2);
                }
            }
        }
    }
}
