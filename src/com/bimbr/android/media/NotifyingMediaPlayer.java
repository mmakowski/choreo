package com.bimbr.android.media;

import android.media.MediaPlayer;

/**
 * Media player that issues additional notifications.
 * 
 * @author mmakowski
 */
public class NotifyingMediaPlayer extends MediaPlayer {
    private OnStartedListener startListener;
    private OnPausedListener pauseListener;

    @Override
    public void start() {
        super.start();
        if (startListener != null) startListener.onStarted(this);
    }

    @Override
    public void pause() {
        super.pause();
        if (pauseListener != null) pauseListener.onPaused(this);
    }

    public void setOnStartedListener(final OnStartedListener listener) {
        startListener = listener;
    }

    public void setOnPausedListener(final OnPausedListener listener) {
        pauseListener = listener;
    }

    public interface OnStartedListener {
        void onStarted(NotifyingMediaPlayer player);
    }

    public interface OnPausedListener {
        void onPaused(NotifyingMediaPlayer player);
    }
}
