package com.bimbr.choreo.activities;

import static com.bimbr.choreo.persistence.Persistence.writeChoreography;
import static com.google.common.collect.Iterables.toArray;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.bimbr.android.media.NotifyingMediaPlayer;
import com.bimbr.choreo.R;
import com.bimbr.choreo.model.Choreography;
import com.bimbr.choreo.model.Dictionary;
import com.bimbr.choreo.model.Move;
import com.bimbr.choreo.view.ChoreographyView;
import com.bimbr.choreo.view.ChoreographyView.OnAddMoveListener;

/**
 * Editing choreography.
 *
 * @author mmakowski
 */
public class EditChoreography extends ChoreoActivity {
    private static final String LOG_TAG = "NewChoreo";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_choreography);
        prepareMediaPlayerFor(choreography().getMusicPath());
        setHandlers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeChoreography(choreography());
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
        application().setChoreography(choreography);
        choreographyView().setChoreography(choreography);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.edit_choreography, menu);
        return true;
    }

    private void setHandlers() {
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
