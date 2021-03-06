package one_phoenix;
import battlecode.common.*;
import one_phoenix.util.Util;

public class ArchonController {
    static final int MINERS_PER_ARCHON = 12;
    static final int SOLDIERS_PER_ARCHON = 15;
    static int miners = 0, soldiers = 0, builders = 0;


    static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

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
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        Util.broadcastEnemyArchonLocs(rc, nearbyEnemies);

        // if enemy archon is near us, soldier rush
        if (nearbyEnemies.length > 0) {
            MapLocation nearbyArchonLoc = null;
            int enemyArchonCode = 0, enemyArchonX = 0, enemyArchonY = 0;
            // TODO: read array locs, check if enemy archon is in vision
            for (int i = 10; i++ <= 13;) {
                enemyArchonCode = rc.readSharedArray(i);
                if (enemyArchonCode != 0) {
                    enemyArchonX = enemyArchonCode / 100;
                    enemyArchonY = enemyArchonCode % 100;
                    nearbyArchonLoc = new MapLocation(enemyArchonX, enemyArchonY);
                    // TODO: target soldier rush
                }
            }
        }

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
        } else {
            // if we've met the quota, just spawn randomly (temp strat)
            RobotType randomType = (Util.rng.nextInt() == 1) ? RobotType.MINER : RobotType.SOLDIER;
            if (rc.canBuildRobot(randomType, dirToSpawn)) {
                rc.buildRobot(randomType, dirToSpawn);
                if (randomType.equals(RobotType.MINER)) {
                    miners++;
                    rc.writeSharedArray(1, miners);
                } else {
                    soldiers++;
                    rc.writeSharedArray(1, soldiers);
                }
            }
        }
    }
}