package scrimv0;
import battlecode.common.*;
import java.util.Arrays;

public class ArchonController {
    static int miners = 0, soldiers = 0, builders = 0;
    static MapLocation me = null;

    static void runArchon(RobotController rc) throws GameActionException {
        if (me != null) { me = rc.getLocation(); }

        if (miners < 20) {
            safeSpawn(rc, RobotType.MINER, Util.directions[Util.rng.nextInt(Util.directions.length)]);
            miners++;
        }

        //If there are coordinates in the array? That means that miners were damaged. Attack.
        if(rc.readSharedArray(0) > 0 || rc.readSharedArray(1) > 0)
        {
            safeSpawn(rc, RobotType.SOLDIER, Util.directions[Util.rng.nextInt(Util.directions.length)]);
            soldiers++;
        }

    }

    static void spamSpawnMiners(RobotController rc) throws GameActionException {
        int mapWidth = rc.getMapWidth(), mapHeight = rc.getMapHeight();
    }

    static boolean safeSpawn(RobotController rc, RobotType type, Direction dir) throws GameActionException {
        // Checks if Archon can spawn
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            rc.setIndicatorString("Spawned in " + dir.toString());
            return true;
        }
        rc.setIndicatorString("Failed to spawn in " + dir.toString());
        return false;
    }
}
