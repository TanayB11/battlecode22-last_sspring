package phoenix;
import battlecode.common.*;
import scrimv2.util.Util;

import java.util.Random;

public class ArchonController {
    // TODO: check when miners die, if HP < 10% of base HP, spawn another miner
    static final int MINERS_PER_ARCHON = 12;
    static final int SOLDIERS_PER_ARCHON = 15;
    static int miners = 0, soldiers = 0, builders = 0;


    static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        // TODO: spawn an amount proportional to map area
        // TODO: make a better build order, spawn more soldiers/other once all our conditions are satisfied
        // TODO: choose a better spawn direction

        // sharedArray has 0 index miner, 1 index soldier, 2 index builder, 3 index watchtower, 4 index sage, 5 index lab
        // indices 6-9 for our archons' status + locations
        // 10-13 for enemy archons' status + locations (heuristic first, then real values | need a flag to distinguish)

        int archons = rc.getArchonCount();
        miners = rc.readSharedArray(0);
        soldiers = rc.readSharedArray(1);
        builders = rc.readSharedArray(2);

        // TODO: erase archon when it dies, can unroll loop for bytecode if needed
        // broadcast our encoded archon locations to the shared arr
        for (int i = 6; i++ <= 9;) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, me.x * 100 + me.y);
                break;
            }
        }

        // spawn bots
        Direction dirToSpawn = Util.directions[Util.rng.nextInt(Util.directions.length)];
        if (miners < MINERS_PER_ARCHON * archons) {
            if (rc.canBuildRobot(RobotType.MINER, dirToSpawn)) {
                rc.buildRobot(RobotType.MINER, dirToSpawn);
                miners++;
                rc.writeSharedArray(0, miners);
            }
        } else if (soldiers < SOLDIERS_PER_ARCHON * archons){
            if (rc.canBuildRobot(RobotType.SOLDIER, dirToSpawn)) {
                rc.buildRobot(RobotType.SOLDIER, dirToSpawn);
                soldiers++;
                rc.writeSharedArray(1, soldiers);
            }
        }

        // TODO: once we know where one archon is, calculate enemy archons by map symmetry, write to shared array
        // differentiate between a guess and actual found values with a flag
        // Case 1: reflectional symmetry (x, y)
        // X-axis symmetry

        // Case 2: rotational symmetry
    }

    // TODO: if we see an enemy archon from our archon, KILL IT (target all soldiers immediately)
}