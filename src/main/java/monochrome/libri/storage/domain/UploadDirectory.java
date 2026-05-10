package monochrome.libri.storage.domain;

import java.util.Arrays;

public enum UploadDirectory {
    PROFILES("profiles"),
    BOOKS("books");

    private final String path;

    UploadDirectory(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public static boolean supports(String value) {
        return Arrays.stream(values())
                .anyMatch(directory -> directory.path.equals(value));
    }
}
