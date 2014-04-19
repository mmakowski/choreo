package com.bimbr.choreo.activities

import android.content.Intent.ACTION_GET_CONTENT
import android.content.Intent.CATEGORY_OPENABLE
import android.content.Intent.createChooser
import com.bimbr.choreo.persistence.Persistence.choreographyLoadedFrom
import java.io.File
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import com.bimbr.choreo.TR
import com.bimbr.choreo.persistence.FileInfo
import com.bimbr.choreo.persistence.Persistence
import android.app.Activity
import scala.collection.JavaConverters._

final class Welcome extends ChoreoActivity {
  private val Title                  = "title"
  private val Description            = "desc"
  private val SelectMusicRequestCode = 1

  private var choreographyFiles: Seq[FileInfo] = Nil

  protected override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(TR.layout.welcome.id)
    setHandlers()
    setFilePickerItems()
  }

  private def setHandlers(): Unit = {
    setNewChoreographyButtonHandler()
    setFilePickedHandler()
  }

  private def setNewChoreographyButtonHandler(): Unit = {
    val newChoreographyButton: Button = findView(TR.newChoreography)
    newChoreographyButton.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) {
        newChoreography()
      }
    })
  }

  private def setFilePickedHandler(): Unit = {
    filePicker.setOnItemClickListener(new AdapterView.OnItemClickListener {
      def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        application.setChoreography(choreographyLoadedFrom(fileWithIndex(position)))
        editChoreography()
      }
    })
  }

  private def fileWithIndex(position: Int): File = {
    new File(choreographyFiles(position).getPath)
  }

  private def newChoreography(): Unit = promptForMusic()

  private def editChoreography(): Unit = startActivity(new Intent(this, classOf[EditChoreography]))

  private def setFilePickerItems(): Unit = {
    choreographyFiles = Persistence.withRoot(getFilesDir).choreographyFiles.asScala.toSeq
    val adapter: SimpleAdapter = new SimpleAdapter(this,
                                                   files.map(_.asJava).asJava,
                                                   android.R.layout.simple_list_item_2,
                                                   Array[String](Title, Description),
                                                   Array[Int](android.R.id.text1, android.R.id.text2))
    filePicker.setAdapter(adapter)
  }

  private def filePicker: ListView = findView(TR.filePicker)

  private def files: Seq[Map[String, String]] = choreographyFiles.map(toTitleAndDescriptionMap)

  private def promptForMusic(): Unit = {
    val path = "/"
    val musicSelection = new Intent(path)
    musicSelection.setType("audio/*")
    musicSelection.setAction(ACTION_GET_CONTENT)
    musicSelection.addCategory(CATEGORY_OPENABLE)
    startActivityForResult(createChooser(musicSelection, "Select music"), SelectMusicRequestCode)
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit =
    if (resultCode == Activity.RESULT_OK)
      requestCode match {
        case SelectMusicRequestCode => onMusicSelected(data)
      }

  private def onMusicSelected(selectionResult: Intent): Unit = {
    choreography.setMusicPath(selectionResult.getData.toString)
    startActivity(new Intent(this, classOf[EditChoreography]))
  }

  private def toTitleAndDescriptionMap(input: FileInfo): Map[String, String] =
    Map(Title       -> input.getTitle,
        Description -> input.getDescription)

  override def onCreateOptionsMenu(menu: Menu): Boolean = false
}
