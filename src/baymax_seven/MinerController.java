package baymax_seven;

import battlecode.common.*;
import baymax_seven.util.pathfinding.BFS;
import baymax_seven.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static baymax_seven.util.Communication.*;
import static baymax_seven.util.Exploration.minerExploreLoc;
import static baymax_seven.util.Miscellaneous.directions;
import static baymax_seven.util.Miscellaneous.retreatFrom;

public class MinerController {
    static MapLocation miningTarget = null, exploreTarget = null;
    static int[] rubbleCounts = new int[1800];
    static int[] rubbleCheckedLocations = new int[120];
    static MapLocation[] ourInitialArchonLocations = new MapLocation[4];
    static MapLocation[] initialEnemyArchLocs = new MapLocation[4];
    static BFS bfs = null;
    static int prevHP = 0;

    // 1 = rotational
    // 2 = vertical
    // 3 = horizontal
    static int symmetryType = 0;

    static void runMiner(RobotController rc) throws GameActionException {
        //part 1: prep
        MapLocation me = rc.getLocation();

        if (symmetryType == 0) {
            int symmetryFlagCount = 0;
            int determineWhich = 0;

            if (checkFlag(rc, 2)) {
                symmetryFlagCount += 1;
                determineWhich += 1;
            }

            if (checkFlag(rc, 3)) {
                symmetryFlagCount += 1;
                determineWhich += 2;
            }

            if (checkFlag(rc, 4)) {
                symmetryFlagCount += 1;
                determineWhich += 3;
            }

            if (symmetryFlagCount == 2) {
                if (determineWhich == 5) {
                    symmetryType = 1;
                } else if (determineWhich == 4) {
                    symmetryType = 2;
                } else {
                    symmetryType = 3;
                }

                if (symmetryType == 1) {
                    for (int i = 0; i < 4; i++) {
                        if (ourInitialArchonLocations[i] != null) {
                            initialEnemyArchLocs[i] = new MapLocation(ourInitialArchonLocations[i].y, ourInitialArchonLocations[i].x);
                        }
                    }
                } else if (symmetryType == 2) {
                    for (int i = 0; i < 4; i++) {
                        if (ourInitialArchonLocations[i] != null) {
                            initialEnemyArchLocs[i] = new MapLocation(ourInitialArchonLocations[i].x, rc.getMapHeight() - ourInitialArchonLocations[i].y);
                        }
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        if (ourInitialArchonLocations[i] != null) {
                            initialEnemyArchLocs[i] = new MapLocation(rc.getMapWidth() - ourInitialArchonLocations[i].x, ourInitialArchonLocations[i].y);
                        }
                    }
                }
            }
        }

        // redundancy exists for a reason :pleading_face:, some slight bytecode savings i think
        if (symmetryType == 0) {
            MapLocation[] visibleLocations = rc.getAllLocationsWithinRadiusSquared(me, 1000);
            for (int i = 0; i < visibleLocations.length; i++) {
                reportRubble(rc, visibleLocations[i]);
            }
        }

        // initialize
        if (bfs == null) {
            bfs = new DroidBFS(rc);
            prevHP = rc.getHealth();

            for (int i = 0; i < rc.getArchonCount(); i++) {
                ourInitialArchonLocations[i] = readArchLoc(rc, i);
            }
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
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold();
        MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(-1, 2);
        MapLocation optimalLoc = null;

        if (nearbyGold.length > 0) {
            optimalLoc = getOptimalMineLoc(rc, nearbyGold);
        } else if (nearbyLead.length > 0) {
            optimalLoc = getOptimalMineLoc(rc, nearbyLead);
        }

        if (miningTarget == null && optimalLoc != null) {
            rc.setIndicatorString(optimalLoc.toString());
            miningTarget = optimalLoc;
        }

        //part 2: execution

        //mine adjacent squares
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }

                if (shouldOffensivelyMine(mineLocation)) {
                    while (rc.canMineLead(mineLocation)) {
                        rc.mineLead(mineLocation);
                    }
                } else {
                    while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                        rc.mineLead(mineLocation);
                    }
                }
            }
        }

        //if attacked, run away
        if (rc.getHealth() < prevHP && retreatFromEnemy != null) {
            retreatFrom(rc, retreatFromEnemy.getLocation());
            exploreTarget = null;
            prevHP = rc.getHealth();
        } else if (miningTarget != null) {
            bfs.move(miningTarget);
        } else if (exploreTarget == null) {
            exploreTarget = minerExploreLoc(rc);
        }

        // will only execute if we haven't moved yet this turn
        bfs.move(exploreTarget);

        //mine adjacent squares again
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }

                if (shouldOffensivelyMine(mineLocation)) {
                    while (rc.canMineLead(mineLocation)) {
                        rc.mineLead(mineLocation);
                    }
                } else {
                    while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                        rc.mineLead(mineLocation);
                    }
                }
            }
        }
    }

    private static MapLocation getOptimalMineLoc(RobotController rc, MapLocation[] nearbyResource) throws GameActionException {
        int minRubble = Integer.MAX_VALUE;
        MapLocation optimalLocation = null;
        int resourceRubble;
        for (int i = 0; i < nearbyResource.length; i++) {

            // check on the square itself
            resourceRubble = rc.senseRubble(nearbyResource[i]);
            if (resourceRubble < minRubble) {
                optimalLocation = nearbyResource[i];
                minRubble = resourceRubble;
            }

            // check adjacent squares
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

    static void reportRubble(RobotController rc, MapLocation loc) throws GameActionException {
        if (!isRubbleChecked(loc)) {
            int squareIndex = loc.y * 60 + loc.x;

            if (squareIndex % 2 == 0) {
                rubbleCounts[squareIndex / 2] = (rubbleCounts[squareIndex / 2] & ~65280) | ((rc.senseRubble(loc)+1) << 8);
            } else {
                rubbleCounts[squareIndex / 2] = (rubbleCounts[squareIndex / 2] & ~255) | (rc.senseRubble(loc)+1);
            }

            updateSharedArraySymmetry(rc, loc);
            markRubbleChecked(loc);
        }
    }

    static void updateSharedArraySymmetry(RobotController rc, MapLocation reportedLoc) throws GameActionException {
        int reportX = reportedLoc.x;
        int reportY = reportedLoc.y;

        MapLocation rotational = new MapLocation(reportY, reportX);
        MapLocation vertical = new MapLocation(reportX, rc.getMapHeight() - reportY);
        MapLocation horizontal = new MapLocation(rc.getMapWidth() - reportX, reportY);

        int rotationalRubble = checkRubble(rotational);
        int verticalRubble = checkRubble(vertical);
        int horizontalRubble = checkRubble(horizontal);

        if (rotationalRubble != 0 && rotationalRubble != rc.senseRubble(reportedLoc)) {
            throwFlag(rc, 2);
        }

        if (verticalRubble != 0 && verticalRubble != rc.senseRubble(reportedLoc)) {
            throwFlag(rc, 3);
        }

        if (horizontalRubble != 0 && verticalRubble != rc.senseRubble(reportedLoc)) {
            throwFlag(rc,4);
        }
    }

    static int checkRubble(MapLocation loc) {
        int squareIndex = loc.x * 60 + loc.x;

        if (squareIndex % 2 == 0) {
            return ((rubbleCounts[squareIndex / 2] & 65280) >> 8);
        } else {
            return (rubbleCounts[squareIndex / 2] & 255);
        }
    }

    static void markRubbleChecked(MapLocation loc) {
        int arrayPos = loc.x / 2 + (loc.y < 32 ? 0 : 1);
        int bitPos = loc.y % 32;
        rubbleCheckedLocations[arrayPos] |= (1 << bitPos);
    }

    static boolean isRubbleChecked(MapLocation loc) {
        int arrayPos = loc.x / 2 + (loc.y < 32 ? 0 : 1);
        int bitPos = loc.y % 32;
        return ((rubbleCheckedLocations[arrayPos] & (1 << bitPos)) > 0);
    }

    static boolean shouldOffensivelyMine(MapLocation mineTarget) {
        if (symmetryType != 0) {
            int ourMinDistance = Integer.MAX_VALUE;
            int theirMinDistance = Integer.MAX_VALUE;

            for (int i = 0; i < 4; i++) {
                if (ourInitialArchonLocations[i] != null) {
                    ourMinDistance = Math.min(ourMinDistance, mineTarget.distanceSquaredTo(ourInitialArchonLocations[i]));
                }

                if (initialEnemyArchLocs[i] != null) {
                    theirMinDistance = Math.min(theirMinDistance, mineTarget.distanceSquaredTo(ourInitialArchonLocations[i]));
                }
            }

            if (theirMinDistance < ourMinDistance) {
                return true;
            }
        }
        return false;
    }
}
