package scrimv1;
import battlecode.common.*;

import java.util.Arrays;

public class MinerController {
    static Direction travelDir = null;
    static MapLocation me = null, destination = null;
    static boolean isMining = false, isExploring = true, isSwarming = false;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }

        Util.safeMove(rc, travelDir);

        // can we bug2 when rubble bad?

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
            }
        }
   }

    static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        while (rc.canMineGold(mineLocation)) {
            isMining = true;
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation)) {
            isMining = true;
            rc.mineLead(mineLocation);
        }
        isMining = false;
    }
}
