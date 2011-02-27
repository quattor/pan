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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/Sources/panc/trunk/src/org/quattor/pan/output/PanFormatter.java $
 $Id: PanFormatter.java 998 2006-11-15 19:44:28Z loomis $
 */

package org.quattor.pan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.FormatterUtils;

public class Panc {

    private static enum TypeOptBfor {
        // before first argument
        BEGIN,
        // option must be followed by an argument
        OPTARG,
        // option without argument
        NOARG,
        // path or argument begining with -
        DMINUS,
        // argument of an option
        ARG,
        // java-opts required as an option
        JOPT,
        // deprecation level required as an option
        DEPR,
        // paths
        END
    };

    private static Pattern debugAllPattern = Pattern.compile(".*");

    private Panc() {

    }

    public static void main(String[] args) {
        Vector<String> authorizedOpt = new Vector<String>();

        Vector<String> authorizedOptArg = new Vector<String>();

        Vector<String> endingOptChar = new Vector<String>();

        Vector<String> options = new Vector<String>();

        Vector<String> arguments = new Vector<String>();

        Vector<String> paths = new Vector<String>();

        String optbforname = null;

        // filling vector containing options
        authorizedOpt.addElement("help");
        authorizedOpt.addElement("usage");
        authorizedOpt.addElement("debug");
        authorizedOpt.addElement("debug-include");
        authorizedOptArg.addElement("debug-include");
        authorizedOpt.addElement("debug-exclude");
        authorizedOptArg.addElement("debug-exclude");
        authorizedOpt.addElement("xml-write");
        authorizedOpt.addElement("no-xml-write");
        authorizedOpt.addElement("objects");
        authorizedOptArg.addElement("objects");
        authorizedOpt.addElement("objects-file");
        authorizedOptArg.addElement("objects-file");
        authorizedOpt.addElement("file");
        authorizedOptArg.addElement("file");
        authorizedOpt.addElement("check");
        authorizedOpt.addElement("session-dir");
        authorizedOptArg.addElement("session-dir");
        authorizedOpt.addElement("include-dir");
        authorizedOptArg.addElement("include-dir");
        authorizedOpt.addElement("output-dir");
        authorizedOptArg.addElement("output-dir");
        authorizedOpt.addElement("xml-style");
        authorizedOptArg.addElement("xml-style");
        authorizedOpt.addElement("dependency");
        authorizedOpt.addElement("max-iteration");
        authorizedOptArg.addElement("max-iteration");
        authorizedOpt.addElement("max-recursion");
        authorizedOptArg.addElement("max-recursion");
        authorizedOpt.addElement("gzip");
        authorizedOpt.addElement("deprecation");
        authorizedOptArg.addElement("deprecation");
        authorizedOpt.addElement("verbose");
        authorizedOpt.addElement("logging");
        authorizedOptArg.addElement("logging");
        authorizedOpt.addElement("logfile");
        authorizedOptArg.addElement("logfile");
        authorizedOpt.addElement("java-opts");
        authorizedOptArg.addElement("java-opts");
        authorizedOpt.addElement("annotation-dir");
        authorizedOptArg.addElement("annotation-dir");
        authorizedOpt.addElement("annotation-base-dir");
        authorizedOptArg.addElement("annotation-base-dir");
        authorizedOpt.addElement("failonwarn");

        endingOptChar.addElement("j");
        endingOptChar.addElement("J");
        endingOptChar.addElement("f");
        endingOptChar.addElement("S");
        endingOptChar.addElement("I");
        endingOptChar.addElement("O");
        endingOptChar.addElement("x");
        endingOptChar.addElement("i");
        endingOptChar.addElement("r");
        endingOptChar.addElement("p");

        TypeOptBfor optbfortype = TypeOptBfor.BEGIN;
        int nbofarg = 1;
        // test des arguments
        for (String arg : args) {
            boolean lastArg = false;
            if (nbofarg == args.length) {
                lastArg = true;
            }
            // option as -[letter]
            if ((arg.startsWith("-")) && (arg.length() < 2)) {
                catchError(1, arg);

            } else if ((arg.startsWith("-")) && (arg.charAt(1) != '-')
                    && (optbfortype != TypeOptBfor.DMINUS)
                    && (optbfortype != TypeOptBfor.DEPR)) {
                if (optbfortype != TypeOptBfor.END) {
                    if (optbfortype == TypeOptBfor.OPTARG) {
                        int numerror = calcNumError(optbforname);
                        catchError(numerror, arg);
                    }
                    if (optbfortype == TypeOptBfor.JOPT) {
                        optbfortype = TypeOptBfor.NOARG;
                    } else {
                        optbfortype = verifySingleCharOpt(arg, endingOptChar,
                                lastArg);
                        int rounds;
                        if (optbfortype != TypeOptBfor.NOARG) {
                            rounds = arg.length() - 1;
                        } else {
                            rounds = arg.length();
                        }
                        for (int i = 1; i < rounds; i++) {
                            options.addElement(arg.substring(i, i + 1));
                            arguments.addElement(null);
                        }
                        optbforname = arg.substring(arg.length() - 1);
                        nbofarg++;
                    }
                }
                // option as --[word]
            } else if ((arg.startsWith("-")) && (arg.charAt(1) == '-')
                    && (optbfortype != TypeOptBfor.DMINUS)) {
                if (optbfortype != TypeOptBfor.END) {
                    if (optbfortype == TypeOptBfor.OPTARG) {
                        int numerror = calcNumError(optbforname);
                        catchError(numerror, arg);
                    } else if (arg.length() == 2) {
                        optbfortype = TypeOptBfor.DMINUS;
                        nbofarg++;
                    } else {
                        optbfortype = verifyCompleteStringOpt(arg,
                                authorizedOptArg, authorizedOpt, lastArg);
                        if (optbfortype == TypeOptBfor.NOARG) {
                            options.addElement(arg.substring(2));
                            arguments.addElement(null);
                            optbforname = arg.substring(2);
                        } else if ((optbfortype == TypeOptBfor.OPTARG)
                                || (optbfortype == TypeOptBfor.DEPR)) {
                            optbforname = arg.substring(2);
                        } else if (optbfortype == TypeOptBfor.JOPT) {
                            // nothing done
                        } else {
                            Pattern p = Pattern.compile("=");
                            String[] opts = p.split(arg, 2);
                            options.addElement(opts[0].substring(2));
                            arguments.addElement(opts[1]);
                            optbforname = opts[0].substring(2);
                        }
                        nbofarg++;
                    }
                }
                // simple string (path or argument)
            } else {
                switch (optbfortype) {
                case JOPT:
                    optbfortype = TypeOptBfor.NOARG;
                    break;
                // path
                case END:
                    paths.addElement(arg);
                    break;
                // No argument needed
                case BEGIN:
                    paths.addElement(arg);
                    optbfortype = TypeOptBfor.END;
                    break;
                case NOARG:
                    paths.addElement(arg);
                    optbfortype = TypeOptBfor.END;
                    break;
                // Option needing an argument
                case OPTARG:
                    if ((Pattern.matches("[IjJf]", optbforname))
                            || (optbforname.equals("objects"))
                            || (optbforname.equals("objects-file"))
                            || (optbforname.equals("file"))
                            || (optbforname.equals("include-dir"))) {
                        if (nbofarg != args.length) {
                            options.addElement(optbforname);
                            arguments.addElement(arg);
                            optbfortype = TypeOptBfor.ARG;
                        } else {
                            options.addElement(optbforname);
                            arguments.addElement(arg);
                            optbfortype = TypeOptBfor.END;
                        }
                    } else {
                        options.addElement(optbforname);
                        arguments.addElement(arg);
                        optbfortype = TypeOptBfor.ARG;
                    }
                    break;
                // Argument of an option
                case ARG:
                    paths.addElement(arg);
                    optbfortype = TypeOptBfor.END;
                    break;
                // --
                case DMINUS:
                    paths.addElement(arg);
                    optbfortype = TypeOptBfor.NOARG;
                    break;
                // Argument of deprecation option
                case DEPR:
                    if (nbofarg == args.length) {
                        System.exit(0);
                    } else {
                        options.addElement(optbforname);
                        arguments.addElement(arg);
                        optbfortype = TypeOptBfor.ARG;
                    }
                    break;
                }
                nbofarg++;
            }
        }

        // Explicitly return the error code from the run of the compiler.
        System.exit(launchCompiler(options, arguments, paths));
    }

    /**
     * Verification of an or several option(s) given as a letter or a group of
     * letter
     * 
     * @param arg
     *            the option(s)
     */
    public static TypeOptBfor verifySingleCharOpt(String arg,
            Vector<String> endingOptChar, boolean lastArg) {
        TypeOptBfor type = TypeOptBfor.NOARG;

        //
        if (arg.substring(1).equals("M")) {
            type = TypeOptBfor.JOPT;
        }
        // authorized options
        else if (!Pattern.matches("^[h?dlbweznjJfcSIOxyirgpa]*$", arg
                .substring(1))) {
            catchError(1, arg);
        } else if ((lastArg) && (!Pattern.matches("^[h?]*$", arg.substring(1)))) {
            for (String opt : endingOptChar) {
                if (arg.endsWith(opt)) {
                    catchError(3, arg);
                }
            }
        } else {
            // Options followed by an argument
            for (int i = 1; i < (arg.length()) - 1; i++) {
                int numerror = calcNumError(arg.substring(i, i + 1));
                if (numerror != 0) {
                    catchError(numerror, arg.substring(i + 1));
                }
            }
            if (Pattern.matches("^[h?]*$", arg.substring(1))) {
                postHelp();
                System.exit(-1);
            } else if (Pattern.matches("[jJfSIOxir]", arg.substring((arg
                    .length()) - 1))) {
                type = TypeOptBfor.OPTARG;
            } else if ((arg.substring((arg.length()) - 1)).equals("p")) {
                type = TypeOptBfor.DEPR;
            }
        }
        return type;
    }

    /**
     * Verification of an option given as an entire word
     * 
     * @param arg
     *            the option (and its argument separated with '=' if exists)
     */
    public static TypeOptBfor verifyCompleteStringOpt(String arg,
            Vector<String> authorizedOptArg, Vector<String> authorizedOpt,
            boolean lastArg) {
        TypeOptBfor type = TypeOptBfor.NOARG;
        boolean optok = false;
        boolean optarg = false;
        String option = null;
        Pattern p = Pattern.compile("=");
        String[] opts = p.split(arg, 2);
        if (opts.length < 2) {
            option = arg.substring(2);
        } else {
            for (String opt : authorizedOptArg) {
                if ((opt.equals(opts[0].substring(2)))
                        && (opts[1].length() < 1)) {
                    int numerror = calcNumError(opts[0].substring(2));
                    catchError(numerror, "");
                }
            }
            option = opts[0].substring(2);
        }
        // Authorized options
        for (String opt : authorizedOpt) {
            if (opt.equals(option)) {
                optok = true;
            }
        }
        for (String opt : authorizedOptArg) {
            if (opt.equals(option)) {
                optarg = true;
            }
        }
        if (!optok) {
            catchError(1, option);
        } else if ((!optarg) && (arg.indexOf("=") >= 0)) {
            catchError(1, arg);
        } else if ((lastArg) && (opts.length < 2) && (!(option.equals("help")))
                && (!(option.equals("usage")))) {
            catchError(3, arg);
        } else if ((option.equals("java-opts")) && (opts.length < 2)) {
            type = TypeOptBfor.JOPT;
        } else if ((option.equals("java-opts")) && (opts.length == 2)) {
            type = TypeOptBfor.NOARG;
        } else {
            if ((option.equals("help")) || (option.equals("usage"))) {
                postHelp();
                System.exit(-1);
                // Options followed by an argument
            } else if (optarg) {
                if (opts.length < 2) {
                    if (option.equals("deprecation")) {
                        type = TypeOptBfor.DEPR;
                    } else {
                        type = TypeOptBfor.OPTARG;
                    }
                } else if (opts.length >= 2) {
                    type = TypeOptBfor.ARG;
                } else {
                }
            }
        }
        return type;
    }

    /**
     * Launches the compiler
     * 
     */
    public static int launchCompiler(Vector<String> options,
            Vector<String> arguments, Vector<String> paths) {

        File annotationOutputDirectory = null;

        File annotationBaseDirectory = null;

        boolean xmlWriteEnabled = true;

        boolean depWriteEnabled = false;

        Formatter formatter = FormatterUtils.getDefaultFormatterInstance();

        int iteration = 5000;

        int callDepth = 10;

        File outputDirectory = new File(System.getProperty("user.dir"));

        File sessionDirectory = null;

        File includDir = null;

        File filePath = null;

        File objectsFile = null;

        boolean gzip = false;

        int deprecation = 0;

        boolean failOnWarn = false;

        boolean verbose = false;

        String logging = "";

        String logfilename = "";

        File logfile = null;

        /* List of debug include patterns. */
        List<Pattern> debugIncludePatterns = new LinkedList<Pattern>();
        List<Pattern> debugExcludePatterns = new LinkedList<Pattern>();

        /* The list of directories to include in search path. */
        LinkedList<File> includeDirectories = new LinkedList<File>();

        /* The list of directories to include in search path. */
        LinkedList<File> includeFiles = new LinkedList<File>();

        /* The list of objects to output in search path. */
        LinkedList<String> objectOutput = new LinkedList<String>();
        int compteur = 0;
        for (String opt : options) {
            if ((opt.equals("d")) || (opt.equals("debug"))) {
                debugIncludePatterns.add(debugAllPattern);
            } else if (opt.equals("debug-include")) {
                Pattern p = verifyPattern(arguments.elementAt(compteur));
                debugIncludePatterns.add(p);
            } else if (opt.equals("debug-exclude")) {
                Pattern p = verifyPattern(arguments.elementAt(compteur));
                debugExcludePatterns.add(p);
            } else if (opt.equals("annotation-dir")) {
                annotationOutputDirectory = new File(arguments
                        .elementAt(compteur));
                if (!annotationOutputDirectory.isAbsolute()) {
                    annotationOutputDirectory = new File(System
                            .getProperty("user.dir"), arguments
                            .elementAt(compteur));
                }
                veriDir(annotationOutputDirectory);
            } else if (opt.equals("annotation-base-dir")) {
                annotationBaseDirectory = new File(arguments
                        .elementAt(compteur));
                if (!annotationBaseDirectory.isAbsolute()) {
                    annotationBaseDirectory = new File(System
                            .getProperty("user.dir"), arguments
                            .elementAt(compteur));
                }
                veriDir(annotationBaseDirectory);
            } else if ((opt.equals("z")) || (opt.equals("xml-write"))) {
                xmlWriteEnabled = true;
            } else if ((opt.equals("n")) || (opt.equals("no-xml-write"))) {
                xmlWriteEnabled = false;
            } else if ((opt.equals("y")) || (opt.equals("dependency"))) {
                depWriteEnabled = true;
            } else if ((opt.equals("c")) || (opt.equals("check"))) {
                xmlWriteEnabled = false;
                depWriteEnabled = false;
            } else if ((opt.equals("x")) || (opt.equals("xml-style"))) {
                String formatterName = arguments.elementAt(compteur);
                formatter = FormatterUtils.getFormatterInstance(formatterName);
                if (formatter == null) {
                    catchError("Invalid XML style: " + formatterName);
                }
            } else if ((opt.equals("i")) || (opt.equals("max-iteration"))) {
                Integer i = new Integer(arguments.elementAt(compteur));
                iteration = i.intValue();
            } else if ((opt.equals("r")) || (opt.equals("max-recursion"))) {
                Integer i = new Integer(arguments.elementAt(compteur));
                callDepth = i.intValue();
            } else if ((opt.equals("O")) || (opt.equals("output-dir"))) {
                outputDirectory = new File(arguments.elementAt(compteur));
                if (!outputDirectory.isAbsolute()) {
                    outputDirectory = new File(System.getProperty("user.dir"),
                            arguments.elementAt(compteur));
                }
                veriDir(outputDirectory);
            } else if ((opt.equals("S")) || (opt.equals("session-dir"))) {
                sessionDirectory = new File(arguments.elementAt(compteur));
                if (!sessionDirectory.isAbsolute()) {
                    sessionDirectory = new File(System.getProperty("user.dir"),
                            arguments.elementAt(compteur));
                }
                veriDir(sessionDirectory);
            } else if ((opt.equals("I")) || (opt.equals("include-dir"))) {
                includDir = new File(arguments.elementAt(compteur));
                if (!includDir.isAbsolute()) {
                    includDir = new File(System.getProperty("user.dir"),
                            arguments.elementAt(compteur));
                }
                veriDir(includDir);
                if (!includeDirectories.contains(includDir)) {
                    includeDirectories.add(includDir);
                }
            } else if ((opt.equals("f")) || (opt.equals("file"))) {
                filePath = new File(arguments.elementAt(compteur));
                if (!filePath.isAbsolute()) {
                    filePath = new File(System.getProperty("user.dir"),
                            arguments.elementAt(compteur));
                }
                LinkedList<String> fileName = readFile(filePath);
                for (String name : fileName) {
                    File file = new File(name);
                    includeFiles.add(file);
                }
            } else if ((opt.equals("J")) || (opt.equals("objects-file"))) {
                objectsFile = new File(arguments.elementAt(compteur));
                objectOutput = readFile(objectsFile);
            } else if ((opt.equals("j")) || (opt.equals("objects"))) {
                objectOutput = splitObject(arguments.elementAt(compteur),
                        objectOutput);
            } else if ((opt.equals("g")) || (opt.equals("gzip"))) {
                gzip = true;
            } else if ((opt.equals("p")) || (opt.equals("deprecation"))) {
                Integer i = new Integer(arguments.elementAt(compteur));
                deprecation = i.intValue();
            } else if ((opt.equals("a")) || (opt.equals("verbose"))) {
                verbose = true;
            } else if (opt.equals("logging")) {
                logging = arguments.elementAt(compteur);
                CompilerLogging.activateLoggers(logging);

            } else if (opt.equals("logfile")) {
                logfilename = arguments.elementAt(compteur);
                logfile = new File(logfilename);
                if (logfile != null) {
                    CompilerLogging.setLogFile(logfile);
                }
            } else if (opt.equals("failonwarn")) {
                failOnWarn = true;
            }
            compteur++;
        }
        for (String path : paths) {
            File path2include = new File(path);
            veriFile(path2include);
            includeFiles.add(path2include);
        }

        CompilerOptions coptions = new CompilerOptions(debugIncludePatterns,
                debugExcludePatterns, xmlWriteEnabled, depWriteEnabled,
                iteration, callDepth, formatter, outputDirectory,
                sessionDirectory, includeDirectories, 0, gzip, deprecation,
                false, annotationOutputDirectory, annotationBaseDirectory,
                failOnWarn);

        CompilerResults results = Compiler.run(coptions, objectOutput,
                includeFiles);

        String errors = results.formatErrors();
        if (errors != null) {
            System.err.println(errors);
        }

        if (verbose) {
            System.out.println(results.formatStats());
        }

        // Return the exit code. (Non-zero if there was an error.)
        return ((errors != null) ? 1 : 0);
    }

    /**
     * Prints the usage message on the screen
     */
    public static void postHelp() {
        System.out.printf("Usage: panc [OPTIONS] [--] [PATHS...]\n\n");
        System.out
                .printf(" -d,--debug                   enable all pan debug/traceback functions\n");
        System.out
                .printf("    --debug-include           enable regex for debug/traceback functions\n");
        System.out
                .printf("    --debug-exclude           exclude regex debug/traceback functions\n");
        System.out
                .printf("    --annotation-dir=PATH     where to store the annotation files\n");
        System.out
                .printf("    --annotation-base-dir=PATH source base directory for generating anno. files\n");
        System.out.printf(" -a,--verbose                 shows statistics\n");
        System.out
                .printf(" -z,--xml-write               write machine config. files (usually XML files)\n");
        System.out
                .printf(" -n,--no-xml-write            disallow write machine configuration files\n");
        System.out
                .printf(" -j,--objects=NAMES           objects to output (comma separated list)\n");
        System.out
                .printf(" -J,--objects-file=NAME       file with list of objects to output\n");
        System.out
                .printf(" -f,--file=NAME               file containing paths of files to process\n");
        System.out
                .printf(" -c,--check                   only check the syntax of the given source files\n");
        System.out
                .printf(" -S,--session-dir=PATH        which session directory to use (default is NULL)\n");
        System.out
                .printf(" -I,--include-dir=PATH        which source directory to consider (default is .)\n");
        System.out
                .printf(" -O,--output-dir=PATH         where to store the output files (default is .)\n");
        System.out
                .printf(" -x,--xml-style=STRING        select the XML output style (default is pan)\n");
        System.out
                .printf(" -y,--dependency              output dependency information\n");
        System.out
                .printf(" -i,--max-iteration=NUMBER    set max. number of iterations (default is 10000)\n");
        System.out
                .printf(" -r,--max-recursion=NUMBER    set max. number of recursions (default is 10)\n");
        System.out
                .printf(" -g,--gzip                    compress the machine config. files\n");
        System.out
                .printf(" -p,--deprecation=NUMBER      set deprecation level (default is 0)\n");
        System.out
                .printf("    --failonwarn              treat warnings as errors\n");
        System.out
                .printf("    --java-opts=STRING        define java options\n");
        System.out
                .printf(" -k,--noconf                  panc does not use any configuration file\n");
        System.out
                .printf("    --logging=STRING          enable logging from the command line\n");
        System.out
                .printf("    --logfile=STRING          set the name of the log file to write\n");
        System.out
                .printf(" -v,--version                 print the version of the compiler\n");
        System.out
                .printf(" -h,--help                    print this help message\n");
        System.out
                .printf(" -?,--usage                   print this help message\n");
    }

    /**
     * Select the usage message
     * 
     * @param arg
     *            the input option
     */
    public static int calcNumError(String arg) {
        int numerror = 0;
        if ((arg.equals("session-dir")) || (arg.equals("include-dir"))
                || (arg.equals("output-dir"))
                || (Pattern.matches("[SIO]", arg))) {
            numerror = 2;
        } else if ((arg.equals("objects")) || (arg.equals("deprecation"))
                || (arg.equals("max-iteration"))
                || (arg.equals("max-recursion"))
                || (Pattern.matches("[jpir]", arg))) {
            numerror = 5;
        } else if ((arg.equals("objects-file")) || (arg.equals("output-dir"))
                || (Pattern.matches("[Jf]", arg))) {
            numerror = 4;
        }
        return numerror;
    }

    /**
     * Display the usage message
     * 
     * @param error
     *            the type of error
     */
    public static void catchError(String error) {
        System.err.println("panc: " + error);
        System.err.printf("\n");
        postHelp();
        System.exit(-1);
    }

    /**
     * Display the usage message
     * 
     * @param numerror
     *            the mumber of the error message
     * @param arg
     *            the input option/argument
     */
    public static void catchError(int numerror, String arg) {
        Vector<String> error = new Vector<String>();
        error.addElement("unrecognized option: " + arg);
        error.addElement("Not an existing directory path: " + arg);
        error.addElement("option requires an argument: " + arg);
        error.addElement("Can't open file: " + arg
                + "\ninput in flex scanner failed");
        error.addElement("input in flex scanner failed");

        System.err.println("panc: " + error.elementAt(numerror - 1));
        System.err.printf("\n");
        postHelp();
        System.exit(-1);
    }

    /**
     * Splitting a string in a list of files and adding it in a list
     * 
     * @param objlist
     *            String containing a list of files
     * @param objectOutput
     *            LinkedList to fill with results
     * 
     * @return LinkedList with results
     */
    public static LinkedList<String> splitObject(String objlist,
            LinkedList<String> objectOutput) {
        Pattern p = Pattern.compile(",");
        String[] objects = p.split(objlist);
        for (String obj : objects) {
            objectOutput.add(obj);
        }
        return objectOutput;
    }

    /**
     * Reading a file line by line filling them in a list of String
     * 
     * @param file
     *            the file to be readen
     */
    public static LinkedList<String> readFile(File file) {
        LinkedList<String> objects = new LinkedList<String>();
        BufferedReader bfrd = null;
        String ligne;
        veriFile(file);
        boolean error = false;
        try {

            bfrd = new BufferedReader(new FileReader(file));

            while ((ligne = bfrd.readLine()) != null) {
                if (!(ligne.equals(""))) {
                    objects.add(ligne);
                }
            }

        } catch (FileNotFoundException exc) {

            System.out.println("File " + file.getName() + " Opening Error");
            error = true;

        } catch (IOException e) {

            System.out.println("Reading " + file.getName() + " Error");
            error = true;

        } finally {

            try {
                if (bfrd != null) {
                    bfrd.close();
                }
            } catch (IOException e) {
                System.out.println("Closing " + file.getName() + " Error");
                error = true;
            }
        }

        if (error) {
            System.exit(-1);
        }

        return objects;
    }

    /**
     * Verify if a directory exists
     * 
     * @param dir
     *            the directory
     */
    public static void veriDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("can't open " + dir.getAbsolutePath()
                    + ": No such file or directory");
            System.exit(-1);
        }
    }

    /**
     * Verify if a file exists
     * 
     * @param file
     *            the file
     */
    public static void veriFile(File file) {
        if (!file.exists() || !file.isFile()) {
            System.out.println("can't open " + file.getAbsolutePath()
                    + ": No such file or directory");
            System.exit(-1);
        }
    }

    /**
     * Verify that a given String is a valid regular expression.
     * 
     * @param regex
     *            regular expression to validate
     * 
     * @return returns compiled Pattern corresponding to the given String
     */
    public static Pattern verifyPattern(String regex) {
        Pattern p = null;
        try {
            p = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            System.out.println("Invalid regular expression: " + e.getMessage());
        }
        return p;
    }
}