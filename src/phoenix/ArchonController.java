package phoenix;
import battlecode.common.*;
import scrimv2.util.Util;

import java.util.Random;

public class ArchonController {
    // TODO: check when miners die, if HP < 10% of base HP, spawn another miner
    static int miners = 0;

    static void runArchon(RobotController rc) throws GameActionException {
        // TODO: spawn proportional to map area
        if (miners < 20) {
            Direction dirToSpawn = Util.directions[Util.rng.nextInt(Util.directions.length)];
            if (rc.canBuildRobot(RobotType.MINER, dirToSpawn)) {
                rc.buildRobot(RobotType.MINER, dirToSpawn);
                miners++;
            }
        }
    }

    // TODO: if we see an enemy archon from our archon, KILL IT
}