package baymax_seven;

import battlecode.common.*;
import baymax_seven.util.pathfinding.BFS;
import baymax_seven.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static baymax_seven.MinerController.isAttackingEnemy;
import static baymax_seven.util.Communication.builderReport;
import static baymax_seven.util.Communication.reportEnemy;
import static baymax_seven.util.Miscellaneous.directions;
import static baymax_seven.util.Miscellaneous.rng;

public class BuilderController {
    static BFS bfs = null;
    static MapLocation babyLocation = null;
    static MapLocation closestWall = null;

    static void runBuilder(RobotController rc) throws GameActionException {
        // i am here, mates :)
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

        /*
        Builder micro strategy
        */
        if (!me.equals(closestWall)) {
            rc.setIndicatorString("moving to closest wall :)");
            bfs.move(closestWall);
        } else {
            Direction buildDir = directions[rng.nextInt(directions.length)];
            if (rc.canBuildRobot(RobotType.LABORATORY, buildDir)) {
                rc.setIndicatorString("building a lab :)");
                rc.buildRobot(RobotType.LABORATORY, buildDir);
                babyLocation = rc.adjacentLocation(buildDir);
            }
        }

        if (babyLocation != null && rc.canSenseRobotAtLocation(babyLocation) && rc.senseRobotAtLocation(babyLocation).getHealth() < RobotType.LABORATORY.health) {
            if (rc.canRepair(babyLocation)) {
                rc.repair(babyLocation);
            }
        }

        // find the nearest wall
        // TODO: make sure this runs only once

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