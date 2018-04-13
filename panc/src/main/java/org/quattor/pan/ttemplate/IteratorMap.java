package org.quattor.pan.ttemplate;

import java.util.Map;
import java.util.TreeMap;

import org.quattor.pan.dml.data.Resource;

/**
 * This class contains a map that holds the currently used iterators. The system
 * hash code value of the resource is used as the key into the map. Only one
 * iterator can be used for a given resource at a time.
 * 
 * @author loomis
 * 
 */
public class IteratorMap {

	private Map<Integer, Resource.Iterator> map;

	/**
	 * Create an instance that contains no iterator mappings initially.
	 */
	public IteratorMap() {
		map = new TreeMap<Integer, Resource.Iterator>();
	}

	/**
	 * Lookup the iterator associated with the given resource. If there is no
	 * iterator, null is returned.
	 * 
	 * @param resource
	 *            resource to use for lookup
	 * 
	 * @return Resource.Iterator iterator associated with given resource or null
	 *         if no mapping exists
	 */
	public Resource.Iterator get(Resource resource) {
		Integer key = Integer.valueOf(System.identityHashCode(resource));
		return map.get(key);
	}

	/**
	 * Associate the iterator to the given resource. If the iterator is null,
	 * then the mapping is removed.
	 * 
	 * @param resource
	 *            resource to associate the iterator to
	 * @param iterator
	 *            iterator to associate to the resource; if null mapping is
	 *            removed
	 */
	public void put(Resource resource, Resource.Iterator iterator) {

		assert (resource != null);

		Integer key = Integer.valueOf(System.identityHashCode(resource));
		if (iterator != null) {
			map.put(key, iterator);
		} else {
			map.remove(key);
		}
	}

}
