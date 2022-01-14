package scalar;
import battlecode.common.*;
import scalar.util.Util;

import static scalar.Communication.*;

// TODO: refactor all current comms code to account for changes in comms
public class ArchonController {
    static final int MINERS_PER_ARCHON = 12;
    static final int SOLDIERS_PER_ARCHON = 15;
    static int INITIAL_ARCHON_COUNT = -1;
    static int miners = 0, soldiers = 0, builders = 0;

    // this archon's index in shared array
    static int thisArchonIndex = -1;

    static void runArchon(RobotController rc) throws GameActionException {
        if (thisArchonIndex == -1) {
            INITIAL_ARCHON_COUNT = rc.getArchonCount();
            thisArchonIndex = earliestEmptyArchonIndex(rc, INITIAL_ARCHON_COUNT);
            writeArchonLocation(rc, thisArchonIndex);
        }

        if (rc.getHealth() <= 60) {
            throwArchonHealthFlag(rc, thisArchonIndex);
        }

        int archons = rc.getArchonCount();
        miners = readNumMiners(rc);
        soldiers = readNumSoldiers(rc);
        builders = readNumBuilders(rc);

        // spawn bots
//        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//        Util.broadcastEnemyArchonLocs(rc, nearbyEnemies);

        // TODO address case when enemy archon is in vision
//        // if enemy archon is near us, soldier rush
//        if (nearbyEnemies.length > 0) {
//            MapLocation nearbyArchonLoc = null;
//            int enemyArchonCode = 0, enemyArchonX = 0, enemyArchonY = 0;
//            // TODO: read array locs, check if enemy archon is in vision
//            for (int i = 10; i++ <= 13;) {
//                enemyArchonCode = rc.readSharedArray(i);
//                if (enemyArchonCode != 0) {
//                    enemyArchonX = enemyArchonCode / 100;
//                    enemyArchonY = enemyArchonCode % 100;
//                    nearbyArchonLoc = new MapLocation(enemyArchonX, enemyArchonY);
//                    // TODO: target soldier rush
//                }
//            }
//        }

        Direction dirToSpawn = Util.directions[Util.rng.nextInt(Util.directions.length)];
        if (miners < MINERS_PER_ARCHON * archons) {
            if (rc.canBuildRobot(RobotType.MINER, dirToSpawn)) {
                rc.buildRobot(RobotType.MINER, dirToSpawn);
                miners++;
                writeNumMiners(rc, miners);
            }
        } else if (soldiers < SOLDIERS_PER_ARCHON * archons){
            if (rc.canBuildRobot(RobotType.SOLDIER, dirToSpawn)) {
                rc.buildRobot(RobotType.SOLDIER, dirToSpawn);
                soldiers++;
                writeNumSoldiers(rc, soldiers);
            }
        } else {
            // if we've met the quota, just spawn randomly (temp strat)
            RobotType randomType = (Util.rng.nextInt() == 1) ? RobotType.MINER : RobotType.SOLDIER;
            if (rc.canBuildRobot(randomType, dirToSpawn)) {
                rc.buildRobot(randomType, dirToSpawn);
                if (randomType.equals(RobotType.MINER)) {
                    miners++;
                    writeNumMiners(rc, miners);
                } else {
                    soldiers++;
                    writeNumSoldiers(rc, soldiers);
                }
            }
        }
    }
}