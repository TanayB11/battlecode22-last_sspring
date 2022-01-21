# Battlecode 2022 Scaffold

This is the Battlecode 2022 scaffold, containing an `examplefuncsplayer`. Read https://play.battlecode.org/getting-started!

### Project Structure

- `README.md`
    This file.
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `test/`
    Player test code.
- `client/`
    Contains the client. The proper executable can be found in this folder (don't move this!)
- `build/`
    Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
    The output folder for match files.
- `maps/`
    The default folder for custom maps.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.


### Useful Commands

- `./gradlew run`
    Runs a game with the settings in gradle`.properties
- `./gradlew update`
    Update to the newest version! Run every so often

### Acknowledgements
*We love you <3*

- [XSquare for BFS pathfinding](https://github.com/IvanGeffner/battlecode2021/tree/master/thirtyone)
- [Teh Devs for Greedy BugNav](https://github.com/battlecode/battlecode22-lectureplayer/tree/main/src/lectureplayer)
- [printf for allowing us to ~~steal~~ borrow their entire soldier micro-strategy](https://discord.com/channels/386965718572466197/401058232346345473/932583881784328242)