package com.bimbr.choreo.activities

import com.google.common.collect.Iterables.toArray
import java.io.IOException
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import com.bimbr.android.media.NotifyingMediaPlayer
import com.bimbr.choreo.{TR, R}
import com.bimbr.choreo.model.Dictionary
import com.bimbr.choreo.model.Move
import com.bimbr.choreo.persistence.Persistence
import com.bimbr.choreo.view.ChoreographyView

final class EditChoreography extends ChoreoActivity {
  private val LogTag: String = "NewChoreo"

  private var mediaPlayer   : NotifyingMediaPlayer = null
  private var playPositionMs: Int                  = 0
  
  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(TR.layout.edit_choreography.id)
    prepareMediaPlayerFor(choreography.getMusicPath)
    setHandlers()
  }

  protected override def onPause(): Unit = {
    super.onPause()
    Persistence.withRoot(getFilesDir).writeChoreography(choreography)
    playPositionMs = mediaPlayer.getCurrentPosition
    mediaPlayer.release()
  }

  protected override def onResume(): Unit = {
    super.onResume()
    prepareMediaPlayerFor(choreography.getMusicPath)
    mediaPlayer.seekTo(playPositionMs)
  }

  private def setControlledMediaPlayer(player: NotifyingMediaPlayer): Unit = {
    val playPauseButton: Button = findViewById(R.id.playPause).asInstanceOf[Button]
    playPauseButton.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) {
        if (player.isPlaying) player.pause()
        else                  player.start()
      }
    })
    choreographyView.setMediaPlayer(player)
  }

  private def prepareMediaPlayerFor(selectedAudioPath: String) {
    if (mediaPlayer != null) mediaPlayer.release()
    mediaPlayer = new NotifyingMediaPlayer
    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener {
      def onPrepared(player: MediaPlayer) {
        setControlledMediaPlayer(mediaPlayer)
        updateChoreographyFrom(mediaPlayer)
      }
    })
    try {
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
      mediaPlayer.setDataSource(this, Uri.parse(selectedAudioPath))
      mediaPlayer.prepare()
      Log.d("AudioPlayer", "prepared " + selectedAudioPath)
    } catch {
      case e: IOException =>
        Log.e("AudioPlayer", "Could not open " + selectedAudioPath + " for playback.", e)
    }
  }

  private def updateChoreographyFrom(mediaPlayer: NotifyingMediaPlayer): Unit = {
    choreography.setMusicDurationMs(mediaPlayer.getDuration)
    choreographyView.setChoreography(choreography)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    getMenuInflater.inflate(R.menu.edit_choreography, menu)
    true
  }

  private def setHandlers(): Unit = {
    val choreoView: ChoreographyView = choreographyView
    choreoView.setOnAddMoveListener(new ChoreographyView.OnAddMoveListener {
      def onAddMove(measureIndex: Int) {
        movePickerDialog(measureIndex).show()
      }
    })
  }

  private def choreographyView: ChoreographyView = findView(TR.choreographyView)

  private def movePickerDialog(measureIndex: Int): Dialog = {
    val dict = try Dictionary.fromInputStream(getAssets.openFd("dictionary.txt").createInputStream)
               catch {
                 case e: IOException =>
                   Log.e(LogTag, "can't open dictionary", e)
                   return new AlertDialog.Builder(this).setMessage("can't open dictionary").create
               }
    val items = toArray(dict.allMoveNames, classOf[String]).asInstanceOf[Array[CharSequence]]
    val builder: AlertDialog.Builder = new AlertDialog.Builder(this)
    builder.setTitle("Select move").setItems(items, new DialogInterface.OnClickListener {
      def onClick(dialog: DialogInterface, which: Int) {
        choreography.addMove(measureIndex, new Move(dict.symbolFor(items(which).asInstanceOf[String])))
      }
    })
    builder.create
  }
}
