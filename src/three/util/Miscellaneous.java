package three.util;

import battlecode.common.Direction;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Miscellaneous {
    public static final Random rng = new Random(314159);

    /** Array containing all the possible movement directions. */
    public static final Comparator<RobotInfo> ATTACK_PRIORITY_COMPARATOR = new attackPriorityComparator();

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
