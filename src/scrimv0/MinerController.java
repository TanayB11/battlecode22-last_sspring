package scrimv0;
import battlecode.common.*;
import java.lang.*;
import java.util.Arrays;

/* This is our miner program */
public class MinerController {
    static Direction travelDir = null;
    static MapLocation me = null, destination = null;
    static boolean isMining = false, isExploring = true, isSwarming = false;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        leadFind(rc);

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

   static void leadFind(RobotController rc) throws GameActionException {
       int movementCooldownTurns = rc.getMovementCooldownTurns();

       if (travelDir == null) {
           Util.rng.setSeed(rc.getID());
           travelDir = Util.directions[Util.rng.nextInt(Util.directions.length)];
       }

       // detect nearby Pb
       MapLocation[] nearbyPb = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
       if (destination == null && nearbyPb.length > 0 && !isMining) {
           rc.setIndicatorString("Searching for a destination");
           Arrays.sort(nearbyPb, (a, b) -> me.distanceSquaredTo(a) - me.distanceSquaredTo(b));
           for (MapLocation potentialDest : nearbyPb) { // find first non-occupied destination
               if (!rc.isLocationOccupied(potentialDest)) {
                   destination = potentialDest;
                   break;
               }
           }
       } else if (destination == null && !isMining) {
           // if we have nowhere to go and no lead in sight,
           // just pick a direction and bug (it's a battlecode metaphor for life :D)
           rc.setIndicatorString("Nothing in sight, picking random travel direction");
           Util.rng.setSeed(rc.getID());
           travelDir = Util.directions[Util.rng.nextInt(Util.directions.length)];
       } else { // we have a destination!
           // TODO: stop the robots from oscillating
           rc.setIndicatorString("Pathfinding to " + destination.toString());
           if (!isMining && movementCooldownTurns == 0) {
               travelDir = Util.greedyNextMove(rc, destination);
               if (!rc.onTheMap(rc.getLocation().add(travelDir))) {
                   travelDir = travelDir.opposite();
               }
               if (!Util.safeMove(rc, travelDir)) {
                   for (int i = 0; i < Util.directions.length; i++) {
                       if (!Util.directions[i].equals(travelDir)) {
                           // if moved successfully, cooldown kicks in and loop will break
                           Util.safeMove(rc, Util.directions[i]);
                       }
                   }
               }
           }

           if (me.distanceSquaredTo(destination) <= rc.getType().visionRadiusSquared && rc.senseLead(destination) == 0) {
               destination = null;
           }
       }

       // can be more efficient by modifying above else block
       if (!isMining && movementCooldownTurns == 0) {
           if (!rc.onTheMap(rc.getLocation().add(travelDir))) { travelDir = travelDir.opposite(); }
           Util.safeMove(rc, travelDir);
       }

       if (me.equals(destination)) {
           rc.setIndicatorString("Reached destination " + destination.toString());
           destination = null;
       }
       if (rc.getHealth() < 40)
       {
           rc.setIndicatorString("Damage taken!");

           //Add info to the array
           rc.writeSharedArray(0, me.x);
           rc.setIndicatorString("I wrote x coords to array: " + me.x);

           rc.writeSharedArray(1, me.y);
           rc.setIndicatorString("I wrote y coords to array: " + me.y);
       }

   }
}
