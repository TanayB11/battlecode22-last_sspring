package voldemort_eight;

import battlecode.common.*;
import voldemort_eight.util.pathfinding.BFS;
import voldemort_eight.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static voldemort_eight.util.Communication.*;
import static voldemort_eight.util.Miscellaneous.directions;

public class BuilderController {
    static BFS bfs = null;
    static MapLocation closestWall = null;
    static MapLocation bestBuildLoc = null;
    static MapLocation bestCampLoc = null;
    static MapLocation[] babyLocs = { null, null, null, null, null, null, null, null };
    static boolean canMove = true;
    static boolean isHealing = false;

    static final int RUBBLE_THRESHOLD_TO_BUILD_LAB = 25;

    static void runBuilder(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        // initialize
        builderReport(rc);
        if (bfs == null) {
            bfs = new DroidBFS(rc);
            closestWall = getClosestWall(rc, me);
        }

        /*
        Macro strategy
        */

        //report nearby enemies to shared array (from v5)
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if (nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, Comparator.comparingInt(a -> me.distanceSquaredTo(a.getLocation())));
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        /*
        Builder micro strategy
        */

        // Outline
        // Pick the archons closest to any wall to spawn builders
        // BFS to the closest wall
        // When we can see the wall, find the lowest rubble square closest to the wall to spawn a lab at
        // Make sure that we track the lab's ID so we can track it even when it moves

        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
        // try to heal all nearby allies first, start with the highest health
        for (int i = 0; i < babyLocs.length; i++) {
            if (babyLocs[i] != null) {
                if (me.isAdjacentTo(babyLocs[i]) && rc.canSenseRobotAtLocation(babyLocs[i])) {
                    if (rc.canRepair(babyLocs[i])) {
                        rc.repair(babyLocs[i]);
                        isHealing = true;
                        canMove = false;
                    }
                }
                if (rc.canSenseRobotAtLocation(babyLocs[i]) && rc.senseRobotAtLocation(babyLocs[i]).getHealth() == rc.senseRobotAtLocation(babyLocs[i]).getType().getMaxHealth(rc.senseRobotAtLocation(babyLocs[i]).getLevel())) {
                    babyLocs[i] = null;
                    canMove = true;
                    isHealing = false;
                }
            }
        }

//        if (nearbyAllies.length > 0) {
//            Arrays.sort(nearbyAllies, Comparator.comparingInt(a -> a.getHealth()));
//            for (int i = nearbyAllies.length - 1; i > 0; i--) {
//                if (nearbyAllies[i].getType().equals(RobotType.LABORATORY) || nearbyAllies[i].getType().equals(RobotType.ARCHON)) {
//                    if (rc.canRepair(nearbyAllies[i].getLocation())) {
//                        if (nearbyAllies[i].getHealth() < nearbyAllies[i].getType().getMaxHealth(rc.getLevel())) {
//                            rc.repair(nearbyAllies[i].getLocation());
//                        }
//                    }
//                }
//            }
//        }

        if (!isHealing) {
            // determine a low rubble location at which to build a lab
            if (bestBuildLoc == null && me.distanceSquaredTo(closestWall) < 4) {
                MapLocation[] buildLocs = rc.getAllLocationsWithinRadiusSquared(me, RobotType.BUILDER.visionRadiusSquared);
                Arrays.sort(buildLocs, Comparator.comparingInt(loc -> {
                    try {
                        return loc.distanceSquaredTo(closestWall);
                    } catch (Exception e) {
                        return Integer.MAX_VALUE;
                    }
                }));

                int bestRubble = Integer.MAX_VALUE;
                for (int i = 0; i < buildLocs.length; i++) {
                    if (!rc.isLocationOccupied(buildLocs[i]) && rc.senseRubble(buildLocs[i]) < bestRubble) {
                        bestBuildLoc = buildLocs[i];
                        bestRubble = rc.senseRubble(buildLocs[i]);
                        if (rc.senseRubble(buildLocs[i]) < RUBBLE_THRESHOLD_TO_BUILD_LAB) {
                            bestBuildLoc = buildLocs[i];
                            break;
                        }
                    }
                }
            }
        }

        // if we're adjacent to the goal, build a lab and repair it
        if (bestBuildLoc != null && me.isAdjacentTo(bestBuildLoc)) {
            // build on the min rubble location
            Direction buildDir = me.directionTo(bestBuildLoc);
            rc.setIndicatorString(bestBuildLoc.toString());
            if (rc.canBuildRobot(RobotType.LABORATORY, buildDir)) {
                rc.buildRobot(RobotType.LABORATORY, buildDir);
                canMove = false;
                labReport(rc);
                for (int i = 0; i < babyLocs.length; i++) {
                    if (babyLocs[i] == null) {
                        babyLocs[i] = bestBuildLoc;
                    }
                }
                bestBuildLoc = null;
            }
        } else {
            if (canMove) {
                Direction bestDir = directions[0];
                int minRubble = Integer.MAX_VALUE;
                for (int i = 0; i < directions.length; i++) {
                    if (bestBuildLoc != null && rc.canSenseLocation(bestBuildLoc) && rc.canSenseLocation(bestBuildLoc.add(directions[i])) && rc.senseRubble(bestBuildLoc.add(directions[i])) < minRubble) {
                        bestDir = directions[i];
                        minRubble = rc.senseRubble(bestBuildLoc.add(directions[i]));
                    }
                }

                bfs.move((bestBuildLoc != null && !me.isAdjacentTo(bestBuildLoc)) ? bestBuildLoc.add(bestDir) : closestWall);
            }
        }

        if (bestBuildLoc != null) {
            rc.setIndicatorString(bestBuildLoc.toString());
        } else {
            rc.setIndicatorString(" WALL " + closestWall.toString());
        }
    }

    private static MapLocation getClosestWall(RobotController rc, MapLocation me) {
        // find the closest wall
        int distToNorthWall = rc.getMapHeight() - me.y;
        int distToSouthWall = me.y;
        int distToEastWall = rc.getMapWidth() - me.x;
        int distToWestWall = me.x;

        if (distToNorthWall < distToSouthWall) {
            if (distToEastWall <= distToNorthWall) {
                // go to east wall
                rc.setIndicatorString("going to east wall :)");
                return new MapLocation(rc.getMapWidth() - 1, me.y);
            } else if (distToWestWall <= distToNorthWall) {
                // go to west wall
                rc.setIndicatorString("going to west wall :)");
                return new MapLocation(0, me.y);
            } else {
                rc.setIndicatorString("going to north wall :)");
                return new MapLocation(me.x, rc.getMapHeight() - 1);
            }
        } else {
            if (distToEastWall < distToSouthWall) {
                // go to east wall
                rc.setIndicatorString("going to east wall :)");
                return new MapLocation(rc.getMapWidth() - 1, me.y);
            } else if (distToWestWall < distToSouthWall) {
                // go to west wall
                rc.setIndicatorString("going to west wall :)");
                return new MapLocation(0, me.y);
            } else {
                rc.setIndicatorString("going to south wall :)");
                return new MapLocation(me.x, 0);
            }
        }
    }
}