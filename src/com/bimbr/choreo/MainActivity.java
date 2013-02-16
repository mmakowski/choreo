package com.bimbr.choreo;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.CATEGORY_OPENABLE;
import static android.content.Intent.createChooser;
import static android.os.Environment.getExternalStorageDirectory;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.bimbr.android.media.NotifyingMediaPlayer;
import com.bimbr.choreo.view.ChoreographyView;

/**
 * Editing choreography.
 * 
 * @author mmakowski
 */
public class MainActivity extends Activity {
    private static final int SELECT_MUSIC_REQUEST_CODE = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        promptForMusic();
    }

    private void setControlledMediaPlayer(final NotifyingMediaPlayer player) {
        final Button button = (Button) findViewById(R.id.playPause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (player.isPlaying()) player.pause(); else player.start();
            }
        });
        ((ChoreographyView) findViewById(R.id.choreographyView)).setMediaPlayer(player);
    }

    private void startMediaPlayer(final String selectedAudioPath) {
        final NotifyingMediaPlayer mediaPlayer = new NotifyingMediaPlayer();

        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer player) {
                setControlledMediaPlayer(mediaPlayer);
            }});

        try {
            mediaPlayer.setDataSource(selectedAudioPath);
            mediaPlayer.prepare();
            Log.d("AudioPlayer", "prepared " + selectedAudioPath);
        } catch (final IOException e) {
            Log.e("AudioPlayer", "Could not open file " + selectedAudioPath + " for playback.", e);
            // TODO: report to user
        }
    }

    private void promptForMusic() {
        final String path = getExternalStorageDirectory().getAbsolutePath();
        final Intent musicSelection = new Intent(path);
        musicSelection.setType("audio/mp3");
        musicSelection.setAction(ACTION_GET_CONTENT);
        musicSelection.addCategory(CATEGORY_OPENABLE);
        startActivityForResult(createChooser(musicSelection, "select music"), SELECT_MUSIC_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case SELECT_MUSIC_REQUEST_CODE:
                final Uri selectedAudioUri = data.getData();
                startMediaPlayer(selectedAudioUri.toString());
                break;
            }
        }
    }
}
