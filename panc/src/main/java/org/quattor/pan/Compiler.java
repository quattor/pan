/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL$
 $Id: Compiler.java 3614 2008-08-20 15:53:05Z loomis $
 */

package org.quattor.pan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.quattor.pan.cache.BuildCache;
import org.quattor.pan.cache.CompileCache;
import org.quattor.pan.cache.Valid1Cache;
import org.quattor.pan.cache.Valid2Cache;
import org.quattor.pan.repository.SourceRepository;
import org.quattor.pan.tasks.Task;
import org.quattor.pan.tasks.TaskResult;

/**
 * Primary java interface for invoking the pan compiler. All external methods of
 * running the compiler (ant tasks, scripts, etc.) should make use of an
 * instance of this class.
 *
 * Instances of this class are thread-safe (the underlying implementation uses
 * threads in the compilation and build of machine templates). However, the
 * <code>process</code> method should only be invoked only by a single thread;
 * the method is synchronized to ensure this. The <code>submit</code> method
 * should be called only by tasks created internally by the compiler.
 *
 * @author loomis
 */
public class Compiler {

    /**
     * The version of this compiler.
     */
    public static final String version;

    // This block of code extracts the version from a resource file. Any error
    // will result in the value being set to "unknown".
    static {

        Properties defaults = new Properties();
        defaults.setProperty("version", "unknown");

        Properties values = new Properties(defaults);

        InputStream is = Compiler.class
                .getResourceAsStream("version.properties");

        try {
            values.load(is);
        } catch (IOException consumed) {
        } catch (NullPointerException consumed) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException consumed) {
                }
            }
        }

        version = values.getProperty("version");
    }

    private final TreeMap<TaskResult.ResultType, ThreadPoolExecutor> executors;

    /**
     * This value is used to synchronize the tasks running within the compiler.
     * Before each task is started, this counter must be incremented. The
     * compiler decrements the counter as tasks finish. When the counter reaches
     * zero, the processing stops.
     */
    private final AtomicInteger remainingTasks = new AtomicInteger(0);

    /**
     * This queue holds the results from all tasks. All tasks must be submitted
     * to this queue.
     */
    private final BlockingQueue<Future<? extends TaskResult>> resultsQueue = new LinkedBlockingQueue<Future<? extends TaskResult>>();

    /**
     * Build queue thread limit. The build queue must be treated specially
     * because object dependencies can deadlock the compiler. The number of
     * concurrent threads must be at least equal to the number of outstanding
     * object dependencies during the build.
     */
    private final AtomicInteger buildThreadLimit;
    private final Object buildThreadLock = new Object();

    /**
     * The initialization of this must be done when the instance is constructed
     * to avoid nasty questions about when the loggers get initialized.
     */
    private final CompilerStatistics stats;

    /**
     * This holds a reference to the compiler options. The options are immutable
     * and hence visible to all threads.
     */
    public final CompilerOptions options;

    /**
     * The complete set of absolute Files to process with the compiler.
     */
    private final Set<File> files;

    private final CompileCache ccache;

    private final BuildCache bcache;

    private final Valid1Cache v1cache;

    private final Valid2Cache v2cache;

    /**
     * Create a compiler object with the given options and that will process the
     * given templates (either by name or absolute path).
     *
     * @param options
     *            compiler options to use for the created compiler
     * @param objectNames
     *            template names to compile/build; these will be looked-up on
     *            the load path
     * @param tplFiles
     *            absolute file names of templates to process
     */
    public Compiler(CompilerOptions options, List<String> objectNames,
            Collection<File> tplFiles) {

        // Sanity check.
        assert (options != null);

        // Ensure that the logging has been initialized. This turns off the log
        // file, so any logging must be set prior to constructing a Compiler
        // object.
        CompilerLogging.setLogFile(null);

        // Initialize the statistics.
        stats = new CompilerStatistics();

        // All parameter checking is done as part of the CompilerOptions
        // object.
        this.options = options;

        // Create the final set of files to build.
        files = options.resolveFileList(objectNames, tplFiles);

        // Setup the template and object caches.
        ccache = new CompileCache(this);
        bcache = new BuildCache(this);
        v1cache = new Valid1Cache(this);
        v2cache = new Valid2Cache(this);

        // Setup the executors for the build. There is one for each stage of the
        // processing.
        // FIXME: FOR DEBUGGING PURPOSES ONLY
        //int nprocs = Runtime.getRuntime().availableProcessors();
        int nprocs = 1;
        executors = new TreeMap<TaskResult.ResultType, ThreadPoolExecutor>();
        for (TaskResult.ResultType t : TaskResult.ResultType.values()) {
            executors.put(t,
                    (ThreadPoolExecutor) Executors.newFixedThreadPool(nprocs));
        }

        // Must initialize the build thread limit to the number used above.
        buildThreadLimit = new AtomicInteger(nprocs);
    }

    /**
     * This is a convenience method which creates a compiler and then invokes
     * the <code>process</code> method.
     *
     * @param options
     *            compiler options to use for the created compiler
     * @param objectNames
     *            object template names to compile/build; these will be
     *            looked-up on the load path
     * @param tplFiles
     *            absolute file names of templates to process
     *
     * @return results from the compilation/build
     */
    public static CompilerResults run(CompilerOptions options,
            List<String> objectNames, Collection<File> tplFiles) {
        return (new Compiler(options, objectNames, tplFiles)).process();
    }

    /**
     * Extracts the version of the compiler and prints the value on the standard
     * output. This is useful for packaging of the compiler.
     *
     * @param args
     *            all arguments are ignored
     */
    public static void main(String[] args) {
        System.out.println(Compiler.version);
        // System.out.println("Pan language compiler (see http://quattor.org)");
        // System.out.println("Version: " + Compiler.version);
        // System.out.println("License: Apache 2.0
        // (http://www.apache.org/licenses/LICENSE-2.0)");
    }

    /**
     * Ensures that the number of threads in the build pool is at least as large
     * as the number given. Because object templates can have dependencies on
     * each other, it is possible to deadlock the compilation with a rigidly
     * fixed build queue. To avoid this, allow the build queue limit to be
     * increased.
     *
     * @param minLimit
     *            minimum build queue limit
     */
    public void ensureMinimumBuildThreadLimit(int minLimit) {
        if (buildThreadLimit.get() < minLimit) {
            synchronized (buildThreadLock) {
                buildThreadLimit.set(minLimit);
                ThreadPoolExecutor buildExecutor = executors
                        .get(TaskResult.ResultType.BUILD);

                // Must set both the maximum and the core limits. If the core is
                // not set, then the thread pool will not be forced to expand to
                // the minimum number of threads required.
                buildExecutor.setMaximumPoolSize(minLimit);
                buildExecutor.setCorePoolSize(minLimit);
            }
        }
    }

    /**
     * Process the templates referenced by the CompilerOptions object used to
     * initialize this instance. This will run through the complete compiling,
     * building, and validation stages as requested. This method should only be
     * invoked once per instance.
     *
     * @return the statistics of the compilation and any exceptions which were
     *         thrown
     */
    public synchronized CompilerResults process() {

        // Create the list to hold all of the exceptions.
        Set<Throwable> exceptions = new TreeSet<Throwable>(
                new ThrowableComparator());

        long start = new Date().getTime();

        stats.setFileCount(files.size());

        // Trigger the compilation of the templates via the template cache. If
        // no building is going on, then the compile() method is used which
        // doesn't actually save the templates. This reduces drastically the
        // memory requirements.
        if (options.formatters.size() > 0) {
            for (File f : files) {
                ccache.retrieve(f.getAbsolutePath(), false);
            }
        } else {
            // FIXME: Determine if this does the correct thing (nothing) for a syntax check.
            for (File f : files) {
                if (!f.isAbsolute() && options.annotationBaseDirectory != null) {
                    f = new File(options.annotationBaseDirectory, f.getPath());
                }
                ccache.compile(f.getAbsolutePath());
            }
        }

        // Now continually loop through the result queue until we have all
        // of the results.
        while (remainingTasks.get() > 0) {
            try {
                Future<? extends TaskResult> future = resultsQueue.take();
                try {
                    stats.incrementFinishedTasks(future.get().type);
                } catch (ExecutionException ee) {
                    exceptions.add(ee.getCause());
                }
                remainingTasks.decrementAndGet();
                stats.updateMemoryInfo();
            } catch (InterruptedException consumed) {
            }
        }

        // Shutdown the executors. In certain environments (e.g. eclipse) the
        // required "modifyThread" permission may not have been granted. Not
        // having this permission may cause a thread leak.
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(new RuntimePermission("modifyThread"));
            }

            // We've got the correct permission, so tell all of the executors to
            // shutdown.
            for (TaskResult.ResultType t : TaskResult.ResultType.values()) {
                executors.get(t).shutdown();
            }

        } catch (SecurityException se) {

            // Emit a warning about the missing permission.
            System.err.println("WARNING: missing modifyThread permission");

        }

        // Finalize the statistics.
        long end = new Date().getTime();
        stats.setBuildTime(end - start);

        return new CompilerResults(stats, exceptions);
    }

    /**
     * Returns a reference to the compile cache used to store compiled
     * templates.
     *
     * @return reference to compile cache
     */
    public CompileCache getCompileCache() {
        return ccache;
    }

    /**
     * Returns a reference to objects (machine profiles) which have already been
     * built. This allows cross-validation of templates.
     *
     * @return reference to object cache
     */
    public BuildCache getBuildCache() {
        return bcache;
    }

    public Valid1Cache getValid1Cache() {
        return v1cache;
    }

    public Valid2Cache getValid2Cache() {
        return v2cache;
    }

    public SourceRepository getSourceRepository() {
        return options.sourceRepository;
    }

    /**
     * Submits a task to one of the compiler's task queues for processing.
     * Although public, this method should only be called by tasks started by
     * the compiler itself.
     *
     * @param task
     *            task to run on one of the compiler's task queues
     */
    public void submit(Task<? extends TaskResult> task) {

        // Increment the counter which indicates the number of results to
        // expect. This must be done BEFORE actually submitting the task for
        // execution.
        remainingTasks.incrementAndGet();

        // Increment the statistics and put the task on the correct queue.
        stats.incrementStartedTasks(task.resultType);
        executors.get(task.resultType).submit(task);

        // Make sure that the task gets added to the results queue.
        resultsQueue.add(task);
        stats.updateMemoryInfo();
    }

    /**
     * This class orders Throwables allowing duplicates to be removed. It orders
     * them based on their system identity hash code. The implementation will
     * not handle null values gracefully and will throw a NPE.
     *
     * @author loomis
     *
     */
    @SuppressWarnings("serial")
    public static class ThrowableComparator implements Serializable,
            Comparator<Throwable> {

        public int compare(Throwable o1, Throwable o2) {
            if (o1 == o2) {
                return 0;
            } else {
                int hc1 = System.identityHashCode(o1);
                int hc2 = System.identityHashCode(o2);
                if (hc1 < hc2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
