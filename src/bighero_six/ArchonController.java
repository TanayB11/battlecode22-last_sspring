package bighero_six;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

import static bighero_six.util.Communication.*;
import static bighero_six.util.Miscellaneous.*;
import static bighero_six.util.SafeActions.safeBuild;


public class ArchonController {
    // archon info
    static int archIndex;
    static boolean isAlpha = false;
    static boolean isPrioritySpawner = false;

    // values updated regularly
    static int prevHP = Integer.MIN_VALUE;
    static final int MIN_MINERS = 3;

    // rolling average queue for income
    static int prevTurnLead = 0;
    static int enemyPrevTurnLead = 0;
    static boolean isInitialized = false;

    // for build order
    static final int SOLDIER_COST = RobotType.SOLDIER.buildCostLead;

    // determine directions to spawn miners
    static int visibleLead = -1;
    static int currMinerSpawnDir = 0;
    static int[] minersInDir = {0, 0, 0, 0, 0, 0, 0, 0};

    // regularly resetting comms array goal
    static final int ROUNDS_BEFORE_WIPING_GOAL = 40;
    static final int SOLDIER_THRESHOLD = 25;
    static MapLocation prevGoal = null;
    static MapLocation goal = null;
    static int goalExpiryDate = 0;

    public static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        /*
        Initialization
        */

        // initialize
        archonReport(rc);
        isAlpha = (readArchonCount(rc) == rc.getArchonCount());

        if (!isInitialized) {
            archIndex = firstArchIndexEmpty(rc);
            writeOwnArchLoc(rc, archIndex);
            isInitialized = true;
        }

        int numMiners = readNumMiners(rc);
        int numSoldiers = readNumSoldiers(rc);

        if (prevHP == Integer.MIN_VALUE) {
            prevHP = rc.getHealth();
        }

        /*
        General strategy implementation
        Soldier rush visible enemy archons
        */

        // 1. If we see an enemy archon, soldier rush
        //    Also report highest priority enemy visible
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo priorityEnemy = null;
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                RobotType enemyType = nearbyEnemies[i].getType();
                if (enemyType.equals(RobotType.ARCHON)) {
                    priorityEnemy = nearbyEnemies[i];
                    break;
                } else if (
                    priorityEnemy == null ||
                    getReportEnemyPriority(enemyType) > getReportEnemyPriority(priorityEnemy.getType())
                ) {
                    priorityEnemy = nearbyEnemies[i];
                }
            }
            reportEnemy(rc, priorityEnemy.getType(), priorityEnemy.getLocation());
        }

        // 2. If we're losing health or if we see an enemy archon, throw flag
        isPrioritySpawner = rc.getHealth() < prevHP ||
            (priorityEnemy != null && priorityEnemy.getType().equals(RobotType.ARCHON));

        // Spawns towards enemies
        if (isPrioritySpawner) {
            throwFlag(rc, 0);
            prevHP = rc.getHealth(); // update prevHP

            // try to build directly to enemy if we see one, else build randomly
            MapLocation priorityEnemyLocation = priorityEnemy != null ? priorityEnemy.getLocation() : null;

            if (numMiners < MIN_MINERS) {
                initVisibleLeadAndMinersInDir(rc, me);
                spawnMinersToLead(rc, MIN_MINERS);
            } else {
                Direction dirToBuild = (
                    priorityEnemy != null &&
                    rc.canBuildRobot(RobotType.SOLDIER, me.directionTo(priorityEnemyLocation))
                ) ?
                me.directionTo(priorityEnemyLocation) :
                directions[rng.nextInt(directions.length)];
                safeBuild(rc, RobotType.SOLDIER, dirToBuild);
            }
        } else {
            resetFlag(rc, 0);
        }

        /*
        Macro strategy implementation:
        Robot build order, healing
        */

        // Healing
//        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
//        if (nearbyAllies.length > 0) {
//            Arrays.sort(nearbyAllies, Comparator.comparingInt(rbt -> {
//                try {
//                    return rbt.getHealth();
//                } catch (Exception e) {
//                    // prioritize highest HP robots
//                    return Integer.MIN_VALUE;
//                }
//            }));
//            for (int i = nearbyAllies.length - 1; i > 0; i--) {
//                if (
//                    nearbyAllies[i].getType().equals(RobotType.SOLDIER) &&
//                    nearbyAllies[i].getHealth() < .92*RobotType.SOLDIER.health
//                ) {
//                    if (rc.canRepair(nearbyAllies[i].getLocation())) {
//                        rc.repair(nearbyAllies[i].getLocation());
//                    }
//                }
//            }
//        }

        // Build order
        int initMinersHeuristic = Math.max(MIN_MINERS, rc.getMapWidth() * rc.getMapHeight() / 375);
        if (numMiners < initMinersHeuristic) {
            initVisibleLeadAndMinersInDir(rc, me);
            spawnMinersToLead(rc, initMinersHeuristic);
        } else if (numSoldiers < initMinersHeuristic) {
            smartSpawnSoldier(rc, me);
        } else if (numSoldiers < SOLDIER_THRESHOLD) {
            if (rc.getRoundNum() % 3 != 2) {
                smartSpawnSoldier(rc, me);
            } else {
                spawnMinersToLead(rc, 1);
            }
        } else {
            if (rc.getRoundNum() % 4 != 3) {
                smartSpawnSoldier(rc, me);
            } else {
                spawnMinersToLead(rc, 1);
            }
        }

        // Spawn soldiers (old)

        // TODO: use the below code for non-soldier-miner, saving up
        // determine # of miners to spawn a soldier every 5 turns
        // we always want to meet this production rate
        // miners * Pb_avg/miner-t = 15Pb/t = 75Pb/5t -> miners = 15Pb/t * miner-t/Pb_avg
//        int minersToMaintainSoldierEq = (int)(15 * rc.getMapWidth() * rc.getMapHeight() / 3600 / incomeAvgQ.calcAverageVal());
//
//        // then add onto that whatever else we want
//        // TODO: change to make this variable, based on some strategy
//        int leadDesiredPerTurn = 180 / 10; // say we want 180 lead over the next 10 turns
//        int addtlMiners = (int)(leadDesiredPerTurn / incomeAvgQ.calcAverageVal());
//
//        if (numMiners < minersToMaintainSoldierEq + addtlMiners) {
//            initVisibleLeadAndMinersInDir(rc, me);
//            spawnMinersToLead(rc, initMinersHeuristic);
//        } else if (rc.getTeamLeadAmount(rc.getTeam()) > 180 + 75) { // TODO: don't hardcode (75 is soldier build cost)
//            safeBuild(rc, RobotType.SOLDIER, directions[rng.nextInt(directions.length)]);
//        } // rest of lead goes into stockpile

        /*
        Alpha archon responsibilities:
        Clear troop counts, update average income queue
        Goal expiry handling
        */

        // TODO: fix income average and comm the value
//        if (archIndex == 0) {
//        }

        if (isAlpha) {
            clearIndex(rc, ARCHON_COUNT_INDEX);
            clearIndex(rc, MINER_COUNT_INDEX);
            clearIndex(rc, SOLDIER_COUNT_INDEX);

//            // expire archon locations
//            clearIndex(rc, 0);
//            clearIndex(rc, 1);
//            clearIndex(rc, 2);
//            clearIndex(rc, 3);

            int currentTurn = rc.getRoundNum();

            // Update income tracker queue
            int income = rc.getTeamLeadAmount(rc.getTeam()) - prevTurnLead;
            incomeAvgQ.enqueue(income);
            prevTurnLead = income;

            int enemyIncome = rc.getTeamLeadAmount(rc.getTeam()) - prevTurnLead;
            enemyIncomeAvgQ.enqueue(income);
            enemyPrevTurnLead = income;

            goal = getGoal(rc);
            // if the comms goal has updated
            if (goal != null && !goal.equals(prevGoal)) {
                prevGoal = goal;
                goalExpiryDate = currentTurn + ROUNDS_BEFORE_WIPING_GOAL;
            }

            // if comms goal has expired
            if (currentTurn == goalExpiryDate) {
                prevGoal = null;
                clearIndex(rc, TARGET_ENEMY_INDEX);
            }
        }
    }

    private static boolean smartSpawnSoldier(RobotController rc, MapLocation me) throws GameActionException {
        int teamLead = rc.getTeamLeadAmount(rc.getTeam());

        MapLocation[] archs = new MapLocation[]{
                readArchLoc(rc, 0),
                readArchLoc(rc, 1),
                readArchLoc(rc, 2),
                readArchLoc(rc, 3),
        };

        // get archons in order of distance to target
        Arrays.sort(archs, Comparator.comparingInt(loc -> {
            try {
                return loc.distanceSquaredTo(goal);
            } catch (Exception e) {
                // nonexistent archon, infinitely far away
                return Integer.MAX_VALUE;
            }
        }));

        Direction dirToSpawn = goal != null ? me.directionTo(goal) : directions[rng.nextInt(directions.length)];
        if (teamLead < 2 * SOLDIER_COST) {
            // spawn 1 soldier from closest arch
            if (archs[0].equals(me)) {
                return safeBuild(rc, RobotType.SOLDIER, dirToSpawn);
            }
        } else if (teamLead < 3 * SOLDIER_COST) {
            // spawn 2 soldier from 2 closest archs
            if (archs[0].equals(me) || archs[1].equals(me)) {
                return safeBuild(rc, RobotType.SOLDIER, dirToSpawn);
            }
        } else if (teamLead < 4 * SOLDIER_COST) {
            // spawn 3 soldier from 3 closest archs
            if (!archs[3].equals(me)) {
                return safeBuild(rc, RobotType.SOLDIER, dirToSpawn);
            }
        }
        // spawn 4 soldiers from all archs
        return safeBuild(rc, RobotType.SOLDIER, dirToSpawn);
    }

    /*
    Helper functions for spawning miners
    */

    // Yes, I know this function is not pure :(
    static void initVisibleLeadAndMinersInDir(RobotController rc, MapLocation me) throws GameActionException {
        minersInDir = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        visibleLead = 0;

        // initialize array of lead in each direction
        MapLocation[] nearbyPbLocs = rc.senseNearbyLocationsWithLead();
        int leadAtLoc;
        if (nearbyPbLocs.length > 0){
            for (int i = 0; i < nearbyPbLocs.length; i++) {
                leadAtLoc = rc.senseLead(nearbyPbLocs[i]);
                visibleLead += leadAtLoc;
                minersInDir[dirToIndex(me, nearbyPbLocs[i])] += leadAtLoc;
            }
        }
    }

    static void spawnMinersToLead(RobotController rc, int numMinersToSpawn) throws GameActionException {
        // assign # of miners in each dir proportional to amt of lead
        if (visibleLead > 0) {
            for (int i = 0; i < minersInDir.length; i++) {
                if (minersInDir[i] != 0) {
                    minersInDir[i] = 1 + numMinersToSpawn * (minersInDir[i] / visibleLead);
                }
            }
        } else {
            // no visible lead, pick random direction (changing numMinersToSpawn is up to the calling method)
            minersInDir[rng.nextInt(minersInDir.length)] = numMinersToSpawn;
        }

        // spawn the miners
        if (minersInDir[currMinerSpawnDir] > 0 && safeBuild(rc, RobotType.MINER, directions[currMinerSpawnDir])) {
            minersInDir[currMinerSpawnDir]--;
        } else if (minersInDir[currMinerSpawnDir] == 0) {
            currMinerSpawnDir++;
            currMinerSpawnDir %= 8; // keeps spawning in a cyclical order
        }
    }
}