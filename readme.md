apkparser - apkanalyzer
=======================

Extracted from android-studio source code.

apkparser - project and library.

apkanalizer - command line utility.

update stages
-------------

**1. integrate external diff library**

[com.google:archivepatcher](https://github.com/google/archive-patcher) - not available
in the **jcenter** repositorylibrary. In the google build used from local precompiled repository.

from local repository. Used for calculate diff.

**2. add StreamEx**

Add [StreamEx](https://github.com/amaembo/streamex) for compatible with never java version
using `DefaultMutableTreeNode`.
An example of usage looked at the source of the [intellij-community](https://github.com/JetBrains/intellij-community) 

**3. update smali version**

Move to [google/smali](https://github.com/google/smali) version 3.0.2

**4. move to JUnit5**

Update all tests to JUnit5

to do
-----

**5. built-in aapt2 utility**

Get aapt2 from maven. Realization from [bundletool](https://github.com/google/bundletool)

**6. replace compare unit**

Replace external compare unit based on the archivepatcher.
Now archivepatcher load both source unit and calculate diff patch. 
This patch used in the main program for display difference.

**7. compare dex units**

Compare dex units - naming and methods body.

**8. compare dex show methods diff**

Show methods diff.

diff
----

Calculate two different diffs.

DiffPatcher simple calculate archive element sizes and show total.

FileByFilePatcher used [bsdiff](https://github.com/mendsley/bsdiff) startegy, for calculation binary diff.
bsdiff produces smaller binary patches.

dependencies
------------

**googl/smali:3.02** google version, forked from original version 2.5.2

**com.google:archivepatcher** library from local repository. Used for calculate diff.
