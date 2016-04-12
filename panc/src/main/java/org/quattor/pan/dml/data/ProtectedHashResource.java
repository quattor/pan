package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_WRITE_TO_PROTECTED_HASH;

import java.util.Map;
import java.util.Set;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;

public class ProtectedHashResource extends HashResource {

	private final HashResource baseHash;

	public ProtectedHashResource(HashResource baseHash) {
		this.baseHash = baseHash;
	}

	@Override
	public Element duplicate() {
		return baseHash.duplicate();
	}

	@Override
	public Element get(Term key) throws InvalidTermException {
		final Element value = baseHash.get(key);
		if (value != null) {
			return value.protect();
		}
		return value;
	}

	@Override
	public Element put(Term key, Element newValue) throws InvalidTermException {
		throw CompilerError.create(MSG_ILLEGAL_WRITE_TO_PROTECTED_HASH);
	}

	@Override
	public int size() {
		return baseHash.size();
	}

	@Override
	public Set<Term> keySet() {
		return baseHash.keySet();
	}

	@Override
	public void checkRange(Range range) throws ValidationException {
		baseHash.checkRange(range);
	}

	@Override
	public Element protect() {
		return this;
	}

	@Override
	public int hashCode() {
		return baseHash.hashCode();
	}

	// As this class is just a wrapper around another HashResource, the
	// superclass' equals method will work as intended.
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	/**
	 * Override this method to return the map from the base hash. This allows
	 * use of the superclass' equals method.
	 *
	 * @return backing map for hash resource
	 */
	@Override
	protected Map<String, Element> getBackingMap() {
		return baseHash.getBackingMap();
	}

	@Override
	public String toString() {
		return baseHash.toString();
	}

	@Override
	public Resource.Iterator iterator() {
		return baseHash.protectedIterator();
	}

	@Override
	public Resource.Iterator protectedIterator() {
		return baseHash.protectedIterator();
	}

	@Override
	public boolean isProtected() {
		return true;
	}

	@Override
	public void rput(Term[] terms, int index, Element value)
			throws InvalidTermException {
		throw CompilerError.create(MSG_ILLEGAL_WRITE_TO_PROTECTED_HASH);
	}

	@Override
	public Element writableCopy() {
		return new HashResource(baseHash);
	}

}
