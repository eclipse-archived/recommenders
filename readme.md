Code Recommenders
=================

![Logo](../plain/readme/recommenders-logo.png)

Eclipse Code Recommenders makes you more productive when working with unfamiliar APIs.
Its intelligent code completion and automatically-generated API documentation tell you exactly how others have solved your problem in a similar situation before.

Building Code Recommenders from Source
--------------------------------------

Building Code Recommenders from source is straight-forward.
But if some of the steps below fail – [let us know](https://dev.eclipse.org/mailman/listinfo/recommenders-dev "Developer Mailing List").

### Building on the Command Line

If you want to build Code Recommenders from the command line, you will need [Apache Maven](http://maven.apache.org/download.html), version 3.x.

    $ git clone http://git.eclipse.org/gitroot/recommenders/org.eclipse.recommenders.git
    $ cd org.eclipse.recommenders
    $ mvn clean install

That’s it.
After a few minutes wait, you should see a `BUILD SUCCESS`.
(The initial build may take a bit longer, as Maven automatically downloads anything required by the build.)

**Windows users:** Code Recommenders uses Unix-style newlines (LF) throughout and fails the build if it finds Windows-style newlines (CRLF).
Please make sure that your Git configuration has `core.autocrlf` set to `false` to prevent Git from changing newlines to Windows-style.
(Changing newlines is unnecessary, as the Eclipse IDE can handle either style.)

### Building within the Eclipse IDE

If you want to contribute to Code Recommenders yourself, we suggest that you use Eclipse.

We recommend the latest [Eclipse IDE for Java and DSL Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-and-dsl-developers/keplersr1).
This package already contains all the required and most of the recommended features for building Code Recommenders.

The following features are **required** for building Code Recommenders:

* [Eclipse Java Development Tools](http://www.eclipse.org/jdt/)
* [Eclipse Plug-in Development Environment](http://www.eclipse.org/pde/)
* [Xtend SDK](http://www.eclipse.org/xtend/),
* [Maven Integration for Eclipse](http://www.eclipse.org/m2e/)

The following features are **recommended** for building Code Recommenders:

* [Eclipse Git Team Provider](http://www.eclipse.org/egit/)
* [Workspace Mechanic](https://code.google.com/a/eclipselabs.org/p/workspacemechanic/)
* [Code Recommenders Developer Tools](http://www.eclipse.org/recommenders/)

After you have installed all necessary features, you can import the source into your Eclipse IDE.
But first you need to clone the Git repository and _once_ build Code Recommenders on the command line.
(See above for how to do this.)

After the command-line build has been successful, import all projects into your Eclipse workspace using the _Existing Maven Projects_ wizard.
Here, select the `org.eclipse.recommenders` as _root directory_.
Upon clicking _Next_, the wizard should prompt you with a list of _Maven plugin connectors_ to set up.
Simply click _Finish_ to install all necessary connectors.
(Depending on which connectors still need to be installed, you may have to restart Eclipse.)

Once the projects have been imported, many of them still contain errors.
This is to be expected, as Eclipse cannot yet find all their required dependencies.
To fix this, you need to set a target platform which points Eclipse to these dependencies.
Open either the `kepler.target` or `luna.target` file residing in the `kepler` or `luna` project, respectively, with the _Target Editor_.
Wait until Eclipse _completely_ resolved the target definition.
Only then click on _Set as target platform_ in the upper right corner of the editor.
This causes Eclipse to build all projects again.
Once this re-build is done, there should be no erroneous projects.

If you followed our recommendation and installed Workspace Mechanic, you should now open the _Workspace Mechanic_ preferences and configure your `org.eclipse.recommenders/tools/mechanic` directory as a _Task Source_.
In case Workspace Mechanic finds any of your settings amiss, just let it fix them for you.

You are now set up to contribute code of your own to Eclipse Code Recommenders.
To test your contributions, just start an Eclipse runtime via the `tools/ide.product` product configuration file residing in the `org.eclipse.recommenders` project.
