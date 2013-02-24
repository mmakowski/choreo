package com.bimbr.choreo.model.json;

import com.bimbr.choreo.model.Choreography;

/**
 * Converts {@link Choreography} from and to JSON representation.
 *
 * @author mmakowski
 */
public class ChoreographyJsonConverter {
    public String toJson(final Choreography choreography) {
        return "{\"what\": \"TODO\"}";
    }

    public Choreography fromJson(final String json) {
        final Choreography choreography = new Choreography("dupa");
        // TODO: load from JSON
        choreography.setMusicPath("content://media/external/audio/media/14");
        return choreography;
    }
}
