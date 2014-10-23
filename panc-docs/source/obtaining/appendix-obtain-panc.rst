Obtaining the Compiler
======================

Binary Distributions
====================

Current binary packages (v10.1 and later) as well as the compiler
documentation are available from the GitHub repository in a variety of
formats.

::

    https://github.com/quattor/pan/releases

Older releases (previous to 10.1) are available from SourceForge:

::

    http://sourceforge.net/projects/quattor/files/panc/

Source
======

The source for the pan compiler is managed through a git repository on
GitHub. The software can be checked out with the following command:

::

    git clone git://github.com/quattor/pan.git

This provides a *read-only* copy of the pan repository. Patches to the
compiler can be provided via GitHub pull requests.

The master branch is the main development branch. Although an effort is
made to ensure that this code functions correctly, there may be times
when it is broken. Released versions can be found through the named
branches and tags. Use the git commands:

::

    git branch -r
    git tag -l

to see the available branches and tags.

Building
--------

Correctly building the Java-implementation of the pan compiler requires
version 1.5.0 or later of a Java Development Kit (JDK). Many linux
distributions include the GNU implementation of Java. *The GNU
implementation cannot build or run the pan compiler correctly.* Full
versions of Java for linux, Solaris, and Windows can be obtained from
Oracle. Maven can be obtained from the Apache Foundation web site.

The build of the compiler is done via Apache Maven that also depends on
Java. For Maven to find the correct version of the compiler, the
environment variable JAVA\_HOME should be defined:

::

    export JAVA_HOME=<path to java area>

or

::

    setenv JAVA_HOME <path to java area> 

depending on the type of shell that you use. After that, the entire
build can be accomplished with:

::

    mvn clean package

where the current working directory is the root of the directory checked
out from subversion. The default build will compile all of the java
sources, run the unit tests, and package the compiler. Tarballs (plain,
gzipped, and bzipped) as well as a zip file are created on all
platforms. The build will also create an RPM on platforms that support
it. The final packages can be found in the ``target`` subdirectory.

    **Note**

    Current builds of the compiler are done with Maven 3; the build
    should work for any Maven version 2.2.1 or later.

Installation
============

The proper installation of the pan compiler depends on how it will be
used. If it will be used from the command line (either directly or
through another program), then the full installation from a binary
package should be done. However, if the compiler will be run via
``ant``, then one really only needs to install the ``panc.jar`` file.

Full Package Installation
-------------------------

Once you have a binary distribution of the compiler (either building it
from source or downloading a pre-built version), installation of the
java compiler should be relatively painless. The binary packages include
the code, scripts, and documentation of the compiler.

| *Tarballs/Zip File*.
| Untar/unzip the package in a convenient area and redefine the PATH
variable to include the ``bin`` subdirectory. You should then have
access to ``panc`` and the various log file analysis scripts from the
command line.

| *RPM*.
| Simply using the command ``rpm`` (as root) to install the package will
be enough. The scripts and binaries will be installed in the standard
locations on the system. The RPM is not relocatable. If you need to
install the compiler as a regular user, use one of the
machine-independent packages.

Using the compiler requires Java 1.5.0 or later to be installed on the
system. If you want to run the compiler from ant, then you must have ant
version 1.7.0 or later installed on your system.

Eclipse Integration
-------------------

To integrate the compiler in an Integrated Development Environment (IDE)
like eclipse, only the file ``panc.jar`` is needed, presuming that the
compiler will be called via the ant task. Build files that reference the
compiler must define the panc task and then may use the task to invoke
the compiler. See the documentation for invoking the compiler from ant.
