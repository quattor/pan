package org.quattor.pan;

import static org.quattor.pan.CompilerLogging.LoggingType.MEMORY;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quattor.pan.tasks.TaskResult;

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

	private int fileCount;

	private final AtomicLong heapUsed;

	private final AtomicLong heapTotal;

	private final AtomicLong nonHeapUsed;

	private final AtomicLong nonHeapTotal;

	private final Map<TaskResult.ResultType, AtomicInteger> startedTasks;

	private final Map<TaskResult.ResultType, AtomicInteger> doneTasks;

	/**
	 * Creates an object to keep track of statistics during the run of the pan
	 * compiler.
	 */
	public CompilerStatistics() {
		buildTime = -1L;

		// Create maps containing the counters for the types of tasks.
		startedTasks = new TreeMap<TaskResult.ResultType, AtomicInteger>();
		doneTasks = new TreeMap<TaskResult.ResultType, AtomicInteger>();

		// Initialize all of the counters to zero.
		for (TaskResult.ResultType t : TaskResult.ResultType.values()) {
			startedTasks.put(t, new AtomicInteger(0));
			doneTasks.put(t, new AtomicInteger(0));
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
	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	/**
	 * Increase the count of the number of tasks of the given type that have
	 * been started.
	 * 
	 * @param type
	 *            type of task that was started
	 */
	public void incrementStartedTasks(TaskResult.ResultType type) {
		startedTasks.get(type).incrementAndGet();
	}

	/**
	 * Increase the count of the number of tasks of the given type that have
	 * finished successfully.
	 * 
	 * @param type
	 *            type of task that finished
	 */
	public void incrementFinishedTasks(TaskResult.ResultType type) {
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
	public String getResults(int totalErrors) {
		StringBuilder sb = new StringBuilder();

		// Number of templates to process.
		sb.append(fileCount);
		sb.append(" templates\n");

		// Compilation statistics.
		sb.append(doneTasks.get(TaskResult.ResultType.COMPILED).get());
		sb.append("/");
		sb.append(startedTasks.get(TaskResult.ResultType.COMPILED).get());
		sb.append(" compiled, ");

		// Output statistics.
		sb.append(doneTasks.get(TaskResult.ResultType.XML).get());
		sb.append("/");
		sb.append(startedTasks.get(TaskResult.ResultType.XML).get());
		sb.append(" xml, ");
		sb.append(doneTasks.get(TaskResult.ResultType.DEP).get());
		sb.append("/");
		sb.append(startedTasks.get(TaskResult.ResultType.DEP).get());
		sb.append(" dep\n");

		// Errors.

		// General statistics. The shift by 20 bits divides by 1024^2 to convert
		// from number of bytes to number of megabytes.
		sb.append(totalErrors);
		sb.append(" errors, ");
		sb.append(buildTime);
		sb.append(" ms, ");
		sb.append(heapUsed.get() >> 20);
		sb.append(" MB/");
		sb.append(heapTotal.get() >> 20);
		sb.append(" MB heap, ");
		sb.append(nonHeapUsed.get() >> 20);
		sb.append(" MB/");
		sb.append(nonHeapTotal.get() >> 20);
		sb.append(" MB nonheap\n");

		return sb.toString();
	}

}
