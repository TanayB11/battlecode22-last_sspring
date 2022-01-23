package bighero_six;

import battlecode.common.*;
import bighero_six.util.pathfinding.BFS;
import bighero_six.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static bighero_six.util.Communication.minerReport;
import static bighero_six.util.Exploration.minerExploreLoc;
import static bighero_six.util.Miscellaneous.directions;
import static bighero_six.util.Communication.reportEnemy;
import static bighero_six.util.Miscellaneous.retreatFrom;
import static bighero_six.util.SafeActions.safeMine;

public class MinerController {
    static MapLocation miningTarget = null, exploreTarget = null;
    static int mineTargetRubble = Integer.MAX_VALUE;
    static BFS bfs = null;
    static int prevHP = 0;
    static boolean mineLock = false;
    final static int MINE_TARGET_DIFF_THRESHOLD = 15;

    static void runMiner(RobotController rc) throws GameActionException {
        //part 1: prep
        MapLocation me = rc.getLocation();

        // initialize
        if (bfs == null) {
            bfs = new DroidBFS(rc);
            prevHP = rc.getHealth();
        }

        // add itself to the unit count
        minerReport(rc);

        // set targets to null accordingly
        if (exploreTarget != null && rc.canSenseLocation(exploreTarget) && rc.senseLead(exploreTarget) < 2 && rc.senseGold(exploreTarget) == 0) {
            exploreTarget = null;
        }

        if (miningTarget != null && rc.canSenseLocation(miningTarget) && rc.senseLead(miningTarget) < 2 && rc.senseGold(miningTarget) == 0) {
            miningTarget = null;
        }

        //report nearby enemies to shared array (from v5)
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo retreatFromEnemy = null; // nearest dangerous enemy (defaults to null)

        if (nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, Comparator.comparingInt(a -> me.distanceSquaredTo(a.getLocation())));
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
                if (isAttackingEnemy(nearbyEnemies[i])) {
                    retreatFromEnemy = nearbyEnemies[i];
                    break;
                }
            }
        }

        //find a mining target
            //find square with the lowest rubble
            //&& its unoccupied && adjacent to/on lead (more lead wins tiebreaker)
            //if low health, go back for healing
        if (!mineLock) {
            MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold();
            MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(-1, 2);
            int minRubble = Integer.MAX_VALUE;

            MapLocation optimalLoc = null;

            if (nearbyGold.length > 0) {
                Arrays.sort(nearbyGold, Comparator.comparingInt(a -> me.distanceSquaredTo(a)));
                optimalLoc = getOptimalMineLoc(rc, nearbyGold);
            } else if (nearbyLead.length > 0) {
                Arrays.sort(nearbyLead, Comparator.comparingInt(a -> me.distanceSquaredTo(a)));
                optimalLoc = getOptimalMineLoc(rc, nearbyLead);
            }

//            rc.setIndicatorString(optimalLoc != null ? optimalLoc.toString() : "NO OPTLOC");

            if (miningTarget == null && optimalLoc != null) {
                rc.setIndicatorString("UPDATED 1");
                miningTarget = optimalLoc;
                mineTargetRubble = rc.senseRubble(miningTarget);
            } else if (optimalLoc != null && rc.senseRubble(optimalLoc) + MINE_TARGET_DIFF_THRESHOLD < mineTargetRubble) {
                rc.setIndicatorString("UPDATED 2");
                miningTarget = optimalLoc;
                mineTargetRubble = rc.senseRubble(optimalLoc);
            }
            mineLock = miningTarget != null;
        } else {
            exploreTarget = null;
        }

//        rc.setIndicatorString(miningTarget != null ? (miningTarget.toString()) : "NO MINE TARGET");

        //part 2: execution

        //mine adjacent squares
        mineAdj(rc, me);

        //if attacked, run away
        if (rc.getHealth() < prevHP && retreatFromEnemy != null) {
            retreatFrom(rc, retreatFromEnemy.getLocation());
            mineLock = false;
            exploreTarget = null;
            prevHP = rc.getHealth();
        } else if (miningTarget != null) {
            // don't combine if statements, this removes janky edge cases :()
            if (!mineLock) {
                bfs.move(miningTarget);
            }
        } else {
            if (exploreTarget == null) {
                exploreTarget = minerExploreLoc(rc);
            }
            bfs.move(exploreTarget);
        }

        //mine adjacent squares again
        mineAdj(rc, me);
    }

    // TODO: check location itself, check if occupied
    private static MapLocation getOptimalMineLoc(RobotController rc, MapLocation[] nearbyResource) throws GameActionException {
        int minRubble = Integer.MAX_VALUE;
        MapLocation optimalLocation = null;
        for (int i = 0; i < nearbyResource.length; i++) {
            for (int j = 0; j < directions.length; j++){
                if (rc.canSenseLocation(nearbyResource[i].add(directions[j]))) {
                    if (rc.senseRubble(nearbyResource[i].add(directions[j])) < minRubble) {
                        optimalLocation = nearbyResource[i].add(directions[j]);
                        minRubble = rc.senseRubble(nearbyResource[i].add(directions[j]));
                    }
                }
            }
        }
        return optimalLocation;
    }

    static boolean isAttackingEnemy(RobotInfo enemy) throws GameActionException {
        RobotType enemyType = enemy.getType();
        if (enemyType.equals(RobotType.SOLDIER) || enemyType.equals(RobotType.SAGE) || enemyType.equals(RobotType.WATCHTOWER)) {
            return true;
        }
        return false;
    }

    static void mineAdj(RobotController rc, MapLocation me) throws GameActionException {
        for (int dx = -1; dx++ <= 1;) {
            for (int dy = -1; dy++ <= 1;) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
                mineLock = false;
            }
        }
    }
}
