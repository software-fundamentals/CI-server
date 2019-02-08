# Lab 2 DD2380 - CI Server
Continious Integration server.

## About
A small CI server that uses a GitHub Webhook and [ngrok](https://ngrok.com) to listen for push events. The CI server has support for [Gradle](https://gradle.org/) and if the repository to where the push was made contains gradle tests, those are executed. The results from the tests are then added as a status to the latest commit at GitHub. The result of the test is also posted to a channel in our Slack team with information about what tests that ran, if they succeeded or failed and a link to GitHub where additional information about the commit can be found.

## Setup
If you don't have Gradle installed, just run `gradle.bat build` (Windows), or `./gradle build` (OS X, GNU/Linux).

## Running
1. First, create a ngrok HTTPS URL:
```
./ngrok http 8001
```
2. Then add the ngrok URL as a [GitHub Webhook](https://github.com/software-fundamentals/CI-server/settings/hooks) and tell it to listen for push events.

3. Finally, start the server:
```
gradle build
```

4. Check out a branch, make a change to a file, push the changes and watch the status of the commit change.

## Testing
* `gradle build` compiles the program and runs all tests
* `gradle test` runs all tests
* `gradle run` runs the program

## Statement of Contributions
Two things that we are extra proud of is the way that we have structured our work on GitHub with the use of a Project board that keeps track of the status of the issues as well as a Slack integration that we have sat up that display the build status of the different branches. As for the code, we have worked on most of the components together, couple programming.

## Josefin
* Clone repo and checkout branch functionality
* Request parsing
* Tests for error handling

## Miguel
* Clone repo and checkout branch functionality
* Request parsing
* Test build functionality

## Moa
* Slack integration
* Set up Travis integration
* GitHub commit status mechanism

## Sebastian
* Gradle test status functionality
* Clone repo and checkout branch functionality

## William
* Webhook setup
* Request parsing
* Gradle test status functionality
* Clone repo and checkout branch functionality