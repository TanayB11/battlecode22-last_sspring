package baymax_seven;

import battlecode.common.*;
import baymax_seven.util.pathfinding.BFS;
import baymax_seven.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static baymax_seven.util.Communication.minerReport;
import static baymax_seven.util.Communication.reportEnemy;
import static baymax_seven.util.Exploration.minerExploreLoc;
import static baymax_seven.util.Miscellaneous.directions;
import static baymax_seven.util.Miscellaneous.retreatFrom;

public class MinerController {
    static MapLocation miningTarget = null, exploreTarget = null;
    static int[] rubbleCounts = new int[900];
    static BFS bfs = null;
    static int prevHP = 0;

    static void runMiner(RobotController rc) throws GameActionException {
        //part 1: prep
        MapLocation me = rc.getLocation();

        // initialize
        if (bfs == null) {
            bfs = new DroidBFS(rc);
            prevHP = rc.getHealth();
        }

        // add itself to the unit count
        minerReport(rc);

        rc.setIndicatorString((exploreTarget!=null) ? exploreTarget.toString() : "NO EXPLORE TARGET");

        // set targets to null accordingly
        if (exploreTarget != null && rc.canSenseLocation(exploreTarget) && rc.senseLead(exploreTarget) < 2 && rc.senseGold(exploreTarget) == 0) {
            exploreTarget = null;
        }

        if (miningTarget != null && rc.canSenseLocation(miningTarget) && rc.senseLead(miningTarget) < 2 && rc.senseGold(miningTarget) == 0) {
            miningTarget = null;
        }

        //report nearby enemies to shared array (from v5)
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo retreatFromEnemy = null; // nearest dangerous enemy (defaults to null)

        if (nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, Comparator.comparingInt(a -> me.distanceSquaredTo(a.getLocation())));
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
                if (isAttackingEnemy(nearbyEnemies[i])) {
                    retreatFromEnemy = nearbyEnemies[i];
                    break;
                }
            }
        }

        //find a mining target
            //find square with the lowest rubble
            //&& its unoccupied && adjacent to/on lead (more lead wins tiebreaker)
            //if low health, go back for healing
        MapLocation[] nearbyGold = rc.senseNearbyLocationsWithGold();
        MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(-1, 2);
        MapLocation optimalLoc = null;

        if (nearbyGold.length > 0) {
            optimalLoc = getOptimalMineLoc(rc, nearbyGold);
        } else if (nearbyLead.length > 0) {
            optimalLoc = getOptimalMineLoc(rc, nearbyLead);
        }

        if (miningTarget == null && optimalLoc != null) {
            rc.setIndicatorString(optimalLoc.toString());
            miningTarget = optimalLoc;
        }

        //part 2: execution

        //mine adjacent squares
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        //if attacked, run away
        if (rc.getHealth() < prevHP && retreatFromEnemy != null) {
            retreatFrom(rc, retreatFromEnemy.getLocation());
            exploreTarget = null;
            prevHP = rc.getHealth();
        } else if (miningTarget != null) {
            bfs.move(miningTarget);
        } else if (exploreTarget == null) {
            exploreTarget = minerExploreLoc(rc);
        }

        // will only execute if we haven't moved yet this turn
        bfs.move(exploreTarget);

        //mine adjacent squares again
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
<<<<<<< Updated upstream
=======

<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
                while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                    rc.mineLead(mineLocation);
                }
            }
        }
    }

    private static MapLocation getOptimalMineLoc(RobotController rc, MapLocation[] nearbyResource) throws GameActionException {
        int minRubble = Integer.MAX_VALUE;
        MapLocation optimalLocation = null;
        int resourceRubble;
        for (int i = 0; i < nearbyResource.length; i++) {

            // check on the square itself
            resourceRubble = rc.senseRubble(nearbyResource[i]);
            if (resourceRubble < minRubble) {
                optimalLocation = nearbyResource[i];
                minRubble = resourceRubble;
            }

            // check adjacent squares
            for (int j = 0; j < directions.length; j++){
                if (rc.canSenseLocation(nearbyResource[i].add(directions[j]))) {
                    if (rc.senseRubble(nearbyResource[i].add(directions[j])) < minRubble) {
                        optimalLocation = nearbyResource[i].add(directions[j]);
                        minRubble = rc.senseRubble(nearbyResource[i].add(directions[j]));
                    }
                }
            }
        }
        return optimalLocation;
    }

    static boolean isAttackingEnemy(RobotInfo enemy) throws GameActionException {
        RobotType enemyType = enemy.getType();
        if (enemyType.equals(RobotType.SOLDIER) || enemyType.equals(RobotType.SAGE) || enemyType.equals(RobotType.WATCHTOWER)) {
            return true;
        }
        return false;
    }
}
