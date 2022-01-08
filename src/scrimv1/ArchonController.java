package scrimv1;
import battlecode.common.*;

public class ArchonController {
    static int miners = 0, soldiers = 0, builders = 0;
    static MapLocation me = null;

    static void runArchon(RobotController rc) throws GameActionException {
        if (me == null) { me = rc.getLocation(); }

        Direction defaultSpawn = Util.initDir(rc);
//        rc.setIndicatorString("Spawning to the " + defaultSpawn.toString());
        safeSpawn(rc, RobotType.MINER, defaultSpawn);
    }

    static boolean safeSpawn(RobotController rc, RobotType type, Direction dir) throws GameActionException {
        // Checks if Archon can spawn
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
//            rc.setIndicatorString("Spawned in " + dir.toString());
            return true;
        }
//        rc.setIndicatorString("Failed to spawn in " + dir.toString());
        return false;
    }
}