How to Contribute to Eclipse Code Recommenders
==============================================

![Logo](../plain/CONTRIBUTING/recommenders-logo.png)

Eclipse Code Recommenders makes you more productive when working with unfamiliar APIs.
Its intelligent code completion and automatically-generated API documentation tell you exactly how others have solved your problem in a similar situation before.

Find Out More
-------------

If you are unfamiliar with Eclipse Code Recommenders, we suggest that you visit the project’s website first.

- [Project website](http://www.eclipse.org/recommenders/)
- [Manual](http://www.eclipse.org/recommenders/manual/)
- [FAQ](http://www.eclipse.org/recommenders/faq/)

Sign the Contributor License Agreement
--------------------------------------

Want to contribute to Eclipse Code Recommenders?
Great!
But before your contribution can be accepted by the project, you need to create and electronically sign the Eclipse Foundation Contributor License Agreement (CLA).

- [Eclipse Foundation Contributor License Agreement](http://www.eclipse.org/legal/CLA.php)

Not sure whether there is a CLA for you on file already?
You can easily check this yourself using the [Contributor License Agreement Lookup Tool](https://projects.eclipse.org/user/cla/validate).

Search For and Fix Bugs
-----------------------

The Eclipse Code Recommenders project uses Bugzilla to track ongoing development and issues.

- [Browse issues](https://bugs.eclipse.org/bugs/buglist.cgi?product=Recommenders)
- [Create a new issue](https://bugs.eclipse.org/bugs/enter_bug.cgi?product=Recommenders)

Be sure to search for existing issue before you create another one.
Remember that contributions are always welcome!

Build Code Recommenders from Source
-----------------------------------

Building Code Recommenders from source is straight-forward.
But if some of the steps below fail – [let us know](https://dev.eclipse.org/mailman/listinfo/recommenders-dev "Developer Mailing List").

### Building on the Command Line

If you want to build Code Recommenders from the command line, you will need both [Git](http://www.git-scm.com/downloads) and  [Apache Maven](http://maven.apache.org/download.html), version 3.x.
First clone the Code Recommenders Git repository:

- `git clone http://git.eclipse.org/gitroot/recommenders/org.eclipse.recommenders.git`

**Windows users:** Code Recommenders uses Unix-style newlines (LF) throughout and fails the build if it finds Windows-style newlines (CRLF).
Please make sure that your Git configuration has `core.autocrlf` set to `false` to prevent Git from changing newlines to Windows-style.
(Changing newlines is unnecessary, as the Eclipse IDE can handle either style.)

After you have successfully cloned the repository, use Maven to build Eclipse Code Recommenders from scratch:

- `cd org.eclipse.recommenders`
- `mvn clean install`

That’s it.
After a few minutes wait, you should see a `BUILD SUCCESS`.
(The initial build may take a bit longer, as Maven automatically downloads anything required by the build.)

If you experience `OutOfMemoryError`s during the `mvn clean install` step, please set the `MAVEN_OPTS` environment variable as follows:

- `export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"`

**Windows users:** Substitute `export` with `set` in the above command.

### Building within the Eclipse IDE

If you want to contribute to Code Recommenders yourself, we suggest that you use Eclipse.

We recommend setting up Eclipse using [Eclipse Oomph](http://www.eclipse.org/oomph/).
Oomph will setup an up-to-date Eclipse IDE, install required and useful plugins, and import Code Recommenders’ source ready to build.

![Selecting an Eclipse Product to install](../plain/CONTRIBUTING/oomph-product-selection.png)

To get started with Oomph, [download the installer](https://wiki.eclipse.org/Eclipse_Oomph_Installer#Installation) and execute it.
Select the Eclipse product and version you wish to install, e. g. `Eclipse IDE for Java Developers`.
From the list of preconfigured projects double-click `Code Recommenders/Core` and click on `Next`.

![Setting up Eclipse for work on Code Recommenders with Oomph](../plain/CONTRIBUTING/oomph-recommenders.png)

You will need to supply Oomph with some information to setup an Eclipse installation for you.
Some of these settings, like folder locations or names are entirely up to you.
Leave `Target Platform:` at `None`.
For the `Recommenders Git or Gerrit Repository:` you can choose `SSH (read-write, Gerrit)`.
You need to [add your SSH public key](https://git.eclipse.org/r/#/settings/ssh-keys) to your Eclipse Gerrit account.
Alternatively, you can choose `HTTPS (read-write, Gerrit)`.
In that case, you need to [generate a HTTP password](https://git.eclipse.org/r/#/settings/http-password) for authentication.
The `Eclipse user ID for Bugzilla/Hudson:` is the e-mail address of your eclipse.org account.
The `Debug Port:` allows you to [remotely debug](http://javarevisited.blogspot.de/2011/02/how-to-setup-remote-debugging-in.html) the installed Eclipse.
**Important:** No two Eclipse instances can run with the same debug port simultaneously; ensure that each instance uses its own port.
The `Eclipse Git/Gerrit user ID:` is the username of your eclipse.org account.
When you are done, press `Next`.

![Setting up Eclipse for work on Code Recommenders with Oomph](../plain/CONTRIBUTING/oomph-variables.png)

The installation may take several minutes.
The freshly installed Eclipse will open during installation and automatically configure itself to let you work on Code Recommenders.

You are now set up to contribute code of your own to Eclipse Code Recommenders.
To test your contributions, just start an Eclipse runtime via the `tools/sdk.product` product configuration file residing in the `org.eclipse.recommenders` project.

Once you have done some changes to the code, you can submit your changes to Gerrit, where Code Recommenders committers can review your change.
To do so, right click on the `org.eclipse.recommenders` project and select `Team/Commit…`.
For the commit comment, provide the ID of the [Bugzilla](https://bugs.eclipse.org/bugs/buglist.cgi?product=Recommenders) issue you are working on in the format `Bug XXXXXX: Change description`.
(If you are not working on an Bugzilla issue, please consider opening a new issue first.)
Also, please click the second (Add Signed-off-by) and third (Compute Change-Id for Gerrit Code Review) button above the commit message field; this will automatically generate the necessary `Signed-off-by` and `Change-Id` headers.
(Do not be alarmed if the generated ID shows as a all zeros; a proper ID will be generated once you submit the change.)
When you have composed your commit message, click on `Commit`.
Next, right click on the `org.eclipse.recommenders` project again and select `Team/Remote/Push to Gerrit…`.
Select `refs/for/master` Gerrit branch, then click on `Finish`.
Congratulations, you have contributed your first change to Code Recommenders!

Other committers will look at your code and provide feedback.
Do not be alarmed if your change is not immediately merged; most changes require a bit of back-and-forth between contributors and committers.

Releasing a New Version of Code Recommenders
--------------------------------------------

Note: The following is of concern only to committers to Eclipse Code Recommenders.

Depending on whether you are performing a new maintenance, minor, or major release, the following steps differ.

### Git (Maintenance Release)

To release a new **maintenance version** (same major/minor version, different micro version) of Code Recommenders, perform the following steps:

- `export MAINTENANCE_VERSION=x.y.z`
- `git fetch`
- `git checkout origin/maintenance`
- `git clean -df`
- `mvn clean -Dtycho.mode=maven`
- `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -Dtycho.mode=maven -Dproperties=recommendersVersion -DnewVersion=${MAINTENANCE_VERSION}`
- `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -Dtycho.mode=maven -Dartifacts=$(basename plugins/*/ tests/*/ features/*/ | paste -sd "," - ) -DnewVersion=${MAINTENANCE_VERSION}-SNAPSHOT`
- `mvn tidy:pom`
- `git commit -a -m "[releng] ${MAINTENANCE_VERSION}"`
- Make sure that a `Change-Id` and `Signed-off-by` header are part of the commit message.
- `git push origin HEAD:refs/for/maintenance`

Thereafter, switch to the next (SNAPSHOT) version (**unless** this is going to be the last release with this major/minor version):

- `export NEXT_MAINTENANCE_VERSION=x.y.(z+1)`
- `git checkout HEAD^ -- '*'`
- `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -Dtycho.mode=maven -Dproperties=recommendersVersion -DnewVersion=${NEXT_MAINTENANCE_VERSION}-SNAPSHOT`

The version numbers of the required `org.eclipse.recommenders.*` bundles will now have to be updated in the `META-INF/MANIFEST.MF` files of each project.
To do this perform the following three steps:

- `export SECOND_NEXT_MAINTENANCE_VERSION=x.y.(z+2)`
- `find plugins -type f -iname "MANIFEST.MF" -print | xargs sed -i.bak "s/\(org.eclipse.recommenders.[a-zA-Z0-9.]*;bundle-version=[[)\"]*\)${MAINTENANCE_VERSION},${NEXT_MAINTENANCE_VERSION}\([)\"]*\)/\1${NEXT_MAINTENANCE_VERSION},${SECOND_NEXT_MAINTENANCE_VERSION}\2/"`
- `find plugins -type f -iname "*.bak" | xargs rm`

Manually bump the version in the `feature/requires/import` elements of `features/*/feature.xml` to `${NEXT_VERSION}` (except for `feature/org.eclipse.recommenders.feature.rcp/feature.xml`, where a version of 2.0.0.qualifier is intended).

- `git commit -a -m "[releng] ${NEXT_MAINTENANCE_VERSION}-SNAPSHOT"`
- Make sure that a `Change-Id` and `Signed-off-by` header are part of the commit message.
- `git push origin HEAD:refs/for/maintenance`

Wait till **both** commits have been built successfully by [Gerrit code review](https://git.eclipse.org/r/#/q/project:recommenders/org.eclipse.recommenders), only then submit the first one.
Then wait till the [maintenance Hudson build](https://hudson.eclipse.org/recommenders/job/org.eclipse.recommenders-maintenance/) is successful, then check out the merge commit and tag it.

* `git fetch`
* `git checkout origin/maintenance`
* `git tag v${MAINTENANCE_VERSION}`
* `git push origin v${MAINTENANCE_VERSION}`

Submit the second change.

After both [builds](https://hudson.eclipse.org/recommenders/job/org.eclipse.recommenders-maintenance/) have been successful, promote the release build to the [maintenance-milestones](download.eclipse.org/recommenders/updates/maintenance-milestones/) and [stable](download.eclipse.org/recommenders/updates/stable/) update sites:

- In [Hudson](https://hudson.eclipse.org/recommenders/job/org.eclipse.recommenders-maintenance/), select the release build.
- Select *Promotion Status* and start the `milestones` jobs.
- Enter a `MILESTONES_VERSION` parameter of `v${MAINTENANCE_VERSION}.R`.
- Select *Promotion Status* and start the `stable` jobs.
- Enter a `STABLE_VERSION` parameter of `v${MAINTENANCE_VERSION}`.
- Select *Configure* and assign a *DisplayName* of `v${MAINTENANCE_VERSION}`.

The new maintenance version is now available for download.

### Git (Minor or Major Release)

To release a new **minor or major version** (different major/minor version, micro version of zero) of Code Recommenders, perform the following steps:

Make sure that the [head of the `maintenance` branch](https://git.eclipse.org/c/recommenders/org.eclipse.recommenders.git/log/?h=maintenance) is tagged with a release version; otherwise, the steps below may **lose** commits.

- `export MASTER_VERSION=x.y.0`
- `git fetch`
- `git checkout origin/master`
- `git clean -df`
- `mvn clean -Dtycho.mode=maven`
- `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -Dtycho.mode=maven -Dproperties=recommendersVersion -DnewVersion=${MASTER_VERSION}`
- `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -Dtycho.mode=maven -Dartifacts=$(basename plugins/*/ tests/*/ features/*/ | paste -sd "," - ) -DnewVersion=${MASTER_VERSION}-SNAPSHOT`
- `mvn tidy:pom`
- `git commit -a -m "[releng] ${MASTER_VERSION}"`
- Make sure that a `Change-Id` and `Signed-off-by` header are part of the commit message.
- `git push --force origin HEAD:refs/for/maintenance`

Thereafter, switch to the next minor or major (SNAPSHOT) version:

- `export NEXT_MASTER_VERSION=x.(y+1).0`
- `git checkout origin/master`
- `git clean -df`
- `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -Dtycho.mode=maven -Dproperties=recommendersVersion -DnewVersion=${NEXT_MASTER_VERSION}-SNAPSHOT`

The version numbers of the required `org.eclipse.recommenders.*` bundles will now have to be updated in the `META-INF/MANIFEST.MF` files of each project.
To do this perform the following three steps:

- `export MASTER_VERSION_MAINTENANCE=x.(y+1).1`
- `export NEXT_MASTER_VERSION_MAINTENANCE=x.(y+1).1`
- `find plugins -type f -iname "MANIFEST.MF" -print | xargs sed -i.bak "s/\(org.eclipse.recommenders.[a-zA-Z0-9.]*;bundle-version=[[)\"]*\)${MASTER_VERSION},${MASTER_VERSION_MAINTENANCE}\([)\"]*\)/\1${NEXT_MASTER_VERSION_MAINTENANCE},${NEXT_MASTER_VERSION_MAINTENANCE}\2/"`
- `find plugins -type f -iname "*.bak" | xargs rm`

Manually bump the version in the `feature/requires/import` elements of `features/*/feature.xml` to `${NEXT_MASTER_VERSION}` (except for `feature/org.eclipse.recommenders.feature.rcp/feature.xml`, where a version of 2.0.0.qualifier is intended).

- `git commit -a -m "[releng] ${NEXT_MASTER_VERSION}-SNAPSHOT"`
- Make sure that a `Change-Id` and `Signed-off-by` header are part of the commit message.
- `git push origin HEAD:refs/for/master`

### Bugzilla

To make the release version and next version known to Bugzilla, perform the following steps:

* Log into the [Bugzilla manager](https://dev.eclipse.org/committers/bugs/bugz_manager.php).
* Edit the `technology.recommenders` project.
* Select *Add bugzilla version* and add the **release** version in question.
* Select *Add bugzilla milestone* and add the **next** version with a *Sortkey* using the following pattern: 1-digit major version, 1-digit minor version, 3-digit micro version.

To ensure that all users of the Automated Error Reporting receive notifications that a given bug has been fixed in the release version, perform the following steps:

* Search for all [issues closed in the release version](https://bugs.eclipse.org/bugs/buglist.cgi?bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&list_id=12164819&product=Recommenders&target_milestone=RELEASE_VERSION).
* Select *Change Several Bugs at Once*.
* Enter the following text in *Status Whiteboard*: `Fixed in Code Recommenders v${RELEASE_VERSION}. Please update.`
* *Commit* the change.
