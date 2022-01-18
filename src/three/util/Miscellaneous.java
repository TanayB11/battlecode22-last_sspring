package three.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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

    // Specifies priority order for attacking enemies
    static final List<RobotType> ATTACK_PRIORITY = Arrays.asList(
            RobotType.SAGE,
            RobotType.WATCHTOWER,
            RobotType.SOLDIER,
            RobotType.ARCHON,
            RobotType.BUILDER,
            RobotType.LABORATORY,
            RobotType.MINER
    );

    static class attackPriorityComparator implements Comparator<RobotInfo> {
        @Override
        public int compare(RobotInfo o1, RobotInfo o2) {
            return ATTACK_PRIORITY.indexOf(o1.getType()) - ATTACK_PRIORITY.indexOf(o2.getType());
        }
    }
}
