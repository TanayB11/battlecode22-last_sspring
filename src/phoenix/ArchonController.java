package phoenix;
import battlecode.common.*;
import scrimv2.util.Util;

import java.util.Random;

public class ArchonController {
    // TODO: check when miners die, if HP < 10% of base HP, spawn another miner
    static int miners = 0;

    static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        // TODO: report our encoded archon locations to the shared arr
        // sharedarray index 0 is our archon status list (check to not overwrite index used in minercontroller)
        // ex. x1y1 x2y2 x3y3 x4y4
        // -> y3 + x3*100 + y2*1000 + x2 * 10000 + y1 * 100000 + x1 * 1000000 + y0 * 10000000 + x0 * 100000000
        // problem: this takes too many bits
        // use base 16? base 2? compress/multiple array locs

        // TODO: spawn an amount proportional to map area
        if (miners < 20) {
            Direction dirToSpawn = Util.directions[Util.rng.nextInt(Util.directions.length)];
            if (rc.canBuildRobot(RobotType.MINER, dirToSpawn)) {
                rc.buildRobot(RobotType.MINER, dirToSpawn);
                miners++;
            }
        }

        // TODO: once we know where one archon is, calculate enemy archons by map symmetry, write to shared array
        // Case 1: reflectional symmetry
        // Case 2: rotational symmetry
    }

    // TODO: if we see an enemy archon from our archon, KILL IT
}