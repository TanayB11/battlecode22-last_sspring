package sad_sprint1_bot;
import battlecode.common.*;
import sad_sprint1_bot.util.Util;

public class ArchonController {
    static int miners = 0, soldiers = 0, builders = 0;
    static MapLocation me = null;

    static void runArchon(RobotController rc) throws GameActionException {
        if (me == null) { me = rc.getLocation(); }

        Direction defaultSpawn = null;
        // TODO: get rid of the 50 :()
        if (miners < 15) {
            defaultSpawn = Util.initDir(rc);
            // create 3 lanes
            if (miners % 3 == 0) {
                defaultSpawn = defaultSpawn.rotateLeft();
            } else if (miners % 3 == 2) {
                defaultSpawn = defaultSpawn.rotateRight();
            }
            if(safeSpawn(rc, RobotType.MINER, defaultSpawn)) {
                miners++;
            }
        } else {
            defaultSpawn = Util.initDir(rc);
            if(safeSpawn(rc, RobotType.SOLDIER, defaultSpawn)) {
                soldiers++;
            }
        }
//        else if (builders < 5) {
//            defaultSpawn = Util.initDir(rc);
//            if(safeSpawn(rc, RobotType.BUILDER, defaultSpawn)) {
//                builders++;
//            }
//        }
//        rc.setIndicatorString("Spawning to the " + defaultSpawn.toString());
    }

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