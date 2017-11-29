# git-depbranch

Easily manage inter-branch dependencies in Git.

## Introduction

When working with a Git repository, a developer may face two conflicting
pressures. On the one hand, it is extremely convenient to put all of your work
into a single Git branch. This way, the user does not need to worry about which
branch a new feature that she is developing may need to go into. However,
putting many code changes into a single branch often makes code review hard, if
not impossible. Additionally, separating features with interdependencies into
different branches creates a new complexity: each branch needs to be based off
of a commit which contains all of the dependencies required by the feature.
Often, what happens to me is that I end up writing a bunch of git commands
chained together on the command line to make a commit in a specific branch and
merge them all together in the right order to make sure all of my downstream
features have the fix.

Instead of dealing with this mess manually, this whole process should be
automated. In particular:
- It should be easy to specify and review which branches the current branch
  depends on.
- It should be easy to generate a base branch upon which the current branch is
  based for the purpose of code review.
- It should be easy to update the base branch if any changes have happened to
  it.

## Installation

Download from http://example.com/FIXME.

## Usage

Run `lein bin` and add the executable in
`target/base+system+user+dev/git-depbranch` to your path.

| Command                           | Explanation                                                                                                        |
| --------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| git depbranch show [branchname]   | Show all of the branches that the current branch is dependent on.                                                  |
| git depbranch add <branchname>    | Add <branchname> as a dependent branch for the current branch.                                                     |
| git depbranch remove <branchname> | Remove <branchname> as a dependent branch for the current branch.                                                  |
| git depbranch base-branch         | Print the name of the base branch for the current branch.                                                          |
| git depbranch update-base         | Creates a base branch for the current branch, and recursively updates it by traversing the branch dependency tree. |

## Examples

### One branch depends on another
In this example, suppose that we have branches `a` and `b`, where `a` depends on `b`.

```shell
$ git checkout a
Switched to branch 'a'
$ git depbranch show                      # empty result
$ git depbranch add b
$ git depbranch show
[b]
$ git checkout b
Switched to branch 'b'

... make some changes and commit ...

$ git checkout a
$ git depbranch update-base   # resets 'a_db_base' to 'b' and merges into 'a'
```

Now, suppose we add a new feature upon which `a` depends, called `c`.

```shell
$ git checkout 'a'
Switched to branch 'a'
$ git depbranch add 'c'
$ git depbranch update-base   # merges 'b' and 'c' into 'a_db_base', then merges
                              # 'a_db_base' into 'a'.
```

### Bugs

- Currently, the base branch into which dependent branches are merged is always
  called `<branchname>_db_base`. This should be configurable per branch.
- There is no help command.
- There is no validation that you are not adding a branch which does not exist.

## License

Copyright © 2017 Miles Yucht

Distributed under the MIT License.
