package drake;

import battlecode.common.*;
import drake.util.pathfinding.BFS;
import drake.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static drake.util.Communication.*;
import static drake.util.Exploration.minerExploreLoc;
import static drake.util.SafeActions.safeMine;
import static drake.util.SafeActions.safeMove;

public class MinerController {
    static MapLocation me = null, targetLoc = null, spawnPt = null;
    static boolean isRetreating = false, isDying = false;
    static int prevHP = Integer.MIN_VALUE, retreatCounter = 0;
    static final int RETREAT_TURNS = 5;
    static BFS bfs = null;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // init on spawn
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            spawnPt = new MapLocation(me.x, me.y);
            bfs = new DroidBFS(rc);
        }

        // reports if dying
        if (!isDying && rc.getHealth() < rc.getType().health * 0.25) {
            writeNumMiners(rc, readNumMiners(rc) - 1);
            isDying = true;
        }

        // if the target location has < 2 lead and 0 gold, reset the target
        // this will even reset when we're exploring
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

        // retreat if taking damage
        if (rc.getHealth() < prevHP) {
            isRetreating = true;
            prevHP = rc.getHealth();
            retreatCounter = 0;
        }

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        if (isRetreating) {
            if (nearbyEnemies.length > 0) {
                Arrays.sort(nearbyEnemies, (a, b) -> me.distanceSquaredTo(a.getLocation()) - me.distanceSquaredTo(b.getLocation()));
                Direction dirAwayFromEnemy = me.directionTo(nearbyEnemies[0].getLocation()).opposite();
                safeMove(rc, dirAwayFromEnemy);
            } else { // default to spawn point
                bfs.move(spawnPt);
            }

            retreatCounter++;
            if (retreatCounter == RETREAT_TURNS) {
                isRetreating = false;
                retreatCounter = 0;
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
                Arrays.sort(nearbyLead, Comparator.comparingInt(a -> me.distanceSquaredTo(a)));
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
            targetLoc = minerExploreLoc(rc);
        }

        //if we have a target, move towards it
        bfs.move(targetLoc);

        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }
}
