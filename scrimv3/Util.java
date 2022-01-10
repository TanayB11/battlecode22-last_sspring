package scrimv3;
import battlecode.common.*;

import java.util.Random;

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
        //rc.setIndicatorString("Safely trying to move to " + dir.toString());
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    static Direction initDir(RobotController rc) {
        MapLocation me = rc.getLocation();
        // distances to different walls (measuring map dimensions to prepare for exploration)
        int mapWidth = rc.getMapWidth(), mapHeight = rc.getMapHeight();
        int distToWestWall = me.distanceSquaredTo(new MapLocation(0, me.y));
        int distToEastWall = me.distanceSquaredTo(new MapLocation(mapWidth, me.y));
        int distToNorthWall = me.distanceSquaredTo(new MapLocation(me.x, mapHeight));
        int distToSouthWall = me.distanceSquaredTo(new MapLocation(me.x,0));


        // if the archon is in the middle X percent, send straight (north/east)
        final double MID_PROP = .4;
        boolean sendingStraight = false;

        Direction defaultSpawn = null;
        if (me.x > (((1-MID_PROP)/2) * mapWidth) && me.x < (((1-MID_PROP)/2 + MID_PROP) * mapWidth)) {
            defaultSpawn = (distToSouthWall < distToNorthWall) ? Direction.NORTH : Direction.SOUTH;
            sendingStraight = true;
//            rc.setIndicatorString("Sending straight " + defaultSpawn.toString());
        } else if (distToEastWall < distToWestWall) {
            defaultSpawn = Direction.WEST;
        } else {
            defaultSpawn = Direction.EAST;
        }

        // edge case: in middle MID_PROP percent both horiz and vert

        if (me.y > (((1-MID_PROP)/2) * mapHeight) && me.y < (((1-MID_PROP)/2 + MID_PROP) * mapHeight)) {
            defaultSpawn = (distToEastWall < distToWestWall) ? Direction.WEST : Direction.EAST;
//            rc.setIndicatorString("Sending straight " + defaultSpawn.toString());
        } else if (!sendingStraight && distToSouthWall < distToNorthWall && defaultSpawn.equals(Direction.EAST)) {
            defaultSpawn = defaultSpawn.rotateLeft();
        } else if (!sendingStraight && distToSouthWall < distToNorthWall) {
            defaultSpawn = defaultSpawn.rotateRight();
        } else if (!sendingStraight && defaultSpawn.equals(Direction.EAST)) {
            defaultSpawn = defaultSpawn.rotateRight();
        } else if (!sendingStraight) {
            defaultSpawn = defaultSpawn.rotateLeft();
        }

        return defaultSpawn;
    }

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