package com.android.camera;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import java.io.IOException;

public class MiuiCameraSound {
    private static final String[] SOUND_FILES = new String[]{"camera_click.ogg", "camera_focus.ogg", "video_record_start.ogg", "video_record_end.ogg", "camera_fast_burst.ogg", "sound_shuter_delay_bee.ogg", "NumberPickerValueChange.ogg", "audio_capture.ogg"};
    private final AssetManager mAssetManager;
    private final AudioManager mAudioManager;
    private long mLastPlayTime = 0;
    private OnLoadCompleteListener mLoadCompleteListener = new C00851();
    private int mSoundIdToPlay;
    private int[] mSoundIds;
    private SoundPool mSoundPool;

    class C00851 implements OnLoadCompleteListener {
        C00851() {
        }

        public void onLoadComplete(SoundPool soundPool, int i, int i2) {
            if (i2 != 0) {
                Log.e("MiuiCameraSound", "Unable to load sound for playback (status: " + i2 + ")");
            } else if (MiuiCameraSound.this.mSoundIdToPlay == i) {
                soundPool.play(i, 1.0f, 1.0f, 0, 0, 1.0f);
                MiuiCameraSound.this.mSoundIdToPlay = -1;
            }
        }
    }

    public MiuiCameraSound(Context context) {
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mAssetManager = context.getAssets();
        this.mSoundPool = new SoundPool(1, Device.isSupportedMuteCameraSound() ? 1 : 7, 0);
        this.mSoundPool.setOnLoadCompleteListener(this.mLoadCompleteListener);
        this.mSoundIds = new int[SOUND_FILES.length];
        for (int i = 0; i < this.mSoundIds.length; i++) {
            this.mSoundIds[i] = -1;
        }
        this.mSoundIdToPlay = -1;
    }

    private int loadFromAsset(int i) {
        int i2 = -1;
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = this.mAssetManager.openFd(SOUND_FILES[i]);
            i2 = this.mSoundPool.load(assetFileDescriptor, 1);
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e) {
                    Log.e("MiuiCameraSound", "IOException occurs when closing Camera Sound AssetFileDescriptor.");
                    e.printStackTrace();
                }
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e22) {
                    Log.e("MiuiCameraSound", "IOException occurs when closing Camera Sound AssetFileDescriptor.");
                    e22.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e222) {
                    Log.e("MiuiCameraSound", "IOException occurs when closing Camera Sound AssetFileDescriptor.");
                    e222.printStackTrace();
                }
            }
        }
        return i2;
    }

    private synchronized void play(int i, int i2) {
        if (i >= 0) {
            if (i < SOUND_FILES.length) {
                if (this.mSoundIds[i] == -1) {
                    if (i == 6 || i == 1) {
                        this.mSoundIdToPlay = this.mSoundPool.load(SOUND_FILES[i], 1);
                    } else {
                        this.mSoundIdToPlay = loadFromAsset(i);
                    }
                    this.mSoundIds[i] = this.mSoundIdToPlay;
                } else {
                    this.mSoundPool.play(this.mSoundIds[i], 1.0f, 1.0f, 0, i2 - 1, 1.0f);
                    this.mLastPlayTime = System.currentTimeMillis();
                }
            }
        }
        throw new RuntimeException("Unknown sound requested: " + i);
    }

    public long getLastSoundPlayTime() {
        return this.mLastPlayTime;
    }

    public synchronized void load(int i) {
        if (i >= 0) {
            if (i < SOUND_FILES.length) {
                if (this.mSoundIds[i] == -1) {
                    this.mSoundIds[i] = loadFromAsset(i);
                }
            }
        }
        throw new RuntimeException("Unknown sound requested: " + i);
    }

    public void playSound(int i) {
        playSound(i, 1);
    }

    public void playSound(int i, int i2) {
        if (Device.IS_CM || this.mAudioManager.getRingerMode() == 2) {
            play(i, i2);
        }
    }

    public void release() {
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
    }
}
