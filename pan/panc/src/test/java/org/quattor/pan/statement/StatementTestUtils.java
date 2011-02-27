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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/statement/StatementTestUtils.java $
 $Id: StatementTestUtils.java 3937 2008-11-22 10:31:49Z loomis $
 */

package org.quattor.pan.statement;

import static org.junit.Assert.fail;
import static org.quattor.pan.utils.TestUtils.getTmpdir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.quattor.pan.Compiler;
import org.quattor.pan.CompilerOptions;
import org.quattor.pan.cache.BuildCache;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.output.XmlDBFormatter;
import org.quattor.pan.tasks.BuildResult;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.ExceptionUtils;

public class StatementTestUtils {

    protected void writeStringToFile(String contents, File file) {

        try {
            FileWriter fw = new FileWriter(file);
            fw.write(contents);
            fw.close();
        } catch (IOException ioe) {
            fail("unexpected IO exception writing file (" + file + "): "
                    + ioe.getMessage());
        }
    }

    protected void runExpectingException(String name, String statement)
            throws Exception {

        File tmpfile = getTmpdir();

        String fullName = "statement_test_" + name;
        File tplfile = new File(tmpfile, fullName + ".tpl");

        writeStringToFile("object template " + fullName + ";\n" + statement
                + "\n", tplfile);

        List<File> paths = new LinkedList<File>();
        paths.add(tmpfile);

        List<File> files = new LinkedList<File>();
        files.add(tplfile);

        CompilerOptions options = new CompilerOptions(null, null, true, true,
                1000, 50, XmlDBFormatter.getInstance(), tmpfile, null, paths,
                0, false, 0, false, false, null, null, false);

        Compiler compiler = new Compiler(options, new LinkedList<String>(),
                files);
        Set<Throwable> throwables = compiler.process().getErrors();

        if (throwables.size() == 1) {
            Throwable[] errorArray = throwables
                    .toArray(new Throwable[throwables.size()]);
            Throwable t = errorArray[0];
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                fail("unexpected throwable encountered: " + t.getClass() + " "
                        + t.getMessage());
            }
        } else if (throwables.size() > 1) {
            fail("too many throwables found: " + throwables.size());
        }

    }

    protected Context setupTemplateToRun2(String name, String statement,
            boolean fullBuild) throws Exception {

        File tmpfile = getTmpdir();

        String fullName = "statement_test_" + name;
        File tplfile = new File(tmpfile, fullName + ".tpl");

        writeStringToFile("object template " + fullName + ";\n" + statement
                + "\n", tplfile);

        List<File> paths = new LinkedList<File>();
        paths.add(tmpfile);

        List<File> files = new LinkedList<File>();
        files.add(tplfile);

        CompilerOptions options = new CompilerOptions(null, null, true, true,
                1000, 50, XmlDBFormatter.getInstance(), tmpfile, null, paths,
                0, false, 0, false, false, null, null, false);

        Compiler compiler = new Compiler(options, new LinkedList<String>(),
                files);
        compiler.process();

        BuildCache ocache = compiler.getBuildCache();
        Future<BuildResult> ft = ocache.retrieve(fullName);
        if (ft == null) {
            fail("object template for " + fullName
                    + " didn't exist in object cache");
        }

        BuildResult result = null;
        try {
            result = ft.get();
        } catch (InterruptedException ie) {
            throw new EvaluationException("compilation interrupted");
        } catch (CancellationException ce) {
            throw new EvaluationException("compilation cancelled");
        } catch (ExecutionException ee) {
            throw ExceptionUtils.launder(ee);
        }

        return result.getObjectContext();
    }

    protected Context setupTemplateToRun3(String name, String statement1,
            String statement2, boolean fullBuild) throws Exception {

        File tmpfile = getTmpdir();

        String fullName = "statement_test_" + name;
        File tplfile = new File(tmpfile, fullName + ".tpl");
        File incfile = new File(tmpfile, fullName + "_include.tpl");

        writeStringToFile("object template " + fullName + ";\n" + statement1
                + "\n", tplfile);
        writeStringToFile("template " + fullName + "_include;\n" + statement2
                + "\n", incfile);

        List<File> paths = new LinkedList<File>();
        paths.add(tmpfile);

        List<File> files = new LinkedList<File>();
        files.add(tplfile);

        CompilerOptions options = new CompilerOptions(null, null, true, true,
                1000, 50, XmlDBFormatter.getInstance(), tmpfile, null, paths,
                0, false, 0, false, false, null, null, false);

        Compiler compiler = new Compiler(options, new LinkedList<String>(),
                files);
        compiler.process();

        BuildCache ocache = compiler.getBuildCache();
        Future<BuildResult> ft = ocache.retrieve(fullName);
        if (ft == null) {
            fail("object template for " + fullName
                    + " didn't exist in object cache");
        }

        BuildResult result = null;
        try {
            result = ft.get();
        } catch (InterruptedException ie) {
            throw new EvaluationException("compilation interrupted");
        } catch (CancellationException ce) {
            throw new EvaluationException("compilation cancelled");
        } catch (ExecutionException ee) {
            throw ExceptionUtils.launder(ee);
        }

        return result.getObjectContext();
    }

}
