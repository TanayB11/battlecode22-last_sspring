package scalar;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Communication {
    static int readNumMiners(RobotController rc) throws GameActionException {
        int commsEight = rc.readSharedArray(8);
        return (commsEight & 65280) >> 8;
    }

    static int readNumSoldiers(RobotController rc) throws GameActionException {
        int commsEight = rc.readSharedArray(8);
        return commsEight & 255;
    }

    static int readNumBuilders(RobotController rc) throws GameActionException {
        int commsNine = rc.readSharedArray(9);
        return (commsNine & 65280) >> 8;
    }

    static void writeNumMiners(RobotController rc, int numMiners) throws GameActionException {
        int commsEight = rc.readSharedArray(8);

        rc.writeSharedArray(8, (commsEight & ~65280) | (numMiners << 8));
    }

    static void writeNumSoldiers(RobotController rc, int numSoldiers) throws GameActionException {
        int commsEight = rc.readSharedArray(8);

        rc.writeSharedArray(8, (commsEight & ~255) | numSoldiers);
    }

    static void writeNumBuilders(RobotController rc, int numBuilders) throws GameActionException {
        int commsNine = rc.readSharedArray(9);

        rc.writeSharedArray(8, (commsNine & ~65280) | (numBuilders << 8));
    }

    static void writeArchonLocation(RobotController rc, int index) throws GameActionException {
        MapLocation me = rc.getLocation();
        int numWritten = (me.x << 10) | (me.y << 4);

        rc.writeSharedArray(index, numWritten);
    }

    static void writeEnemyArchonLocation(RobotController rc, MapLocation enemyLocation, int index) throws GameActionException {
        int numWritten = (enemyLocation.x << 10) | (enemyLocation.y << 4);

        rc.writeSharedArray(index, numWritten);
    }

    static MapLocation readArchonLocation(RobotController rc, int index) throws GameActionException {
        int archonComm = rc.readSharedArray(index);

        if (archonComm != 0) {
            int archonX = (archonComm & 64512) >> 10;
            int archonY = (archonComm & 1008) >> 4;

            MapLocation archonLocation = new MapLocation(archonX, archonY);

            return archonLocation;
        }

        return null;
    }

    static void throwArchonHealthFlag(RobotController rc, int index) throws GameActionException {
        rc.writeSharedArray(index, rc.readSharedArray(index) | (1 << 3));
    }

    static void throwArchonsFoundFlag(RobotController rc) throws GameActionException {
        rc.writeSharedArray(0, rc.readSharedArray(0) | (1 << 2));
    }

    static void throwFlag(RobotController rc, int index, int bitShift) throws GameActionException {
        rc.writeSharedArray(index, rc.readSharedArray(index) | (1 << bitShift));
    }

    static int earliestEmptyArchonIndex(RobotController rc, int numInitialArchons) throws GameActionException {
        if (rc.readSharedArray(0) == 0) {
            return 0;
        } else if (numInitialArchons >= 2 && rc.readSharedArray(1) == 0) {
            return 1;
        } else if (numInitialArchons >= 3 && rc.readSharedArray(2) == 0) {
            return 2;
        } else if (numInitialArchons >= 4 && rc.readSharedArray(3) == 0) {
            return 3;
        }

        // if nothing, return -1
        return -1;
    }

    // given index, generates all possible enemy archon locations for that index
    // returns in order horizontal, vertical, rotational
    static MapLocation[] possibleEnemyArchonLocations(RobotController rc, int index) throws GameActionException {
        MapLocation ourArchon = readArchonLocation(rc, index);

        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        if (ourArchon != null) {
            MapLocation horizontalReflection = new MapLocation(mapWidth - ourArchon.x, ourArchon.y);
            MapLocation verticalReflection = new MapLocation(ourArchon.x, mapHeight - ourArchon.y);

            if (mapWidth == mapHeight) {
                MapLocation rotation = new MapLocation(ourArchon.y, ourArchon.x);

                MapLocation[] possibleLocations = {horizontalReflection, verticalReflection, rotation};
                return possibleLocations;
            } else {
                MapLocation[] possibleLocations = {horizontalReflection, verticalReflection};
                return possibleLocations;
            }
        } else {
            return null;
        }
    }

    // given a found enemy archon, generate all enemy archons and write to shared array
    // returns true if operation was successful, returns false otherwise
    // false implies that archons have moved?
    static boolean generateEnemyArchons(RobotController rc, MapLocation knownEnemyArchonLocation, int numInitialArchons) throws GameActionException {
        boolean shouldBreak = false;
        int positionMatched = -1;

        for (int i = 0; i < numInitialArchons; i++){
            MapLocation[] currentPossibleSet = possibleEnemyArchonLocations(rc, i);

            for (int j = 0; j < currentPossibleSet.length; j++){
                if (currentPossibleSet[j].equals(knownEnemyArchonLocation)){
                    positionMatched = j;
                    shouldBreak = true;
                    break;
                }
            }

            if (shouldBreak){
                break;
            }
        }

        if (positionMatched != -1){
            // this means archons have been found :eyes:
            for (int i = 0; i < numInitialArchons; i++){
                MapLocation generatedLocation = possibleEnemyArchonLocations(rc, i)[positionMatched];
                writeEnemyArchonLocation(rc, generatedLocation, i+4);
            }

            if (positionMatched == 1) {
                throwFlag(rc, 4, 0);
                throwFlag(rc, 4, 1);
            } else if (positionMatched == 0) {
                throwFlag(rc, 4, 0);
                throwFlag(rc, 4, 2);
            } else if (positionMatched == 2) {
                throwFlag(rc, 4, 1);
                throwFlag(rc, 4, 2);
            }

            throwArchonsFoundFlag(rc);
            return true;
        }

        return false;
    }
}
