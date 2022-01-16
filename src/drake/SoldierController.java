package drake;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import drake.util.pathfinding.BFS;
import drake.util.pathfinding.DroidBFS;

import java.util.Arrays;

import static drake.util.Communication.*;
import static drake.util.Exploration.minerExploreLoc;
import static drake.util.Miscellaneous.ATTACK_PRIORITY_COMPARATOR;
import static drake.util.SafeActions.safeAttack;


public class SoldierController {
    static final int ACCEPTABLE_TARGET_LOC_RUBBLE = 50;
    // disintegrate if we're dying and this close to our archon
    static final int MAX_DISINTEGRATION_DISTANCE = 4;

    static BFS bfs = null;
    static MapLocation me = null, targetLoc = null;
    static int prevHP = Integer.MIN_VALUE;
    static boolean isDying = false;
    static int currTargetingEnemyID = -1; // we're not targeting an enemy

    // TODO: erase from array

    public static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // spawn init
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            bfs = new DroidBFS(rc);
        }

        // reports if dying, returns to nearest archon to sacrifice
        if (!isDying && rc.getHealth() < rc.getType().health * 0.2) {
            int soldiersCt = readNumSoldiers(rc);
            writeNumSoldiers(rc, soldiersCt - 1);
            isDying = true;

            // check if returns null
            // TODO: uncomment and re-implement findNearestFriendlyArchon
//            MapLocation nearestArchon = Util.findNearestFriendlyArchon(rc);
//            targetLoc = nearestArchon;
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
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        // sense nearby robots, attack in priority order
        // TODO (TEST): keep tabs on the id of the robot we're attacking, chase it down
        if (nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, ATTACK_PRIORITY_COMPARATOR);
            RobotInfo targetEnemy = nearbyEnemies[0];
            if (currTargetingEnemyID != -1) { // already locked onto a target
                for (RobotInfo enemy : nearbyEnemies) {
                    if (enemy.getID() == currTargetingEnemyID) {
                        targetEnemy = enemy;
                        break;
                    }
                }
            }
            currTargetingEnemyID = targetEnemy.getID();
            targetLoc = targetEnemy.getLocation();
            safeAttack(rc, targetLoc);
         } else {
            currTargetingEnemyID = -1;
            // if we're within sight of target and we see no enemies, reset
            if (targetLoc != null && rc.canSenseLocation(targetLoc)) {
                targetLoc = null;
            }
        }

        // get target from comms
        if (targetLoc == null) {
            // TODO: replace this with a unique soldier exploration method eventually
            // this is not an ASAP TO-DO
            // TODO : fix
            MapLocation potentialTarget = getTarget(rc);
            targetLoc = (potentialTarget != null) ? potentialTarget : minerExploreLoc(rc);
//            if (potentialTarget != null) {
//                targetLoc = potentialTarget;
//                rc.setIndicatorString("NEW TARGET: " + targetLoc.toString());
//            } else {
//                targetLoc = minerExploreLoc(rc);
//            }
        }

        bfs.move(targetLoc);
        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }
}