package voldemort_eight;

import battlecode.common.*;
import voldemort_eight.util.pathfinding.BFS;
import voldemort_eight.util.pathfinding.DroidBFS;

import static voldemort_eight.util.Communication.*;
import static voldemort_eight.util.Miscellaneous.getBestBuildingLoc;

public class LabController {
    static MapLocation bestLabLoc = null;
    static BFS bfs = null;
    static final int ALLY_THRESHOLD = 12;

    static void runLab(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

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
        bestLabLoc = getBestBuildingLoc(rc);

        if (me.equals(bestLabLoc)) {
            if (rc.isTransformReady() && rc.getMode().equals(RobotMode.PORTABLE)) {
                rc.transform();
            }
        } else {
            if (rc.getMode().equals(RobotMode.TURRET)) {
                if (rc.isTransformReady()) {
                    rc.transform();
                }
            } else {
                bfs.move(bestLabLoc);
            }
        }

        // Transmute when the number of nearby allies is less than the threshold
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());

        if (rc.readSharedArray(50) >= 4 && rc.canTransmute() && nearbyAllies.length < ALLY_THRESHOLD) {
            rc.transmute();
        } else if (rc.canTransmute() && rc.getTeamLeadAmount(rc.getTeam()) > 500) {
            rc.transmute();
        } else if (rc.canTransmute() && liveNumMiners(rc) >= 12) {
            rc.transmute();
        } else if (checkFlag(rc, 0) && rc.canTransmute() && nearbyAllies.length < ALLY_THRESHOLD) {
            rc.transmute();
        }
    }
}
