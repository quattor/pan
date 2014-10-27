Running the Compiler
====================

To facilitate the use of the pan configuration language compiler in
different contexts, several mechanisms for running the compiler are
supported, ranging from direct invocation from the command line to use
within build frameworks like ant and maven.

The performance of the compiler can vary significantly depending on how
the compiler is invoked and on what options are used. Some general
points to keep in mind are:

-  For large builds, try to start the underlying Java Virtual Machine
   (JVM) only once. That is, avoid the command line interface and
   instead use one of the build framework integrations.

-  The pan compiler can be memory-intensive to medium to large-scale
   builds. Use the verbose output to see the allocated and used heap
   space. Increase the allocated memory for the JVM if the used memory
   exceeds about 80% of the total.

-  Other JVM optimizations and options can improve performance. Check
   out what options are available with your Java implementation and
   experiment with those options.

The following sections provide details on the supported mechanisms for
invoking the pan configuration language compiler.

Command Line
============

The compiler can be invoked from the command line by using ``panc``.
This is a script, which works in Unix-like environments, that starts a
Java Virtual Machine and invokes the compiler.

The full list of options can be obtained with the ``--help`` option or
by looking on the relevant man page.

Using ``java`` Command
======================

If the Java compiler class is being directly invoked via the ``java``
command, then the option ``-Xmx`` must be used to change the VM memory
available (for any reasonably sized compilation). For example to start
``java`` with 1024 MB of memory, the following command and options can
be used:

::

    java -Xmx1024M org.quattor.pan.Compiler [options...]

The same can be done for other options. The options are the same as for
the ``panc`` command, except that the java options parameter is not
supported.

Maven
=====

The pan compiler release contains a simple maven plug-in that will
perform a pan syntax check and build a simple set of files. The plug-in
is available from the central maven repository. To use this, you will
need to configure maven for that repository. A maven archetype is also
provided that can be used to generate a working skeleton that
demonstrates the pan maven plugin.

    **Warning**

    The options of the plug-in have changed from the previous version.
    They mirror those of the ``panc`` script. Details for the options
    are given below.

To generate a skeleton maven project from the archetype use the
following command (use the latest version of the archetype):

::

    $ mvn archetype:generate \
      -DarchetypeArtifactId=panc-maven-archetype \
      -DarchetypeGroupId=org.quattor.pan \
      -DarchetypeVersion=9.3

    ...

    Define value for property 'groupId': : org.example.pan
    Define value for property 'artifactId': : mysite
    Define value for property 'version':  1.0-SNAPSHOT: : 
    Define value for property 'package':  org.example.pan: : 
    Confirm properties configuration:
    groupId: org.example.pan
    artifactId: mysite
    version: 1.0-SNAPSHOT
    package: org.example.pan
     Y: : 

    ...

    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 19.690s
    [INFO] Finished at: Mon Feb 20 08:23:52 CET 2012
    [INFO] Final Memory: 9M/81M
    [INFO] ------------------------------------------------------------------------

As can be seen above, the process will ask for general information about
the project that you want to create. The process should end with a
"BUILD SUCCESS" and create a subdirectory with the maven project. In the
example, the subdirectory (and artifactId) are named "mysite".

Within this subdirectory ("mysite"), you can then invoke the entire
build process by doing the following:

::

    $ cd mysite/
    $ mvn clean install

    ...

    [INFO] --- panc-maven-plugin:9.2-SNAPSHOT:pan-check-syntax (check-syntax) @ mysite ---
    [INFO] 
    [INFO] --- panc-maven-plugin:9.2-SNAPSHOT:pan-build (build) @ mysite ---

    ...

    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 1.782s
    [INFO] Finished at: Mon Feb 20 08:27:51 CET 2012
    [INFO] Final Memory: 3M/81M
    [INFO] ------------------------------------------------------------------------

Again, this should end with a "BUILD SUCCESS". It will have generated
the machine profile in the ``target/profiles/node.example.org.xml``
file:

::

    $ cat target/profiles/node.example.org.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <nlist format="pan" name="profile">
        <list name="alpha">
            <long>1</long>
            <long>2</long>
            <long>3</long>
            <long>4</long>
        </list>
        <nlist name="beta">
            <string name="delta">OK</string>
            <boolean name="epsilon">true</boolean>
            <string name="gamma">OK</string>
            <double name="zeta">3.14</double>
        </nlist>
    </nlist>

The ``pom.xml`` file in the skeleton provides a good example on how to
run the plug-in. You can also obtain more detailed help via the maven
help system:

::

    $ mvn help:describe -Dplugin=panc -Ddetail=true

The following tables show the available parameters for the PanBuild and
PanCheckSyntax mojos.

+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| Parameter         | Description                                                                                                                                                                                                                        | Required                                       |
+===================+====================================================================================================================================================================================================================================+================================================+
| sourceDirectory   | Location of pan language sources.                                                                                                                                                                                                  | No. Default value: '${basedir}/src/main/pan'   |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| profiles          | Name of the profiles subdirectory inside of the sourceDirectory. Used to find the object profiles to build.                                                                                                                        | No. Default value: 'profiles'                  |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| verbose           | Whether to include a summary of the compilation, including number of profiles compiled and overall memory utilization.                                                                                                             | No. Default value: false                       |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| warnings          | Sets whether warnings are printed and whether they are treated as fatal errors. Allowed values are 'on', 'off', and 'fatal'.                                                                                                       | No. Default value: 'on'                        |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| debugNsInclude    | Pattern to apply to template namespace to determine whether to activate debugging output.                                                                                                                                          | No. Default value: '^$'                        |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| debugNsExclude    | Pattern to apply to template namespace to determine whether to exclude debugging output.                                                                                                                                           | No. Default value: '.+'                        |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| initialData       | A compile-time expression that evaluates to an dict. This dict is used as the root dict for all compiled object templates. A convenient mechanism for injecting build numbers and other metadata into the profiles.                | No. Default value: null (empty dict)           |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| outputDir         | The directory that will contain the output of the compilation.                                                                                                                                                                     | Yes.                                           |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| formats           | A comma-separated list of output formats to use. The accepted values are: "pan", "pan.gz", "xml", "xml.gz", "json", "json.gz", "txt", "dep" and "dot".                                                                             | No. Default value: 'pan,dep'                   |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| maxIteration      | Set the maximum number of iterations. This is a failsafe to avoid infinite loops.                                                                                                                                                  | No. Default value: 10000                       |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| maxRecursion      | Maximum number of recursive calls.                                                                                                                                                                                                 | No. Default value: 50                          |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| logging           | Enable different types of logging. The possible values are: "all", "none", "include", "call", "task", and "memory". Multiple values may be included as a comma-separated list. The value "none" will override any other setting.   | No.                                            |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| logFile           | The name of the file to use for logging information. This value must be defined in order to enable logging.                                                                                                                        | Yes, if logging attribute is used.             |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| nthread           | The number of threads to use for profile processing. The default value of zero will use the a number equal to the number of CPU cores on the machine.                                                                              | No. Default value: 0.                          |
+-------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+

Table: PanBuild Mojo Parameters

+-------------------+--------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| Parameter         | Description                                                                                                                    | Required                                       |
+===================+================================================================================================================================+================================================+
| sourceDirectory   | Location of pan language sources.                                                                                              | No. Default value: '${basedir}/src/main/pan'   |
+-------------------+--------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| verbose           | Whether to include a summary of the compilation, including number of profiles compiled and overall memory utilization.         | No. Default value: false                       |
+-------------------+--------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+
| warnings          | Sets whether warnings are printed and whether they are treated as fatal errors. Allowed values are 'on', 'off', and 'fatal'.   | No. Default value: 'on'                        |
+-------------------+--------------------------------------------------------------------------------------------------------------------------------+------------------------------------------------+

Table: PanCheckSyntax Mojo Parameters

Ant
===

Using an ant task to invoke the compiler allows the compiler to be
easily integrated with other machine management tasks. To use the pan
compiler within an ant build file, the pan compiler tasks must be
defined. This can be done with a task definition element like:

::

    <target name="define.panc.task">

      <taskdef resource="org/quattor/ant/panc-ant.xml">
        <classpath>
          <pathelement path="${panc.jar}" />
        </classpath>
      </taskdef>

    </target>

where the property ${panc.jar} points to the jar file ``panc.jar``
distributed with the pan compiler release.

There are four tasks defined:

``panc``
    Provides all of the functionality available through the compiler.

``panc-check-syntax``
    Checks only the syntax of the pan source files. This is the
    recommended way of doing a syntax check.

``panc-annotations``
    Processes panc annotations found in the templates and produces XML
    files with the resulting content.

``panc-version``
    Displays the pan compiler version.

Running the compiler can be done with tasks like the following:

::

    <target name="compile.cluster.profiles">

      <!-- Define the load path.  By default this is just the cluster area. -->
      <path id="pan.loadpath">
        <dirset dir="${basedir}" includes="**/*" />
      </path>

      <panc-check-syntax ...options... >
        <fileset dir="${basedir}/profiles" casesensitive="yes" includes="*.pan" />
      </panc-check-syntax>

      <panc ...options... >
        <path refid="pan.loadpath" />
        <fileset dir="${basedir}/profiles" casesensitive="yes" includes="*.pan" />
      </panc>

      <panc-annotations ...options... >
        <fileset dir="${basedir}/profiles" casesensitive="yes" includes="*.pan" />
      </panc-annotations>

    </target>
        

where ...options... is replaced with valid options (attributes) for the
pan compiler ant tasks. The following tables describe all of the
attributes supported by the these tasks (task ``panc-version`` accepts
no option).

+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| Option                    | Description                                                                                                                                                                                                                        | Required                               |
+===========================+====================================================================================================================================================================================================================================+========================================+
| debugNsInclude            | Pattern to apply to template namespace to determine whether to activate debugging output.                                                                                                                                          | No. Default value: '^$'                |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| debugNsExclude            | Pattern to apply to template namespace to determine whether to exclude debugging output.                                                                                                                                           | No. Default value: '.+'                |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| initialData               | A compile-time expression that evaluates to an dict. This dict is used as the root dict for all compiled object templates. A convenient mechanism for injecting build numbers and other metadata into the profiles.                | No. Default value: null (empty dict)   |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| includeRoot               | Directory to use as the root of the compilation.                                                                                                                                                                                   | Yes.                                   |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| includes                  | Set of directories below the include root to use in the compilation. This is a "glob".                                                                                                                                             | Yes.                                   |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| outputDir                 | The directory that will contain the output of the compilation.                                                                                                                                                                     | Yes.                                   |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| formats                   | A comma-separated list of output formats to use. The accepted values are: "pan", "pan.gz", "xml", "xml.gz", "json", "json.gz", "txt", "dep" and "dot".                                                                             | No. Default value: 'pan,dep'           |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| maxIteration              | Set the maximum number of iterations. This is a failsafe to avoid infinite loops.                                                                                                                                                  | No. Default value: 10000               |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| maxRecursion              | Maximum number of recursive calls.                                                                                                                                                                                                 | No. Default value: 50                  |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| logging                   | Enable different types of logging. The possible values are: "all", "none", "include", "call", "task", and "memory". Multiple values may be included as a comma-separated list. The value "none" will override any other setting.   | No.                                    |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| logFile                   | The name of the file to use for logging information. This value must be defined in order to enable logging.                                                                                                                        | Yes, if logging attribute is used.     |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| warnings                  | Sets whether warnings are printed and whether they are treated as fatal errors. Allowed values are 'on', 'off', and 'fatal'.                                                                                                       | No. Default value: 'on'                |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| verbose                   | Whether to include a summary of the compilation, including number of profiles compiled and overall memory utilization.                                                                                                             | No. Default value: false               |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| checkDependencies         | Whether or not to check dependencies and only build profiles that have not changed.                                                                                                                                                | No. Default value: true                |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| debugTask                 | Emit debugging messages for the ant task itself. If the value is 1, then normal debugging is turned on; if the value is greater than 1 then verbose debugging is turned on. A value of zero turns off the task debugging.          | No. Default value: 0                   |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| ignoreDependencyPattern   | A pattern which will select dependencies to ignore during the task's dependency calculation. The pattern will be matched against the namespaced template name.                                                                     | No. Default value: null                |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| batchSize                 | If set to a positive integer, the outdated templates will be processed in batches of batchSize.                                                                                                                                    | No. Default value: 0                   |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+
| nthread                   | The number of threads to use for profile processing. The default value of zero will use the a number equal to the number of CPU cores on the machine.                                                                              | No. Default value: 0                   |
+---------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------+

Table: Attributes for Ant Task ``panc``

+------------+--------------------------------------------------------------------------------------------------------------------------------+----------------------------+
| Option     | Description                                                                                                                    | Required                   |
+============+================================================================================================================================+============================+
| warnings   | Sets whether warnings are printed and whether they are treated as fatal errors. Allowed values are 'on', 'off', and 'fatal'.   | No. Default value: 'on'    |
+------------+--------------------------------------------------------------------------------------------------------------------------------+----------------------------+
| verbose    | Whether to include a summary of the compilation, including number of profiles compiled and overall memory utilization.         | No. Default value: false   |
+------------+--------------------------------------------------------------------------------------------------------------------------------+----------------------------+

Table: Attributes for Ant Task ``panc-check-syntax``

+-------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------------------+
| Option      | Description                                                                                                                                                        | Required              |
+=============+====================================================================================================================================================================+=======================+
| baseDir     | Base directory used to locate the templates if their names is a relative path and to build the relative path used to create output file if the path is absolute.   | Yes.                  |
+-------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------------------+
| outputDir   | Parent directory used to create output XML files. The output file name is built by appending the template relative path to this directory.                         | Yes.                  |
+-------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------------------+
| verbose     | If true, displays statistics after processing the annotations.                                                                                                     | No. Default: false.   |
+-------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------+-----------------------+

Table: Attributes for Ant Task ``panc-annotations``

Nested Elements
---------------

Some of the configuration options are specified via nested elements. The
``panc`` task supports all of these; the ``panc-check-syntax`` and
``panc-annotations`` task only supports the ``fileset`` nested element.

Fileset
~~~~~~~

Nested ``fileset`` elements specify the list of files to process with
the compiler. These are standard ant element and take all of the usual
attributes.

Path
~~~~

A nested ``path`` element specifies the list of include directories to
use during the compilation. This is a standard ant element and takes all
of the usual attributes.

Setting JVM Parameters
----------------------

If the compiler is invoked via the pan compiler ant task, then the
memory option can be added with the ANT\_OPTS environmental variable.

::

    export ="-Xmx1024M"

or

::

    setenv  "-Xmx1024M"

depending on whether you use a c-shell or a bourne shell. Other options
can be similarly added to the environmental variable. (The value is a
space-separated list.)

Invocation Inside Eclipse
=========================

If you use the default VM to run the pan compiler ant task, then you
will need to increase the memory when starting eclipse. From the command
line you can add the VM arguments like:

::

    eclipse -vmargs -Xmx<memory size>

You may also need to increase the memory in the "permanent" generation
for a Sun VM with

::

    eclipse -vmargs -XX:MaxPermSize=<memory size>

This will increase the memory available to eclipse and to all tasks
using the default virtual machine. For Max OS X, you will have to edit
the application "ini" file. See the eclipse instructions for how to do
this.

If you invoke a new Java virtual machine for each build, then you can
change the ant arguments via the run parameters. From within the "ant"
view, right-click on the appropriate ant build file, and then select
"Run As -> Ant Build...". In the pop-up window, select the JRE tab. In
the "VM arguments" panel, add the ``-Xmx`` option. The next build will
use these options. Other VM options can be changed in the same way.

The options can also be set using the "Window -> Preferences -> Java ->
Installed JREs" panel. Select the JRE you want use, click edit and add
the additional parameters in the "DefaultVM arguments" field.

Displaying the compiler version
===============================

There are different ways of displaying the pan compiler version,
depending on the invocation method.

+--------------------+-------------------------------+
| Invocation         | Command                       |
+====================+===============================+
| Java               | java -jar /path/to/panc.jar   |
+--------------------+-------------------------------+
| panc               | panc --version                |
+--------------------+-------------------------------+
| panc-annotations   | panc-annotations --version    |
+--------------------+-------------------------------+
| Ant                | task panc-version             |
+--------------------+-------------------------------+

Table: How to get panc version

