package com.bimbr.choreo.app;

import android.app.Application;

import com.bimbr.choreo.model.Choreography;

/**
 * An object that holds global application state.
 *
 * @author mmakowski
 */
public class ChoreoApplication extends Application {
    private Choreography choreography = new Choreography("New Choreography");

    public Choreography getChoreography() {
        return choreography;
    }

    public void setChoreography(final Choreography choreography) {
        this.choreography = choreography;
    }
}
