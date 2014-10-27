
==================
Standard Functions
==================

Pan provides a large (and growing) number of standard functions. These
are treated as operators by the pan compiler implementation and are thus
highly optimized. Consequently, they should be preferred to writing your
own user-defined functions when possible. Because they are built into
the compiler, the argument processing is different than that for
user-defined functions. In particular, some arguments may be evaluated
only when necessary and ``null`` can be a valid function argument.

.. toctree::
   :maxdepth: 1
   :glob:

   standard-functions*
