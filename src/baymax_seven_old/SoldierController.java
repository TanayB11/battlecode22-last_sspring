package baymax_seven_old;

import battlecode.common.*;
import baymax_seven_old.util.pathfinding.BFS;
import baymax_seven_old.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static baymax_seven_old.util.Communication.*;
import static baymax_seven_old.util.Miscellaneous.*;

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
// 10. healing (IMPORTANT)

// overhaul troop counter, add old methods to comms ( DONE )
// implement retreat to nearest friendly archon (implement for miner, too) (DONE)
    // if too many friendlies nearby need healing, disintegrate -> lead farm
    // the ones with lowest health disintegrate in nonlead squares (threshold = ?)
// TODO: add archon healing (heal by lowest health, prioritize troops)
    // (archon priority) Soldiers > miners
// TODO: test soldier-miner (wihthoulikelikngs for exiting insert modebke
// TODO (archon): move to lowest rubble if the lowest rubble in vision is at least 25 less than our current
    // compare fixed and variable cost

public class SoldierController {
    static BFS bfs = null;
    static MapLocation goal = null; // either waypoint or comms goal

    static MapLocation[] waypoints;
    static boolean goingToWaypt = false;
    static int numValidWaypoints;

    static MapLocation nearestArch = null;
    static MapLocation templeOfSacrifice = null;

    static boolean isDying = false;

    static final int SURROUNDED_THRESHOLD = 5;
    static final int ACCEPTABLE_TARGET_LOC_RUBBLE = 50;

    public static void runSoldier(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        // initialize
        if (bfs == null) {
            bfs = new DroidBFS(rc);
            waypoints = calcWaypoints(rc);
            numValidWaypoints = rc.getArchonCount();
        }

        /*
        Macro strategy implementation
        */

        soldierReport(rc);

        // report nearby enemies (macro)
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        // if we're dying, return to be healed
        // consider ourselves dying until we're near full HP
        if (isDying && rc.getHealth() > 0.92 * RobotType.SOLDIER.health) {
            isDying = false;
        } else if (!isDying && rc.getHealth() < 0.4 * RobotType.SOLDIER.health) {
            isDying = true;
            goingToWaypt = false;

            nearestArch = getNearestFriendlyArch(rc);
            goal = nearestArch;
        }

        // Disintegrate or return to archon for healing
        if (
            isDying && rc.getHealth() < 0.3 * RobotType.SOLDIER.health
        ) {
            MapLocation nearestFriendlyArch = getNearestFriendlyArch(rc);

            // find best suicide location
            if (me.distanceSquaredTo(nearestFriendlyArch) < RobotType.SOLDIER.visionRadiusSquared && templeOfSacrifice == null) {
                MapLocation[] possibleDisintLocs = rc.getAllLocationsWithinRadiusSquared(me, RobotType.SOLDIER.visionRadiusSquared);

                // TODO: change to simple check for max value
                Arrays.sort(possibleDisintLocs, Comparator.comparingInt(loc -> {
                    try {
                        return nearestFriendlyArch.distanceSquaredTo(loc);
                    } catch (Exception e) {
                        return Integer.MAX_VALUE;
                    }
                }));

                for (int i = 0; i < possibleDisintLocs.length; i++) {
                    if (rc.senseLead(possibleDisintLocs[i]) == 0 && !rc.isLocationOccupied(possibleDisintLocs[i])) {
                        templeOfSacrifice = possibleDisintLocs[i];
                        goal = templeOfSacrifice;
                        break;
                    }
                }
            }

            if (me.equals(templeOfSacrifice)) {
                rc.disintegrate();
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
        } else if (!isDying) {
            goal = commsGoal != null ? commsGoal : waypoints[rng.nextInt(numValidWaypoints)];
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

        boolean surroundedByAllies = getNearbyAtkAllies(rc, me) > SURROUNDED_THRESHOLD;
        if (targetEnemy != null && !isDying) {
            MapLocation enemyLoc = targetEnemy.getLocation();
            boolean enemyInRange = me.isWithinDistanceSquared(enemyLoc, RobotType.SOLDIER.actionRadiusSquared);

            // 2.1 Attack strategy: attack-retreat like a fencer

            // implement move-attack-retreat decision tree
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
        } else {
            bfs.move(goal);
        }
    }

    // TODO : do this in archonController
    // NumWaypoints = num of archons
    private static MapLocation[] calcWaypoints(RobotController rc) throws GameActionException {
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
            int mapWidth = rc.getMapWidth();
            int mapHeight = rc.getMapHeight();
            boolean isXReflection =
                flipLocation(arch0Loc, false, mapWidth, mapHeight).equals(arch1Loc) ||
                flipLocation(arch0Loc, false, mapWidth, mapHeight).equals(arch2Loc) ||
                flipLocation(arch0Loc, false, mapWidth, mapHeight).equals(arch3Loc);
            waypoints[0] = avgLoc(arch0Loc, flipLocation(arch0Loc, isXReflection, mapWidth, mapHeight));
            waypoints[1] = avgLoc(arch1Loc, flipLocation(arch1Loc, isXReflection, mapWidth, mapHeight));
            waypoints[2] = avgLoc(arch2Loc, flipLocation(arch2Loc, isXReflection, mapWidth, mapHeight));
            waypoints[3] = avgLoc(arch3Loc, flipLocation(arch3Loc, isXReflection, mapWidth, mapHeight));
        }
        return waypoints;
    }

    // helper function to determine whether two locations are rotated by 180°
    private static boolean areRotated(MapLocation l1, MapLocation l2) {
        return l1 != null && l2 != null && l1.x == l2.y && l1.y == l2.x;
    }

    private static MapLocation rotateLocation(MapLocation loc) {
        return (loc != null) ? new MapLocation(loc.y, loc.x) : null;
    }

    private static MapLocation flipLocation(MapLocation loc, boolean reflectX, int mapWidth, int mapHeight) {
        if (reflectX) {
            return (loc != null) ? new MapLocation(loc.x, mapHeight - loc.y) : null;
        } else {
            return (loc != null) ? new MapLocation(mapWidth - loc.x, loc.y) : null;
        }
    }


    private static MapLocation avgLoc(MapLocation l1, MapLocation l2) {
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