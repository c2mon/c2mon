# Contributing to C2MON

Have something you'd like to contribute to the framework?  We welcome pull
requests but ask that you carefully read this document first to understand how
best to submit them; what kind of changes are likely to be accepted; and what
to expect from the C2MON team when evaluating your submission.

_Please refer back to this document as a checklist before issuing any pull
request; this will save time for everyone!_

## Take Your First Steps

### Understand the basics

Not sure what a pull request is, or how to submit one? Take a look at GitHub's
excellent [help documentation][] first.

### Search issues; create an issue if necessary

Is there already an issue that addresses your concern? Do a bit of searching
in our [issue tracker][] to see if
you can find something similar. If you do not find something similar, please
create a new issue before submitting a pull request unless the change is truly
trivial -- for example: typo fixes, removing compiler warnings, etc.

## Fork and clone the repository

You will need to fork the main C2MON repository and clone it to your local machine. See
[github help page](https://help.github.com/articles/fork-a-repo) for help.

## Create a Branch

### Branch from `master`

Please submit all pull requests to master, even bug fixes and minor improvements.

### Use short branch names

Branches used when submitting pull requests should preferably be named using
succinct, lower-case, dash (-) delimited names, such as 'fix-warnings',
'fix-typo', etc. Otherwise, name the branch according to the issue number,
e.g. 'issue-123'.


## Use the C2MON code style

Please read the [C2MON code style guide][] for full details, but for the
meantime, here's a quick summary:

### Mind the whitespace

Please carefully follow the whitespace and formatting conventions already
present in the framework.

1. Two spaces, no tabs
1. Unix (LF), not DOS (CRLF) line endings
1. Eliminate all trailing whitespace
1. Wrap Javadoc at 80 characters
1. Aim to wrap code at 120 characters, but favour readability over wrapping
1. Preserve existing formatting; i.e. do not reformat code for its own sake
1. Search the codebase using `git grep` and other tools to discover common
    naming conventions, etc.
1. Latin-1 (ISO-8859-1) encoding for Java sources; use `native2ascii` to convert
    if necessary


### Add LGPLv3 license header to all new classes

```java
/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package ...;
```

### Update LGPLv3 license header in modified files as necessary

Always check the date range in the license header. For example, if you've
modified a file in 2016 whose header still reads:

```java
/*
 * Copyright (C) 2010-2015 CERN. All rights not expressly granted are reserved.
```

Then be sure to update it to 2016 accordingly:

```java
/*
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
```

## Prepare Your Commit

### Submit JUnit test cases for all behaviour changes

Search the codebase to find related tests and add additional `@Test` methods
as appropriate.

### Use real name in git commits

Please configure git to use your real first and last name for any commits you
intend to submit as pull requests. For example, this is not acceptable:

    Author: Nickname <user@mail.com>

Rather, please include your first and last name, properly capitalized:

    Author: First Last <user@mail.com>

This helps ensure traceability and also goes a long way to
ensuring useful output from tools like `git shortlog` and others.

You can configure this via the account admin area in GitHub (useful for
fork-and-edit cases); _globally_ on your machine with

    git config --global user.name "First Last"
    git config --global user.email user@mail.com

or _locally_ for the `c2mon` repository only by omitting the
'--global' flag:

    cd c2mon
    git config user.name "First Last"
    git config user.email user@mail.com


### Format commit messages

Please read and follow the [Commit Guidelines section of Pro Git][].

Most importantly, please format your commit messages in the following way
(adapted from the commit template in the link above):

    #25: Short (50 chars or less) summary of changes

    More detailed explanatory text, if necessary. Wrap it to about 72
    characters or so. In some contexts, the first line is treated as the
    subject of an email and the rest of the text as the body. The blank
    line separating the summary from the body is critical (unless you omit
    the body entirely); tools like rebase can get confused if you run the
    two together.

    Further paragraphs come after blank lines.

     - Bullet points are okay, too

     - Typically a hyphen or asterisk is used for the bullet, preceded by a
       single space, with blank lines in between, but conventions vary here


1. Prefix the subject line with the a hash symbol, followed by the related
   issue number, followed by a colon and a space, e.g. "#25: Fix ...".
1. Use imperative statements in the subject line, e.g. "Fix broken Javadoc link".
1. Begin the subject line with a capitalized verb, e.g. "Add, Prune, Fix,
    Introduce, Avoid" etc.
1. Do not end the subject line with a period.
1. Restrict the subject line to 50 characters or less if possible.
1. Wrap lines in the body at 72 characters or less.
1. Mention associated issue(s) at the end of the commit comment, prefixed
    with "Issue: " as above.
1. In the body of the commit message, explain how things worked before this
    commit, what has changed, and how things work now.
1. _Check your spelling!_

## Run the Final Checklist

### Run all tests prior to submission

See the [building from source][] section of the `README` for instructions. Make
sure that all tests pass prior to submitting your pull request.


### Submit your pull request

Subject line:

Follow the same conventions for pull request subject lines as mentioned above
for commit message subject lines.

In the body:

1. Explain your use case. What led you to submit this change? Why were existing
    mechanisms in the framework insufficient? Make a case that this is a
    general-purpose problem and that yours is a general-purpose solution, etc.
1. Add any additional information and ask questions; start a conversation or
    continue one from an issue.
1. Mention the issue ID.

Note that for pull requests containing a single commit, the subject line and
body of the pull request will be defaulted to match the subject line and body of
the commit message. This is fine, but please also include the items above in the
body of the request.


### Mention your pull request on the associated issue

Add a comment to the associated issue(s) linking to your new pull request.


### Expect discussion and rework

The C2MON team takes a very conservative approach to accepting contributions to
the framework. This is to keep code quality and stability as high as possible,
and to keep complexity at a minimum. Your changes, if accepted, may be heavily
modified prior to merging. You will retain "Author:" attribution for your Git
commits granted that the bulk of your changes remain intact. You may be asked to
rework the submission for style (as explained above) and/or substance. Again, we
strongly recommend discussing any serious submissions with the C2MON
team _prior_ to engaging in serious development work.

Note that you can always force push (`git push -f`) reworked / rebased commits
against the branch used to submit your pull request. In other words, you do not
need to issue a new pull request when asked to make changes.

[help documentation]: http://help.github.com/send-pull-requests
[issue tracker]: https://gitlab.cern.ch/c2mon/c2mon/issues
[C2MON Code Style]: https://gitlab.cern.ch/c2mon/c2mon/blob/master/CODESTYLE.md
[Rewriting History section of Pro Git]: http://git-scm.com/book/en/Git-Tools-Rewriting-History
[Commit Guidelines section of Pro Git]: http://git-scm.com/book/en/Distributed-Git-Contributing-to-a-Project#Commit-Guidelines
[building from source]: https://gitlab.cern.ch/c2mon/c2mon#building-from-source
