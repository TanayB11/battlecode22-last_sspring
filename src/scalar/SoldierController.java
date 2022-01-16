package scalar;

import battlecode.common.*;
import scalar.util.BFS;
import scalar.util.DroidBFS;
import scalar.util.Util;

import java.util.Arrays;

public class SoldierController {
    static MapLocation me = null, targetLoc = null;
    static boolean isRetreating = false;
    static BFS bfs = null;

    static int prevHP = Integer.MIN_VALUE;
    static final int RETREAT_TURNS = 5;
    static int retreatCounter = 0; // no. of times soldier has been retreating

    static final int ACCEPTABLE_TARGET_LOC_RUBBLE = 50;
    // disintegrate if we're dying and this close to our archon
    static final int MAX_DISINTEGRATION_DISTANCE = 4;

    static boolean isTargetingArchon = false;
    static int targetArchonArrLoc = -1;

    static boolean isDying = false;

    // soldier micro: head to enemy archon, kill others on the way

    public static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // spawn init
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            bfs = new DroidBFS(rc);
        }

        // reports if dying, returns to nearest archon to sacrifice
        if (!isDying && rc.getHealth() < rc.getType().health * 0.25) {
            int soldiersCt = rc.readSharedArray(1);
            rc.writeSharedArray(1, soldiersCt - 1);
            isDying = true;

            // check if returns null
            MapLocation nearestArchon = Util.findNearestFriendlyArchon(rc);
            targetLoc = nearestArchon;
        }

        // if we're at target, reset target
        if (me.equals(targetLoc)) {
            targetLoc = null;
        }

        if (isDying && me.distanceSquaredTo(targetLoc) <= MAX_DISINTEGRATION_DISTANCE) {
            rc.disintegrate();
        }

        // if we're near our archon target and see too much rubble on it, null
        if (
            targetLoc != null &&
            rc.canSenseLocation(targetLoc) &&
            rc.senseRubble(targetLoc) > ACCEPTABLE_TARGET_LOC_RUBBLE
        ) {
            targetLoc = null;
        }

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//        Util.broadcastEnemyArchonLocs(rc, nearbyEnemies);

        // TODO: keep tabs on the id of the robot we're attacking

        // sense nearby robots, attack in priority order
        // we just want to return if we're dying
        if (!isDying && nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, Util.ATTACK_PRIORITY_COMPARATOR);
            targetLoc = nearbyEnemies[0].getLocation();
            safeAttack(rc, targetLoc);

            // if attacking enemy archon below 5% health, reset in shared array & our target
            // also reset if we just don't see the archon
            RobotInfo victim = rc.senseRobotAtLocation(targetLoc);
            if (
                isTargetingArchon &&
                (victim == null || (victim != null && victim.getType().equals(RobotType.ARCHON) && victim.getHealth() <= victim.getType().health * 0.05))
            ) {
                targetLoc = null;
                rc.writeSharedArray(targetArchonArrLoc, 0);
            }
//            rc.setIndicatorString("NEAREST PRIORITY ENEMY IS " + nearbyEnemies[0].getLocation().toString());
        }

        // dumb archon targeting (find nearest archon broadcasted)
        if (targetLoc == null) {
            int distSqToNearestEnemyArchon = Integer.MAX_VALUE;
            int distSqToCurrentEnemArchon = 0;
            MapLocation nearestEnemyArchonLoc = null;
            int enemArchonCode = 0, enemArchonX = 0, enemArchonY = 0;

            for (int commsIndex = 10; commsIndex++ <= 13;) {
                enemArchonCode = rc.readSharedArray(commsIndex);
                enemArchonX = enemArchonCode / 100;
                enemArchonY = enemArchonCode % 100;

                MapLocation enemArchonLoc = new MapLocation(enemArchonX, enemArchonY);
                distSqToCurrentEnemArchon = me.distanceSquaredTo(enemArchonLoc);

                if (enemArchonCode != 0 && distSqToCurrentEnemArchon < distSqToNearestEnemyArchon) {
                    distSqToNearestEnemyArchon = distSqToCurrentEnemArchon;
                    nearestEnemyArchonLoc = enemArchonLoc;
                    targetArchonArrLoc = commsIndex; // so we can overwrite it when archon dies
                }
            }

            // random explore loc is just a safety
            if (nearestEnemyArchonLoc != null) {
                targetLoc = nearestEnemyArchonLoc;
                isTargetingArchon = true;
            } else {
                targetLoc = Util.getExploreLoc(rc);
                isTargetingArchon = false;
            }
        }

        bfs.move(targetLoc);
        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }

    static void safeAttack(RobotController rc, MapLocation attackLocation) throws GameActionException {
        if (rc.canAttack(attackLocation)) {
            rc.attack(attackLocation);
        }
    }
}
