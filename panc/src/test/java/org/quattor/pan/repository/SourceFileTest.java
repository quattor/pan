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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/RangeTest.java $
 $Id: RangeTest.java 3552 2008-08-03 09:55:58Z loomis $
 */

package org.quattor.pan.repository;

import java.io.File;

import org.junit.Test;
import org.quattor.pan.exceptions.CompilerError;

public class SourceFileTest {

    @Test(expected = CompilerError.class)
    public void testIllegalArguments() {
        new SourceFile(null, true, null);
    }

    @Test
    public void validNullPath() {
        new SourceFile("valid.pan", true, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSourceFileName1() {
        new SourceFile("/illegal-name.pan", true, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSourceFileName2() {
        new SourceFile("path/.illegal-name.pan", true, null);
    }

    @Test(expected = CompilerError.class)
    public void invalidRelativePath() {
        new SourceFile("valid.pan", true, new File("home/valid.pan"));
    }

    // Force an absolute file to be generated because on windows just a leading
    // slash is not sufficient.
    @Test
    public void matchedNameAndSource() {
        new SourceFile("a/b/name", true,
                new File("/home/a/b/name.pan").getAbsoluteFile());
        new SourceFile("a/b/name.pan", false,
                new File("/home/a/b/name.pan").getAbsoluteFile());
        new SourceFile("a/b/name.txt", false,
                new File("/home/a/b/name.txt").getAbsoluteFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mismatchedNameAndSource1() {
        new SourceFile("a/b/name", true,
                new File("/home/a/c/name.pan").getAbsoluteFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void mismatchedNameAndSource2() {
        new SourceFile("a/c/name.pan", false,
                new File("/home/a/b/name.pan").getAbsoluteFile());
    }

}
