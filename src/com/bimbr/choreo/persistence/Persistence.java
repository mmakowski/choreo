package com.bimbr.choreo.persistence;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.io.Files.write;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.util.Log;

import com.bimbr.choreo.model.Choreography;
import com.bimbr.choreo.model.json.ChoreographyJsonConverter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

/**
 * Functions responsible for reading and writing files.
 *
 * @author mmakowski
 */
public class Persistence {
    private static final String LOG_TAG = "Persistence";
    private final File choreographyDir;

    /**
     * @param rootDir root of application's data files
     * @return a persistance instance for given root directory
     */
    public static Persistence withRoot(final File rootDir) {
        return new Persistence(rootDir);
    }

    private Persistence(final File rootDir) {
        choreographyDir = new File(rootDir, "choreographies");
    }

    /**
     * Writes supplied choreography to a file whose name is derived from choreography name.
     *
     * @param choreography the choreography to write
     */
    public void writeChoreography(final Choreography choreography) {
        final String json = new ChoreographyJsonConverter().toJson(choreography);
        ensureExists(choreographyDir);
        final File file = new File(choreographyDir, "test.choreo"); // TODO: derive from name
        try {
            write(json, file, UTF_8);
            Log.d(LOG_TAG, "saved choreography to " + file.getAbsolutePath());
        } catch (final IOException e) {
            throw new RuntimeException("save of " + file.getAbsolutePath() + " failed", e);
        }
    }

    private static void ensureExists(final File directory) {
        if (!directory.exists()) {
            if (!directory.mkdirs()) throw new RuntimeException("unable to create directory " + directory.getAbsolutePath());
        }
    }

    /**
     * @return a list of choreography file information
     */
    public Iterable<FileInfo> choreographyFiles() {
        ensureExists(choreographyDir);
        return transform(ImmutableList.copyOf(choreographyDir.listFiles()), toChoreographyFileInfo);
    }

    private static final Function<File, FileInfo> toChoreographyFileInfo = new Function<File, FileInfo>() {
        @Override
        public FileInfo apply(final File file) {
            return new FileInfo(file.getName(), new Date(file.lastModified()).toString(), file.getAbsolutePath());
        }};

    /**
     * @param file file that contains choreography
     *
     * @return choreography loaded from supplied file
     */
    public static Choreography choreographyLoadedFrom(final File file) {
        try {
            return new ChoreographyJsonConverter().fromJson(Files.toString(file, UTF_8));
        } catch (final IOException e) {
            throw new RuntimeException("error loading choreography from " + file.getAbsolutePath(), e);
        }
    }

}
