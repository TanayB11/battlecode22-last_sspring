package bighero_six;

import battlecode.common.*;

import static bighero_six.util.Communication.*;
import static bighero_six.util.Miscellaneous.*;
import static bighero_six.util.SafeActions.safeBuild;

// TODO: rewrite completely
public class ArchonController {
    static boolean isAlpha = false, isPrioritySpawner = false;
    static boolean underAttack = false;
    static int prevHP = Integer.MIN_VALUE;

    static int buildPhase = -1; // 0 initial miners, 1 soldiers, 2 extra miners
    static int archIndex = -1;
    static int prevTarget = 0, turnsSinceEnemyEstablished = 0;
    static int soldierMinerBuildAlternator = 0;

    // determining miner spawn dirs
    static int initialMinersToSpawn = -1;
    static int totalLead = -1, currSpawnDir = 0;
    static int[] minersInDir = {0, 0, 0, 0, 0, 0, 0, 0};

    // can be tuned
    static final int MINER_HEURISTIC_K = 450;
    static final int ROUNDS_BEFORE_WIPING_TARGET = 40;
    static final int BASE_MINERS = 3;

    public static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        if (archIndex == -1) { // initialize
            archIndex = firstArchIndexEmpty(rc);
            writeOwnArchLoc(rc, archIndex);
            prevHP = rc.getHealth();
            isAlpha = (archIndex == 0);
            buildPhase = 0;
        }

        // if we see an archon, soldier rush
        int numMiners = readNumMiners(rc);
        int numSoldiers = readNumSoldiers(rc);

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo priorityEnemy = null;
        if (nearbyEnemies.length > 0) {
            for (RobotInfo enemy : nearbyEnemies) {
                RobotType enemType = enemy.getType();
                // for archons, we care if enemy archons are nearby
                if (enemy.getType().equals(RobotType.ARCHON)) {
                    priorityEnemy = enemy;
                    break;
                } else if (priorityEnemy == null ||
                        getReportEnemyPriority(enemType) > getReportEnemyPriority(priorityEnemy.getType())
                ) {
                    priorityEnemy = enemy;
                }
            }
            reportEnemy(rc, priorityEnemy.getType(), priorityEnemy.getLocation());
        }

        isPrioritySpawner = rc.getHealth() < prevHP ||
            priorityEnemy != null && priorityEnemy.getType().equals(RobotType.ARCHON);

        if (isPrioritySpawner) {
            throwFlag(rc, 0, 0);
            Direction dirToBuild = (priorityEnemy != null) ?
                    me.directionTo(priorityEnemy.getLocation()) : directions[rng.nextInt(directions.length)];
            safeBuild(rc, RobotType.SOLDIER, dirToBuild);
            prevHP = rc.getHealth();
        } else {
            resetFlag(rc, 0, 0);
        }

        // does one of our archons have an enemy archon nearby?
        boolean enemArchFlag = checkFlag(rc, 0, 0);
        boolean allowedToSpawn = !enemArchFlag || isPrioritySpawner;

        if (buildPhase == 0 && allowedToSpawn) { // init miners
            rc.setIndicatorString("IN PHASE 0");
            if (totalLead == -1) { initMinerSpawns(rc, me); }

            if (minersInDir[currSpawnDir] > 0) {
//                System.out.println(rc.getID() + " " + currSpawnDir +" " + minersInDir[currSpawnDir]);
                if (safeBuild(rc, RobotType.MINER, directions[currSpawnDir])) {
                    minersInDir[currSpawnDir]--;
                }
            } else {
                currSpawnDir++;
            }

            if (currSpawnDir == 7) {
                buildPhase++;
            }
        } else if (buildPhase == 1 && allowedToSpawn && rc.getRoundNum()%rc.getArchonCount()==archIndex) { // soldiers
            rc.setIndicatorString("IN PHASE 1");
            if (numSoldiers < 2*numMiners && numMiners > BASE_MINERS * rc.getArchonCount()) {
                Direction randomSpawnDir = directions[rng.nextInt(directions.length)];
                safeBuild(rc, RobotType.SOLDIER, randomSpawnDir);
            } else {
                safeBuild(rc, RobotType.MINER, directions[rng.nextInt(directions.length)]);
            }
        }

        if (rc.getHealth() <= 60) { throwFlag(rc, archIndex, 3); }

        if (isAlpha) {
            alphaSendHeartbeat(rc);
            int currentTurn = rc.getRoundNum();
            if (rc.readSharedArray(7) != prevTarget) {
                prevTarget = rc.readSharedArray(7);
                turnsSinceEnemyEstablished = currentTurn + ROUNDS_BEFORE_WIPING_TARGET;
            }
            if (currentTurn == turnsSinceEnemyEstablished) {
                prevTarget = 0;
                rc.writeSharedArray(7, 0);
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

    private static void initMinerSpawns(RobotController rc, MapLocation me) throws GameActionException {
        totalLead = 0;
        int leadAtLoc;
        MapLocation[] nearbyLeadLocs = rc.senseNearbyLocationsWithLead();
        for (MapLocation loc : nearbyLeadLocs) {
            leadAtLoc = rc.senseLead(loc);
            totalLead += leadAtLoc;
            minersInDir[dirToIndex(me, loc)] += leadAtLoc;
        }

        // set heuristic
        initialMinersToSpawn = Math.min(4, rc.getMapHeight() * rc.getMapWidth() * totalLead / 52500);

        // leadInDir becomes miners to spawn in each direction
        // send # of miners in each dir proportional to amt of lead
        if (totalLead != 0) {
            for (int i = 0; i < minersInDir.length; i++) {
                if (minersInDir[i] != 0) {
                    minersInDir[i] = 1 + initialMinersToSpawn * minersInDir[i] / totalLead;
//                    System.out.println(rc.getID() + " " + i + " " + minersInDir[i] + " " + totalLead);
                }
            }
        } else {
            initialMinersToSpawn = BASE_MINERS;
            minersInDir[0] = initialMinersToSpawn;
        }
    }

    static void alternateSpawnMinerSoldier(RobotController rc, Direction dir, int numMiners, int numSoldiers) throws GameActionException {
        // alternate 2 soldiers, 1 miner
        if (soldierMinerBuildAlternator % 3 != 2) {
            safeBuild(rc, RobotType.SOLDIER, dir);
        } else {
            safeBuild(rc, RobotType.MINER, dir);
        }
        soldierMinerBuildAlternator++;
    }
}