package five;

import battlecode.common.*;
import five.util.pathfinding.BFS;
import five.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static five.util.Communication.reportEnemy;
import static five.util.Exploration.minerExploreLoc;
import static five.util.Miscellaneous.rng;
import static five.util.SafeActions.safeMove;
import static four.util.SafeActions.safeMine;

public class MinerController {
    static final int MIN_ACCEPTABLE_RUBBLE = 25;
    static MapLocation targetLoc = null;
    static BFS bfs = null;


    static void runMiner(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        boolean movedThisTurn = false;

        if (bfs == null) {
            bfs = new DroidBFS(rc);
        }

        // report all enemies that can be seen
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo retreatFromEnemy = null;

        if (nearbyEnemies.length > 0) {
            Arrays.sort(nearbyEnemies, Comparator.comparingInt(a -> me.distanceSquaredTo(a.getLocation())));
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
                if (nearbyEnemies[i].getType().equals(RobotType.SAGE) || nearbyEnemies[i].getType().equals(RobotType.SOLDIER) || nearbyEnemies[i].getType().equals(RobotType.WATCHTOWER)) {
                    retreatFromEnemy = nearbyEnemies[i];
                }
            }
        }

        if (
                targetLoc != null && rc.canSenseLocation(targetLoc) &&
                        !(rc.senseLead(targetLoc) > 1) && rc.senseGold(targetLoc) == 0
        ) {
            targetLoc = null;
        }

        // if enemies visible that have the power to attack, retreat
        if (retreatFromEnemy != null) {
            Direction dirToEnemy = me.directionTo(retreatFromEnemy.getLocation());
            Direction dirOfRetreat = null;
            Direction[] initPossibleDirs = {null, null, null};

            // Ideal directions to move
            initPossibleDirs[0] = dirToEnemy.opposite().rotateLeft();
            initPossibleDirs[1] = dirToEnemy.opposite();
            initPossibleDirs[2] = dirToEnemy.opposite().rotateRight();

            Arrays.sort(initPossibleDirs, Comparator.comparingInt(dir -> {
                try {
                    return rc.senseRubble(rc.adjacentLocation(dir));
                } catch (GameActionException e) {
                    // probably off the map, infinite rubble
                    return Integer.MAX_VALUE;
                    // e.printStackTrace();
                }
            }));

            int bestIndex = -1;
            if (rc.onTheMap(rc.adjacentLocation(initPossibleDirs[0]))) {
                bestIndex = 0;
            } else if (rc.onTheMap(rc.adjacentLocation(initPossibleDirs[1]))) {
                bestIndex = 1;
            } else if (rc.onTheMap(rc.adjacentLocation(initPossibleDirs[2]))) {
                bestIndex = 2;
            }

            int lowestRubble = Integer.MAX_VALUE;
            if (bestIndex != -1) {
                lowestRubble = rc.senseRubble(rc.adjacentLocation(initPossibleDirs[bestIndex]));
            }

            if (lowestRubble > MIN_ACCEPTABLE_RUBBLE) {
                Direction neutralDir1 = dirToEnemy.rotateRight().rotateRight();
                Direction neutralDir2 = dirToEnemy.rotateLeft().rotateLeft();
                int neutralDirRubble = Integer.MAX_VALUE; // avoids bestIndex = -1 issue

                if (rc.onTheMap(rc.adjacentLocation(neutralDir1))) {
                    neutralDirRubble = rc.senseRubble(rc.adjacentLocation(neutralDir1));
                    if (neutralDirRubble < lowestRubble) {
                        bestIndex = 3;
                        lowestRubble = neutralDirRubble;
                    }
                }

                if (rc.onTheMap(rc.adjacentLocation(neutralDir2))) {
                    neutralDirRubble = rc.senseRubble(rc.adjacentLocation(neutralDir2));
                    if (neutralDirRubble < lowestRubble) {
                        bestIndex = 4;
                    }
                }
            }

            switch (bestIndex){
                case 3:     dirOfRetreat = dirToEnemy.rotateRight().rotateRight(); break;
                case 4:     dirOfRetreat = dirToEnemy.rotateLeft().rotateLeft(); break;
                case -1:    dirOfRetreat = initPossibleDirs[rng.nextInt(initPossibleDirs.length)]; break; // duct-tape solution
                default:    dirOfRetreat = initPossibleDirs[bestIndex]; break;
            }

            rc.setIndicatorString("RETREATING IN " + dirOfRetreat.toString());
            safeMove(rc, dirOfRetreat);
            movedThisTurn = true;
        }

        boolean hasMiningTarget = false;
        if (movedThisTurn == false) {
            MapLocation miningTarget = bfs.miningLocation();

            if (miningTarget != null) {
                targetLoc = miningTarget;
                hasMiningTarget = true;
            } else {
                if (targetLoc == null) {
                    targetLoc = minerExploreLoc(rc);
                }
            }
        }

        // mine
        boolean didMine = false;
        for (int dx = -1; dx++ <= 1;) {
            for (int dy = -1; dy++ <= 1;) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
                didMine = true;
            }
        }

        // if can mine from lower rubble spot, do that
        // unless surrounded by fellow miner friends :)
        if (hasMiningTarget && didMine) {
            if (rc.senseRubble(me) < rc.senseRubble(targetLoc)){
                movedThisTurn = true;
            }

            RobotInfo[] nearbyAllies = rc.senseNearbyRobots(4, rc.getTeam());
            int minerCount = 0;

            if (nearbyAllies.length > 0) {
                for (int i = 0; i < nearbyAllies.length; i++) {
                    if (nearbyAllies[i].getType().equals(RobotType.MINER)) {
                        minerCount += 1;
                    }
                }
            }

            if (minerCount >= 2) {
                movedThisTurn = false;
            }
        }

        if (movedThisTurn == false) {
            bfs.minerMove(targetLoc);
        }
    }
}