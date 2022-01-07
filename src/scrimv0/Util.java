package scrimv0;
import battlecode.common.*;
import scala.util.Random;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Util {
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static boolean safeMove(RobotController rc, Direction dir) throws GameActionException {
        rc.setIndicatorString("Safely trying to move to " + dir.toString());
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    // TODO: return a list in impassibility order? might stop oscillation
    static Direction greedyNextMove(RobotController rc, MapLocation goal) throws GameActionException {
        MapLocation me = rc.getLocation();
        double lowestImpassibility = Double.MAX_VALUE;
        Direction bestDir = null;

        for (Direction dir : directions) {
            if (dir.equals(me.directionTo(goal).opposite())) { continue; }

            MapLocation nextLoc = me.add(dir);

            double rubble = rc.senseRubble(nextLoc);
            double cooldown = Math.floor(rc.getType().movementCooldown * (1 + rubble / 10));
            double distToGoal = nextLoc.distanceSquaredTo(goal);
            double impassability = cooldown + distToGoal;

            if (impassability < lowestImpassibility) {
                lowestImpassibility = impassability;
                bestDir = dir;
            }
        }

        return bestDir;
    }
}