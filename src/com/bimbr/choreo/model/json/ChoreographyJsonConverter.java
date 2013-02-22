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
        return new Choreography(45000);
    }
}
