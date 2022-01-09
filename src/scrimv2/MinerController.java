package scrimv2;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import scrimv2.util.Util;

public class MinerController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static boolean isMining = false;
    static int failedMoves = 0;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // TODO: if we're on the other side of the map start just looking for lead/scouting
        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }

        // introduce some mutation probability
        if (rc.getRoundNum() >= 250 && Util.rng.nextDouble() <= 0.01) {
            travelDir = Util.directions[Util.rng.nextInt(Util.directions.length)];
        }

        if (failedMoves >= 3) {
            travelDir = (Util.rng.nextDouble() <= 0.02)
                    ? Util.directions[Util.rng.nextInt(Util.directions.length)]
                    : travelDir.opposite();
        }

        // communicate who is mining, will get rid of this if
        // TODO: once teh devs implement an overloaded senseNearbyLocationsWithLead
        // send a miner to the lead direction if there is a nearby loc with lead > 1
        // then set travelDir back to null to go back to swarm
        if (rc.getRobotCount() < (3 * rc.getArchonCount())) {
            MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
            if (nearbyLead.length > 0) {
            }
        } else {
            if (Util.safeMove(rc, travelDir)) {
                failedMoves = 0;
            } else if (rc.getMovementCooldownTurns() == 0) {
                failedMoves++;
            }
        }

        // sense and communicate enemies
//        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
//        for (RobotInfo enemy : nearbyEnemies) {
//            if (enemy.getType().equals(RobotType.ARCHON)) {
//                rc.writeSharedArray();
//            }
//        }

        // mine
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
            }
        }
   }

    static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        // leave lead to regen
        while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
            isMining = true;
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
            isMining = true;
            rc.mineLead(mineLocation);
        }
        isMining = false;
    }
}
