## Welcome to the Jenkins Parameterized Build Plug-in
[![Build Status](https://travis-ci.org/ParameterizedBuilds/parameterized-builds.svg?branch=master)](https://travis-ci.org/ParameterizedBuilds/parameterized-builds)
[![Join the chat at https://gitter.im/parameterized-builds/Lobby](https://badges.gitter.im/parameterized-builds/Lobby.svg)](https://gitter.im/parameterized-builds/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Setup
1. [Define a server](#define-a-server)
2. [Define a job](#define-a-job)
3. [Link your bitbucket server account to Jenkins](#link-your-bitbucket-server-account-to-jenkins)

#### Define a server
Jenkins servers can be defined globally on the Administration page of Bitbucket Server
or per project on the project settings page.

![Jenkins administration settings](readme/img/jenkins_admin.png)  
* `Base URL` should be the http/https root address to you jenkins server
* `Server Nickname` is a unique name for this server. This is used by job settings to choose which
Bitbucket Server to run the job on.
* `Default User` and `Default Token` are optional and used as a fall back 
authentication for triggering jobs
* `Build Token Root Plugin` 
uses an alternate address for triggering builds anonymously. Enable this only if you have the [Build Token Root Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Token+Root+Plugin) installed in Jenkins 
* `CSRF` if your Jenkins is [CSRF](https://wiki.jenkins.io/display/JENKINS/CSRF+Protection) protected, enable this option

#### Define a job
![Jenkins hook settings](readme/img/jenkins_hook2.png)  
![Jenkins hook settings](readme/img/jenkins_hook.png)  
Enable Parameterized Builds for Jenkins under the hooks settings for a repository.  
You can add as many jobs as you want (within reason), 
they will be triggered in the order they are added.
* `Job Name` is the name of the jenkins job to trigger. If the job is contained in a
 [folder](https://wiki.jenkins.io/display/JENKINS/CloudBees+Folders+Plugin) set the job name as
 _folderName_/job/_jobName_.
* `Ref Type` will apply your triggers to either branch operations or tag operations
* [Multibranch Pipeline](#multibranch-pipeline-setup) should only be used to trigger jenkins multibranch pipelines
* [Triggers](#triggers)
* `Token` correspondes to the authorization token on your job configuration page in Jenkins
* [Build Parameters](#build-parameters)
* `Ref Filter` uses java regex syntax to only trigger a 
build for branches/tags that match the filter
  * the filter will only be used for branch/tag creation, branch pushes, and branch/tag deletion
  * leave blank to match all branches
* `Monitored Paths` will only trigger a build if a file in the diff matches the filter
  * the filter will only be used for branch pushes and PR events
  * leave blank to match all changes
* `Required Build Permission` will restrict who can trigger a Jenkins job using the repository permissions
  * This is only available on manual triggers
* `PR Destination Filter` functions identically to `Ref Filter` but on the branch being merged into
* `Ignore Committers` list of bitbucket user names separated by new line, Commits from any of these specified users 
will be ignored and will not trigger any builds
  * This is only available on push event triggers
* `Ignore Commits With String` If a commit message contains this configured string in it, 
that commit will be ignored and will not trigger any builds. Uses java regex syntax
  * This is only available on push event triggers

#### Link your bitbucket server account to Jenkins
![Jenkins user settings](readme/img/jenkins_user.png)  
On your user account page you can add your Jenkins API token.
When added bitbucket server will use your Jenkins account 
to trigger jobs for branches you create/push, pull requests you open, or for manual builds.  
If you have multiple Jenkins servers setup you can set the api token for each one.

Order of authentication:  
The Jenkins user `Token` from the Jenkins User Settings will be used for authentation first.  
If that is not set then the `Token` parameter in the job configuration will be used second.  
If that is not set then the `Default User` and `Default Token` on the Jenkins 
server settings page will be used.


## Triggers
![Triggers](readme/img/triggers.png) 

You can use any combination of triggers.
* `REF CREATED`
  * triggers a build for branch or tag creation events
  * the branch or tag name must match the `Ref Filter` parameter (leave blank to match all)
* `PUSH EVENT`
  * triggers a build for branch push events to the repository (new branches do not count even if they contain new commits)
  * the branch must match the `Ref Filter` parameter (leave blank to match all)
  * the files modified in the commits must match the `Monitored Paths` parameter (leave blank to match all)
* `MANUAL`
  * will add a "Build in Jenkins" button in the branch context menu and pull request page in bitbucket server  
![build from the source page](readme/img/build1.png) 
![build from the branch listings page](readme/img/build3.png)
![build from a pull request](readme/img/build2.png)
  * if you have multiple jobs with the `MANUAL` trigger then the user will be prompted to choose which job to trigger  
![build from a pull request](readme/img/build_dialog.png)
  * the user can also edit the build parameters before triggering the job
* `REF DELETED`
  * triggers a build for branch or tag deletion events
  * the branch or tag name must match the `Ref Filter` parameter (leave blank to match all)
* `AUTO MERGED`
  * triggers a build for automatic merge events
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR OPENED`
  * triggers a build for pull request creation events
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR REOPENED`  
  * triggers a build if the pull request is re-opened
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR SOURCE RESCOPED`  
  * triggers a build if changes are pushed to the source branch (e.g. "feature/yourfeature")
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR DEST RESCOPED`  
  * triggers a build if changes are pushed to the destination branch (e.g. "master")
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR MERGED`
  * triggers a build for pull request merge events
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR DECLINED`
  * triggers a build for pull request declined events
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR DELETED`
  * triggers a build for pull request declined events
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)
* `PR APPROVED`
  * triggers a build whenever a pull request is approved
  * the files modified in the PR must match the `Monitored Paths` parameter (leave blank to match all)


## Build Parameters
```
branch=$BRANCH  
environment=dev;test;prod
boolean=true
```
Build parameters can be specified using key=value pairs.  
You can use predefined variables that will be replaced when the build is triggered. 
For example `branch=$BRANCH` will replace $BRANCH with the branch name that triggered the build.  
Built-in variables: 
* $BRANCH: the branch name or tag name that triggered the build (without refs/heads/ or refs/tags/)
* $COMMIT: the commit hash that triggered the build 
* $REPOSITORY: the repository slug
* $PROJECT: the project key
* $TRIGGER: the trigger that triggered the build
* PR-specific variables (only availably when using a 'PR' prefixed trigger)
  * $PRID: bitbucket id of the PR
  * $PRDESTINATION: name of the destination branch in the PR
  * $PRTITLE: title of the PR
  * $PRDESCRIPTION: description of the PR (may be empty)
  * $PRAUTHOR: display name of the author of the PR
  * $PRUSERNAME: user name of the author of the PR
  * $PREMAIL: display email address of the author of the PR
  * $PRURL: bitbucket URL of the PR
  * $PRSOURCEPROJECT: the project where the source branch of a PR resides (if not using a fork then this is the same as $PROJECT)
  * $PRSOURCEREPOSITORY: the repository where the source branch of a PR resides (if not using a fork then this is the same as $REPOSITORY)
  * $MERGECOMMIT: the commit hash of the commit on the destination branch caused by the PR merge. Only available to `PR MERGED` trigger

Parameter Types:
* string: `key=value`
* choice: `key=option1;option2;option3`
  * when using the `MANUAL` trigger users will be prompted with a dropdown list of the choices
  * separate each choice with a semicolon
  * `option1` will be the default for push events
* boolean: `key=true`
  * when using the `MANUAL` trigger users will be prompted with a checkbox
  * `true` will be the default, use `key=false` to make false the default  
 
## Multibranch Pipeline Setup
Jenkins Multibranch pipelines can be built directly or through scans. Therefore, if the Multibranch Pipeline checkbox is
selected, `REF CREATED`, `REF DELETED`, and `PUSH EVENT` triggers will cause a scan while all others will trigger a build
for the given branch. Triggers still need to be selected like any other job.

 
# Contributing
* Pull requests should be opened against master
* Write unit tests for any changes you make
* A passing [Travis CI](https://travis-ci.org/KyleLNicholls/parameterized-builds) build is required
