package sma.util;

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

    Alpha archon heartbeat:
    1. Send a ping to say the alpha archon is alive
    2. Detect the alpha archon ping
     */

    // Section 1: Number of droids / buildings
    // dedicated indices: 4-6

    // Section 1a: Reading number of units
    public static int readNumMiners(RobotController rc) throws GameActionException {
        return (rc.readSharedArray(4) & 65280) >> 8;
    }

    public static int readNumBuilders(RobotController rc) throws GameActionException {
        return (rc.readSharedArray(5) & 65280) >> 8;
    }

    public static int readNumSages(RobotController rc) throws GameActionException {
        return (rc.readSharedArray(6) & 65280) >> 8;
    }

    public static int readNumSoldiers(RobotController rc) throws GameActionException {
        return rc.readSharedArray(4) & ~(255 << 8);
    }

    public static int readNumWatchtowers(RobotController rc) throws GameActionException {
        return rc.readSharedArray(5) & ~(255 << 8);
    }

    public static int readNumLabs(RobotController rc) throws GameActionException {
        return rc.readSharedArray(6) & ~(255 << 8);
    }

    // Section 1b: Writing to number of units
    public static void writeNumMiners(RobotController rc, int numUnits)  throws GameActionException {
        rc.writeSharedArray(4, (rc.readSharedArray(4) & ~65280) | (numUnits << 8));
    }

    public static void writeNumBuilders(RobotController rc, int numUnits)  throws GameActionException {
        rc.writeSharedArray(5, (rc.readSharedArray(5) & ~65280) | (numUnits << 8));
    }

    public static void writeNumSages(RobotController rc, int numUnits)  throws GameActionException {
        rc.writeSharedArray(6, (rc.readSharedArray(6) & ~65280) | (numUnits << 8));
    }

    public static void writeNumSoldiers(RobotController rc, int numUnits) throws GameActionException {
        rc.writeSharedArray(4, ((rc.readSharedArray(4) & (255 << 8)) | numUnits));
    }

    public static void writeNumWatchtowers(RobotController rc, int numUnits) throws GameActionException {
        rc.writeSharedArray(5, (rc.readSharedArray(5) & (255 << 8)) | numUnits);
    }

    public static void writeNumLabs(RobotController rc, int numUnits) throws GameActionException {
        rc.writeSharedArray(6, (rc.readSharedArray(6) & (255 << 8)) | numUnits);
    }

    // Section 2: Archon positioning
    // dedicated indices: 0-3

    // Section 2a: Reading / Setting Locations
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

    public static void clearIndex(RobotController rc, int index) throws GameActionException {
        rc.writeSharedArray(index, 0);
    }

    // Section 4: Targeting helpers
    // dedicated index 7
    static void writeEnemy(RobotController rc, RobotType type, MapLocation locationSpotted) throws GameActionException {
        int locCode = (locationSpotted.x << 10) | (locationSpotted.y << 4);
        int priority = getReportEnemyPriority(type);
        rc.writeSharedArray(7, (locCode & ~14) | (priority << 1));
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
        int enemyComm = rc.readSharedArray(7);
        if (enemyComm != 0) {
            int archonX = (enemyComm & 64512) >> 10;
            int archonY = (enemyComm & 1008) >> 4;
            return new MapLocation(archonX, archonY);
        }
        return null;
    }

    public static void reportEnemy(RobotController rc, RobotType type, MapLocation locationSpotted) throws GameActionException {
        int currEnemyPriority = (rc.readSharedArray(7)) & 14 >> 1;
        if (currEnemyPriority == 0 || currEnemyPriority < getReportEnemyPriority(type)) {
            writeEnemy(rc, type, locationSpotted);
        }
    }

    // Section 5: Alpha archon heartbeat
    // alpha archon pings w/ round num if it's alive
    // dedicated index 8
    public static void alphaSendHeartbeat(RobotController rc) throws GameActionException {
        rc.writeSharedArray(8, rc.getRoundNum());
    }

    public static int listenAlphaHeartbeat(RobotController rc) throws GameActionException {
        return rc.readSharedArray(8);
    }

    // Section 6: Lead requests
    // Units such as builders can request how much lead they need
    // Archon will spawn miners accordingly
    // dedicated index 9
    public static void addLeadRequestedForBuildings(RobotController rc, int amtToRequest) throws GameActionException {
        rc.writeSharedArray(9, amtToRequest + readLeadRequestedForBuildings(rc));
    }

    public static int readLeadRequestedForBuildings(RobotController rc) throws GameActionException {
        return rc.readSharedArray(9);
    }

    // Section 7: Build order
    // Builders tell the archons to save up lead
}
