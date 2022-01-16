package drake.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Communication {
    /*
    Functions:

    Number of droids / buildings:
    1. Read number of [bot type] - readNum[bot type]
    2. Write to number of [bot type] - writeNum[bot type]

    Archon Positioning:
    1. Earliest available archon index - firstArchIndexEmpty
    2. Archon writes own location - writeOwnArchLoc
    3. Reading own Archon positions - readArchLoc

    Helper functions:
    1. Change a particular bit to a 1 - throwFlag
    2. Check if a particular bit is 1 - checkFlag

    Targeting helpers:
    1. Enemy report - reportEnemy
    2. Return highest priority enemy - getEnemy
    2. Writes enemy - writeEnemy
     */

    // Section 1: Number of droids / buildings
    // dedicated indices: 4-6

    // Section 1a: Reading number of units
    public static int readNumMiners(RobotController rc) throws GameActionException {
        // 65280 comes from (255 << 8), and 255 comes from 11111111
        // xxxxxxxxyyyyyyyy
        // 1111111100000000 = 65280_10
        // xxxxxxxx00000000
        // xxxxxxxx
        return (rc.readSharedArray(4) & 65280) >> 8;
    }

    static int readNumBuilders(RobotController rc) throws GameActionException {
        return (rc.readSharedArray(5) & 65280) >> 8;
    }

    static int readNumSages(RobotController rc) throws GameActionException {
        return (rc.readSharedArray(6) & 65280) >> 8;
    }

    public static int readNumSoldiers(RobotController rc) throws GameActionException {
//        System.out.println("TESTING READ: " + Integer.toString(rc.readSharedArray(4) & ~(255 << 8)));
        // 00000000yyyyyyyy
        // 0000000011111111
        // 255 >> 8
        return rc.readSharedArray(4) & ~(255 << 8);
    }

    static int readNumWatchtowers(RobotController rc) throws GameActionException {
        return rc.readSharedArray(5) & ~(255 << 8);
    }

    static int readNumLabs(RobotController rc) throws GameActionException {
        return rc.readSharedArray(6) & ~(255 << 8);
    }

    // Section 1b: Writing to number of units
    public static void writeNumMiners(RobotController rc, int numUnits)  throws GameActionException {
        // xxxxxxxxyyyyyyyy
        // 0000000011111111
        // 00000000yyyyyyyy
        // zzzzzzzz00000000
        // zzzzzzzzyyyyyyyy
        rc.writeSharedArray(4, (rc.readSharedArray(4) & ~65280) | (numUnits << 8));
    }

    static void writeNumBuilders(RobotController rc, int numUnits)  throws GameActionException {
        rc.writeSharedArray(5, (rc.readSharedArray(5) & ~65280) | (numUnits << 8));
    }

    static void writeNumSages(RobotController rc, int numUnits)  throws GameActionException {
        rc.writeSharedArray(6, (rc.readSharedArray(6) & ~65280) | (numUnits << 8));
    }

    public static void writeNumSoldiers(RobotController rc, int numUnits) throws GameActionException {
        rc.writeSharedArray(4, ((rc.readSharedArray(4) & (255 << 8)) | numUnits));
    }

    static void writeNumWatchtowers(RobotController rc, int numUnits) throws GameActionException {
        rc.writeSharedArray(5, (rc.readSharedArray(5) & (255 << 8)) | numUnits);
    }

    static void writeNumLabs(RobotController rc, int numUnits) throws GameActionException {
        rc.writeSharedArray(6, (rc.readSharedArray(6) & (255 << 8)) | numUnits);
    }

    // Section 2: Archon positioning
    // dedicated indices: 0-3

    // Section 2a: Reading / Setting Locations
    static MapLocation readArchLoc(RobotController rc, int index) throws GameActionException {
        // return null if no archon found
        int archonComm = rc.readSharedArray(index);

        if (archonComm != 0) {
            // xxxxxxyyyyyyzzzz
            // 1111110000000000 = 64512
            // 0000001111110000 = 1008
            // xxxxxxyyyyyyzzzz

            int archonX = (archonComm & 64512) >> 10;
            int archonY = (archonComm & 1008) >> 4;

            MapLocation archonLocation = new MapLocation(archonX, archonY);

            return archonLocation;
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
        } else {
            return -1;
        }
    }

    public static void writeOwnArchLoc(RobotController rc, int index) throws GameActionException {
        MapLocation me = rc.getLocation();

        rc.writeSharedArray(index, (me.x << 10) | (me.y << 4));
    }

    // Section 3: Helper functions
    // no dedicated indices, helps other parts of bot understand / tweak array
    public static void throwFlag(RobotController rc, int index, int bitShift) throws GameActionException {
        rc.writeSharedArray(index, rc.readSharedArray(index) | (1 << bitShift));
    }

    public static void resetFlag(RobotController rc, int index, int bitShift) throws GameActionException {
        rc.writeSharedArray(index, rc.readSharedArray(index) & ~(1 << bitShift));
    }

    public static boolean checkFlag(RobotController rc, int index, int bitShift) throws GameActionException {
        // Return true if flag has been thrown, false otherwise
        if (1 == (rc.readSharedArray(index) & (0 ^ (1 << bitShift))) >> bitShift) {
            return true;
        }

        return false;
    }

    // Section 4: Targeting helpers
    // dedicated indices 7-10
    static void writeEnemy(RobotController rc, RobotType type, MapLocation locationSpotted, int index) throws GameActionException {
        int numWritten = (locationSpotted.x << 10) | (locationSpotted.y << 4);
        int priority = 0;

        if (type.equals(RobotType.MINER)) {
            priority = 0;
        } else if (type.equals(RobotType.LABORATORY)) {
            priority = 1;
        } else if (type.equals(RobotType.SOLDIER)) {
            priority = 2;
        } else if (type.equals(RobotType.BUILDER)) {
            priority = 3;
        } else if (type.equals(RobotType.WATCHTOWER)) {
            priority = 4;
        } else if (type.equals(RobotType.SAGE)) {
            priority = 5;
        } else if (type.equals(RobotType.ARCHON)) {
            priority = 6;
        }

        rc.writeSharedArray(index, (numWritten & ~14) | (priority << 1));
    }

    static MapLocation readEnemyLoc(RobotController rc, int index) throws GameActionException {
        // return null if no archon found
        int enemyComm = rc.readSharedArray(index);

        if (enemyComm != 0) {
            int archonX = (enemyComm & 64512) >> 10;
            int archonY = (enemyComm & 1008) >> 4;

            MapLocation enemyLocation = new MapLocation(archonX, archonY);

            return enemyLocation;
        }

        return null;
    }

    public static void reportEnemy(RobotController rc, RobotType type, MapLocation locationSpotted) throws GameActionException {
        int indexUsed = -1;
        int priority = 0;
        int enemySeven = rc.readSharedArray(7);
        int enemyEight = rc.readSharedArray(8);
        int enemyNine = rc.readSharedArray(9);
        int enemyTen = rc.readSharedArray(10);

        if (type.equals(RobotType.MINER)) {
            priority = 0;
        } else if (type.equals(RobotType.LABORATORY)) {
            priority = 1;
        } else if (type.equals(RobotType.SOLDIER)) {
            priority = 2;
        } else if (type.equals(RobotType.BUILDER)) {
            priority = 3;
        } else if (type.equals(RobotType.WATCHTOWER)) {
            priority = 4;
        } else if (type.equals(RobotType.SAGE)) {
            priority = 5;
        } else if (type.equals(RobotType.ARCHON)) {
            priority = 6;
        }

        if (enemySeven == 0) {
            indexUsed = 7;
        } else if (enemyEight == 0) {
            indexUsed = 8;
        } else if (enemyNine == 0) {
            indexUsed = 9;
        } else if (enemyTen == 0) {
            indexUsed = 10;
        } else {
            // find lowest priority in slots
            int lowPrio = -1;
            int prioritySeven = (enemySeven & 14) >> 1;
            int priorityEight = (enemyEight & 14) >> 1;
            int priorityNine = (enemyNine & 14) >> 1;
            int priorityTen = (enemyTen & 14) >> 1;

            if (prioritySeven < priorityEight && prioritySeven < priorityNine && prioritySeven < priorityTen) {
                lowPrio = 7;
            } else if (priorityEight < priorityNine && priorityEight < priorityTen) {
                lowPrio = 8;
            } else if (priorityNine < priorityTen) {
                lowPrio = 9;
            } else {
                lowPrio = 10;
            }

            if (indexUsed != -1 && priority > lowPrio) {
                writeEnemy(rc, type, locationSpotted, indexUsed);
            }
        }
    }

    public static MapLocation getTarget(RobotController rc) throws GameActionException {
        // returns null if array has no targets

        int enemySeven = rc.readSharedArray(7);
        int enemyEight = rc.readSharedArray(8);
        int enemyNine = rc.readSharedArray(9);
        int enemyTen = rc.readSharedArray(10);

        if (enemySeven == 0 && enemyEight == 0 && enemyNine == 0 && enemyTen == 0) {
            return null;
        }

        int prioritySeven = (enemySeven & 14) >> 1;
        int priorityEight = (enemyEight & 14) >> 1;
        int priorityNine = (enemyNine & 14) >> 1;
        int priorityTen = (enemyTen & 14) >> 1;

        if (prioritySeven > priorityEight && prioritySeven > priorityNine && prioritySeven > priorityTen) {
            return readEnemyLoc(rc, 7);
        } else if (priorityEight > priorityNine && priorityEight > priorityTen) {
            return readEnemyLoc(rc, 8);
        } else if (priorityNine > priorityTen) {
            return readEnemyLoc(rc, 9);
        } else {
            // priorityTen is largest
            return readEnemyLoc(rc, 10);
        }
    }
}
