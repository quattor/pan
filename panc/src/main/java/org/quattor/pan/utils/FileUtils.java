package org.quattor.pan.utils;

import java.io.File;

public class FileUtils {

    private FileUtils() {

    }

    // A utility to localize the filename. This actually only does something
    // when on a system that doesn't use the normal unix convention for
    // filenames.
    // 
    // NOTE: This should NEVER use the function replaceAll because a String
    // containing a single backslash is interpreted specially by that method.
    //
    // See SF Bug #3196167 for more information about this.
    //
    public static String localizeFilename(String filename) {
        return filename.replace('/', File.separatorChar);
    }

}
