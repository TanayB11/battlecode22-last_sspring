package drake;

import battlecode.common.*;

import static drake.util.Communication.*;
import static drake.util.Miscellaneous.directions;
import static drake.util.Miscellaneous.rng;
import static drake.util.SafeActions.safeBuild;


// TODO: BUILD ORDER
// TODO: KILL NEARBY ARCHONS IMMEDIATELY
// heuristic for # miners before soldier
// spawn X miners / archon (based on heuristic), consistently send soldiers, ~~then if lead production is decreasing/miners dying spawn more~~
    // if there 2x soldiers than miners, alternate b/t miner and 2 soldiers
// if we have a target, we should send a soldier closest to that target
// later: build queue with linkedlist/array and bitshifts

public class ArchonController {
    static int archIndex = -1, miners = 0, soldiers = 0;
    static boolean isAlpha = false;
    static int prevEnemySeven = 0, prevEnemyEight = 0, prevEnemyNine = 0, prevEnemyTen = 0, turnsSeven = 0, turnsEight = 0, turnsNine = 0, turnsTen = 0;
    static int soldierMinerBuildAlternator = 0; // don't need this when we implement build queue

    // can be tuned
    static int initialMinersToSpawn = -1;
    static final int MINER_HEURISTIC_K = 450;

    public static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        if (archIndex == -1 || initialMinersToSpawn == -1) { // init values
            initialMinersToSpawn = rc.getMapWidth() * rc.getMapHeight() / MINER_HEURISTIC_K;
            archIndex = firstArchIndexEmpty(rc);
            writeOwnArchLoc(rc, firstArchIndexEmpty(rc));
            if (archIndex == 0) {
                isAlpha = true;
            }
        }

        // if we see an archon, soldier rush
        // NOTE for later: getTarget() -> returns null if no target
        // DONE: report enemies
        // DONE: throw flag to divert all resources to this archon
            // DONE: set 0th archon flag on least significant (0th) bit
            // DONE: spawn direction for soldiers (rc.directionto(archon))
            // DONE: check if flag set, then don't spawn resources if we're not the flagged archon
        MapLocation nearbyArchon = null;
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (int i = 0; i <= nearbyEnemies.length; i++) {
            reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
        }

        for (RobotInfo enemy : nearbyEnemies) {
            if (enemy.getType().equals(RobotType.ARCHON)) {
                nearbyArchon = enemy.getLocation();
                break;
            }
        }

        // determine build order
        int numMiners = readNumMiners(rc);
        int numSoldiers = readNumSoldiers(rc);
        Direction randomSpawnDir = directions[rng.nextInt(directions.length)];

        // highest priority is to attack visible enemy archons
        // TODO: reset flag
        boolean adjacentArchonFlag = checkFlag(rc, 0, 0);
        if (nearbyArchon != null && !adjacentArchonFlag) {
            // check flag to make sure another archon hasn't already set
            alternateSpawnMinerSoldier(rc, me.directionTo(nearbyArchon), numMiners, numSoldiers);
            throwFlag(rc, 0, 0); // throw a flag on 0th archon (comms array index 0)
        } else if (numMiners < initialMinersToSpawn && !adjacentArchonFlag) {
            safeBuild(rc, RobotType.MINER, randomSpawnDir);
            writeNumMiners(rc, numMiners + 1);
        } else if (!adjacentArchonFlag) {
            // build soldiers unless...
            // if we sense a nearby archon or have enough soldiers
            if (numSoldiers < 2*numMiners) {
                // TODO: make smart spawn direction for soldiers
                safeBuild(rc, RobotType.SOLDIER, randomSpawnDir);
                writeNumSoldiers(rc, numSoldiers + 1);
            } else {
                alternateSpawnMinerSoldier(rc, randomSpawnDir, numMiners, numSoldiers);
            }
        }

        if (rc.getHealth() <= 60) {
            throwFlag(rc, archIndex, 3);
        }

        int archons = rc.getArchonCount();
        miners = readNumMiners(rc);
        soldiers = readNumSoldiers(rc);

        if (isAlpha) {
            int currentTurn = rc.getRoundNum();

            if (rc.readSharedArray(7) != prevEnemySeven) {
                turnsSeven = currentTurn + 20;
            }

            if (rc.readSharedArray(8) != prevEnemyEight) {
                turnsEight = currentTurn + 20;
            }

            if (rc.readSharedArray(9) != prevEnemyNine) {
                turnsNine = currentTurn + 20;
            }

            if (rc.readSharedArray(10) != prevEnemyTen) {
                turnsTen = currentTurn + 20;
            }

            if (currentTurn == turnsSeven) {
                rc.writeSharedArray(7, 0);
            }

            if (currentTurn == turnsEight) {
                rc.writeSharedArray(8, 0);
            }

            if (currentTurn == turnsNine) {
                rc.writeSharedArray(9, 0);
            }

            if (currentTurn == turnsTen) {
                rc.writeSharedArray(10, 0);
            }
        }
    }

    static void alternateSpawnMinerSoldier(RobotController rc, Direction dir, int numMiners, int numSoldiers) throws GameActionException {
        // alternate 2 soldiers, 1 miner
        if (soldierMinerBuildAlternator % 3 != 2) {
            safeBuild(rc, RobotType.SOLDIER, dir);
            writeNumSoldiers(rc, numSoldiers + 1);
        } else {
            safeBuild(rc, RobotType.MINER, dir);
            writeNumMiners(rc, numMiners + 1);
        }
        soldierMinerBuildAlternator++;
    }
}