package scrimv1;
import battlecode.common.*;
import java.util.Arrays;

public class MinerController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static boolean isMining = false;
    static int failedMoves = 0;

    private static final int ACCEPTABLE_RUBBLE = 25;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }

        if (failedMoves >= 3) { travelDir = travelDir.opposite(); }

        // TODO: distinguish exploration miners vs travelers
        // communicate who is mining, will get rid of this if
        if (rc.getRobotCount() < (3 * rc.getArchonCount())) {
            MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
            if (nearbyLead.length > 0) {
                walkTowards(rc, nearbyLead[0]);
            }
        } else {
            if (Util.safeMove(rc, travelDir)) {
                failedMoves = 0;
            } else if (rc.getMovementCooldownTurns() == 0) {
                failedMoves++;
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
            }
        }
   }

    static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        // leave lead to regen
        while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
            isMining = true;
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
            isMining = true;
            rc.mineLead(mineLocation);
        }
        isMining = false;
    }

    // Bug0 pathing: TODO implement bug2 instead
    // Taken from https://github.com/battlecode/battlecode22-lectureplayer/blob/main/src/lectureplayer/Pathing.java
    static void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        if (!rc.isMovementReady() || currentLocation.equals(target)) { return; }

        Direction d = currentLocation.directionTo(target);
        if (rc.canMove(d) && !isObstacle(rc, d)) {
            // No obstacle in the way, so let's just go straight for it!
            rc.move(d);
            failedMoves = 0;
            travelDir = null;
        } else {
            // There is an obstacle in the way, so we're gonna have to go around it.
            if (travelDir == null) {
                // If we don't know what we're trying to do
                // make something up
                // And, what better than to pick as the direction we want to go in
                // the best direction towards the goal?
                travelDir = d;
            }
            // Now, try to actually go around the obstacle
            // Repeat 8 times to try all 8 possible directions.
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(travelDir) && !isObstacle(rc, travelDir)) {
                    rc.move(travelDir);
                    failedMoves = 0;
                    travelDir = travelDir.rotateLeft();
                    break;
                } else {
                    travelDir = travelDir.rotateRight();
                }
            }
        }
    }

    private static boolean isObstacle(RobotController rc, Direction d) throws GameActionException {
        MapLocation adjacentLocation = rc.getLocation().add(d);
        int rubbleOnLocation = rc.senseRubble(adjacentLocation);
        return rubbleOnLocation > ACCEPTABLE_RUBBLE;
    }
}
