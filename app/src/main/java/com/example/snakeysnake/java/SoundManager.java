package com.example.snakeysnake.java;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.io.IOException;

public class SoundManager {

    private SoundPool mSoundPool;
    private int mEatSoundId;
    private int mCrashSoundId;

    public SoundManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load the sounds into memory
            descriptor = assetManager.openFd("get_apple.ogg");
            mEatSoundId = mSoundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashSoundId = mSoundPool.load(descriptor, 0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playEatSound() {
        mSoundPool.play(mEatSoundId, 1, 1, 0, 0, 1);
    }

    public void playCrashSound() {
        mSoundPool.play(mCrashSoundId, 1, 1, 0, 0, 1);
    }

    public void release() {
        mSoundPool.release();
    }
}
