package bighero_six;

import battlecode.common.*;
import bighero_six.util.pathfinding.BFS;
import bighero_six.util.pathfinding.DroidBFS;

import static bighero_six.util.Communication.*;
import static bighero_six.util.Miscellaneous.*;

//printf's soldier micro
//1. Avoid rubble when in combat.
//2. Move out of enemy range when not able to attack.
//3. Retreat when low health to get healed by archon.
//4. Multi-tiered pathfinding based on bytecode left.
//5. Put enemy targets in shared array.
//6. Prioritize low HP, dangerous enemies for targeting.
//7. If surrounded by allied soldiers, advance even if already in range of target.
//8. Keep track of past few locations so you don't get stuck in pathfinding loop on pathological cases.
//=======
//9. stay on the outside of attacking range

// TODO:
// implement waypoints, retreat as soon as the soldier attacks (like a fencer)
// 10. healing (IMPORTANT)

public class SoldierController {
    static BFS bfs = null;
    static MapLocation goal = null; // either waypoint or comms goal
    static boolean goingToWaypt = false;

    static final int SURROUNDED_THRESHOLD = 5, MIN_ACCEPTABLE_RUBBLE = 25;
    static final int ACCEPTABLE_TARGET_LOC_RUBBLE = 50;

    public void runSoldier(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        // initialize
        if (bfs == null) {
            bfs = new DroidBFS(rc);
        }

        /*
        Macro strategy implementation
        */

        // report nearby enemies (macro)
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        // avoid a waypoint being a high rubble square
        if (
            goingToWaypt && goal != null &&
            rc.canSenseLocation(goal) && rc.senseRubble(goal) > ACCEPTABLE_TARGET_LOC_RUBBLE
        ) {
            goal = null;
        }

        // refresh goal location from comms array
        MapLocation commsGoal = getGoal(rc);
        if (goingToWaypt && commsGoal != null) {
            goal = commsGoal;
            goingToWaypt = false;
        } else {
            goal = commsGoal != null ? commsGoal : getNearestWaypt(rc);
            goingToWaypt = (commsGoal == null);
        }

        /*
        Micro strategy implementation
        */

        // 1. Sense enemy to attack (target based on priority + low HP)
        // Prioritize based on damage to health to ratio for attacking units
        // Non-damaging robots: prioritize based on specified order
        RobotInfo targetEnemy = null;
        if (nearbyEnemies.length > 0) {
            targetEnemy = nearbyEnemies[0];
            for (int i = 1; i < nearbyEnemies.length; i++) {
                if (compareDamageHPRatio(nearbyEnemies[i], targetEnemy)) {
                    targetEnemy = nearbyEnemies[i];
                } else if (
                    !isAttackingUnit(targetEnemy.getType()) &&
                    getNonAttackingUnitPriority(nearbyEnemies[i].getType()) >
                        getNonAttackingUnitPriority(targetEnemy.getType())
                ) {
                    targetEnemy = nearbyEnemies[i];
                }
            }
        }

        // 2. If enemy is hostile, travel until enemy is on edge of attack radius, unless we're swarmed by allies
            // If we're swarmed by allies, then go to the enemy
            // If we're low on health, retreat to nearest archon (needs comms)
        if (targetEnemy != null && isAttackingUnit(targetEnemy.getType())) {
            MapLocation enemyLoc = targetEnemy.getLocation();
            boolean surroundedByAllies = getNearbyAtkAllies(rc, me) > SURROUNDED_THRESHOLD;
            boolean enemyInRange = me.isWithinDistanceSquared(enemyLoc, RobotType.SOLDIER.actionRadiusSquared;

            // 2.1 Attack strategy: attack-retreat like a fencer

            // implement move-attack-retreat decision tree
                // TODO (TESTING): ensure rc.attack never throws exceptions, then replace all attacks with safeAttack
            if (rc.isActionReady()) {
                if (enemyInRange) {
                    rc.attack(enemyLoc);
                    if (surroundedByAllies){
                        bfs.move(enemyLoc);
                    } else {
                        retreatFrom(rc, enemyLoc);
                    }
                } else {
                    bfs.move(enemyLoc);
                    if (rc.canAttack(enemyLoc)) { rc.attack(enemyLoc); }
                    if (!surroundedByAllies) { retreatFrom(rc, enemyLoc); }
                }
            } else if (enemyInRange) {
                retreatFrom(rc, enemyLoc);
            }
        } else if (targetEnemy != null){
            MapLocation enemyLoc = targetEnemy.getLocation();
            bfs.move(enemyLoc);
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }
        } else {
            bfs.move(goal);
        }
    }

    // TODO : calculate and store waypoints at start of the game, do this in archonController
    // NumWaypoints = num of archons
    private MapLocation[] getNearestWaypt(RobotController rc) throws GameActionException {
        // figure out symmetery
        MapLocation arch0Loc = readArchLoc(rc, 0);
        MapLocation arch1Loc = readArchLoc(rc, 1);
        MapLocation arch2Loc = readArchLoc(rc, 2);
        MapLocation arch3Loc = readArchLoc(rc, 3);

        MapLocation[] waypoints = {null, null, null, null};
        if (arch1Loc == null) {
            waypoints[0] = rotateLocation(arch0Loc);
            return waypoints;
        }

        boolean isRotationallySymmetric = areRotated(arch0Loc, arch1Loc) ||
            areRotated(arch0Loc, arch2Loc) || areRotated(arch0Loc, arch3Loc);

        if (isRotationallySymmetric) {
            waypoints[0] = avgLoc(arch0Loc, rotateLocation(arch0Loc));
            waypoints[1] = avgLoc(arch0Loc, rotateLocation(arch1Loc));
            waypoints[2] = avgLoc(arch0Loc, rotateLocation(arch2Loc));
            waypoints[3] = avgLoc(arch0Loc, rotateLocation(arch3Loc));
        } else {
            // find out whether it's x or y axis reflection
        }
        return waypoints;
    }

    // helper function to determine whether two locations are rotated by 180°
    private boolean areRotated(MapLocation l1, MapLocation l2) {
        return l1 != null && l2 != null && l1.x == l2.y && l1.y == l2.x;
    }

    private MapLocation rotateLocation(MapLocation loc) {
        return (loc != null) ? new MapLocation(loc.y, loc.x) : null;
    }

    private MapLocation flipLocation(MapLocation loc, boolean reflectX) {
        if (reflectX) {
            return new M
        } else {

        }
    }


    private MapLocation avgLoc(MapLocation l1, MapLocation l2) {
        return (l1 != null && l2 != null) ? new MapLocation((l1.x + l2.x) / 2, (l1.y + l2.y) / 2) : null;
    }

    // Helper method to count number of nearby attacking troops
    public static int getNearbyAtkAllies(RobotController rc, MapLocation me) {
        RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (nearbyFriendlies.length > 0) {
            int nearbyAtkAllies = 0;
            for (int i = 0; i < nearbyFriendlies.length; i++) {
                if (isAttackingUnit(nearbyFriendlies[i].getType())) {
                    nearbyAtkAllies++;
                }
            }
            return  nearbyAtkAllies;
        }
        return 0;
    }
}