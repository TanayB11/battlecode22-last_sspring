package scrimv0;
import battlecode.common.*;

import java.util.Arrays;

public class MinerController {
    static Direction travelDir = null;
    static MapLocation me = null, destination = null;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        if (travelDir == null) {
            Util.rng.setSeed(rc.getID());
            travelDir = Util.directions[Util.rng.nextInt(Util.directions.length)];
        }

        // Detect nearby Pb
        MapLocation[] nearbyPb = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
        if (destination == null && nearbyPb.length > 0) {
            rc.setIndicatorString("Searching for a destination");
            Arrays.sort(nearbyPb, (a, b) -> me.distanceSquaredTo(a) - me.distanceSquaredTo(b));
            for (MapLocation potentialDest : nearbyPb) { // find first non-occupied destination
                if (!rc.isLocationOccupied(potentialDest)) {
                    destination = potentialDest;
                    break;
                }
            }
        } else if (destination == null) {
            // if we have nowhere to go and no lead in sight,
            // just pick a direction and bug (it's a battlecode metaphor for life :D)
            rc.setIndicatorString("No ");
            Util.rng.setSeed(rc.getID());
            travelDir = Util.directions[Util.rng.nextInt(Util.directions.length)];
            // TODO: move in traveldir
        } else { // we have a destination!
            rc.setIndicatorString("Pathfinding to " + destination.toString());
            travelDir = Util.greedyNextMove(rc, destination);
            if (rc.getMovementCooldownTurns() == 0 && !Util.safeMove(rc, travelDir)) {
                for (int i = 0; i < Util.directions.length; i++) {
                    if (!Util.directions[i].equals(travelDir)) {
                        // if moved successfully, cooldown kicks in and loop will break
                        Util.safeMove(rc, Util.directions[i]);
                    }
                }
            }
        }

        // can be more efficient by modifying above else block
        Util.safeMove(rc, travelDir);

        if (me.equals(destination)) {
            rc.setIndicatorString("Reached destination " + destination.toString());
            destination = null;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                while (rc.canMineGold(mineLocation)) { rc.mineGold(mineLocation); }
                while (rc.canMineLead(mineLocation)) { rc.mineLead(mineLocation); }
            }
        }
   }
}
