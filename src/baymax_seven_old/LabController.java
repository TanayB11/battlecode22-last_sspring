package baymax_seven_old;

import battlecode.common.*;
import baymax_seven_old.util.pathfinding.BFS;
import baymax_seven_old.util.pathfinding.DroidBFS;

import static baymax_seven_old.util.Communication.*;
import static baymax_seven_old.util.Miscellaneous.getBestBuildingLoc;

public class LabController {
    static final int NEARBY_ALLIES_THRESHOLD = 5;
    static final int MOVEMENT_RUBBLE_THRESHOLD = 40;

    static MapLocation bestLabLoc = null;
    static BFS bfs = null;

    static void runLab(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        labReport(rc);

        // initialize bfs
        if (bfs == null) {
            bfs = new DroidBFS(rc);
        }

        // Report any enemies in vision radius
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        // 1. If we're on too high of a rubble square, move to nearest acceptable location
        if (bestLabLoc == null) {
            bestLabLoc = getBestBuildingLoc(rc);
        }


        if (bestLabLoc != null && me.equals(bestLabLoc)) {
            if (rc.isTransformReady()) {
                rc.transform();
                bestLabLoc = null;
            }
        }

        if (bestLabLoc != null && !me.equals(bestLabLoc)) {
            if (rc.getMode().equals(RobotMode.TURRET)) {
                if (rc.isTransformReady()) {
                    rc.transform();
                }
            } else {
                bfs.move(bestLabLoc);
            }
        }
    }

    static void turretModeSequence(RobotController rc) throws GameActionException {
        // Transmute when the number of nearby allies is less than the threshold
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (checkFlag(rc, 1) && rc.canTransmute() && nearbyAllies.length <= NEARBY_ALLIES_THRESHOLD) {
            rc.transmute();
        }
    }
}