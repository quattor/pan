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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/utils/PathTest.java $
 $Id: PathTest.java 998 2006-11-15 19:44:28Z loomis $
 */

package org.quattor.pan.utils;

import static org.quattor.pan.utils.MessageUtils.MSG_ATTEMPT_TO_REPLACE_EXISTING_NODE;

import java.util.TreeMap;

import org.quattor.pan.exceptions.CompilerError;

/**
 * Data structure that hold final flag information for paths in a machine
 * configuration.
 * 
 * @author loomis
 * 
 */
public class FinalFlags {

	private final Node root;

	/**
	 * Create a new FinalFlags object to hold information about which paths are
	 * marked as final.
	 */
	public FinalFlags() {
		root = new Node();
	}

	/**
	 * Determine if the given path is marked as final. The path is final if the
	 * path or any parent is marked as final or the full path has descendants
	 * which are marked as final. If the path is final, then the
	 * getFinalReason() method should be called to determine why.
	 * 
	 * @param path
	 *            Path to check
	 * 
	 * @return boolean indicating if the Path is final
	 */
	public boolean isFinal(Path path) {

		// Step through the nodes based on the given path. If any intermediate
		// nodes are marked as final, we can just return true.
		Node currentNode = root;
		for (Term t : path.getTerms()) {
			Node nextNode = currentNode.getChild(t);
			if (nextNode == null) {
				return false;
			} else if (nextNode.isFinal()) {
				return true;
			}
			currentNode = nextNode;
		}

		// If we've made it to here, then the current node is the one which
		// corresponds to the last element in the path. Return true if it is
		// marked as final or it has any descendants.
		return currentNode.isFinal() || currentNode.hasChild();
	}

	/**
	 * Return a String with a message indicating why the given path is final.
	 * 
	 * @param path
	 *            Path to check
	 * 
	 * @return String reason why the path is final or null if the path isn't
	 *         final
	 */
	public String getFinalReason(Path path) {

		StringBuilder sb = new StringBuilder(path + " cannot be modified; ");
		StringBuilder finalPath = new StringBuilder("/");

		// Step through the nodes based on the given path. If any intermediate
		// nodes are marked as final, we can just return true.
		Node currentNode = root;
		for (Term t : path.getTerms()) {
			finalPath.append(t.toString());
			Node nextNode = currentNode.getChild(t);
			if (nextNode == null) {
				return null;
			} else if (nextNode.isFinal()) {
				sb.append(finalPath.toString() + " is marked as final");
				return sb.toString();
			}
			finalPath.append("/");
			currentNode = nextNode;
		}

		// Strip off the last slash. It is not needed.
		finalPath.deleteCharAt(finalPath.length() - 1);

		// Either the path itself is final or a descendant.
		if (currentNode.isFinal()) {
			sb.append(finalPath.toString() + " is marked as final");
		} else if (currentNode.hasChild()) {
			sb.append(finalPath.toString()
					+ currentNode.getFinalDescendantPath()
					+ " is marked as final");
			return sb.toString();
		} else {
			return null;
		}

		return null;
	}

	/**
	 * Mark the given Path as being final.
	 * 
	 * @param path
	 *            Path to mark as final
	 */
	public void setFinal(Path path) {

		// Step through the nodes creating any nodes which don't exist.
		Node currentNode = root;
		for (Term t : path.getTerms()) {
			Node nextNode = currentNode.getChild(t);
			if (nextNode == null) {
				nextNode = currentNode.newChild(t);
			}
			currentNode = nextNode;
		}

		// The current node is now the one corresponding to the last term in the
		// path. Set this as final.
		currentNode.setFinal();
	}

	/**
	 * A private static class to hold individual nodes in the tree. Each node
	 * corresponds to a single level in the path hierarchy.
	 * 
	 * @author loomis
	 */
	private static class Node {

		private boolean flag;

		private final TreeMap<Term, Node> map;

		/**
		 * Create a new node. It is not marked as final and has no children.
		 */
		public Node() {
			flag = false;
			map = new TreeMap<Term, Node>();
		}

		/**
		 * Determine if this node is marked as final.
		 * 
		 * @return "final" status of this node
		 */
		public boolean isFinal() {
			return flag;
		}

		/**
		 * Mark this node as being final.
		 */
		public void setFinal() {
			flag = true;
		}

		/**
		 * Get the child corresponding to the given term or null if it does not
		 * exist.
		 * 
		 * @param term
		 *            Term to use as the index
		 * 
		 * @return Node corresponding to the given term or null if it doesn't
		 *         exist
		 */
		public Node getChild(Term term) {
			return map.get(term);
		}

		/**
		 * Return a flag indicating if this Node has any children.
		 * 
		 * @return true if this Node has any children; false otherwise
		 */
		public boolean hasChild() {
			return map.size() != 0;
		}

		/**
		 * Return the path to a descendant which is final.
		 */
		public String getFinalDescendantPath() {
			if (flag) {
				return "";
			} else {
				Term term = map.firstKey();
				return "/" + term.toString()
						+ map.get(term).getFinalDescendantPath();
			}

		}

		/**
		 * Create a new child associated with the given term.
		 * 
		 * @param term
		 *            Term to use as the index
		 * 
		 * @return newly created Node
		 */
		public Node newChild(Term term) {
			Node child = new Node();
			if (map.put(term, child) != null) {
				throw CompilerError
						.create(MSG_ATTEMPT_TO_REPLACE_EXISTING_NODE);
			}
			return child;
		}

	}

}
