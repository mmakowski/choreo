package com.bimbr.choreo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.bimbr.choreo.R;

/**
 * The welcome screen.
 *
 * @author mmakowski
 */
public class Welcome extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        setHandlers();
    }

    private void setHandlers() {
        setNewChoreographyButtonHandler();
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

    private void newChoreography() {
        // TODO: settings first
        final Intent intent = new Intent(this, EditChoreography.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return false;
    }
}
