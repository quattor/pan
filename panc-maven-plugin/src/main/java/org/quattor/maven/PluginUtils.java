package org.quattor.maven;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.TreeSet;

public class PluginUtils {

    private PluginUtils() {

    }

    public static Set<File> collectPanSources(File directory) {
        Set<File> sources = new TreeSet<File>();
        collectPanSources(sources, directory);
        return sources;
    }

    public static void collectPanSources(Set<File> sources, File directory) {

        if (directory.exists()) {

            for (File file : panSourceFiles(directory)) {
                sources.add(file);
            }

            for (File subdir : subdirectories(directory)) {
                collectPanSources(sources, subdir);
            }
        }

    }

    public static File[] panSourceFiles(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            return directory.listFiles(PanSourceFileFilter.getInstance());
        } else {
            return new File[0];
        }
    }

    public static File[] subdirectories(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            return directory.listFiles(DirectoryFileFilter.getInstance());
        } else {
            return new File[0];
        }
    }

    public static class DirectoryFileFilter implements FileFilter {

        public static DirectoryFileFilter singleton = new DirectoryFileFilter();

        private DirectoryFileFilter() {

        }

        public static DirectoryFileFilter getInstance() {
            return singleton;
        }

        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }

    public static class PanSourceFileFilter implements FileFilter {

        public static PanSourceFileFilter singleton = new PanSourceFileFilter();

        private PanSourceFileFilter() {

        }

        public static PanSourceFileFilter getInstance() {
            return singleton;
        }

        public boolean accept(File pathname) {
            String fname = pathname.getName();
            return fname.endsWith(".pan") || fname.endsWith(".tpl");
        }
    }
}
