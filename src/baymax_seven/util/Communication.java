package baymax_seven.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Communication {
    // our archon locations are 0-3
    public final static int ARCHON_COUNT_INDEX = 4;
    public final static int MINER_COUNT_INDEX = 5;
    public final static int SOLDIER_COUNT_INDEX = 6;
    public final static int LAB_COUNT_INDEX = 7;
    public final static int SAGE_COUNT_INDEX = 8;
    // 7-10 are for the other units
    public final static int FLAG_INDEX = 11;
    public final static int TARGET_ENEMY_INDEX = 12;

    /*
    Section 1: Archons
    */

    public static void archonReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(ARCHON_COUNT_INDEX, rc.readSharedArray(ARCHON_COUNT_INDEX) + 1);
    }

    public static int readArchonCount(RobotController rc) throws GameActionException {
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

    // Build queue
//    public static void enqueueBuild(RobotController rc, int index) throws GameActionException {
//        // 44444
//        // 001 -> miner, 010 -> soldier, 011 -> builder, 100 -> sage
//        // 0001 | 000 000 000 111
//        //                    111
//        // 0001 | 000 000 000 111
//        // 0001 | 000 000 000 111
//        // (bq & ~(0 << 2)) << (3 * counter)
//    }

    /*
    Section 2: Miners
    */

    public static void minerReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(MINER_COUNT_INDEX, rc.readSharedArray(MINER_COUNT_INDEX) + 1);
    }

    public static int readNumMiners(RobotController rc) throws GameActionException {
        return rc.readSharedArray(MINER_COUNT_INDEX);
    }

    /*
    Section 3: Soldiers
    */

    public static void soldierReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(SOLDIER_COUNT_INDEX, rc.readSharedArray(SOLDIER_COUNT_INDEX) + 1);
    }

    public static int readNumSoldiers(RobotController rc) throws GameActionException {
        return rc.readSharedArray(SOLDIER_COUNT_INDEX);
    }

    /*
    Section 4: Labs
    */

    public static void labReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(LAB_COUNT_INDEX, rc.readSharedArray(LAB_COUNT_INDEX) + 1);
    }

    public static int readNumLabs(RobotController rc) throws GameActionException {
        return rc.readSharedArray(LAB_COUNT_INDEX);
    }

    /*
    Section 5: Sages
    */

    public static void sageReport(RobotController rc) throws GameActionException {
        rc.writeSharedArray(SAGE_COUNT_INDEX, rc.readSharedArray(SAGE_COUNT_INDEX) + 1);
    }

    public static int readNumSages(RobotController rc) throws GameActionException {
        return rc.readSharedArray(SAGE_COUNT_INDEX);
    }

    /*
    Section 6: Enemy reporting
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

    /*
    Section 7: Flag throwing, utilities
    Notify other troops that something has happened
        0th bit is archon: priority spawner
        1st bit is labs: ok/not ok to transmute
        2nd bit is "not rotational"
        3rd bit is "not vertical"
        4th bit is "not horizontal"
    */

    public static void throwFlag(RobotController rc, int bitShift) throws GameActionException {
        rc.writeSharedArray(FLAG_INDEX, rc.readSharedArray(FLAG_INDEX) | (1 << bitShift));
    }

    public static void resetFlag(RobotController rc, int bitShift) throws GameActionException {
        rc.writeSharedArray(FLAG_INDEX, rc.readSharedArray(FLAG_INDEX) & ~(1 << bitShift));
    }

    public static boolean checkFlag(RobotController rc, int bitShift) throws GameActionException {
        // Return true if flag has been thrown, false otherwise
        return (1 == (rc.readSharedArray(FLAG_INDEX) & (0 ^ (1 << bitShift))) >> bitShift);
    }

    public static void clearIndex(RobotController rc, int index) throws GameActionException {
        rc.writeSharedArray(index, 0);
    }
}