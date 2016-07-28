package org.quattor.pan;

import org.quattor.pan.exceptions.SyntaxException;
import java.util.*;

/**
 * Created by iliclaey.
 *
 * When building the panc-project a panc-10.3-SNAPSHOT-fat-jar-with-tests.jar file is created.
 * This jar can be used to execute pan test files and should be used as follows:
 * `java -Dpanc.testdir="path" -Dpanc.tmpdir="path" -jar path/to/panc-10.3-SNAPSHOT-fat-jar-with-tests.jar`.
 * The panc.testdir and panc.tmpdir properties need to be set, otherwise an error will be thrown.
 *
 * To be able to execute your tests, they need to conform to a specific directory structure.
 * The directory you pass to the panc.testdir property needs to contain the _Functionality_,  _Dependency_ or
 * _RootElement_ directories, depending on what type of tests you want to run. Each of these directories need to be
 * divided in subdirectories, which contain the test files (or more subdirectories).
 *
 * panc.testdir
 * |-- Dependency
 * |-- Functionality
 * |   |-- subdir1
 * |   |   |-- file1.pan
 * |   |   |-- file2.pan
 * |   |-- subdir2
 * |   |   |-- file1.pan
 * |   |   |-- file2.pan
 * |-- RootElement
 *
 * Dependency tests
 * ================
 * A simple example:
 *
 * # dep: simple
 * object template simple;
 *
 * Functionality tests
 * ===================
 * You can declare whether the expected result will be an exception or a value in the tree.
 * For example, if you expect a SyntaxException to be thrown, you would place the following line on top of the file:
 *
 * # @expect=org.quattor.pan.exception.SyntaxException regex.
 *
 * The regex is optional and is used to check specific error messages.
 * If you expect a specific result, you should use an XPath. For example:
 *
 * # @expect="/profile/result=1"
 *
 * RootElement tests
 * =================
 * Specific functionality tests to test the root element in the tree.
 *
 *
 * To run the tests in a specific directory, additional properties can be passed on to java:
 *      - test_functionality: runs the tests in the _Functionality_ directory. Default value is true.
 *      - test_dependency: runs the tests in the _Dependency_ directory. Default value is false.
 *      - test_rootelement: runs the tests in the _RootElement_ directory. Default value is false.
 *
 * For example, when you only want to run the tests in the _Dependency_ directory you would use the following command:
 * `java -Dpanc.testdir="path" -Dpanc.tmpdir="path" -Dtest_functionality="false" -Dtest_dependency="true" -jar ...`.
 *
 */
public class PanTest {

    static Properties props;

    public static void main(String[] args) throws SyntaxException {

        props = System.getProperties();
        runTests();
    }

    public static void runTests() throws SyntaxException {

        JavaCompilerTest jct = new JavaCompilerTest();

        String functionality = props.getProperty("test_functionality");
        if (functionality != null) {
            if (Boolean.parseBoolean(functionality)) {
                jct.javaFunctionalTests();
            }
        } else {
            jct.javaFunctionalTests();
        }

        String dependency = props.getProperty("test_dependency");
        if (dependency != null) {
            if (Boolean.parseBoolean(dependency)) {
                jct.javaDependencyTests();
            }
        }

        String rootElement = props.getProperty("test_rootelement");
        if (rootElement != null) {
            if (Boolean.parseBoolean(rootElement)) {
                jct.javaRootElementTests();
            }
        }
    }
}
