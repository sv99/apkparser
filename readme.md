apkparser
=========

Extracted from android-studio source code.

sdk compatibility
-----------------

Now supported only Java 1.8.

Problems with realisation `DefaultMutableTreeNode` through  `protected Vector<TreeNode> children;`
Errors when try cast from `children` to `List<ArchiveNode>`.

В `IntlliJ Community` похожая проблема решена при помощи [amaembo/streamex](https://github.com/amaembo/streamex)

```java
// platform/dvcs-impl/src/com/intellij/dvcs/push/ui/PushLog.java
import one.util.streamex.StreamEx;

...

  private static List<CommitNode> collectSelectedCommitNodes(
        @NotNull List<DefaultMutableTreeNode> selectedNodes) {
        //addAll Commit nodes from selected Repository nodes;
    List<CommitNode> nodes = StreamEx.of(selectedNodes)
      .select(RepositoryNode.class)
      .toFlatList(node -> getChildNodesByType(node, CommitNode.class, true));
    // add all others selected Commit nodes;
    nodes.addAll(StreamEx.of(selectedNodes)
                   .select(CommitNode.class)
                   .filter(node -> !nodes.contains(node))
                   .toList());
    return nodes;
  }        
```

Решение проблемы: `StreamEx.select().toList()`

Нужно проверить.

dependencies
------------

**smali/backsmali:2.2.4** newer version have changed interface, need update code

**com.google:archivepatcher** library from local repository. Used for calculate diff.