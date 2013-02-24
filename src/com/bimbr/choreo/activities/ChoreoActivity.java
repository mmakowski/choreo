package com.bimbr.choreo.activities;

import android.app.Activity;

import com.bimbr.choreo.app.ChoreoApplication;
import com.bimbr.choreo.model.Choreography;

/**
 * Functionality common to all Choreo activities.
 *
 * @author mmakowski
 */
public abstract class ChoreoActivity extends Activity {
    protected ChoreoApplication application() {
        return (ChoreoApplication) getApplication();
    }

    protected Choreography choreography() {
        return application().getChoreography();
    }
}
