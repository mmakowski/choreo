package com.bimbr.choreo;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ScrollView;

public class MainActivity extends Activity {
	private static final int SELECT_MUSIC_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        promptForMusic();
    }

	private void startMediaPlayer(String selectedAudioPath) {
		final ScrollView choreoView = (ScrollView) findViewById(R.id.choreoView);        

        final MediaController mediaController = new MediaController(this);
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new OnPreparedListener(){
			@Override
			public void onPrepared(MediaPlayer mp) {
			    mediaController.setMediaPlayer(new MediaPlayerControl(){

					@Override
					public void start() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void pause() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public int getDuration() {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public int getCurrentPosition() {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public void seekTo(int pos) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public boolean isPlaying() {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public int getBufferPercentage() {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public boolean canPause() {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean canSeekBackward() {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public boolean canSeekForward() {
						// TODO Auto-generated method stub
						return false;
					}});
			    mediaController.setAnchorView(choreoView);

			    new Handler().post(new Runnable() {
			      public void run() {
			        mediaController.setEnabled(true);
			        mediaController.show();
			      }
			    });				
			}});

        try {
          mediaPlayer.setDataSource(selectedAudioPath);
          mediaPlayer.prepare();
          mediaPlayer.start();
          Log.d("AudioPlayer", "started playing " + selectedAudioPath);
        } catch (IOException e) {
          Log.e("AudioPlayer", "Could not open file " + selectedAudioPath + " for playback.", e);
        }
	}

	private void promptForMusic() {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Intent musicSelection = new Intent(path);
        musicSelection.setType("audio/mp3");
        musicSelection.setAction(Intent.ACTION_GET_CONTENT);
        musicSelection.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(musicSelection, "select music"), SELECT_MUSIC_REQUEST_CODE);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SELECT_MUSIC_REQUEST_CODE:
				Uri selectedAudioUri = data.getData();
				String selectedAudioPath = getPathAudio(selectedAudioUri);
		        startMediaPlayer(selectedAudioPath);
				break;
			}
		}
	}
	
	private String getPathAudio(Uri uriAudio) {
		// String selectedImagePath;
		// 1:MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
		String selectedAudioPath = "";
		String[] projection = { MediaStore.Audio.Media.DATA };

		Cursor cursor = managedQuery(uriAudio, projection, null, null, null);

		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
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
