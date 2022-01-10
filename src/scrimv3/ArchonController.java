package scrimv3;
import battlecode.common.*;

public class ArchonController {
    static int miners = 0, soldiers = 0, builders = 0;
    static MapLocation me = null;
    static int stratHeuristic;
    static int leadUsed = 0;
    static int turnNumber = 0;


//Heuristic for strategy: Larger map, more likely to soldier rush. Smaller map, more likely to watchtower.
//As long of one of the side lengths is smaller, watchtower is also preferred also
//Based on amount of lead in first x turns -> guesstimate how much lead. More lead favors watchtower
//More archons -> soldier spam strategy

//Alor's 12 AM heuristic for strategy: Area of map / (difference in length and width) * amount of lead in first 10 moves * # of archons

    static void runArchon(RobotController rc) throws GameActionException {

        stratHeuristic = rc.getArchonCount() * ((rc.getMapHeight() * rc.getMapWidth())/(Math.abs(rc.getMapHeight() - rc.getMapWidth()))) * (leadUsed + rc.getTeamLeadAmount(rc.getTeam()));

        me = rc.getLocation();

        Direction defaultSpawn;

        rc.setIndicatorString("ALIVE?");

        //stops producing miners when we have enough based on number of them and the heuristic
        while (miners < 15 || rc.getTeamLeadAmount(rc.getTeam())/rc.getArchonCount() < 500)
        {
            rc.setIndicatorString("MINER CREATED!");
            defaultSpawn = Util.initDir(rc);

            // create 3 lanes
            if (miners % 3 == 0 && (safeSpawn(rc, RobotType.MINER, defaultSpawn))) {

                defaultSpawn = defaultSpawn.rotateLeft();
                rc.buildRobot(RobotType.MINER, defaultSpawn);
                miners++;
                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 50;
                }
            }

            else if (miners % 3 == 2 && (safeSpawn(rc, RobotType.MINER, defaultSpawn))) {
                miners++;
                defaultSpawn = defaultSpawn.rotateRight();
                rc.buildRobot(RobotType.MINER, defaultSpawn);
                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 50;
                }
            }

            else if ((safeSpawn(rc, RobotType.MINER, defaultSpawn)))

            {
                miners++;
                rc.buildRobot(RobotType.MINER, defaultSpawn);
                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 50;
                }
            }


        }

        while (soldiers < 15 || stratHeuristic > 80000) {
            rc.setIndicatorString("SOLDIER CREATED!");
            defaultSpawn = Util.initDir(rc);
            // create 3 lanes
            if (soldiers % 3 == 0 && (safeSpawn(rc, RobotType.SOLDIER, defaultSpawn))){
                defaultSpawn = defaultSpawn.rotateLeft();
                soldiers++;
                rc.buildRobot(RobotType.SOLDIER, defaultSpawn);

                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 75;
                }
            }

            else if (soldiers % 3 == 2 && (safeSpawn(rc, RobotType.SOLDIER, defaultSpawn)) || stratHeuristic > 80000) {
                defaultSpawn = defaultSpawn.rotateRight();
                soldiers++;
                rc.buildRobot(RobotType.SOLDIER, defaultSpawn);

                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 75;
                }
            }

            else if ((safeSpawn(rc, RobotType.SOLDIER, defaultSpawn)) || stratHeuristic > 80000)
            {
                soldiers++;
                rc.buildRobot(RobotType.SOLDIER, defaultSpawn);

                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 75;
                }
            }

        }

        if ((builders<10 || stratHeuristic <= 80000)) {
            rc.setIndicatorString("BUILDER CREATED!");
            defaultSpawn = Util.initDir(rc);
            // create 3 lanes
            if (builders % 3 == 0 && safeSpawn(rc, RobotType.BUILDER, defaultSpawn)) {
                builders++;
                defaultSpawn = defaultSpawn.rotateLeft();
                rc.buildRobot(RobotType.BUILDER, defaultSpawn);

                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 45;
                }
            }

            else if (builders % 3 == 2 && safeSpawn(rc, RobotType.BUILDER, defaultSpawn)) {
                builders++;
                defaultSpawn = defaultSpawn.rotateRight();
                rc.buildRobot(RobotType.BUILDER, defaultSpawn);

                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 45;
                }

            }
            else if (safeSpawn(rc, RobotType.BUILDER, defaultSpawn)) {
                builders++;
                rc.buildRobot(RobotType.BUILDER, defaultSpawn);

                // Modifies leadUsed, which is in heuristic
                if (turnNumber <= 10) {
                    leadUsed += 45;
                }
            }
        }
        turnNumber++;
    }
    //If there are coordinates in the array? That means that miners were damaged. Attack.

    //rc.setIndicatorString("Spawning to the " + defaultSpawn.toString());

    static boolean safeSpawn(RobotController rc, RobotType type, Direction dir) throws GameActionException {
        // Checks if Archon can spawn
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            rc.setIndicatorString("Spawned in " + dir.toString());
            return true;
        }
        rc.setIndicatorString("Failed to spawn " + dir.toString());
        return false;
    }
}