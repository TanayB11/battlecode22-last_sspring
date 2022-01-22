package sma;

import battlecode.common.*;
import sma.util.pathfinding.BFS;
import sma.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static sma.util.Communication.getGoal;
import static sma.util.Communication.reportEnemy;
import static sma.util.Exploration.soldierExploreLoc;
import static sma.util.Miscellaneous.*;
import static sma.util.SafeActions.safeMove;

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
//10. healing (IMPORTANT)

public class SoldierController {
    static BFS bfs = null;
    static MapLocation goal = null; // comes from comms
    static boolean isExploring = false;

    static final int SURROUNDED_THRESHOLD = 4, MIN_ACCEPTABLE_RUBBLE = 25;
    static final int ACCEPTABLE_TARGET_LOC_RUBBLE = 50;

    static void runSoldier(RobotController rc) throws GameActionException {
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

        // if we're at goal, reset
        // also avoid an exploration target being a high rubble square
        if (
                isExploring &&
                        goal != null && rc.canSenseLocation(goal) &&
                        rc.senseRubble(goal) > ACCEPTABLE_TARGET_LOC_RUBBLE
        ) {
            goal = null;
        }

        // refresh goal location from comms array
        MapLocation commsGoal = getGoal(rc);
        if (isExploring && commsGoal != null) {
            goal = commsGoal;
            isExploring = false;
        } else {
            goal = commsGoal != null ? commsGoal : soldierExploreLoc(rc);
            isExploring = (commsGoal == null);
        }

        /*
        Micro strategy implementation
        */

        // 1. Sense enemy to attack (target based on priority + low HP)

        RobotInfo targetEnemy = null;

        if (nearbyEnemies.length > 0) {
            targetEnemy = nearbyEnemies[0];

            // Prioritize based on damage to health to ratio for attacking units
            // Non-damaging robots: prioritize based on specified order
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
        // Travel away from target if needed
        if (targetEnemy != null) {
            MapLocation targetLoc = targetEnemy.getLocation();
            boolean surroundedByAllies = getNearbyAtkAllies(rc, me) > SURROUNDED_THRESHOLD;

            // Move to target enemy until:
            // 1. The target is on the edge of attack radius
            // 2. If we're surrounded by allies, continue

            // TODO: fix attacking (doesn't always attack on edge of action circle)

            if (rc.canAttack(targetLoc)) {
                rc.attack(targetLoc);
            } else if (
                    (!me.isWithinDistanceSquared(targetLoc, RobotType.SOLDIER.actionRadiusSquared) &&
                            rc.isActionReady()) || surroundedByAllies
            ) {
                bfs.move(targetLoc);
            } else if (me.isWithinDistanceSquared(targetLoc, RobotType.SOLDIER.actionRadiusSquared)) {
                // Retreat:
                // Of 8 possible movement directions, eliminate 3
                // Favor the 3 away from enemy, if those are not beyond rubble threshold
                // Otherwise look at the other two "neutral"/sideways directions, pick min rubble of all 5

                Direction dirToTarget = me.directionTo(targetLoc);
                Direction dirOfRetreat = null;
                Direction[] initPossibleDirs = {null, null, null};

                // Ideal directions to move
                initPossibleDirs[0] = dirToTarget.opposite().rotateLeft();
                initPossibleDirs[1] = dirToTarget.opposite();
                initPossibleDirs[2] = dirToTarget.opposite().rotateRight();

                Arrays.sort(initPossibleDirs, Comparator.comparingInt(dir -> {
                    try {
                        return Integer.valueOf(rc.senseRubble(rc.adjacentLocation(dir)));
                    } catch (GameActionException e) {
                        // probably off the map, infinite rubble
                        return Integer.MAX_VALUE;
                        // e.printStackTrace();
                    }
                }));

                int bestIndex = 0;
                if (rc.onTheMap(rc.adjacentLocation(initPossibleDirs[0]))) {
                    bestIndex = 0;
                } else if (rc.onTheMap(rc.adjacentLocation(initPossibleDirs[1]))) {
                    bestIndex = 1;
                } else if (rc.onTheMap(rc.adjacentLocation(initPossibleDirs[2]))) {
                    bestIndex = 2;
                }

                // Neutral positions (move sideways)
                int lowestRubble = rc.senseRubble(rc.adjacentLocation(initPossibleDirs[bestIndex]));
                if (lowestRubble > MIN_ACCEPTABLE_RUBBLE) {
                    Direction neutralDir1 = dirToTarget.rotateRight().rotateRight();
                    Direction neutralDir2 = dirToTarget.rotateLeft().rotateLeft();
                    int neutralDir1Rubble = Integer.MAX_VALUE;
                    int neutralDir2Rubble = Integer.MAX_VALUE;

                    if (rc.onTheMap(rc.adjacentLocation(neutralDir1))) {
                        neutralDir1Rubble = rc.senseRubble(rc.adjacentLocation(neutralDir1));
                        if (neutralDir1Rubble < lowestRubble) {
                            bestIndex = 3;
                            lowestRubble = neutralDir1Rubble;
                        }
                    }

                    if (rc.onTheMap(rc.adjacentLocation(neutralDir2))) {
                        neutralDir2Rubble = rc.senseRubble(rc.adjacentLocation(neutralDir2));
                        if (neutralDir2Rubble < lowestRubble) {
                            bestIndex = 4;
                        }
                    }
                }

                switch (bestIndex){
                    case 3:     dirOfRetreat = dirToTarget.rotateRight().rotateRight(); break;
                    case 4:     dirOfRetreat = dirToTarget.rotateLeft().rotateLeft(); break;
                    default:    dirOfRetreat = initPossibleDirs[bestIndex]; break;
                }

                rc.setIndicatorString("RETREATING IN " + dirOfRetreat.toString()); // TODO: remove
                safeMove(rc, dirOfRetreat);
            }
        } else {
            // Last. Move to goal (if we can)
            bfs.move(goal);
        }
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