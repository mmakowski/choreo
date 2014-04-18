package com.bimbr.choreo.persistence;

/**
 * Information about a file used to display a picker.
 *
 * @author mmakowski
 */
public class FileInfo {
    private final String title;
    private final String description;
    private final String path;

    public FileInfo(final String title, final String description, final String path) {
        super();
        this.title = title;
        this.description = description;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        return path;
    }
}
