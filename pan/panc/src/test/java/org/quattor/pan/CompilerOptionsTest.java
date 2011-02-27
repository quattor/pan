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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/CompilerOptionsTest.java $
 $Id: CompilerOptionsTest.java 3937 2008-11-22 10:31:49Z loomis $
 */

package org.quattor.pan;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.XmlDBFormatter;

public class CompilerOptionsTest {

    @Test
    public void testConstructorAndGetters1() {

        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = false;
        int iterationLimit = 1001;
        int callDepthLimit = 101;
        Formatter formatter = XmlDBFormatter.getInstance();
        File outputDirectory = new File(System.getProperty("user.dir"));
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(outputDirectory);
        includeDirectories.add(sessionDirectory);

        CompilerOptions options = new CompilerOptions(null, null,
                xmlWriteEnabled, depWriteEnabled, iterationLimit,
                callDepthLimit, formatter, outputDirectory, sessionDirectory,
                includeDirectories, 0, false, 0, false, false, null, null,
                false);

        assertTrue(xmlWriteEnabled == options.xmlWriteEnabled);
        assertTrue(depWriteEnabled == options.depWriteEnabled);

        assertTrue(iterationLimit == options.iterationLimit);
        assertTrue(callDepthLimit == options.callDepthLimit);

        assertTrue(formatter == options.formatter);

        assertTrue(outputDirectory.equals(options.outputDirectory));
    }

    @Test
    public void testConstructorAndGetters2() {

        boolean xmlWriteEnabled = false;
        boolean depWriteEnabled = true;
        int iterationLimit = 2002;
        int callDepthLimit = 202;
        Formatter formatter = XmlDBFormatter.getInstance();
        File outputDirectory = new File(System.getProperty("user.dir"));
        File sessionDirectory = null;
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(outputDirectory);

        CompilerOptions options = new CompilerOptions(null, null,
                xmlWriteEnabled, depWriteEnabled, iterationLimit,
                callDepthLimit, formatter, outputDirectory, sessionDirectory,
                includeDirectories, 0, false, 0, false, false, null, null,
                false);

        assertTrue(xmlWriteEnabled == options.xmlWriteEnabled);
        assertTrue(depWriteEnabled == options.depWriteEnabled);

        assertTrue(iterationLimit == options.iterationLimit);
        assertTrue(callDepthLimit == options.callDepthLimit);

        assertTrue(formatter == options.formatter);

        assertTrue(outputDirectory.equals(options.outputDirectory));
    }

    @Test
    public void checkUnlimitedValues() {

        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = false;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = XmlDBFormatter.getInstance();
        File outputDirectory = new File(System.getProperty("user.dir"));
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(outputDirectory);
        includeDirectories.add(sessionDirectory);

        CompilerOptions options = new CompilerOptions(null, null,
                xmlWriteEnabled, depWriteEnabled, iterationLimit,
                callDepthLimit, formatter, outputDirectory, sessionDirectory,
                includeDirectories, 0, false, 0, false, false, null, null,
                false);

        assertTrue(Integer.MAX_VALUE == options.iterationLimit);
        assertTrue(Integer.MAX_VALUE == options.callDepthLimit);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkBadOutputDirectory() {

        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = false;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = XmlDBFormatter.getInstance();
        File outputDirectory = new File("/xxxyyy");
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(outputDirectory);
        includeDirectories.add(sessionDirectory);

        new CompilerOptions(null, null, xmlWriteEnabled, depWriteEnabled,
                iterationLimit, callDepthLimit, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, false, 0, false,
                false, null, null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkBadSessionDirectory() {

        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = false;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = XmlDBFormatter.getInstance();
        File outputDirectory = new File(System.getProperty("user.dir"));
        File sessionDirectory = new File("/xxxyyy");
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(outputDirectory);
        includeDirectories.add(sessionDirectory);

        new CompilerOptions(null, null, xmlWriteEnabled, depWriteEnabled,
                iterationLimit, callDepthLimit, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, false, 0, false,
                false, null, null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkIncludeDirectory() {

        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = false;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = XmlDBFormatter.getInstance();
        File outputDirectory = new File(System.getProperty("user.dir"));
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(new File("/xxxyyy"));

        new CompilerOptions(null, null, xmlWriteEnabled, depWriteEnabled,
                iterationLimit, callDepthLimit, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, false, 0, false,
                false, null, null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMissingFormatter() {

        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = false;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = null;
        File outputDirectory = new File(System.getProperty("user.dir"));
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(outputDirectory);
        includeDirectories.add(sessionDirectory);

        new CompilerOptions(null, null, xmlWriteEnabled, depWriteEnabled,
                iterationLimit, callDepthLimit, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, false, 0, false,
                false, null, null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMissingOutputDirectory1() {

        boolean xmlWriteEnabled = true;
        boolean depWriteEnabled = false;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = null;
        File outputDirectory = null;
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(outputDirectory);
        includeDirectories.add(sessionDirectory);

        new CompilerOptions(null, null, xmlWriteEnabled, depWriteEnabled,
                iterationLimit, callDepthLimit, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, false, 0, false,
                false, null, null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkMissingOutputDirectory2() {

        boolean xmlWriteEnabled = false;
        boolean depWriteEnabled = true;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = null;
        File outputDirectory = null;
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();

        new CompilerOptions(null, null, xmlWriteEnabled, depWriteEnabled,
                iterationLimit, callDepthLimit, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, false, 0, false,
                false, null, null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkNullIncludeDirectory() {

        boolean xmlWriteEnabled = false;
        boolean depWriteEnabled = true;
        int iterationLimit = -1;
        int callDepthLimit = 0;
        Formatter formatter = null;
        File outputDirectory = null;
        File sessionDirectory = new File(System.getProperty("user.dir"));
        List<File> includeDirectories = new LinkedList<File>();
        includeDirectories.add(null);

        new CompilerOptions(null, null, xmlWriteEnabled, depWriteEnabled,
                iterationLimit, callDepthLimit, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, false, 0, false,
                false, null, null, false);
    }

}