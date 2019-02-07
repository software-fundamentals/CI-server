# Lab 2 DD2380 - CI Server
Continious Integration server.

## About
A small CI server that uses a GitHub Webhook and [ngrok](https://ngrok.com) to listen for push events. The CI server has support for [Gradle](https://gradle.org/) and if the repository to where the push was made contains gradle tests, those are executed. The results from the tests are then added as a status to the latest commit at GitHub.

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