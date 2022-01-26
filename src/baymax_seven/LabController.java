package baymax_seven;

import battlecode.common.*;
import baymax_seven.util.pathfinding.BFS;
import baymax_seven.util.pathfinding.DroidBFS;

import static baymax_seven.util.Communication.*;
import static baymax_seven.util.Miscellaneous.getBestBuildingLoc;

public class LabController {
    static MapLocation bestLabLoc = null;
    static BFS bfs = null;
    static int MIN_MINERS = 4;

    static void runLab(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        // initialize bfs
        if (bfs == null) {
            bfs = new DroidBFS(rc);
        }

        boolean shouldTransmute = (liveNumMiners(rc) < 3 * MIN_MINERS);

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
        if (shouldTransmute && rc.canTransmute() && nearbyAllies.length < 12) {
            rc.transmute();
        } else if (shouldTransmute && rc.canTransmute() && rc.getTeamLeadAmount(rc.getTeam()) > 500) {
            rc.transmute();
        }
    }
}