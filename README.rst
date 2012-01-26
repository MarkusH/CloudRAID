README
======

License
-------

The application's license can be found in LICENSE.


Requirements
------------

- `HSQLDB <http://hsqldb.org/>`_ is the standard database system for the
  backend. CloudRAID is tested with HSQLDB 2.2.8. Therefore HSQLDB is
  required as long as no other wrapper for another database system is
  used. If you want to use another database, have a look at
  `de.dhbw.mannheim.cloudraid.persistence.DatabaseConnector`.

- `scribe-java <https://github.com/Markush2010/scribe-java>`_ from the
  linked fork. The original version has some restrictions and does
  therefore not work.

- `JSON-java <https://github.com/Markush2010/JSON-java>`_ from the
  linked fork. The original version has some restrictions and does
  therefore not work.

- `jersey-core
  <https://maven.java.net/service/local/repositories/releases/content/com/sun/jersey/jersey-core/1.11/jersey-core-1.11.jar>`_
  >= 1.11

- `jersey-server
  <https://maven.java.net/service/local/repositories/releases/content/com/sun/jersey/jersey-server/1.11/jersey-server-1.11.jar>`_
  >= 1.11

- `jsr311-api
  <http://search.maven.org/remotecontent?filepath=javax/ws/rs/jsr311-api/1.1.1/jsr311-api-1.1.1.jar>`_
  >= 1.1.1

- In order to run the unittests you need JUnit 4.


Native Libraries
----------------

For performance reasons crucial parts of the application are not written
in Java but in C.  The C source code is located in src/native.

To build the shared objects change to the native source folder and
execute::

    make clean (optional)
    make
    sudo make install

If you want to build a debug version of the libraries, execute::

    make clean (optional)
    make DEBUG=1
    sudo make install

Testing the C code::

    make clean (optional)
    make test (optional DEBUG=1)
    make run-test
