package sma.util;

import battlecode.common.*;

public class SafeActions {
    public static boolean safeBuild(RobotController rc, RobotType type, Direction dir) throws GameActionException {
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        }
        return false;
    }

    public static boolean safeMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    public static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        while (rc.canMineGold(mineLocation)) { rc.mineGold(mineLocation); }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) { rc.mineLead(mineLocation); }
    }
}
