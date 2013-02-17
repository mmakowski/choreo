package com.bimbr.choreo;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.CATEGORY_OPENABLE;
import static android.content.Intent.createChooser;
import static android.os.Environment.getExternalStorageDirectory;

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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.bimbr.android.media.NotifyingMediaPlayer;
import com.bimbr.choreo.model.Choreography;
import com.bimbr.choreo.model.Move;
import com.bimbr.choreo.view.ChoreographyView;
import com.bimbr.choreo.view.ChoreographyView.OnAddMoveListener;

/**
 * Editing choreography.
 *
 * @author mmakowski
 */
public class MainActivity extends Activity {
    private static final int SELECT_MUSIC_REQUEST_CODE = 1;
    private static final String LOG_TAG = "NewChoreo";

    private Choreography choreography;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        promptForMusic();
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
        choreography = new Choreography(mediaPlayer.getDuration());
        choreographyView().setChoreography(choreography);
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
        getMenuInflater().inflate(R.menu.activity_main, menu);
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
        final String[] items = {"jump", "spin"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select move")
               .setItems(items, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(final DialogInterface dialog, final int which) {
                       choreography.addMove(measureIndex, new Move(items[which].substring(0, 1)));
                   }
               });
        return builder.create();
    }
}
