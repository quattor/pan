/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use e file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.quattor.pan.dml.data;

public class ElementUtils {

    private ElementUtils() {

    }

    /**
     * Determine if the element contains any undefined (transient) elements. The call will return null if no undefined
     * elements are found; it will return a string indicating the relative path if an undefined element is found.
     *
     * @param e Element to search for undefined Elements
     * @return String representation of the path of the undefined element, null otherwise
     */
    public static String locateUndefinedElement(Element e) {

        if (e instanceof Resource) {
            // recursively check all of the resources children
            Resource r = (Resource) e;
            for (Resource.Entry entry : r) {
                String rpath = locateUndefinedElement(entry.getValue());
                String term = entry.getValue().toString();
                if (rpath != null) {
                    return (!"".equals(rpath)) ? term + "/" + rpath : term;
                }
            }
            return null;
        } else if (e instanceof TransientElement) {
            // return empty string as relative path and indicating transient element was found
            return "";
        } else {
            // property that isn't a transient element
            return null;
        }
    }

}
