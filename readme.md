apkparser
=========

Extracted from android-studio source code.

sdk compatibility
-----------------

Now supported only Java 1.8.

Problems with realisation `DefaultMutableTreeNode` through  `protected Vector<TreeNode> children;`
Errors when try cast from `children` to `List<ArchiveNode>`.

dependencies
------------

**smali/backsmali:2.2.4** newer version have changed interface, need update code

**com.google:archivepatcher** library from local repository. Used for calculate diff.