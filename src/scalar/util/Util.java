package scalar.util;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Util {
    // TODO: Remove the seed for competitions
    public static final Random rng = new Random(23636);
    static final int ACCEPTABLE_RUBBLE = 25; // don't greedy move to a square with more than this rubble
    private static Direction travelDir = null; // each robot has its own instance of util
    public static final Comparator<RobotInfo> ATTACK_PRIORITY_COMPARATOR = new attackPriorityComparator();

    // indices for shared array information about a given robot type
    // indices 6-9 are for archons' status + locations
    // 10-13 for enemy archons' status + location (distinguish b/t heuristic and actual)
    // rest of array is enemy locations
    public static int typeToArrIndex(RobotType type) {
        switch (type) {
            case MINER:             return 0;
            case SOLDIER:           return 1;
            case BUILDER:           return 2;
            case WATCHTOWER:        return 3;
            case SAGE:              return 4;
            case LABORATORY:        return 5;
            default: throw new RuntimeException("Invalid type " + type.toString());
        }
    }

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

    // Specifies priority order for attacking enemies
    static final List<RobotType> ATTACK_PRIORITY = Arrays.asList(
            RobotType.ARCHON,
            RobotType.WATCHTOWER,
            RobotType.SAGE,
            RobotType.BUILDER,
            RobotType.SOLDIER,
            RobotType.LABORATORY,
            RobotType.MINER
    );

    static class attackPriorityComparator implements Comparator<RobotInfo> {
        @Override
        public int compare(RobotInfo o1, RobotInfo o2) {
            return ATTACK_PRIORITY.indexOf(o1.getType()) - ATTACK_PRIORITY.indexOf(o2.getType());
        }
    }

//    public static void broadcastEnemyArchonLocs(RobotController rc, RobotInfo[] nearbyEnemies) throws GameActionException {
//        // if we find a nearby archon, report its location
//        // TODO: track enemy archon health?
//        for (RobotInfo enemy : nearbyEnemies) {
//            if (enemy.getType().equals(RobotType.ARCHON)) {
//                // 10-13 is enemy archon info
//                for (int commsIndex = 10; commsIndex++ <= 13;) {
//                    if (rc.readSharedArray(commsIndex) == 0) {
//                        MapLocation enemyLoc = enemy.getLocation();
//                        rc.writeSharedArray(commsIndex, enemyLoc.x * 100 + enemyLoc.y);
//                        break;
//                    }
//                }
//            }
//        }
//    }

    public static boolean safeMove(RobotController rc, Direction dir) throws GameActionException {
//        rc.setIndicatorString("Safely trying to move to " + dir.toString());
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    public static MapLocation getExploreLoc(RobotController rc) throws GameActionException {
        int mapWidth = rc.getMapWidth(), mapHeight = rc.getMapHeight();
        // lol dumb strat go brrr
        // generate a random location between 1 and 2 vision radii away from robot
        // 5 tries to get on the map, else just go to center of map
        for (int i = 0; i++ <= 5;) {
            int x = rng.nextInt(mapWidth);
            int y = rng.nextInt(mapHeight);
            if (Math.pow(x, 2) + Math.pow(y, 2) > rc.getType().visionRadiusSquared) {
                return new MapLocation(x, y);
            }
        }
        return null;
    }

    public static void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
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
