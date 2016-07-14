
.. _append:

append
======

Name
----

append -- adds a value to the end of a list

Synopsis
--------

list **append** (element *value*)

list **append** (list *target*, element *value*)

list **append** (variable_reference *target*, element *value*)

Description
-----------

The ``append`` function will add the given value to the end of the
target list. There are three variants of this function. For all of the
variants, an explicit ``null`` value is illegal and will terminate the
compilation with an error.

The first variant takes a single argument and always operates on
``SELF``. It will directly modify the value of ``SELF`` and give the
modified list (``SELF``) as the return value. If ``SELF`` does not
exist, is ``undef``, or is ``null``, then an empty list will be created
and the given value appended to that list. If ``SELF`` exists but is not
a list, an error will terminate the compilation. This variant cannot be
used to create a compile-time constant.

::

    # /result will have the values 1 and 2 in that order
    '/result' = list(1);
    '/result' = append(2);

The second variant takes two arguments. The first argument is a list
value, either a literal list value or a list calculated from a DML
block. This version will create a copy of the given list and append the
given value to the copy. The modified copy is returned. If the target is
not a list, then an error will terminate the compilation. This variant
can be used to create a compile-time constant as long as the target
expression does not reference information outside of the DML block by
using, for example, the ``value`` function.

::

    # /result will have the values 1 and 2 in that order
    # /x will only have the value 1
    '/x' = list(1);
    '/result' = append(value('/x'), 2);

The third variant also takes two arguments, where the first value is a
variable reference. This variant will take precedence over the second
variant. This variant will directly modify the referenced variable and
return the modified list. If the referenced variable does not exist, it
will be created. As for the other forms, if the referenced target exists
and is not a list, then an error will terminate the compilation.
``SELF`` or descendants of ``SELF`` can be used as the target. This
variant can be used to create a compile-time constant if the referenced
variable is an *existing* local variable. Referencing a global variable
(except via ``SELF``) is not permitted as modifying global variables
from within a DML block is forbidden.

::

    # /result will have the values 1 and 2 in that order
    '/result' = {
      append(x, 1); # will create local variable x
      append(x, 2);
    };


.. _base64_decode:

base64\_decode
==============

Name
----

base64\_decode -- decodes a string that has been encoded in base64 format

Synopsis
--------

string **base64\_decode** (string *encoded*)

Description
-----------

The ``base64_decode`` function will return the unencoded value of the
base64 (RFC 2045) encoded argument. If the argument is not a valid
base64 encoded value a fatal error will occur.

::

    # /result have the string value 'hello world'
    '/result' = base64_decode('aGVsbG8gd29ybGQ=');

.. _base64_encode:

base64\_encode
==============

Name
----

base64\_encode -- encodes a string in base64 format

Synopsis
--------

string **base64\_encode** (string *encoded)

Description
-----------


The ``base64_encode`` function will return the base64 (RFC 2045) encoded
format of the argument.

::

    # /result have the string value 'aGVsbG8gd29ybGQ='
    '/result' = base64_encode('hello world');

.. _clone:

clone
=====

Name
----

clone -- returns a clone (copy) of the argument

Synopsis
--------

element **clone** (element *arg*)

Description
-----------

The ``clone`` function may return a clone (copy) of the argument. If the
argument is a resource, the result will be a "deep" copy of the
argument; subsequent changes to the argument will not affect the clone
and vice versa. Because properties are immutable internally, this
function will not actually copy a property instead returning the
argument itself.

.. _create:

create
======

Name
----

create -- create a dict from a structure template

Synopsis
--------

dict **create** (string *tpl_name*, ...)

Description
-----------

The ``create`` function will return an dict from the named structure
template. The optional additional arguments are key, value pairs that
will be added to the returned dict, perhaps overwriting values from the
structure template. The keys must be strings that contain valid dict
keys (see Path Literals Section). The values can be any element. Null
values will delete the given key from the resulting dict.

::

    # description of CD mount entry with the device undefined
    # (in file 'mount_cdrom.pan')
    structure template mount_cdrom;
    'device' = undef;
    'path' = '/mnt/cdrom';
    'type' = 'iso9660';
    'options' = list('noauto', 'owner', 'ro');

    # use from within another template
    '/system/mounts/0' = create('mount_cdrom', 'device', 'hdc');

    # the above is equivalent to the following two lines
    '/system/mounts/0' = create('mount_cdrom');
    '/system/mounts/0/device' = 'hdc';

.. _debug:

debug
=====

Name
----

debug -- print debugging information to the console

Synopsis
--------

string **debug** (string *msg*)

string **debug** (string *fmt*, element *param*, ...)

Description
-----------

This function will print the given string to the console (on stdout) and
return the message as the result. The function also accepts format strings,
similar to the ``format`` function. The string has '[object] ' prepended
to it, where 'object' is the name of the object template. This
functionality must be activated either from the command line or via a
compiler option (see compiler manual for details). If this is not
activated, the function will not evaluate the argument and will return
undef.

.. _delete:

delete
======

Name
----
delete -- delete the element identified by the variable expression

Synopsis
--------

undef **delete** (variable\_expression *arg*)

Description
-----------

This function will delete the element identified by the variable
expression given in the argument and return undef. The variable
expression can be a simple or subscripted variable reference (e.g. x,
x[0], x['abc'][1], etc.). Only variables local to a DML block can be
modified with this function. Attempts to modify a global variable will
cause a fatal error. For subscripted variable references, this function
has the same effect as assigning the variable reference to null.

::

    # /result will contain the list ('a', 'c')
    '/result' = {
      x = list('a', 'b', 'c');
      delete(x[1]);
      x;
    };

.. _deprecated:

deprecated
==========

Name
----

deprecated -- print deprecation warning to console

Synopsis
--------

string **deprecated** (long *level*, string *msg*)

Description
-----------

This function will print the given string to the console (on stderr) and
return the message as the result, if ``level`` is less than or equal to
the deprecation level given as a compiler option. If the message is not
printed, the function returns undef. The value of ``level`` must be
non-negative.

.. _dict:

dict
====

Name
----

dict -- create an dict from the arguments

Synopsis
--------

dict **dict** (string *key*, element *property*, ...)

Description
-----------

The ``dict`` function returns a new dict consisting of the passed
arguments; the arguments must be key value pairs. All of the keys must
be strings and have values that are legal path terms (see Path Literals
Section).

::

    # resulting dict associates name with long value
    '/result' = dict(
      'one', 1,
      'two', 2,
      'three', 3,
    };

.. _digest:

digest
======

Name
----

digest -- creates a digest of a message using the specified algorithm

Synopsis
--------

string **digest** (string *algorithm*, string *message*)

Description
-----------

This function returns a digest of the message using the specified
algorithm. The valid algorithms are: ``MD2``, ``MD5``, ``SHA``,
``SHA-1``, ``SHA-256``, ``SHA-384``, and ``SHA-512``. The algorithm name
is not case sensitive.

.. _error:

error
=====

Name
----

error -- print message to console and abort compilation

Synopsis
--------

void **error** (string *msg*)

void **error** (string *fmt*, element *param*, ...)

Description
-----------

This function prints the given message to the console (stderr) and
aborts the compilation. The function also accepts format strings,
similar to the ``format`` function. The message has '[object ]' prepended
to it as a convenience. This function cannot appear neither in variable
subscripts nor in function arguments; a fatal error will occur if found
in either place.

::

    # a user-defined function requiring one argument
    function foo = {

      if (ARGC != 1) {
        error("foo(): wrong number of arguments: " + to_string(ARGC));
      };

      # normal processing...
    };

.. _escape:

escape
======

Name
----

escape -- escape non-alphanumeric characters to allow use as dict key

Synopsis
--------

string **escape** (string *str*)

Description
-----------

This function escapes non-alphanumeric characters in the argument so
that it can be used inside paths, for instance as an dict key.
Non-alphanumeric characters are replaced by an underscore followed by
the hex value of the character. If the string begins with a digit, the
initial digit is also escaped. If the argument is the empty string, the
returned value is a single underscore '\_'.

::

    # /result will have the value '1_2b1'
    '/result' = escape('1+1');

.. _exists:

exists
======

Name
----

exists -- determines if a variable expression, path, or template exists

Synopsis
--------

boolean **exists** (variable\_expression *var*)

boolean **exists** (string *path*)

boolean **exists** (string *tpl*)

Description
-----------

This function will return a boolean indicating whether a variable
expression, path, or template exists. If the argument is a variable
expression (with or without subscripts) then this function will return
true if the given variable exists; the value of referenced variable is
not used. If the argument is not a variable reference, the argument is
evaluated; the value must be a string. If the resulting string is a
valid external or absolute path, the path is checked. Otherwise, the
string is interpreted as a template name and the existence of this
template is checked.

Note that if the argument is a variable expression, only the existence
of the variable is checked. For example, the following code will always
leave ``r`` with a value of ``true``.

::

    v = '/some/absolute/path';
    r = exists(v);

If you want to test the path, remove the ambiguity by using a construct
like the following:

::

    v = '/some/absolute/path';
    r = exists(v+'');

The value of ``r`` in this case will be ``true`` if
``/some/absolute/path`` exists or ``false`` otherwise.

.. _file_contents:

file\_contents
==============

Name
----

file\_contents -- provide contents of file as a string

Synopsis
--------

string **file\_contents** (string *filename*)

Description
-----------

This function will return a string containing the contents of the named
file. The file is located using the standard source file lookup
algorithm. Because the load path is used to find the file, this function
may not be used to create a compile-time constant. If the file cannot be
found, an error will be raised.

.. _file_exists:

file\_exists
============

Name
----

file\_exists -- determine if the named file exists

Synopsis
--------

string **file\_exists** (string *filename*)

Description
-----------

This function will return a boolean indicating whether the named file
exists. The file is located using the standard source file lookup
algorithm. Because the load path is used to find the file, this function
may not be used to create a compile-time constant.

.. _first:

first
=====

Name
----

first -- initialize an iterator over a resource and return first entry

Synopsis
--------

boolean **first** (resource *r*, variable\_expression *key*,
                   variable\_expression *value*)

Description
-----------

This function resets the iterator associated with ``r`` so that it
points to the beginning of the resource. It will return ``false`` if the
resource is empty; ``true``, otherwise. If the resource is not empty,
then it will also set the variable identified by ``key`` to the child's
index and the variable identified by ``value`` to the child's value.
Either ``key`` or ``value`` may be ``undef``, in which case no
assignment is made. For a list resource ``key`` is the child's numeric
index; for an dict resource, the string value of the key itself. An
example of using ``first`` with a list:

::

    # compute the sum of the elements inside numlist
    numlist = list(1, 2, 4, 8);
    sum = 0;
    ok = first(numlist, k, v);
    while (ok) {
      sum = sum + v;
      ok = next(numlist, k, v);
    };
    # value of sum will be 15

An example of using ``first`` with an dict:

::

    # put the list of all the keys of table inside keys
    table = dict("a", 1, "b", 2, "c", 3);
    keys = list();
    ok = first(table, k, v);
      while (ok) {
      keys[length(keys)] = k;
      ok = next(table, k, v);
    };
    # keys will be ("a", "b", "c")

.. _format:

format
======

Name
----

format -- format a string by replacing references to parameters

Synopsis
--------

string **format** (string *fmt*, element *param*, ...)

Description
-----------

The ``format`` function will replace all references within the ``fmt``
string with the values of the referenced elements. This provides
functionality similar to the c-language's ``printf`` function. The
syntax of the ``fmt`` string follows that provided in the java language;
see the Formatter entry for full details. When passing a resource as an
argument, the string replacement field should be used.

.. _if_exists:

if\_exists
==========

Name
----

if\_exists -- check if a template exists, returning template name if it does

Synopsis
--------

string\|undef **if\_exists** (string *tpl*)

Description
-----------

The ``if_exists`` function checks if the named template exists on the
current load path. If it does, the function returns the name of the
template. If it does not, ``undef`` is returned. This can be used to
conditionally include a template:

::

    include {if_exists('my/conditional/template')};

This function should be used with caution as this brings in dependencies
based on the state of the file system and may cause dependency checking
to be inaccurate.

.. _index:

index
=====

Name
----

index -- finds substring within a string or element within a resource

Synopsis
--------

long **index** (string *sub*, string *arg*, long *start*)

long **index** (property *sub*, string *list*, long *start*)

string **index** (property *sub*, dict *arg*, long *start*)

long **index** (dict *sub*, list *arg*, long *start*)

string **index** (dict *sub*, dict *arg*, long *start*)

Description
-----------

The ``index`` function returns the location of a substring within a
string or an element within a resource. In detail the five different
forms perform the following actions.

The first form searches for the given substring inside the given string
and returns its position from the beginning of the string or ``-1`` if
not found; if the third argument is given, starts initially from that
position.

::

    '/s1' = index('foo', 'abcfoodefoobar'); # 3
    '/s2' = index('f0o', 'abcfoodefoobar'); # -1
    '/s3' = index('foo', 'abcfoodefoobar', 4); # 8

The second form searches for the given property inside the given list of
properties and returns its position or ``-1`` if not found; if the third
argument is given, starts initially from that position; it is an error
if ``sub`` and ``arg``\ ’s children are not of the same type.

::

    # search in a list of strings (result = 2)
    "/l1" = index("foo", list("Foo", "FOO", "foo", "bar"));

    # search in a list of longs (result = 3)
    "/l2" = index(1, list(3, 1, 4, 1, 6), 2);

The third form searches for the given property inside the given named
list of properties and returns its name or the empty string if not
found; if the third argument is given, skips that many matching
children; it is an error if ``sub`` and ``arg``\ ’s children are not of
the same type.

::

    # simple color table
    '/table' = dict('red', 0xf00, 'green', 0x0f0, 'blue', 0x00f);

    # result will be the string 'green'
    '/name1' = index(0x0f0, value('/table'));

    # result will be the empty string
    '/name2' = index(0x0f0, value('/table'), 1);

The fourth form searches for the given dict inside the given list of
dicts and returns its position or ``-1`` if not found. The comparison is
done by comparing all the children of ``sub``, these children must all
be properties. If the third argument is given, starts initially from
that position. It is an error if ``sub`` and ``arg``\ ’s children are
not of the same type or if their common children don’t have the same
type.

::

    # search a record in a list of records (result = 1, the second dict)
    '/ll1' = index(
                  dict('key', 'foo'),
                  list(
                       dict('key', 'bar', 'val', 101),
                       dict('key', 'foo')
                      )
                 );

    # search a record in a list of records starting at index (result = 1, the second dict)
    '/ll2' = index(
                  dict('key', 'foo'),
                  list(
                       dict('key', 'bar', 'val', 101),
                       dict('key', 'foo'),
                       dict('key', 'bar', 'val', 101),
                       dict('key', 'foo'),
                       dict('key', 'bar', 'val', 101),
                       dict('key', 'foo')
                      ),
                  1
                 );

The last form searches for the given dict inside the given dict of dicts
and returns its name or the empty string if not found. If the third
argument is given, the function skips that many matching children. It is
an error if ``sub`` and ``arg``\ ’s children are not of the same type or
if their common children don’t have the same type.

::

    # search for matching dict (result = 'b')
    '/nn1' = index(
                  dict('key', 'foo'),
                  dict(
                       'a', dict('key', 'bar', 'val', 101),
                       'b', dict('key', 'foo')
                      )
                 );

    # skip first match and return index of second match (result='d')
    '/nn2' = index(
                  dict('key', 'foo'),
                  dict(
                       'a', dict('key', 'bar', 'val', 101),
                       'b', dict('key', 'foo'),
                       'c', dict('key', 'bar', 'val', 101),
                       'd', dict('key', 'foo'),
                       'e', dict('key', 'bar', 'val', 101),
                       'f', dict('key', 'foo')
                      ),
                  1
                 );


.. _ip4_to_long:

ip4\_to\_long
=============

Name
----

ip4\_to\_long -- converts an IP address in dotted format with an optional bitmask to a
list of longs

Synopsis
--------

long[] **ip4\_to\_long** (string *ip*)

Description
-----------

The ``ip4_to_long`` function returns the binary representation of an
IPv4 address or network specification represented as a dotted string,
where the netmask part is optional, like ``inet_aton`` does in the C
standard library.

The first element of the return value is the binary representation of
the IP address, where the second, if present, is the binary
representation of the network mask.

This can be used for applying network masks and calculating network
ranges.

::

    variable NETWORK_RANGE_FOR_LOCALHOST = {
        l = ip4_to_long("127.0.0.1/8");
        l[0] & l[1];
    };

::

    variable BINARY_LOCALHOST = ip4_to_long("127.0.0.1");

.. _is_boolean:

is\_boolean
===========

Name
----

is\_boolean -- checks to see if the argument is a double

Synopsis
--------

boolean **is\_boolean** (element *arg*)

Description
-----------

The ``is_boolean`` function will return ``true`` if the argument is a
boolean value; it will return ``false`` otherwise.

.. _is_defined:

is\_defined
===========

Name
----

is\_defined -- checks to see if the argument is anything but undef or null

Synopsis
--------

boolean **is\_defined** (element *arg*)

Description
-----------

The ``is_defined`` function will return a ``true`` value if the argument
is anything but ``undef`` or ``null``; it will return ``false``
otherwise.

.. _is_double:

is\_double
==========

Name
----

is\_double --checks to see if the argument is a double

Synopsis
--------

boolean **is\_double** (element *arg*)

Description
-----------

The ``is_double`` function will return ``true`` if the argument is a
double value; it will return ``false`` otherwise.

.. _is_list:

is\_list
========

Name
----

is\_list -- checks to see if the argument is a double

Synopsis
--------

boolean **is\_list** (element *arg*)

Description
-----------

The ``is_list`` function will return ``true`` if the argument is a list;
it will return ``false`` otherwise.

.. _is_long:

is\_long
========

Name
----

is\_long -- checks to see if the argument is a long

Synopsis
--------

boolean **is\_long** (element *arg*)

Description
-----------

The ``is_long`` function will return ``true`` if the argument is a long
value; it will return ``false`` otherwise.

.. _is_dict:

is\_dict
========

Name
----

is\_dict -- checks to see if the argument is an dict

Synopsis
--------

boolean **is\_dict** (element *arg*)

Description
-----------

The ``is_dict`` function will return ``true`` if the argument is an
dict; it will return ``false`` otherwise.

.. _is_null:

is\_null
========

Name
----

is\_null -- checks to see if the argument is null

Synopsis
--------

boolean **is\_null** (element *arg*)

Description
-----------

The ``is_null`` function will return a ``true`` value if the argument is
``null``; it will return ``false`` otherwise.

.. _is_number:

is\_number
==========

Name
----

is\_number -- checks to see if the argument is a number

Synopsis
--------

boolean **is\_number** (element *arg*)

Description
-----------

The ``is_number`` function will return a ``true`` value if the argument
is a number (long or double); it will return ``false`` otherwise.

.. _is_property:

is\_property
============

Name
----

is\_property -- checks to see if the argument is a property

Synopsis
--------

boolean **is\_property** (element *arg*)

Description
-----------

The ``is_property`` function will return a ``true`` value if the
argument is a property (atomic value); it will return ``false``
otherwise.

.. _is_resource:

is\_resource
============

Name
----

is\_resource -- checks to see if the argument is a resource

Synopsis
--------

boolean **is\_resource** (element *arg*)

Description
-----------

The ``is_resource`` function will return a ``true`` value if the
argument is a resource (collection); it will return ``false`` otherwise.

.. _is_string:

is\_string
==========

Name
----

is\_string -- checks to see if the argument is a string

Synopsis
--------

boolean **is\_string** (element *arg*)

Description
-----------

The ``is_string`` function will return ``true`` if the argument is a
string value; it will return ``false`` otherwise.

.. _key:

key
===

Name
----

key -- returns name of child based on the index

Synopsis
--------

string **key** (dict *resource*, long *index*)

Description
-----------

This function returns the name of the child identified by its index,
this can be used to iterate through all the children of an dict. The
index corresponds to the key's position in the list of all keys, sorted
in lexical order. The first index is 0.

::

    '/table' = dict('red', 0xf00, 'green', 0x0f0, 'blue', 0x00f);

    '/keys' = {

      tbl = value('/table');
      res = '';
      len = length(tbl);
      idx = 0;
      while (idx < len) {
        res = res + key(tbl, idx) + ' ';
        idx = idx + 1;
      };

      if (length(res) > 0) splice(res, -1, 1);
      return(res);
    };
    # /keys will be the string 'blue green red '

.. _length:

length
======

Name
----

length -- returns size of a string or resource

Synopsis
--------

long **length** (string *str*, long *length*, resource *res*)

Description
-----------

Returns the size of the given string or the number of children of the
given resource.

.. _list:

list
====

Name
----

list -- create a new list consisting of the function arguments

Synopsis
--------

list **list** (element *elem*, ...)

Description
-----------

Returns a newly created list containing the function arguments.

::

    # creates an empty list
    '/empty' = list();

    # define list of two DNS servers
    '/dns' = list('137.138.16.5', '137.138.17.6');

.. _long_to_ip4:

long\_to\_ip4
=============

Name
----

long\_to\_ip4 -- converts a long into an IP address in dotted format

Synopsis
--------

string **long\_to\_ip4** (long *ip*)

Description
-----------

The ``long_to_ip4`` function converts an IP address represented as a
long into a string with numbers and dots, like ``inet_ntoa`` does in the
C standard library.

::

    "/ipaddr" = long_to_ip4(0x01020304); # 1.2.3.4

.. _match:

match
=====

Name
----

match -- checks if a regular expression matches a string

Synopsis
--------

boolean **match** (string *target*, string regex)

Description
-----------

This function checks if the given string matches the regular expression.

::

    # device_t is a string that can only be "disk", "cd" or "net"
    type device_t = string with match(self, ’ˆ(disk|cd|net)$’);

.. _matches:

matches
=======

Name
----

matches -- returns captured substrings matching a regular expression

Synopsis
--------

string[] **matches** (string *target*, string *regex*)

Description
-----------

This function matches the given string against the regular expression
and returns the list of captured substrings, the first one (at index 0)
being the complete matched string.

::

    # IPv4 address in dotted number notation
    type ipv4 = string with {
      result = matches(self, ’ˆ(\d+)\.(\d+)\.(\d+)\.(\d+)$’);
      if (length(result) == 0)
      return("bad string");
      i = 1;
      while (i <= 4) {
        x = to_long(result[i]);
        if (x > 255) return("chunk " + to_string(i) + " too big: " + result[i]);
        i = i + 1;
      };
      return(true);
    };

.. _merge:

merge
=====

Name
----

merge -- combine two resources into a single one

Synopsis
--------

resource **merge** (resource *res1*, resource *res2*, ...)

Description
-----------

This function returns the resource which combines the resources given as
arguments, all of which must be of the same type: either all lists or
all dicts. If more than one dict has a child of the same name, an error
occurs.

::

    # /z will contain the list 'a', 'b', 'c', 'd', 'e'
    '/x' = list('a', 'b', 'c');
    '/y' = list('d', 'e');
    '/z' = merge (value('/x'), value('/y'));

.. _next:

next
====

Name
----

next -- increment iterator over a resource

Synopsis
--------

boolean **next** (resource *res*, identifier *key*, identifier *value*)

Description
-----------

This function increments the iterator associated with ``res`` so that it
points to the next child element. The key and value of the next child
are stored in the named variables ``key`` and ``value``, either of which
could be ``undef``. The function returns ``true`` if the child exists,
or ``false`` otherwise.

.. _path_exists:

path\_exists
============

Name
----

path\_exists -- determines if a path exists

Synopsis
--------

boolean **path\_exists** (string *path*)

Description
-----------

This function will return a boolean indicating whether the given path
exists. The path must be an absolute or external path. This function
should be used in preference to the ``exists`` function to avoid an
ambiguity in handling the argument to ``exists`` as a path or variable
reference.

.. _prepend:

prepend
=======

Name
----

prepend -- adds a value to the beginning of a list

Synopsis
--------

list **prepend** (element *value*)

list **prepend** (list *target*, element *value*)

list **prepend** (variable\_reference *target*, element *value*)

Description
-----------

The ``prepend`` function will add the given value to the beginning of
the target list. There are three variants of this function. For all of
the variants, an explicit ``null`` value is illegal and will terminate
the compilation with an error.

The first variant takes a single argument and always operates on
``SELF``. It will directly modify the value of ``SELF`` and give the
modified list (``SELF``) as the return value. If ``SELF`` does not
exist, is ``undef``, or is ``null``, then an empty list will be created
and the given value prepended to that list. If ``SELF`` exists but is
not a list, an error will terminate the compilation. This variant cannot
be used to create a compile-time constant.

::

    # /result will have the values 2 and 1 in that order
    '/result' = list(1);
    '/result' = prepend(2);

The second variant takes two arguments. The first argument is a list
value, either a literal list value or a list calculated from a DML
block. This version will create a copy of the given list and prepend the
given value to the copy. The modified copy is returned. If the target is
not a list, then an error will terminate the compilation. This variant
can be used to create a compile-time constant as long as the target
expression does not reference information outside of the DML block by
using, for example, the ``value`` function.

::

    # /result will have the values 2 and 1 in that order
    # /x will only have the value 1
    '/x' = list(1);
    '/result' = prepend(value('/x'), 2);

The third variant also takes two arguments, where the first value is a
variable reference. This variant will take precedence over the second
variant. This variant will directly modify the referenced variable and
return the modified list. If the referenced variable does not exist, it
will be created. As for the other forms, if the referenced target exists
and is not a list, then an error will terminate the compilation.
``SELF`` or descendants of ``SELF`` can be used as the target. This
variant can be used to create a compile-time constant if the referenced
variable is an *existing* local variable. Referencing a global variable
(except via ``SELF``) is not permitted as modifying global variables
from within a DML block is forbidden.

::

    # /result will have the values 2 and 1 in that order
    '/result' = {
      prepend(x, 1); # will create local variable x
      prepend(x, 2);
    };

.. _replace:

replace
=======

Name
----

replace -- replace all occurrences of a regular expression

Synopsis
--------

string **replace** (string *regex*, string *repl*, string *target*)

Description
-----------

The ``replace`` function will replace all occurrences of the given
regular expression with the replacement string. The regular expression
is specified using the standard pan regular expression syntax. The
replacement string may contain references to groups identified within
the regular expression. The group references are indicated with a dollar
sign ($) followed by the group number. A literal dollar sign can be
obtained by preceding it with a backslash.

.. _return:

return
======

Name
----

return -- exit DML block with given value

Synopsis
--------

element **return** (element *value*)

Description
-----------

This function interrupts the processing of the current DML block and
returns from it with the given value. This is often used in user-defined
functions.

::

    function facto = {
      if (ARGV[0] < 2) return(1);
      return(ARGV[0] * facto(ARGV[0] - 1));
    };

.. _splice:

splice
======

Name
----

splice -- insert string or list into another

Synopsis
--------

string **splice** (string *str*, long *start*, long *length*, string *repl*)

list **splice** (list *list*, long *start*, long *length*, list *repl*)

Description
-----------

The first form of this function deletes the substring identified by
``start`` and ``length`` and, if a fourth argument is given, inserts
``repl``.

::

    '/s1' = splice('abcde', 2, 0, '12');  # ab12cde
    '/s2' = splice('abcde', -2, 1);       # abce
    '/s3' = splice('abcde', 2, 2, 'XXX'); # abXXXe

The second form of this function deletes the children of the given list
identified by ``start`` and ``length`` and, if a fourth argument is
given, replaces them with the contents of ``repl``.

::

    # will be the list 'a', 'b', 1, 2, 'c', 'd', 'e'
    '/l1' = splice(list('a','b','c','d','e'), 2, 0, list(1,2));

    # will be the list 'a', 'b', 'c', 'e'
    '/l2' = splice(list('a','b','c','d','e'), -2, 1);

    # will be the list 'a', 'b', 'XXX', 'e'
    '/l3' = splice(list('a','b','c','d','e'), 2, 2, list('XXX'));

    **Important**

    This function will *not* modify the arguments directly. Instead a
    copy of the input string or list is created, modified, and returned
    by the function. If you ignore the return value, then the function
    call will have no effect.

.. _split:

split
=====

Name
----

split -- split a string using a regular expression

Synopsis
--------

string[] **split** (string *regex*, string *target*)

string[] **split** (string *regex*, long *limit*, string *target*)

Description
-----------

The ``split`` function will split the ``target`` string around matches
of the given regular expression. The regular expression is specified
using the standard pan regular expression syntax. If the ``limit``
parameter is not specified, a default value of 0 is used. If the
``limit`` parameter is negative, then the function will match all
occurrences of the regular expression and return the result. A value of
0 will do the same, except that empty strings at the end of the sequence
will be removed. A positive value will return an array with at most
``limit`` entries. That is, the regular expression will be matched at
most ``limit``-1 times; the unmatched part of the string will be
returned in the last element of the list.

.. _substitute:

substitute
==========

Name
----

substitute -- substitute named values in string template

Synopsis
--------

string **substitute** (string *template*)
string **substitute** (string *template*, dict *substitutions*)

Description
-----------

The ``substitute`` function will replace all named values in the
template, delimited like '${myvar}', with associated values. If only one
argument is given, then the values will be looked up in the local and
global variable definitions. If two arguments are given, then the lookup
will be done in the explicit dict provided; this form will *not* use
local or global variable values.

::

    variable vars = dict('freq', 3, 'msg', 'hello');

    # produces string 'say hello 3 times'
    '/result' = substitute('say ${msg} ${freq} times', vars);

The substitution allows for recursive references. If you need to have
something like '${myvar}' literally in the string, then use '$${myvar}'.
If the template references an undefined value, then an
EvaluationException will be raised.

.. _substr:

substr
======

Name
----

substr -- extract a substring from a string

Synopsis
--------

string **substr** (string *target*, long *start*)

string **substr** (string *target*, long *start*, long *length*)

Description
-----------

This function returns the part of the given string characterised by its
``start`` position (starting from 0) and its ``length``. If ``length``
is omitted, returns everything to the end of the string. If ``start`` is
negative, starts that far from the end of the string; if ``length`` is
negative, leaves that many characters off the end of the string.

::

    "/s1" = substr("abcdef", 2); # cdef
    "/s2" = substr("abcdef", 1, 1); # b
    "/s3" = substr("abcdef", 1, -1); # bcde
    "/s4" = substr("abcdef", -4); # cdef
    "/s5" = substr("abcdef", -4, 1); # c
    "/s6" = substr("abcdef", -4, -1); # cde

.. _to_boolean:

to\_boolean
===========

Name
----

to\_boolean -- convert argument to a boolean value

Synopsis
--------

boolean **to\_boolean** (property *prop*)

Description
-----------

This function converts the given property into a boolean value. The
numeric values 0 and 0.0 are considered ``false``; other numbers,
``true``. The empty string and the string "false" (ignoring case) will
return ``false``; all other strings will return ``true``. The function
will not accept resources.

.. _to_double:

to\_double
==========

Name
----

to\_double -- convert argument to a double value

Synopsis
--------

double **to\_double** (property *prop*)

Description
-----------

This function converts the given property into a double.

If the argument is a string, then the string will be parsed to determine
the double value. Any valid literal double syntax can be used. Strings
that do not represent a valid double value will cause a fatal error.

If the argument is a boolean, then the function will return ``0.0`` or
``1.0`` depending on whether the boolean value is ``false`` or ``true``,
respectively.

If the argument is a long, then the corresponding double value will be
returned.

If the argument is a double, then the value is returned directly.

.. _to_long:

to\_long
========

Name
----

to\_long -- convert argument to a long value

Synopsis
--------

long **to\_long** (property *prop*)

long **to\_long** (property *prop*, long *radix*)

Description
-----------

This function converts the given property into a long value.

If the argument is a string, then the string will be parsed to determine
the long value. The string may represent a long value as an octal,
decimal, or hexadecimal value. The syntax is exactly the same as for
specifying literal long values. String values that cannot be parsed as a
long value will result in an error. If the radix is supplied, then it
will be used for the conversion. When using the radix, string values
should not be prefixed with the radix. That is, use
``to_long('ff', 16)`` or ``to_long('0xff')``.

If the argument is a boolean, then the return value will be either ``0``
or ``1`` depending on whether the boolean is ``false`` or ``true``,
respectively.

If the argument is a double value, then the double value is rounded to
the nearest long value.

If the argument is a long value, it is returned directly.

.. _to_lowercase:

to\_lowercase
=============

Name
----

to\_lowercase -- change all uppercase letters to lowercase

Synopsis
--------

string **to\_lowercase** (string *target*)

Description
-----------

The ``to_lowercase`` function will convert all uppercase letters in the
``target`` to lowercase. The United States (US) locale is forced for the
conversion to guarantee consistent behavior independent of the current
default locale.

.. _to_string:

to\_string
==========

Name
----

to\_string -- convert argument to a string value

Synopsis
--------

string **to\_string** (element *elem*)

Description
-----------

This function will convert the argument into a string. The function will
create a reasonable human-readable representation of all data types,
including lists and dicts.

.. _to_uppercase:

to\_uppercase
=============

Name
----

to\_uppercase -- change all lowercase letters to uppercase

Synopsis
--------

string **to\_uppercase** (string *target*)

Description
-----------

The to\_uppercase function will convert all lowercase letters in the
target to uppercase. The United States (US) locale is forced for the
conversion to guarantee consistent behavior independent of the current
default locale.

.. _traceback:

traceback
=========

Name
----

traceback -- print message and traceback to console

Synopsis
--------

string **traceback** (string *msg*)

Description
-----------

Prints the argument and a traceback from the current execution point to
the console (stderr). Value returned is the argument. An argument that
is not a string will cause a fatal error; the traceback will still be
printed. This may be selectively enabled or disabled via a compiler
option. See the compiler manual for details.

.. _unescape:

unescape
========

Name
----

unescape -- replaces escaped characters with ASCII characters

Synopsis
--------

string **unescape** (string *str*)

Description
-----------

This function replaces escaped characters in the given string ``str`` to
get back the original string. This is the inverse of the ``escape``
function.

.. _value:

value
=====

Name
----

value -- retrieve a value specified by a path

Synopsis
--------

element **value** (string *path*)

Description
-----------

This function returns the element identified by the given path, which
can be an external path. An error occurs if there is no such element.

::

    # /y will be 200
    '/x' = 100;
    '/y' = 2 * value('/x');
