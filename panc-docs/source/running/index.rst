
====================
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

.. toctree::
   :maxdepth: 1
   :glob:

   running*
