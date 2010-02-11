package org.quattor.pan.utils;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to cache file modification times. This class minimizes the number of
 * disk accesses to determine the modification times of files. This improves the
 * performance when the same file is requested many times.
 * 
 * @author loomis
 * 
 */
public class FileStatCache {

	final private ConcurrentHashMap<File, Long> cachedTimes;

	public FileStatCache() {
		cachedTimes = new ConcurrentHashMap<File, Long>();
	}

	public boolean exists(File file) {
		return (getModificationTime(file) > 0L);
	}

	public boolean isMissingOrModifiedAfter(File file, long targetTime) {
		long modtime = getModificationTime(file);
		return ((modtime == 0L) || (modtime > targetTime));
	}

	public boolean isMissingOrModifiedBefore(File file, long targetTime) {
		long modtime = getModificationTime(file);
		return ((modtime == 0L) || (modtime < targetTime));
	}

	public long getModificationTime(File file) {

		Long modtime = cachedTimes.get(file);

		if (modtime == null) {
			cachedTimes.putIfAbsent(file, Long.valueOf(file.lastModified()));
			modtime = cachedTimes.get(file);
		}

		return modtime.longValue();
	}
}
