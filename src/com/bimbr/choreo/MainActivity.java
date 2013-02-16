package com.bimbr.choreo;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private static final int SELECT_MUSIC_REQUEST_CODE = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        promptForMusic();
    }

    private void setControlledMediaPlayer(final MediaPlayer player) {
        final Button button = (Button) findViewById(R.id.playPause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (player.isPlaying()) player.pause(); else player.start();
            }
        });
    }

    private void startMediaPlayer(final String selectedAudioPath) {
        final MediaPlayer mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer player) {
                setControlledMediaPlayer(player);
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
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final Intent musicSelection = new Intent(path);
        musicSelection.setType("audio/mp3");
        musicSelection.setAction(Intent.ACTION_GET_CONTENT);
        musicSelection.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(musicSelection, "select music"), SELECT_MUSIC_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case SELECT_MUSIC_REQUEST_CODE:
                final Uri selectedAudioUri = data.getData();
                final String selectedAudioPath = getPathAudio(selectedAudioUri);
                startMediaPlayer(selectedAudioPath);
                break;
            }
        }
    }

    private String getPathAudio(final Uri uriAudio) {
        // String selectedImagePath;
        // 1:MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
        String selectedAudioPath = "";
        final String[] projection = { MediaStore.Audio.Media.DATA };

        final Cursor cursor = managedQuery(uriAudio, projection, null, null, null);

        if (cursor != null) {
            final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            selectedAudioPath = cursor.getString(column_index);

        } else {
            selectedAudioPath = null;
        }

        if (selectedAudioPath == null) {
            selectedAudioPath = uriAudio.getPath();
        }

        return selectedAudioPath;
    }
}
