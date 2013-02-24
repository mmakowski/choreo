package com.bimbr.choreo.persistence;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageState;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.io.Files.write;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.os.Environment;
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
    private static final File ROOT_DIR = new File(Environment.getExternalStorageDirectory(), "Choreo");
    private static final File CHOREOGRAPHY_DIR = new File(ROOT_DIR, "choreographies");

    /**
     * Writes supplied choreography to a file whose name is derived from choreography name.
     *
     * @param choreography the choreography to write
     */
    public static void writeChoreography(final Choreography choreography) {
        checkState(sdCardIsWriteable(), "SD card is not writeable");
        final String json = new ChoreographyJsonConverter().toJson(choreography);
        ensureExists(CHOREOGRAPHY_DIR);
        final File file = new File(CHOREOGRAPHY_DIR, "test.choreo");
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
    public static Iterable<FileInfo> choreographyFiles() {
        ensureExists(CHOREOGRAPHY_DIR);
        return transform(ImmutableList.copyOf(CHOREOGRAPHY_DIR.listFiles()), toChoreographyFileInfo);
    }

    private static final Function<File, FileInfo> toChoreographyFileInfo = new Function<File, FileInfo>() {
        @Override
        public FileInfo apply(final File file) {
            return new FileInfo(file.getName(), new Date(file.lastModified()).toString(), file.getAbsolutePath());
        }};

    private static boolean sdCardIsWriteable() {
        return MEDIA_MOUNTED.equals(getExternalStorageState());
    }

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
