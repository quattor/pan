package org.quattor.pan.dml.data;

import static org.quattor.pan.utils.MessageUtils.MSG_ILLEGAL_WRITE_TO_PROTECTED_HASH;

import java.util.Set;

import org.quattor.pan.exceptions.CompilerError;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.utils.Range;
import org.quattor.pan.utils.Term;

public class ProtectedHashResource extends HashResource {

	private static final long serialVersionUID = 6050739954052910954L;

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
		return baseHash.get(key);
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
	public String locateUndefinedElement() {
		return baseHash.locateUndefinedElement();
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

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof ProtectedHashResource) {
			result = baseHash.equals(((ProtectedHashResource) o).baseHash);
		}
		return result;
	}

	@Override
	public String toString() {
		return baseHash.toString();
	}

	@Override
	public Resource.Iterator iterator() {
		return baseHash.iterator();
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
