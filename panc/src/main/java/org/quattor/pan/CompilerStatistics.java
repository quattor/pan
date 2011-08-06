package org.quattor.pan;

import static org.quattor.pan.CompilerLogging.LoggingType.MEMORY;
import static org.quattor.pan.tasks.TaskResult.ResultType.ANNOTATION;
import static org.quattor.pan.tasks.TaskResult.ResultType.COMPILED;
import static org.quattor.pan.tasks.TaskResult.ResultType.DEP;
import static org.quattor.pan.tasks.TaskResult.ResultType.XML;
import static org.quattor.pan.utils.MessageUtils.MSG_STATISTICS_TEMPLATE;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.tasks.TaskResult.ResultType;
import org.quattor.pan.utils.MessageUtils;

/**
 * Provides statistics about a run of the pan compiler. These statistics can be
 * used to judge the performance of the compiler with different options.
 * 
 * @author loomis
 * 
 */
public class CompilerStatistics {

	private final static Logger memoryLogger = MEMORY.logger();

	private long buildTime;

	private long fileCount;

	private final AtomicLong heapUsed;

	private final AtomicLong heapTotal;

	private final AtomicLong nonHeapUsed;

	private final AtomicLong nonHeapTotal;

	private final Map<ResultType, AtomicLong> startedTasks;

	private final Map<ResultType, AtomicLong> doneTasks;

	/**
	 * Creates an object to keep track of statistics during the run of the pan
	 * compiler.
	 */
	public CompilerStatistics() {
		buildTime = -1L;

		// Create maps containing the counters for the types of tasks.
		startedTasks = new TreeMap<ResultType, AtomicLong>();
		doneTasks = new TreeMap<ResultType, AtomicLong>();

		// Initialize all of the counters to zero.
		for (ResultType t : ResultType.values()) {
			startedTasks.put(t, new AtomicLong(0));
			doneTasks.put(t, new AtomicLong(0));
		}

		heapUsed = new AtomicLong(-1);
		heapTotal = new AtomicLong(-1);
		nonHeapUsed = new AtomicLong(-1);
		nonHeapTotal = new AtomicLong(-1);

		// Update the memory information at least once so we don't leave the
		// default values.
		updateMemoryInfo();
	}

	/**
	 * Define the total (clock) time for a complete build in milliseconds.
	 * 
	 * @param buildTime
	 *            wall-clock build time
	 */
	public void setBuildTime(long buildTime) {
		this.buildTime = buildTime;
	}

	/**
	 * The total number of files which were processed by the compiler.
	 * 
	 * @param fileCount
	 *            number of processed files
	 */
	public void setFileCount(long fileCount) {
		this.fileCount = fileCount;
	}

	/**
	 * Increase the count of the number of tasks of the given type that have
	 * been started.
	 * 
	 * @param type
	 *            type of task that was started
	 */
	public void incrementStartedTasks(ResultType type) {
		startedTasks.get(type).incrementAndGet();
	}

	/**
	 * Increase the count of the number of tasks of the given type that have
	 * finished successfully.
	 * 
	 * @param type
	 *            type of task that finished
	 */
	public void incrementFinishedTasks(ResultType type) {
		doneTasks.get(type).incrementAndGet();
	}

	/**
	 * Take a snapshot of the current memory usage of the JVM and update the
	 * high-water marks.
	 */
	public void updateMemoryInfo() {

		MemoryMXBean meminfo = ManagementFactory.getMemoryMXBean();

		MemoryUsage usage = meminfo.getHeapMemoryUsage();
		long _heapUsed = usage.getUsed();
		updateMaximum(heapUsed, _heapUsed);
		updateMaximum(heapTotal, usage.getMax());

		usage = meminfo.getNonHeapMemoryUsage();
		updateMaximum(nonHeapUsed, usage.getUsed());
		updateMaximum(nonHeapTotal, usage.getMax());

		// Log the memory usage if requested. Check the log level before logging
		// to minimize object creation overheads during preparation of the call
		// parameters.
		if (memoryLogger.isLoggable(Level.INFO)) {
			memoryLogger.log(Level.INFO, "MEM", new Object[] { _heapUsed });
		}
	}

	/**
	 * Compares the current value against the value of the counter and updates
	 * the counter if the current value is greater. This is done in a way to
	 * ensure that no updates are lost.
	 * 
	 * @param counter
	 *            AtomicLong counter to update
	 * @param currentValue
	 *            current value of the counter
	 */
	private static void updateMaximum(AtomicLong counter, long currentValue) {
		boolean unset = true;
		long counterValue = counter.get();
		while ((currentValue > counterValue) && unset) {
			unset = !counter.compareAndSet(counterValue, currentValue);
			counterValue = counter.get();
		}
	}

	/**
	 * Generates a terse String representation of the statistics.
	 * 
	 * @return statistics as a String value
	 */
	public String getResults(long totalErrors) {

		Object[] info = { fileCount, doneTasks.get(COMPILED).get(),
				startedTasks.get(COMPILED).get(),
				doneTasks.get(ANNOTATION).get(),
				startedTasks.get(ANNOTATION).get(), doneTasks.get(XML).get(),
				startedTasks.get(XML).get(), doneTasks.get(DEP).get(),
				startedTasks.get(DEP).get(), totalErrors, buildTime,
				convertToMB(heapUsed.get()), convertToMB(heapTotal.get()),
				convertToMB(nonHeapUsed.get()), convertToMB(nonHeapTotal.get()) };

		return MessageUtils.format(MSG_STATISTICS_TEMPLATE, info);
	}

	private static Long convertToMB(long value) {
		return Long.valueOf(value >> 20);
	}
}
