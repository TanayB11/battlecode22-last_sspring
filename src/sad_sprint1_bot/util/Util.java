package sad_sprint1_bot.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;

public class Util {
    public static final Random rng = new Random(6147);
    private static Direction travelDir = null;
    private static final int ACCEPTABLE_RUBBLE = 25;

    /** Array containing all the possible movement directions. */
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static boolean safeMove(RobotController rc, Direction dir) throws GameActionException {
        rc.setIndicatorString("Safely trying to move to " + dir.toString());
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    public static Direction initDir(RobotController rc) {
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

        // TODO: Make sure this works on both sides of the board
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

    static void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        if (!rc.isMovementReady() || currentLocation.equals(target)) { return; }

        Direction d = currentLocation.directionTo(target);
        if (rc.canMove(d) && !isObstacle(rc, d)) {
            // No obstacle in the way, so let's just go straight for it!
            rc.move(d);
            travelDir = null;
        } else {
            // There is an obstacle in the way, so we're gonna have to go around it.
            if (travelDir == null) {
                // If we don't know what we're trying to do
                // make something up
                // And, what better than to pick as the direction we want to go in
                // the best direction towards the goal?
                travelDir = d;
            }
            // Now, try to actually go around the obstacle
            // Repeat 8 times to try all 8 possible directions.
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(travelDir) && !isObstacle(rc, travelDir)) {
                    rc.move(travelDir);
                    travelDir = travelDir.rotateLeft();
                    break;
                } else {
                    travelDir = travelDir.rotateRight();
                }
            }
        }
    }

    private static boolean isObstacle(RobotController rc, Direction d) throws GameActionException {
        MapLocation adjacentLocation = rc.getLocation().add(d);
        int rubbleOnLocation = rc.senseRubble(adjacentLocation);
        return rubbleOnLocation > ACCEPTABLE_RUBBLE;
    }
}