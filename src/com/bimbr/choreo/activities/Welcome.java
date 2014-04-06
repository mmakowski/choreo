package com.bimbr.choreo.activities;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.CATEGORY_OPENABLE;
import static android.content.Intent.createChooser;
import static com.bimbr.choreo.persistence.Persistence.choreographyLoadedFrom;
import static com.google.common.collect.Iterables.transform;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.bimbr.choreo.R;
import com.bimbr.choreo.persistence.FileInfo;
import com.bimbr.choreo.persistence.Persistence;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * The welcome screen.
 *
 * @author mmakowski
 */
public class Welcome extends ChoreoActivity {
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "desc";
    // TODO: move to settings
    private static final int SELECT_MUSIC_REQUEST_CODE = 1;

    private List<FileInfo> choreographyFiles;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        setHandlers();
        setFilePickerItems();
    }

    private void setHandlers() {
        setNewChoreographyButtonHandler();
        setFilePickedHandler();
    }

    private void setNewChoreographyButtonHandler() {
        final Button newChoreographyButton = (Button) findViewById(R.id.newChoreography);
        newChoreographyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                newChoreography();
            }
        });
    }

    private void setFilePickedHandler() {
        filePicker().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                application().setChoreography(choreographyLoadedFrom(fileWithIndex(position)));
                editChoreography();
            }});

    }

    private File fileWithIndex(final int position) {
        return new File(choreographyFiles.get(position).getPath());
    }

    private void newChoreography() {
        // TODO: settings activity
        promptForMusic();
    }

    private void editChoreography() {
        final Intent intent = new Intent(this, EditChoreography.class);
        startActivity(intent);
    }

    private void setFilePickerItems() {
        choreographyFiles = ImmutableList.copyOf(Persistence.withRoot(getFilesDir()).choreographyFiles());
        final SimpleAdapter adapter = new SimpleAdapter(this,
                                                        files(),
                                                        android.R.layout.simple_list_item_2,
                                                        new String[] { TITLE,             DESCRIPTION },
                                                        new int[]    {android.R.id.text1, android.R.id.text2 });
        filePicker().setAdapter(adapter);
    }

    private ListView filePicker() {
        return (ListView) findViewById(R.id.filePicker);
    }

    private List<Map<String, String>> files() {
        return ImmutableList.copyOf(transform(choreographyFiles, toTitleAndDescriptionMap));
    }

    private static final Function<FileInfo, Map<String, String>> toTitleAndDescriptionMap = new Function<FileInfo, Map<String, String>>() {
        @Override
        public Map<String, String> apply(final FileInfo input) {
            return ImmutableMap.of(TITLE,       input.getTitle(),
                                   DESCRIPTION, input.getDescription());
        }};

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return false;
    }

    // TODO: move to settings:

    private void promptForMusic() {
        final String path = "/";
        final Intent musicSelection = new Intent(path);
        musicSelection.setType("audio/*");
        musicSelection.setAction(ACTION_GET_CONTENT);
        musicSelection.addCategory(CATEGORY_OPENABLE);
        startActivityForResult(createChooser(musicSelection, "Select music"), SELECT_MUSIC_REQUEST_CODE);
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
        choreography().setMusicPath(selectionResult.getData().toString());
        startActivity(new Intent(this, EditChoreography.class));
    }
}
