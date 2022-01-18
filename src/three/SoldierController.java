package three;

import battlecode.common.*;
import three.util.pathfinding.BFS;
import three.util.pathfinding.DroidBFS;

import java.util.Arrays;

import static three.util.Communication.*;
import static three.util.Exploration.minerExploreLoc;
import static three.util.Miscellaneous.ATTACK_PRIORITY_COMPARATOR;
import static three.util.SafeActions.safeAttack;
import static three.util.SafeActions.safeMove;


// TODO: use a unique soldier exploration method eventually
// TODO: swap disintegration for healing (if worth it)
// TODO: check that targetLoc should always have a value
    // update reportEnemy

public class SoldierController {
    static final int ACCEPTABLE_TARGET_LOC_RUBBLE = 50;
    static final int ACCEPTABLE_PAUSE_LOC_RUBBLE = 25;
    static final int MAX_DISINTEGRATION_DISTANCE = 4; // disintegrate if dying + this close to our archon
    static final int NUM_ALLIES_TO_BE_SURROUNDED = 8;

    static BFS bfs = null;
    static boolean isDying = false;
    static int prevHP = Integer.MIN_VALUE;
    static MapLocation me = null, targetLoc = null;
    static int currTargetingEnemyID = -1; // not targeting an enemy
    static boolean wantToContinue = true; // don't retreat

    public static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // spawn init
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            bfs = new DroidBFS(rc);
        }

        // if we're on low hp, consider retreating to heal (if it's worth)
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

        // TODO: replace with healing
        if (targetLoc != null && isDying && me.distanceSquaredTo(targetLoc) <= MAX_DISINTEGRATION_DISTANCE) {
            rc.disintegrate();
        }

        // avoid an exploration target being a high rubble square
        if (
            targetLoc != null &&
            rc.canSenseLocation(targetLoc) &&
            rc.senseRubble(targetLoc) > ACCEPTABLE_TARGET_LOC_RUBBLE
        ) {
            targetLoc = null;
        }

        // report nearby enemies
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        // sense nearby robots, attack in priority order (even while heading to target)
        if (nearbyEnemies.length > 0) {
            RobotInfo currentEnemy = nearbyEnemies[0];
            RobotInfo targetEnemy = nearbyEnemies[0];
            for (int i = 0; i < nearbyEnemies.length; i++) {
                currentEnemy = nearbyEnemies[i];

                if (getReportEnemyPriority(currentEnemy.getType()) > getReportEnemyPriority(targetEnemy.getType())) {
                    targetEnemy = currentEnemy;
                } else if (getReportEnemyPriority(currentEnemy.getType()) == getReportEnemyPriority(targetEnemy.getType())) {
                    if (targetEnemy.getHealth() > currentEnemy.getHealth()) {
                        targetEnemy = currentEnemy;
                    }
                }
            }

            // get nearby friendlies (advance if we have friendlies with us)
            RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
            int numNearbyAllies = 0;
            for (RobotInfo ally : nearbyAllies) {
                if (ally.getType().equals(RobotType.SOLDIER)) {
                    numNearbyAllies++;
                }
            }

            currTargetingEnemyID = targetEnemy.getID();
            targetLoc = targetEnemy.getLocation();
            RobotType enemType = targetEnemy.getType();
            wantToContinue = (numNearbyAllies >= NUM_ALLIES_TO_BE_SURROUNDED &&
                    rc.senseRubble(me) <= ACCEPTABLE_PAUSE_LOC_RUBBLE) ||
                    (enemType == RobotType.MINER && enemType == RobotType.LABORATORY);

            // retreat temporarily if we can't attack
            if (!safeAttack(rc, targetLoc) && !wantToContinue) {
                safeMove(rc, me.directionTo(targetLoc).opposite());
            }
         } else {
            currTargetingEnemyID = -1;
            // if we're within sight of target and we see no enemies, reset
            if (targetLoc != null && rc.canSenseLocation(targetLoc)) {
                targetLoc = null;
            }
        }

        // get target from comms
        if (targetLoc == null) {
            MapLocation potentialTarget = getTarget(rc);
            targetLoc = (potentialTarget != null) ? potentialTarget : minerExploreLoc(rc);
        }

        // if we can attack enemy and are not on rubble, don't move closer
        if (!rc.canAttack(targetLoc) || !wantToContinue) {
            bfs.move(targetLoc);
        }

        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }
}
