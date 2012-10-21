======
README
======

Version: 1.0.0

License
=======

The application's license can be found in LICENSE.


Requirements
============

Core Application
----------------

- `HSQLDB <http://hsqldb.org/>`_ is the standard database system for the
  backend. CloudRAID is tested with HSQLDB 2.2.8. Therefore HSQLDB is required
  as long as no other wrapper for another database system is used. If you want
  to use another database, have a look at
  `de.dhbw.mannheim.cloudraid.persistence.DatabaseConnector`.

- `scribe-java <https://github.com/Markush2010/scribe-java>`_ from the linked
  fork. The original version has some restrictions and does therefore not work.
  Downloads can be found at
  `<https://github.com/Markush2010/scribe-java/downloads>`_

- `JSON-java <https://github.com/Markush2010/JSON-java>`_ from the linked fork.
  The original version has some restrictions and does therefore not work.
  Downloads can be found at
  `<https://github.com/Markush2010/JSON-java/downloads>`_

- `MiGBase64 <https://github.com/Markush2010/MiGBase64>`_ from the
  linked fork, since the original version is not an OSGi bundle.
  Downloads can be found at
  `<https://github.com/Markush2010/MiGBase64/downloads>`_

- In order to run the unittests you need JUnit 4.

RESTful API
-----------
The RESTful API requires additional dependencies that are stated here:

- `javax.servlet
  <http://repo1.maven.org/maven2/org/eclipse/jetty/orbit/javax.servlet/2.5.0.v201103041518/javax.servlet-2.5.0.v201103041518.jar>`_
  >= 2.5.0.v201103041518
- `org.eclipse.equinox.ds
  <http://www.java2s.com/Code/JarDownload/org.eclipse.equinox/org.eclipse.equinox.ds_1.3.1.R37x_v20110701.jar.zip>`_
  >=1.3.1
- `org.eclipse.equinox.http.servlet
  <http://www.java2s.com/Code/JarDownload/org.eclipse.equinox/org.eclipse.equinox.http.servlet_1.1.200.v20110502.jar.zip>`_
  ==1.1.200
- `org.eclipse.equinox.util
  <http://www.java2s.com/Code/JarDownload/org.eclipse.equinox/org.eclipse.equinox.util_1.0.300.v20110502.jar.zip>`_
  >=1.0.300
- `org.eclipse.osgi.services
  <http://www.java2s.com/Code/JarDownload/org.eclipse.osgi/org.eclipse.osgi.services_3.3.0.v20110513.jar.zip>`_
  >=3.3.0


Native Libraries
================

For performance reasons crucial parts of the application are not written
in Java but in C.  The C source code is located in src/native.

There are two ways to compile and use the shared objects:

#. Compile it for your architecture (x86 or x86_64), and install it to /usr/lib.
#. Compile it for x86 and x86_64, and install it into the CloudRAID core/lib directory.

For both ways make sure the JAVA_HOME variable is set correctly. If not, the build
will fail since the file jni.h cannot be located.

First Approach
--------------

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

Second Approach
---------------

Build the shared objects for both architectures and copy them
into core/lib::

   make clean (optional, but recommended)
   make crosscompile

If you want to compile only for one architecture, use for x86::

   make clean (optional)
   make bundlecompile ARCH=-m32
   make bundleinstall

and use for x86_64::

   make clean (optional)
   make bundlecompile ARCH=-m64
   make bundleinstall

Note that the ARCH parameter is not optional. You have to define it for the bundlecompile command.

Run
===

To run CloudRAID, you need to start the OSGi console with
``-Dorg.osgi.service.http.port=PORT``. Replace ``PORT`` with any unbound port
larger than 1024 if you don't have administrative privileges.

Please start the bundles in the following order:

#. org.eclipse.equinox.ds_1.3.1.R37x_v20110701
#. org.eclipse.equinox.http.servlet_1.1.200.v20110502
#. CloudRAID-Password
#. CloudRAID-Config
#. CloudRAID-Metadata
#. CloudRAID-Core
#. CloudRAID-RESTful
