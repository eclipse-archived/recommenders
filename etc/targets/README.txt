RULE #1: YOU DON'T EDIT BUILD TARGET FILES IN THE EDITOR!
RULE #2: YOU DON'T EDIT BUILD TARGET FILES IN THE EDITOR!

These .target files define several different development environments. Some of 
these target platform definitions are used for building Recommenders with Maven; others
are defined for use inside your IDE as Eclipse target platforms. 

One for Eclipse 3.7 (e37.target),
one for Eclipse 4.2 (e42.target and e42-devel.target) etc.

NOTE:
When editing target definition files with 3.8 or 4.2, please note that "<?pde version="3.6"?>"
header is changed from "3.6" to "3.8". When you open these files than with say, Eclipse 3.7 the
fields in the target editor remain empty. You have to change the version field back to "3.6" with
a text editor manually and reopen the target editor again...  

>>>>>
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?pde version="3.6"?>

<target...
<<<<<<


== When target files get out dated ==
Sometimes the contents of an update site change and previously found features cannot be found
anymore and the update site fails to resolve all required plug-ins.

You now have to update the target files manually. But there is an easy way how to do this.
On the right of the first editor tab is an "update" button. Select the update site that has
changed, click the "update" button and wait until Eclipse returns. The missing libraries
probably show up again.

If not, call your service agent (i.e., send an email to the mailing-list)