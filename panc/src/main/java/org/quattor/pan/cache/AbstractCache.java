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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/cache/ObjectCache.java $
 $Id: ObjectCache.java 3350 2008-07-12 13:21:38Z loomis $
 */

package org.quattor.pan.cache;

import static org.quattor.pan.utils.MessageUtils.MSG_CANCELLED_THREAD;
import static org.quattor.pan.utils.MessageUtils.MSG_INTERRUPTED_THREAD;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.quattor.pan.Compiler;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.tasks.Task;
import org.quattor.pan.tasks.TaskResult;
import org.quattor.pan.utils.ExceptionUtils;

/**
 * A cache that maps a key, typically an object or template name, to an
 * associated task. The task is one that compiles, builds, or validates the
 * named entity. Any attempt to retrieve something from the cache will return
 * with the associated task, creating a new task if one doesn't already exist.
 * The client can wait on the task for the entity to become available.
 * 
 * @author loomis
 * 
 */
public abstract class AbstractCache<T extends TaskResult> {

	/**
	 * Reference to the compiler associated with this cache.
	 */
	protected final Compiler compiler;

	/**
	 * The cache itself should not need to be referenced by the implementing
	 * classes and is marked private.
	 */
	private final ConcurrentHashMap<String, Task<T>> cache;

	/**
	 * Initializes the internal reference to the associated
	 * <code>Compiler</code> and creates a cache with the given initial size.
	 * 
	 * @param compiler
	 *            <code>Compiler</code> related to this cache
	 * @param size
	 *            number of initial entries
	 */
	protected AbstractCache(Compiler compiler, int size) {

		// Sanity checks on arguments.
		assert (compiler != null);
		assert (size > 0);

		// Copy information and initialize.
		this.compiler = compiler;
		cache = new ConcurrentHashMap<String, Task<T>>(size);
	}

	/**
	 * Returns a <code>Future</code> corresponding to the named object template;
	 * this will add a new entry if one does not yet exist for the given key.
	 * 
	 * @param key
	 *            key to use for the lookup, typically an object or template
	 *            name
	 * @param executeInline
	 *            If the client is going to wait for the task to complete, then
	 *            it can indicate that the task can be run inline.
	 * 
	 * @return <code>Future</code> that references the compiled template
	 * 
	 */
	public Future<T> retrieve(String key, boolean executeInline) {

		// Sanity checks.
		assert (key != null);

		// Must keep track of whether a new task was created, so that it can
		// possibly be submitted to an execution queue.
		boolean createdTask = false;

		// Extract the task related to this object from the cache.
		Task<T> task = cache.get(key);

		// Oops, the associated task did not exist.
		if (task == null) {

			// Create a new task for the given object.
			Task<T> newTask = createTask(key);

			// Atomically insert this into the cache.
			Task<T> existingEntry = cache.putIfAbsent(key, newTask);

			if (existingEntry == null) {

				// The new task was actually inserted into the cache. Indicate
				// that the a new task was created and set value for treatment
				// below.
				createdTask = true;
				task = newTask;
			} else {

				// Unlucky; another thread beat us to creating the task. Use the
				// task created by the other thread, throwing away the one we
				// created above.
				task = existingEntry;
			}

		}

		if (executeInline) {

			// The client indicated that it will wait for the result and it
			// is OK to try to build this task inline.
			task.run();

		} else {

			// If we created a task and we're not going to run it immediately,
			// make sure it gets submitted to an execution queue.
			if (createdTask) {
				compiler.submit(task);
			}

		}

		return task;
	}

	/**
	 * A convenience method that will retrieve (or create) the task associated
	 * with the key, wait for a result to be ready, and process any thrown
	 * exceptions.
	 * 
	 * @param key
	 *            Key for the associated task, usually an object or template
	 *            name
	 * @return T the result associated with the given key
	 * 
	 * @throws RuntimeException
	 * 
	 * @throws ValidationException
	 * 
	 */
	public T waitForResult(String key) throws RuntimeException,
			ValidationException {

		// Pull out the Future associated with the key.
		Future<T> future = retrieve(key, true);

		// Now wait for the result and process any exceptions.
		T result = null;
		try {
			result = future.get();
		} catch (InterruptedException ie) {
			throw EvaluationException.create(MSG_INTERRUPTED_THREAD, key);
		} catch (CancellationException ce) {
			throw EvaluationException.create(MSG_CANCELLED_THREAD, key);
		} catch (ExecutionException ee) {
			throw ExceptionUtils.launder(ee);
		}

		return result;
	}

	/**
	 * Returns directly the <code>Future</code> associated with the key or null
	 * if an entry does not exist. This method will not create a task for a
	 * non-existent entry. This method is primarily used for testing.
	 * 
	 * @param key
	 *            Key to use for the entity lookup.
	 * 
	 * @return <code>Future</code> for the named template or null if it is not
	 *         in the cache
	 */
	public Future<T> retrieve(String key) {
		assert (key != null);
		return cache.get(key);
	}

	/**
	 * Creates a new task to be put into the cache, if necessary. Concrete
	 * implementations must supply a method that generates a task that will
	 * return the appropriate type.
	 * 
	 * @param key
	 *            Key used to generate the associated task.
	 * 
	 * @return new task associated with the given key
	 * 
	 */
	protected abstract Task<T> createTask(String key);

	/**
	 * Subclasses must return the executor queue type for the particular class.
	 * 
	 * @return executor queue type to use for created tasks
	 */
	protected abstract TaskResult.ResultType getExecutorQueueType();

}
