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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/Term.java $
 $Id: Term.java 3107 2008-04-07 07:03:42Z loomis $
 */

package org.quattor.pan.utils;

import java.io.Serializable;

import org.quattor.pan.exceptions.InvalidTermException;

/**
 * This interface provides the methods path Term objects must implement.
 * 
 * @author loomis
 * 
 */
public interface Term extends Comparable<Term>, Serializable {

	/**
	 * A utility function to determine if this Term is a key (String value).
	 * 
	 * @return boolean indicating if this Term is a key
	 */
	public boolean isKey();

	/**
	 * Method to extract the Integer index for this Term. This will throw an
	 * exception if the Term is not an index.
	 * 
	 * @return Integer index of the Term
	 * @throws InvalidTermException
	 *             thrown if the term is not an index; message should contain
	 *             the string value of the term that was used
	 */
	public Integer getIndex() throws InvalidTermException;

	/**
	 * Method to extract the String key for this Term. This will throw an
	 * exception if the Term is not a key.
	 * 
	 * @return String key of the Term
	 * @throws InvalidTermException
	 *             thrown if the term is not a key; message should contain the
	 *             string value of the term that was used
	 */
	public String getKey() throws InvalidTermException;

	/**
	 * Term objects may be used in nlists. Consequently, implementations should
	 * provide a highly-efficient hashCode method.
	 * 
	 * @return hash code for this Term
	 */
	public int hashCode();

	/**
	 * Term objects may be used in hashes and should provide an efficient equals
	 * method.
	 * 
	 * @return boolean indicating whether two objects are equal
	 */
	public boolean equals(Object term);

	/**
	 * This method must return a String representation of the Term object. It
	 * must be exactly the same String which would be returned by the underlying
	 * value.
	 */
	public String toString();

}
