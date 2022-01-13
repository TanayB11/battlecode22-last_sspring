package phoenix;

import battlecode.common.*;
import phoenix.util.BFS;
import phoenix.util.DroidBFS;
import phoenix.util.Util;

import java.util.Arrays;

public class SoldierController {
    static MapLocation me = null, targetLoc = null;
    static MapLocation spawnPt = null;
    static boolean isRetreating = false;
    static BFS bfs = null;

    static int prevHP = Integer.MIN_VALUE;
    static final int RETREAT_TURNS = 5;
    static int retreatCounter = 0; // no. of times soldier has been retreating

    // TODO: if dying, retreat to spawnPt so we can harvest its guts
    // TODO: manage comms about enemy archons to swarm

    // TODO: compute 3 possible enemy archon locations (in archoncontroller) and share in array
    // if we don't have a target, just send all soldiers to one of those locs

    // soldier micro: head to enemy archon, kill others on the way

    public static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // spawn init
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            spawnPt = new MapLocation(me.x, me.y);
            bfs = new DroidBFS(rc);
        }

        // if we're at target, reset target
        if (me.equals(targetLoc)) {
            targetLoc = null;
        }

        // TODO: keep tabs on the id of the robot we're attacking
        // sense nearby robots, attack in priority order
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, Util.ATTACK_PRIORITY_COMPARATOR);
            targetLoc = nearbyEnemies[0].getLocation();
//            rc.setIndicatorString("NEAREST PRIORITY ENEMY IS " + nearbyEnemies[0].getLocation().toString());
        }

        if (targetLoc == null) {
            targetLoc = Util.getExploreLoc(rc);
        }

        bfs.move(targetLoc);
        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }
}
