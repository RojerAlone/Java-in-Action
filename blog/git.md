# Git 笔记

## git cherry-pick
`git cherry-pick` 的作用是从提交历史中"pick" commit 到当前分支中。

现在 master 分支中的一些东西在其他分支中进行了修改，但是不想 merge 分支到 master 中，那么就可以用到 `git cherry-pick` 命令了（也可以用 `git format-patch`，不过比较麻烦）。

master 分支如下所示：
```sbtshell
rojeralone@root ~/IdeaProjects/git
 % git log
commit c64811303484755ddb69e46ce4e6731688c6d181
Author: rojeralone
Date:   Wed Dec 20 20:19:10 2017 +0800

    test3

commit 4e59c29d4637127703f0d93ed78d707f53c19d75
Author: rojeralone
Date:   Wed Dec 20 20:18:23 2017 +0800

    test2

commit 2dd5f9c269f3528c23252edc433f2680e2dc91ab
Author: rojeralone
Date:   Wed Dec 20 20:18:07 2017 +0800

    test1

```

test 分支如下所示：
```sbtshell
rojeralone@root ~/IdeaProjects/git
 % git log   
commit 251f5485253a5e9f095cd6049d30c4c9500b71e9
Author: rojeralone
Date:   Wed Dec 20 20:42:27 2017 +0800

    test5

commit 75804e53dc7a0f5f737e0cc682050d5e5408d582
Author: rojeralone
Date:   Wed Dec 20 20:31:03 2017 +0800

    test4

commit c64811303484755ddb69e46ce4e6731688c6d181
Author: rojeralone
Date:   Wed Dec 20 20:19:10 2017 +0800

    test3

commit 4e59c29d4637127703f0d93ed78d707f53c19d75
Author: rojeralone
Date:   Wed Dec 20 20:18:23 2017 +0800

    test2

commit 2dd5f9c269f3528c23252edc433f2680e2dc91ab
Author: rojeralone
Date:   Wed Dec 20 20:18:07 2017 +0800

    test1

```
现在我想把 test4 这个 commit 添加到 master 中，就可以执行 `git cherry-pick 75804e53dc7a0f5f737e0cc682050d5e5408d582` 这个命令：
```sbtshell
rojeralone@root ~/IdeaProjects/git
 % git cherry-pick 75804e53dc7a0f5f737e0cc682050d5e5408d582
[master c683d04] test4
 Date: Wed Dec 20 20:31:03 2017 +0800
 1 file changed, 0 insertions(+), 0 deletions(-)
 create mode 100644 test4

```
这样就可以只把一次提交合并到当前分支了。
## git rebase