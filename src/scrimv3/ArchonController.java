package scrimv3;
import battlecode.common.*;

public class ArchonController {
    static int miners = 0, soldiers = 0, builders = 0;
    static MapLocation me = null;

//Heuristic for strategy: Larger map, more likely to soldier rush. Smaller map, more likely to watchtower.
//As long of one of the side lengths is smaller, watchtower is also preferred also
//Based on amount of lead in first x turns -> guesstimate how much lead. More lead favors watchtower
//More archons -> soldier strat

    static void runArchon(RobotController rc) throws GameActionException {


        if (me == null) { me = rc.getLocation(); }

        Direction defaultSpawn = null;
//change how many miners to have here
        while (miners < 15 && rc.getTeamLeadAmount(rc.getTeam())/rc.getArchonCount() < 500) //stops producing miners when we have enough  (500 * # of archons)
        {   
            rc.setIndicatorString("MINER CREATED!");
            defaultSpawn = Util.initDir(rc);
            // create 3 lanes
            if (miners % 3 == 0 && (safeSpawn(rc, RobotType.MINER, defaultSpawn))) {
                miners++;
                defaultSpawn = defaultSpawn.rotateLeft();
                rc.buildRobot(RobotType.MINER, defaultSpawn);
            }

            else if (miners % 3 == 2 && (safeSpawn(rc, RobotType.MINER, defaultSpawn))) {
                miners++;
                defaultSpawn = defaultSpawn.rotateRight();
                rc.buildRobot(RobotType.MINER, defaultSpawn);
            }

            else if ((safeSpawn(rc, RobotType.MINER, defaultSpawn)))

            {
                miners++;
                rc.buildRobot(RobotType.MINER, defaultSpawn);
            }


        }

        if (soldiers < 15) {
            rc.setIndicatorString("SOLDIER CREATED!");
            defaultSpawn = Util.initDir(rc);
            // create 3 lanes
            if (soldiers % 3 == 0 && (safeSpawn(rc, RobotType.SOLDIER, defaultSpawn))){
                defaultSpawn = defaultSpawn.rotateLeft();
                soldiers++;
                rc.buildRobot(RobotType.SOLDIER, defaultSpawn);
            }

            else if (soldiers % 3 == 2 && (safeSpawn(rc, RobotType.SOLDIER, defaultSpawn))) {
                defaultSpawn = defaultSpawn.rotateRight();
                soldiers++;
                rc.buildRobot(RobotType.SOLDIER, defaultSpawn);
            }

            else if ((safeSpawn(rc, RobotType.SOLDIER, defaultSpawn)))
            {
                soldiers++;
                rc.buildRobot(RobotType.SOLDIER, defaultSpawn);
            }

        }

            if ((builders<10)) {
            rc.setIndicatorString("BUILDER CREATED!");
                defaultSpawn = Util.initDir(rc);
                // create 3 lanes
                if (builders % 3 == 0 && safeSpawn(rc, RobotType.BUILDER, defaultSpawn)) {
                    builders++;
                    defaultSpawn = defaultSpawn.rotateLeft();
                    rc.buildRobot(RobotType.BUILDER, defaultSpawn);
                }
        
                else if (builders % 3 == 2 && safeSpawn(rc, RobotType.BUILDER, defaultSpawn)) {
                    builders++;
                    defaultSpawn = defaultSpawn.rotateRight();
                    rc.buildRobot(RobotType.BUILDER, defaultSpawn);

                }
                else if (safeSpawn(rc, RobotType.BUILDER, defaultSpawn)) {
                    builders++;
                    rc.buildRobot(RobotType.BUILDER, defaultSpawn);
                }
            }
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