package four.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

import java.util.Comparator;
import java.util.Random;

public class Miscellaneous {
    public static final Random rng = new Random(3141592);

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
        switch (l1.directionTo(l2)){
            case NORTH:        return 0;
            case NORTHEAST:    return 1;
            case EAST:         return 2;
            case SOUTHEAST:    return 3;
            case SOUTH:        return 4;
            case SOUTHWEST:    return 5;
            case WEST:         return 6;
            case NORTHWEST:    return 7;
            default:           return -1;
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
}
