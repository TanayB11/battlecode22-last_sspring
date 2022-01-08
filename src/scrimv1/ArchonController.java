package scrimv1;
import battlecode.common.*;

public class ArchonController {
    static int miners = 0, soldiers = 0, builders = 0;
    static MapLocation me = null;

    static void runArchon(RobotController rc) throws GameActionException {
        if (me == null) { me = rc.getLocation(); }

        Direction defaultSpawn = null;
        if (miners < 3) {
            MapLocation[] nearbyPb = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);

            if(safeSpawn(rc, RobotType.MINER, defaultSpawn)) {
                miners++;
            }
        } else if (miners < 20) { // create 3 lanes
            defaultSpawn = Util.initDir(rc);
            if (miners % 3 == 0) {
                defaultSpawn = defaultSpawn.rotateLeft();
            } else if (miners % 3 == 2) {
                defaultSpawn = defaultSpawn.rotateRight();
            }
            if(safeSpawn(rc, RobotType.MINER, defaultSpawn)) {
                miners++;
            }
        }
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