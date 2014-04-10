package com.bimbr.choreo.model.json;

import com.bimbr.choreo.model.Choreography;
import com.google.gson.Gson;

/**
 * Converts {@link Choreography} from and to JSON representation.
 *
 * @author mmakowski
 */
public class ChoreographyJsonConverter {
    public String toJson(final Choreography choreography) {
        final Gson gson = new Gson();
        return gson.toJson(choreography);
    }

    public Choreography fromJson(final String json) {
        final Gson gson = new Gson();
        return gson.fromJson(json, Choreography.class);
    }
}
