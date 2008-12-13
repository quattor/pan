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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/TransientElement.java $
 $Id: TransientElement.java 3595 2008-08-17 07:35:14Z loomis $
 */

package org.quattor.pan.dml.data;

import net.jcip.annotations.Immutable;

/**
 * Subclasses of this abstract class may not appear in a final configuration
 * tree. (Hence are transient.) These Elements are used only during the
 * construction of the configuration tree.
 * 
 * @author loomis
 * 
 */
@Immutable
abstract public class TransientElement extends Element {

	private static final long serialVersionUID = -2665790417849267703L;

}
