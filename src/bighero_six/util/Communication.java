package bighero_six.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Communication {
    final static int ARCHON_COUNT_INDEX = 4;
    final static int MINER_COUNT_INDEX = 5;
    final static int SOLDIER_COUNT_INDEX = 6;
    final static int BUILD_ORDER_INDEX = 11;
    final static int TARGET_ENEMY_INDEX = 12;

    /*
    Section 1: Archons
    */

    public static void archonReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(ARCHON_COUNT_INDEX, rc.readSharedArray(ARCHON_COUNT_INDEX) + 1);
    }

    public static int currArchonCount(RobotController rc) throws GameActionException {
        return rc.readSharedArray(ARCHON_COUNT_INDEX);
    }

    public static MapLocation readArchLoc(RobotController rc, int index) throws GameActionException {
        int archonComm = rc.readSharedArray(index);
        if (archonComm != 0) {
            int archonX = (archonComm & 64512) >> 10;
            int archonY = (archonComm & 1008) >> 4;
            return new MapLocation(archonX, archonY);
        }
        return null;
    }

    public static int firstArchIndexEmpty(RobotController rc) throws GameActionException {
        if (readArchLoc(rc, 0) == null) {
            return 0;
        } else if (readArchLoc(rc, 1) == null) {
            return 1;
        } else if (readArchLoc(rc, 2) == null) {
            return 2;
        } else if (readArchLoc(rc, 3) == null) {
            return 3;
        }
        return -1;
    }

    public static void writeOwnArchLoc(RobotController rc, int index) throws GameActionException {
        MapLocation me = rc.getLocation();
        rc.writeSharedArray(index, (me.x << 10) | (me.y << 4));
    }

    /*
    Section 2: Miners
    */

    public static void minerReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(MINER_COUNT_INDEX, rc.readSharedArray(MINER_COUNT_INDEX) + 1);
    }

    public static int currMinerCount(RobotController rc) throws GameActionException {
        return rc.readSharedArray(MINER_COUNT_INDEX);
    }

    /*
    Section 3: Soldiers
    */

    public static void soldierReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(SOLDIER_COUNT_INDEX, rc.readSharedArray(SOLDIER_COUNT_INDEX) + 1);
    }

    public static int currSoldierCount(RobotController rc) throws GameActionException {
        return rc.readSharedArray(SOLDIER_COUNT_INDEX);
    }

    /*
    Section 4: Enemy reporting
    */

    public static void writeEnemy(RobotController rc, RobotType type, MapLocation locationSpotted) throws GameActionException {
        int locCode = (locationSpotted.x << 10) | (locationSpotted.y << 4);
        int priority = getReportEnemyPriority(type);
        rc.writeSharedArray(TARGET_ENEMY_INDEX, (locCode & ~14) | (priority << 1));
    }

    public static int getReportEnemyPriority(RobotType type) {
        switch (type) {
            case MINER:         return 0;
            case SOLDIER:       return 1;
            case BUILDER:       return 2;
            case SAGE:          return 3;
            case WATCHTOWER:    return 4;
            case LABORATORY:    return 5;
            case ARCHON:        return 6;
            default:            return -1;
        }
    }

    public static MapLocation getGoal(RobotController rc) throws GameActionException {
        int enemyComm = rc.readSharedArray(TARGET_ENEMY_INDEX);
        if (enemyComm != 0) {
            int targetX = (enemyComm & 64512) >> 10;
            int targetY = (enemyComm & 1008) >> 4;
            return new MapLocation(targetX, targetY);
        }
        return null;
    }

    public static void reportEnemy(RobotController rc, RobotType type, MapLocation locationSpotted) throws GameActionException {
        int currEnemyPriority = (rc.readSharedArray(TARGET_ENEMY_INDEX)) & 14 >> 1;
        if (currEnemyPriority == 0 || currEnemyPriority < getReportEnemyPriority(type)) {
            writeEnemy(rc, type, locationSpotted);
        }
    }
}
