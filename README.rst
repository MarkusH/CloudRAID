======
README
======

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
- `javax.servlet.jsp
  <http://www.java2s.com/Code/JarDownload/javax/javax.servlet.jsp_2.0.0.v201101211617.jar.zip>`_
  >=2.0.0
- org.apache.ant >=1.8.2 (see `org.apache.ant`_)
- `org.apache.commons.el
  <http://www.java2s.com/Code/JarDownload/org.apache.commons/org.apache.commons.el_1.0.0.v201101211617.jar.zip>`_
  >=1.0.0
- `org.apache.commons.logging
  <http://www.java2s.com/Code/JarDownload/org.apache.commons/org.apache.commons.logging_1.0.4.v201101211617.jar.zip>`_
  >=1.0.4
- `org.eclipse.equinox.ds
  <http://www.java2s.com/Code/JarDownload/org.eclipse.equinox/org.eclipse.equinox.ds_1.3.1.R37x_v20110701.jar.zip>`_
  >=1.3.1
- `org.eclipse.equinox.http.jetty
  <http://www.java2s.com/Code/JarDownload/org.eclipse.equinox/org.eclipse.equinox.http.jetty_2.0.100.v20110502.jar.zip>`_
  >=2.0.100
- `org.eclipse.equinox.http.servlet
  <http://www.java2s.com/Code/JarDownload/org.eclipse.equinox/org.eclipse.equinox.http.servlet_1.1.200.v20110502.jar.zip>`_
  >=1.1.200
- `org.eclipse.equinox.util
  <http://www.java2s.com/Code/JarDownload/org.eclipse.equinox/org.eclipse.equinox.util_1.0.300.v20110502.jar.zip>`_
  >=1.0.300
- `org.eclipse.osgi.services
  <http://www.java2s.com/Code/JarDownload/org.eclipse.osgi/org.eclipse.osgi.services_3.3.0.v20110513.jar.zip>`_
  >=3.3.0
- `org.mortbay.jetty.server
  <http://repo1.maven.org/maven2/org/mortbay/jetty/jetty/6.1.23/jetty-6.1.23.jar>`_
  >=6.1.23
- `org.mortbay.jetty.util
  <http://repo1.maven.org/maven2/org/mortbay/jetty/jetty-util/6.1.23/jetty-util-6.1.23.jar>`_
  >=6.1.23

org.apache.ant
^^^^^^^^^^^^^^
.. note::

   The ant packages are part of the Eclipse Classic IDE and usually installed
   in ``/usr/share/eclipse/plugins/org.apache.ant_1.8.2.v20120109-1030``. The
   individual Jar files are not correctly *OSGi-ified*. You can download the
   single Java Archives but you will need to adjust the bundles!

- `ant <http://repo1.maven.org/maven2/org/apache/ant/ant/1.8.2/ant-1.8.2.jar>`_
- `ant-antlr
  <http://repo1.maven.org/maven2/org/apache/ant/ant-antlr/1.8.2/ant-antlr-1.8.2.jar>`_
- `ant-apache-bcel
  <http://repo1.maven.org/maven2/org/apache/ant/ant-apache-bcel/1.8.2/ant-apache-bcel-1.8.2.jar>`_
- `ant-apache-bsf
  <http://repo1.maven.org/maven2/org/apache/ant/ant-apache-bsf/1.8.2/ant-apache-bsf-1.8.2.jar>`_
- `ant-apache-log4j
  <http://repo1.maven.org/maven2/org/apache/ant/ant-apache-log4j/1.8.2/ant-apache-log4j-1.8.2.jar>`_
- `ant-apache-oro
  <http://repo1.maven.org/maven2/org/apache/ant/ant-apache-oro/1.8.2/ant-apache-oro-1.8.2.jar>`_
- `ant-apache-regexp
  <http://repo1.maven.org/maven2/org/apache/ant/ant-apache-regexp/1.8.2/ant-apache-regexp-1.8.2.jar>`_
- `ant-apache-resolver
  <http://repo1.maven.org/maven2/org/apache/ant/ant-apache-resolver/1.8.2/ant-apache-resolver-1.8.2.jar>`_
- `ant-apache-xalan2
  <http://repo1.maven.org/maven2/org/apache/ant/ant-apache-xalan2/1.8.2/ant-apache-xalan2-1.8.2.jar>`_
- `ant-commons-logging
  <http://repo1.maven.org/maven2/org/apache/ant/ant-commons-logging/1.8.2/ant-commons-logging-1.8.2.jar>`_
- `ant-commons-net
  <http://repo1.maven.org/maven2/org/apache/ant/ant-commons-net/1.8.2/ant-commons-net-1.8.2.jar>`_
- `ant-jai
  <http://repo1.maven.org/maven2/org/apache/ant/ant-jai/1.8.2/ant-jai-1.8.2.jar>`_
- `ant-javamail
  <http://repo1.maven.org/maven2/org/apache/ant/ant-javamail/1.8.2/ant-javamail-1.8.2.jar>`_
- `ant-jdepend
  <http://repo1.maven.org/maven2/org/apache/ant/ant-jdepend/1.8.2/ant-jdepend-1.8.2.jar>`_
- `ant-jmf
  <http://repo1.maven.org/maven2/org/apache/ant/ant-jmf/1.8.2/ant-jmf-1.8.2.jar>`_
- `ant-jsch
  <http://repo1.maven.org/maven2/org/apache/ant/ant-jsch/1.8.2/ant-jsch-1.8.2.jar>`_
- `ant-junit
  <http://repo1.maven.org/maven2/org/apache/ant/ant-junit/1.8.2/ant-junit-1.8.2.jar>`_
- `ant-launcher
  <http://repo1.maven.org/maven2/org/apache/ant/ant-launcher/1.8.2/ant-launcher-1.8.2.jar>`_
- `ant-netrexx
  <http://repo1.maven.org/maven2/org/apache/ant/ant-netrexx/1.8.2/ant-netrexx-1.8.2.jar>`_
- `ant-swing
  <http://repo1.maven.org/maven2/org/apache/ant/ant-swing/1.8.2/ant-swing-1.8.2.jar>`_
- `ant-testutil
  <http://repo1.maven.org/maven2/org/apache/ant/ant-testutil/1.8.2/ant-testutil-1.8.2.jar>`_

Native Libraries
================

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

Run
===

To run CloudRAID, you need to start the OSGi console with
``-Dorg.osgi.service.http.port=PORT``. Replace ``PORT`` with any unbound port
larger than 1024 if you don't have administrative privileges.

Please start the bundles in the following order:

#. org.eclipse.equinox.ds_1.3.1.R37x_v20110701
#. org.eclipse.equinox.http.jetty_2.0.100.v20110502
#. org.eclipse.equinox.http.servlet_1.1.200.v20110502
#. CloudRAID-Password
#. CloudRAID-Config
#. CloudRAID-Metadata
#. CloudRAID-Core
#. CloudRAID-RESTful
