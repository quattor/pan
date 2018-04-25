Pan Configuration Language Compiler
===================================

The pan configuration language allows system administrators to define
simultaneously a site configuration and a schema for validation. As a
core component of the Quattor fabric management toolkit, the pan
compiler translates this high-level site configuration to a
machine-readable representation, which other tools can then use to
enact the desired configuration changes.

Documentation
-------------

All of the pan language and pan language compiler documentation is now
hosted on the [ReadTheDocs service](https://quattor-pan.readthedocs.org).
Via that service, you can also download PDF and EPUB versions of the
documentation.

Release Procedure
-----------------

To generate all supported packages, the release build must be performed
on a CentOS (or compatible) machine with the `rpmbuild` command installed.
Ensure also that a recent, certified version of the JVM (1.6+) and maven
(3.0.3+) are installed.

Before doing anything else, **ensure that the release notes and the change
log for the release are up to date**.  The source is in the
`panc-docs/source/release-notes/release-notes.rst` file.  The change log
should list all of the issues resolved in the release, by referencing the
related GitHub issue. Look at existing entries for the actual format expected.

Clone this repository to your build machine and then verify that the full
build runs correctly.  Run the following command from the top-level of the
cloned repository:
```
$ mvn clean install
```
If this doesn't end with a "BUILD SUCCESS" message, then correct the
problems before going any farther with the release.

To perform the release you must have:
  * A registered GPG key installed on your build machine.
  * Write access to this repository (for tagging).
  * Access to the Sonatype OSS maven repository as a Quattor member.

If this is the case, then use the normal maven procedures for preparing
and performing a release:
```
$ mvn clean
$ mvn release:prepare
$ mvn release:perform
```
During the preparation phase you will be asked about the versions to
tag and for your GPG key password.  You will also be asked about the
GPG key password during the perform stage.

If all worked without errors, then the packages will have been
successfully staged to the
[OSS maven repository](https://oss.sonatype.org/).  You will need to
log into the repository to promote and release the artifacts.  Once
logged in:
  * Click on "Staging Repositories" in left panel.
  * Select the "orgquattor" repository in the list.
  * Click on "Close" at the top below the tabs, give a message and
    confirm the action.
  * Wait for the action to finish.  You can click on "Refresh" to
    update the status.
  * Finish the release by clicking "Release" at the top below the
    tabs.
You have now successfully released all of the artifacts into the central
maven repository.

You must also create the release in GitHub.  Collect all of the
files you will distribute as part of the release.  The list includes:
  * Jar file with dependencies,
  * Tar archive,
  * Zip archive, and
  * RPM package
Put them on the machine you'll use to access the GitHub web interface.

Visit the [pan releases page](https://github.com/quattor/pan/releases).
You should see a tag that corresponds to the release that you've just
created.  Click on the tag link on this page and then on the "Edit Tag"
button.

Now use the interface to upload the packages to GitHub.  You can either
drop and drag them into place or just select them with the chooser.
Provide a title and description for the release and then click on the
"Publish" button.

After producing the release, the documentation on ReadTheDocs (RTD) must be updated,
as RTD currently does not automatically build new versions.
You must log into ReadTheDocs.org with an account 
listed as a maintainer for the `quattor-pan` project and add the tag for the
release to the list of versions to build (this is done in the `Versions` tab
of the `Settings` page access through the `Admin` button of the project home page).
Once added it will build automatically.  The default version displayed on
ReadTheDocs is normally `stable` which will automatically mapped to the new version
once it is built (if it is the highest tag). You can also explicitly select the version
to show on lower-left corner.

To finish up, close the current milestone and create a new milestone
in the pan repository for the next release.

The last step after releasing a new version is to add the new version to Quattor
externals YUM repositories (there is one per OS version, in the `noarch` branch),
hosted on `yum.quattor.org server.


License
-------

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License.  You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied.  See the License for the specific language governing
permissions and limitations under the License.
