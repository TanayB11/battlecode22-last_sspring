package drake;

import battlecode.common.*;

import static drake.util.Communication.*;
import static drake.util.Miscellaneous.directions;
import static drake.util.Miscellaneous.rng;
import static drake.util.SafeActions.safeBuild;


// TODO: if archon is attacked spawn soldiers (needs fixing)
    // can use a flag

// heuristic for # miners before soldier
// spawn X miners / archon (based on heuristic), consistently send soldiers, ~~then if lead production is decreasing/miners dying spawn more~~
    // if there 2x soldiers than miners, alternate b/t miner and 2 soldiers
// TODO: if we have a target, we should send a soldier closest to that target
// TODO: build queue with linkedlist/array and bitshifts

public class ArchonController {
    static int archIndex = -1, miners = 0, soldiers = 0;
    static boolean isAlpha = false;
    static int prevEnemySeven = 0, prevEnemyEight = 0, prevEnemyNine = 0, prevEnemyTen = 0, turnsSeven = 0, turnsEight = 0, turnsNine = 0, turnsTen = 0;
    static int soldierMinerBuildAlternator = 0; // don't need this when we implement build queue
    static int prevHP = Integer.MIN_VALUE;

    // can be tuned
    // TODO: test different heuristics
    static int initialMinersToSpawn = -1;
    static final int MINER_HEURISTIC_K = 450;
    static final int ROUNDS_BEFORE_WIPING_TARGET = 40;

    public static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        if (archIndex == -1) { // init values
            initialMinersToSpawn = rc.getMapWidth() * rc.getMapHeight() / MINER_HEURISTIC_K;
            archIndex = firstArchIndexEmpty(rc);
            writeOwnArchLoc(rc, archIndex);
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
        int numMiners = readNumMiners(rc);
        int numSoldiers = readNumSoldiers(rc);

        MapLocation nearbyArchon = null;
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }

            if (rc.getHealth() < prevHP) {
                // spawn soldiers
                prevHP = rc.getHealth();
                if (safeBuild(rc, RobotType.SOLDIER, me.directionTo(nearbyEnemies[0].getLocation()))) {
                    writeNumSoldiers(rc, numSoldiers + 1);
                }
            }
        }

        for (RobotInfo enemy : nearbyEnemies) {
            if (enemy.getType().equals(RobotType.ARCHON)) {
                nearbyArchon = enemy.getLocation();
                break;
            }
        }

        // determine build order
        Direction randomSpawnDir = directions[rng.nextInt(directions.length)];

        // highest priority is to attack visible enemy archons

        boolean adjacentArchonFlag = checkFlag(rc, 0, 0);

        // resets flag if the neighboring archon is gone
        if (adjacentArchonFlag && nearbyArchon == null) {
            resetFlag(rc, 0, 0);
        }

        if (nearbyArchon != null && adjacentArchonFlag) {
            // check flag to make sure another archon hasn't already set
            alternateSpawnMinerSoldier(rc, me.directionTo(nearbyArchon), numMiners, numSoldiers);
            throwFlag(rc, 0, 0); // throw a flag on 0th archon (comms array index 0)
        } else if (numMiners < initialMinersToSpawn && !adjacentArchonFlag) {
            if (safeBuild(rc, RobotType.MINER, randomSpawnDir)) {
                writeNumMiners(rc, numMiners + 1);
            }
        } else if (!adjacentArchonFlag) {
            // build soldiers unless...
            // if we sense a nearby archon or have enough soldiers
            if (numSoldiers < 2*numMiners) {
                // TODO: make smart spawn direction for soldiers
                if (safeBuild(rc, RobotType.SOLDIER, randomSpawnDir)) {
                    writeNumSoldiers(rc, numSoldiers + 1);
                }
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

        // URGENT TODO: Reset things if the alpha archon dies
        if (isAlpha) {
            alphaSendHeartbeat(rc);

            int currentTurn = rc.getRoundNum();

            if (rc.readSharedArray(7) != prevEnemySeven) {
                prevEnemySeven = rc.readSharedArray(7);
                turnsSeven = currentTurn + ROUNDS_BEFORE_WIPING_TARGET;
            }

            if (rc.readSharedArray(8) != prevEnemyEight) {
                prevEnemyEight = rc.readSharedArray(8);
                turnsEight = currentTurn + ROUNDS_BEFORE_WIPING_TARGET;
            }

            if (rc.readSharedArray(9) != prevEnemyNine) {
                prevEnemyNine = rc.readSharedArray(9);
                turnsNine = currentTurn + ROUNDS_BEFORE_WIPING_TARGET;
            }

            if (rc.readSharedArray(10) != prevEnemyTen) {
                prevEnemyTen = rc.readSharedArray(10);
                turnsTen = currentTurn + ROUNDS_BEFORE_WIPING_TARGET;
            }

            if (currentTurn == turnsSeven) {
                prevEnemySeven = 0;
                rc.writeSharedArray(7, 0);
            }

            if (currentTurn == turnsEight) {
                prevEnemyEight = 0;
                rc.writeSharedArray(8, 0);
            }

            if (currentTurn == turnsNine) {
                prevEnemyNine = 0;
                rc.writeSharedArray(9, 0);
            }

            if (currentTurn == turnsTen) {
                prevEnemyTen = 0;
                rc.writeSharedArray(10, 0);
            }
        } else { // check that the alpha is pinging
            int alphaPing = listenAlphaHeartbeat(rc);
            int roundNum = rc.getRoundNum();
            if (alphaPing != roundNum) {
                clearIndex(rc, archIndex);
                writeOwnArchLoc(rc, 0);
            }
        }
    }

    static void alternateSpawnMinerSoldier(RobotController rc, Direction dir, int numMiners, int numSoldiers) throws GameActionException {
        // alternate 2 soldiers, 1 miner
        if (soldierMinerBuildAlternator % 3 != 2) {
            if(safeBuild(rc, RobotType.SOLDIER, dir)) {
                writeNumSoldiers(rc, numSoldiers + 1);
            }
        } else {
            if (safeBuild(rc, RobotType.MINER, dir)) {
                writeNumMiners(rc, numMiners + 1);
            }
        }
        soldierMinerBuildAlternator++;
    }
}