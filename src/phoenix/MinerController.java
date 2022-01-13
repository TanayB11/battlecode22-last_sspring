package phoenix;
import battlecode.common.*;
import phoenix.util.BFS;
import phoenix.util.DroidBFS;
import phoenix.util.Util;

import java.util.Arrays;
import java.util.Comparator;

public class MinerController {
    static MapLocation me = null, targetLoc = null;
    static MapLocation spawnPt = null;
    static boolean isRetreating = false;
    static BFS bfs = null;

    static int prevHP = Integer.MIN_VALUE;
    static final int RETREAT_TURNS = 5;
    static int retreatCounter = 0; // no. of times miner has been retreating

    static boolean isDying = false;

    // TODO: bytecode optimize
    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // init on spawn
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            spawnPt = new MapLocation(me.x, me.y);
            bfs = new DroidBFS(rc);
        }

        // reports if dying
        if (!isDying && rc.getHealth() < rc.getType().health * 0.1) {
            int minersCt = rc.readSharedArray(0);
            rc.writeSharedArray(0, minersCt - 1);
            isDying = true;
        }

        // if the target location has insufficient lead and 0 gold, reset the target
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

        // if we are attacked, report crime scene loc on comms if someone else hasn't already
        // soldiers will reset that message to 0 when they reach it
        // 2nd element of array is current enemy location (only overwrite once we reach there)
        if (rc.getHealth() < prevHP) {
            if (rc.readSharedArray(1) == 0) {
                rc.writeSharedArray(1, me.x * 100 + me.y);
            }
            isRetreating = true;
            prevHP = rc.getHealth();
        }

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        // if we find a nearby archon, report its location
        // TODO: track enemy archon health?
        for (RobotInfo enemy : nearbyEnemies) {
            if (enemy.getType().equals(RobotType.ARCHON)) {
                // 10-13 is enemy archon info
                for (int commsIndex = 10; commsIndex++ <= 13;) {
                    if (rc.readSharedArray(commsIndex) == 0) {
                        MapLocation enemyLoc = enemy.getLocation();
                        rc.writeSharedArray(commsIndex, enemyLoc.x * 100 + enemyLoc.y);
                        break;
                    }
                }
            }
        }

        if (isRetreating) {
            if (nearbyEnemies.length > 0) {
                Arrays.sort(nearbyEnemies, (a, b) -> me.distanceSquaredTo(a.getLocation()) - me.distanceSquaredTo(b.getLocation()));
                Direction dirAwayFromEnemy = me.directionTo(nearbyEnemies[0].getLocation()).opposite();
                Util.safeMove(rc, dirAwayFromEnemy);
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
            targetLoc = Util.getExploreLoc(rc);
        }

        //if we have a target, move towards it
        bfs.move(targetLoc);

        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }

    static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        while (rc.canMineGold(mineLocation)) { rc.mineGold(mineLocation); }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) { rc.mineLead(mineLocation); }
    }
}
