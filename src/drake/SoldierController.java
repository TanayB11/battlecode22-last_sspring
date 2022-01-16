package drake;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.Arrays;

import static drake.Communication.*;
import static drake.Exploration.minerExploreLoc;
import static drake.Miscellaneous.ATTACK_PRIORITY_COMPARATOR;
import static drake.SafeActions.safeAttack;


public class SoldierController {
    static MapLocation me = null, targetLoc = null, spawnPt = null;
    static boolean isRetreating = false, isDying = false;
    static BFS bfs = null;
    static int prevHP = Integer.MIN_VALUE;

    static final int ACCEPTABLE_TARGET_LOC_RUBBLE = 50;

    public static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // initalize when spawned
        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getType().health;
            spawnPt = new MapLocation(me.x, me.y);
            bfs = new DroidBFS(rc);
        }

        // reports if dying
        if (!isDying && rc.getHealth() < rc.getType().health * 0.2) {
            writeNumSoldiers(rc, readNumSoldiers(rc) - 1);
            isDying = true;
        }

        // if we're at target, reset target
        if (me.equals(targetLoc)) {
            targetLoc = null;
        }

        // if we're near our target and see too much rubble, null it
        if (targetLoc != null && rc.canSenseLocation(targetLoc) && rc.senseRubble(targetLoc) > ACCEPTABLE_TARGET_LOC_RUBBLE) {
            targetLoc = null;
        }

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (int i = 0; i <= nearbyEnemies.length; i++) {
            reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
        }

        // sense nearby robots, attack in priority order
        // TODO: keep tabs on the id of the robot we're attacking, chase it down
        if (nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, ATTACK_PRIORITY_COMPARATOR);
            targetLoc = nearbyEnemies[0].getLocation();
            safeAttack(rc, targetLoc);
        }

        // get target from comms
        if (targetLoc == null) {
            targetLoc = getTarget(rc);
            if (targetLoc == null) {
                // TODO: replace this with a unique soldier exploration method eventually
                // TODO: this is not an ASAP TODO
                targetLoc = minerExploreLoc(rc);
            }
        }

        bfs.move(targetLoc);
        rc.setIndicatorString("TARGET: " + targetLoc.toString());
    }
}