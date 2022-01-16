package drake.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SafeActions {
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

    public static void safeAttack(RobotController rc, MapLocation attackLocation) throws GameActionException {
        if (rc.canAttack(attackLocation)) {
            rc.attack(attackLocation);
        }
    }
}
