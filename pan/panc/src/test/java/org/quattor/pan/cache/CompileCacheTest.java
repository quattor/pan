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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/BooleanPropertyTest.java $
 $Id: BooleanPropertyTest.java 998 2006-11-15 19:44:28Z loomis $
 */

package org.quattor.pan.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Test;

public class CompileCacheTest {

    @Test
    public void checkRelativeFilesOK() {
        File fileBase = new File("/tmp/root");
        File file = new File("/tmp/root/alpha/beta.pan");
        String result = CompileCache.getRelativePath(fileBase, file);
        assertEquals("alpha/beta.pan", result);
    }

    @Test
    public void checkNonrelativeFilesFail() {
        File fileBase = new File("/tmp/root/xxx");
        File file = new File("/tmp/root/alpha/beta.pan");
        String result = CompileCache.getRelativePath(fileBase, file);
        assertNull("non-base directory returned value", result);
    }

    @Test
    public void checkNullArgumentsToGetRelativePathOK() {
        File fileBase = new File("/tmp/root/xxx");
        File file = new File("/tmp/root/alpha/beta.pan");

        String result = CompileCache.getRelativePath(null, file);
        assertNull("non-null result when given null argument", result);

        result = CompileCache.getRelativePath(fileBase, null);
        assertNull("non-null result when given null argument", result);

        result = CompileCache.getRelativePath(null, null);
        assertNull("non-null result when given null argument", result);
    }

    @Test
    public void checkNullArgumentToAnnotationOutputFileOK() {
        File annotationDirectory = new File("/tmp/output");
        File result = CompileCache.annotationOutputFile(annotationDirectory,
                null);
        assertNull("non-null result when given null argument", result);
    }

    @Test
    public void checkCorrectAbsoluteFile() {
        File annotationDirectory = new File("/tmp/output");
        File result = CompileCache.annotationOutputFile(annotationDirectory,
                "alpha/beta.pan");
        String correctPath = "/tmp/output/alpha/beta.pan.annotation.xml"
                .replace("/", File.separator);
        assertEquals(new File(correctPath), result);
    }

}
