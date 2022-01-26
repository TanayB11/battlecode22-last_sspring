package baymax_seven_old.util;

import battlecode.common.*;
import baymax_seven_old.util.data_structures.RingBufferQueue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static baymax_seven_old.util.Communication.readArchLoc;
import static baymax_seven_old.util.SafeActions.safeMove;

public class Miscellaneous {
    public static final Random rng = new Random();

    // used for computing average income
    static final int ROLLING_AVG_LENGTH = 10;
    public static RingBufferQueue incomeAvgQ = new RingBufferQueue(ROLLING_AVG_LENGTH);
    public static RingBufferQueue enemyIncomeAvgQ = new RingBufferQueue(ROLLING_AVG_LENGTH);

    // used for building movement
    static final int ACCEPTABLE_BUILDING_RUBBLE = 25;

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

    public static int dirToIndex(MapLocation l1, MapLocation l2) {
        switch (l1.directionTo(l2)) {
            case NORTH:        return 0;
            case NORTHEAST:    return 1;
            case EAST:         return 2;
            case SOUTHEAST:    return 3;
            case SOUTH:        return 4;
            case SOUTHWEST:    return 5;
            case WEST:         return 6;
            case NORTHWEST:    return 7;
            default:           return rng.nextInt(8);
        }
    }

    public static boolean isAttackingUnit(RobotType type) {
        return type.equals(RobotType.SAGE) || type.equals(RobotType.SOLDIER) || type.equals(RobotType.WATCHTOWER);
    }

    public static int getNonAttackingUnitPriority(RobotType type) {
        switch (type) {
            case MINER:         return 3;
            case BUILDER:       return 2;
            case ARCHON:        return 1;
            case LABORATORY:    return 0;
            default:            return -1;
        }
    }

    public static boolean compareDamageHPRatio(RobotInfo r1, RobotInfo r2) {
        return (double) r1.getType().getDamage(r1.getLevel()) / r1.getHealth() >
                (double) r2.getType().getDamage(r2.getLevel()) / r2.getHealth();
    }

    public static void retreatFrom(RobotController rc, MapLocation enemyLoc) throws GameActionException {
        // Retreat:
        // Of 8 possible movement directions, eliminate 3
        // Favor the 3 away from enemy, if those are not beyond rubble threshold
        // Otherwise look at the other two "neutral"/sideways directions, pick min rubble of all 5
        final int MIN_ACCEPTABLE_RUBBLE = 25;

        Direction dirToTarget = rc.getLocation().directionTo(enemyLoc);
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
        MapLocation adjLoc = rc.adjacentLocation(initPossibleDirs[bestIndex]);
        int lowestRubble = rc.onTheMap(adjLoc) ? rc.senseRubble(adjLoc) : Integer.MAX_VALUE;
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

        safeMove(rc, dirOfRetreat);
    }

    public static MapLocation getNearestFriendlyArch(RobotController rc) throws GameActionException {
        MapLocation currLoc;
        MapLocation nearestArch = null;
        int currDist;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            currLoc = readArchLoc(rc, i);
            if (currLoc != null){
                currDist = rc.getLocation().distanceSquaredTo(currLoc);
                if (currDist < bestDist) {
                    bestDist = currDist;
                    nearestArch = currLoc;
                }
            }
        }
        return nearestArch;
    }

    public static MapLocation getBestBuildingLoc(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        MapLocation bestBuildingLoc = null;

        if (rc.senseRubble(me) > ACCEPTABLE_BUILDING_RUBBLE) {
            MapLocation[] visibleLocs = rc.getAllLocationsWithinRadiusSquared(me, rc.getType().visionRadiusSquared);

            // 1st prioritize rubble, then prioritize distance
            Arrays.sort(visibleLocs, Comparator.comparingInt(loc -> {
                try {
                    return rc.senseRubble(loc);
                } catch (Exception e) {
                    return Integer.MAX_VALUE;
                }
            }));

            int bestDist = Integer.MAX_VALUE;
            for (int i = 0; i < visibleLocs.length; i++) {
                if (
                    rc.senseRubble(visibleLocs[i]) < ACCEPTABLE_BUILDING_RUBBLE &&
                    me.distanceSquaredTo(visibleLocs[i]) < bestDist
                ) {
                    bestBuildingLoc = visibleLocs[i];
                    bestDist = me.distanceSquaredTo(visibleLocs[i]);
                }
            }
        }

        return bestBuildingLoc;
    }
}
