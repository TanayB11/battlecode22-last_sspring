package phoenix;
import battlecode.common.*;
import phoenix.util.BFS;
import phoenix.util.DroidBFS;
import phoenix.util.Util;

import java.util.Arrays;

public class MinerController {
    static MapLocation me = null, targetLoc = null;
    static MapLocation spawnPt = null;
    static boolean isRetreating = false;
    static BFS bfs = null;

    static int prevHP = Integer.MIN_VALUE;
    static final int RETREAT_TURNS = 5;
    static int retreatCounter = 0; // no. of times miner has been retreating

    // TODO: bytecode optimize
    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // init on spawn
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            spawnPt = new MapLocation(me.x, me.y);
            bfs = new DroidBFS(rc);
        }

        // if the target location has insufficient lead and 0 gold, reset the target
        if (
            targetLoc != null && rc.canSenseLocation(targetLoc) &&
            !(rc.senseLead(targetLoc) > 1) && rc.senseGold(targetLoc) == 0
        ) {
            targetLoc = null;
        }

        // if we're at target, reset target
        if (me.equals(targetLoc)) {
            targetLoc = null;
        }

        // did we get attacked?? recon and RUN back to spawn!!!
        if (rc.getHealth() < prevHP) {
            // TODO: comms
            isRetreating = true;
            prevHP = rc.getHealth();
        }

        if (isRetreating) {
            targetLoc = spawnPt;
            bfs.move(spawnPt);
            retreatCounter++;
            if (retreatCounter == RETREAT_TURNS) {
                isRetreating = false;
                retreatCounter = 0;
                targetLoc = null;
            }
        }

        // check if any locations have gold
        // only worry about first index because chances of >1 gold are close to none
        // note: will overwrite any existing travelLoc
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold(); // mine it all out
        if (nearbyGold.length > 0 && !rc.isLocationOccupied(nearbyGold[0])) {
            targetLoc = nearbyGold[0];
        } else { // check for lead > 1 (allow to regen)
            MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(-1, 2);
            if (nearbyLead.length > 0) {
                Arrays.sort(nearbyLead, (a, b) -> me.distanceSquaredTo(a) - me.distanceSquaredTo(b));
                for (MapLocation pb : nearbyLead) {
                    if (!rc.isLocationOccupied(pb)) {
                        targetLoc = pb;
                        break;
                    }
                }
            }
        }

        // mine
        for (int dx = -1; dx++ <= 1;) {
            for (int dy = -1; dy++ <= 1;) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
            }
        }

        if (targetLoc == null) {
            targetLoc = Util.getExploreLoc(rc);
        }

        //if we have a target, move towards it
        if (rc.isMovementReady()) {
            bfs.move(targetLoc);
        }
        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }

    static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        while (rc.canMineGold(mineLocation)) { rc.mineGold(mineLocation); }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) { rc.mineLead(mineLocation); }
    }
}
