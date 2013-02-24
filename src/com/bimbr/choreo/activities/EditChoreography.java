package com.bimbr.choreo.activities;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.CATEGORY_OPENABLE;
import static android.content.Intent.createChooser;
import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.io.Files.write;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.bimbr.android.media.NotifyingMediaPlayer;
import com.bimbr.choreo.R;
import com.bimbr.choreo.app.ChoreoApplication;
import com.bimbr.choreo.model.Choreography;
import com.bimbr.choreo.model.Dictionary;
import com.bimbr.choreo.model.Move;
import com.bimbr.choreo.model.json.ChoreographyJsonConverter;
import com.bimbr.choreo.view.ChoreographyView;
import com.bimbr.choreo.view.ChoreographyView.OnAddMoveListener;

/**
 * Editing choreography.
 *
 * @author mmakowski
 */
public class EditChoreography extends Activity {
    private static final int SELECT_MUSIC_REQUEST_CODE = 1;
    private static final String LOG_TAG = "NewChoreo";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_choreography);
        promptForMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeChoreography();
    }

    private void setControlledMediaPlayer(final NotifyingMediaPlayer player) {
        final Button playPauseButton = (Button) findViewById(R.id.playPause);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (player.isPlaying()) player.pause(); else player.start();
            }
        });
        // TODO: set choreography document instead and make playback tracking work through listeners on notifying media player
        choreographyView().setMediaPlayer(player);
    }

    private void prepareMediaPlayerFor(final String selectedAudioPath) {
        final NotifyingMediaPlayer mediaPlayer = new NotifyingMediaPlayer();

        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer player) {
                setControlledMediaPlayer(mediaPlayer);
                createChoreography(mediaPlayer);
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

    private void createChoreography(final NotifyingMediaPlayer mediaPlayer) {
        final Choreography choreography = new Choreography("test");
        choreography.setMusicDurationMs(mediaPlayer.getDuration());
        ((ChoreoApplication) getApplication()).setChoreography(choreography);
        choreographyView().setChoreography(choreography);
    }

    private void writeChoreography() {
        writeChoreography(new ChoreographyJsonConverter().toJson(choreography()));
    }

    private Choreography choreography() {
        return ((ChoreoApplication) getApplication()).getChoreography();
    }

    private void writeChoreography(final String json) {
        if (sdCardIsWriteable()) {
            final File dir = new File(Environment.getExternalStorageDirectory(), "Choreo");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    // TODO: aler the user
                    Log.e(LOG_TAG, "unable to create directory " + dir.getAbsolutePath());
                }
            }
            final File file = new File(dir, "test.choreo");
            try {
                write(json, file, UTF_8);
                Log.d(LOG_TAG, "saved choreography to " + file.getAbsolutePath());
            } catch (final IOException e) {
                // TODO: aler the user
                Log.e(LOG_TAG, "save failed", e);
            }
        } else {
            // TODO: alert the user
            Log.e(LOG_TAG, "SD card is unavailable");
        }
    }

    private boolean sdCardIsWriteable() {
        return MEDIA_MOUNTED.equals(getExternalStorageState());
    }

    private void promptForMusic() {
        final String path = getExternalStorageDirectory().getAbsolutePath();
        final Intent musicSelection = new Intent(path);
        musicSelection.setType("audio/mp3");
        musicSelection.setAction(ACTION_GET_CONTENT);
        musicSelection.addCategory(CATEGORY_OPENABLE);
        startActivityForResult(createChooser(musicSelection, "Select music"), SELECT_MUSIC_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_choreography, menu);
        return true;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case SELECT_MUSIC_REQUEST_CODE:
                onMusicSelected(data);
                break;
            }
        }
    }

    private void onMusicSelected(final Intent selectionResult) {
        final Uri selectedAudioUri = selectionResult.getData();
        prepareMediaPlayerFor(selectedAudioUri.toString());
        // TODO: more appropriate place for this
        final ChoreographyView choreoView = choreographyView();
        choreoView.setOnAddMoveListener(new OnAddMoveListener(){
            @Override
            public void onAddMove(final int measureIndex) {
                movePickerDialog(measureIndex).show();
            }});
    }

    private ChoreographyView choreographyView() {
        return (ChoreographyView) findViewById(R.id.choreographyView);
    }

    private Dialog movePickerDialog(final int measureIndex) {
        // TODO: read dictionary once, when choreography is created
        final Dictionary dict;
        try {
            dict = Dictionary.fromInputStream(getAssets().openFd("dictionary.txt").createInputStream());
        } catch (final IOException e) {
            Log.e(LOG_TAG, "can't open dictionary", e);
            return new AlertDialog.Builder(this).setMessage("can't open dictionary").create();
        }

        final String[] items = toArray(dict.allMoveNames(), String.class);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select move")
               .setItems(items, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(final DialogInterface dialog, final int which) {
                       choreography().addMove(measureIndex, new Move(dict.symbolFor(items[which])));
                   }
               });
        return builder.create();
    }
}
