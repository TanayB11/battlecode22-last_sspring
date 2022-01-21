package five;

import battlecode.common.*;

import static five.util.Communication.*;
import static five.util.Miscellaneous.*;
import static five.util.SafeActions.safeBuild;

// TODO: test different heuristics

public class ArchonController {
    // archon info
    static int archIndex = -1;
    static boolean isAlpha = false;
    static boolean isPrioritySpawner = false;

    // values updated regularly
    static int prevHP = Integer.MIN_VALUE;
    static int numMiners = 0;
    static int numSoldiers = 0;

    // rolling average queue for income
    static int prevTurnLead = 0;

    // determining miner spawn amounts/dirs
    static final int BASE_LEAD_WANTED = 30;
    static final int MIN_MINERS = 4;
    static int initMiners = -1;
    static int visibleLead = -1;
    static int currMinerSpawnDir = 0;
    static int[] minersInDir = {0, 0, 0, 0, 0, 0, 0, 0};

    // regularly resetting comms array goal
    static final int ROUNDS_BEFORE_WIPING_GOAL = 40;
    static int prevGoal = 0;
    static int goalExpiryDate = 0;

    public static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        numMiners = readNumMiners(rc);
        numSoldiers = readNumSoldiers(rc);

        // initialize values
        if (archIndex == -1) {
            archIndex = firstArchIndexEmpty(rc);
            writeOwnArchLoc(rc, archIndex);
            isAlpha = (archIndex == 0);
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

        if (isPrioritySpawner) {
            throwFlag(rc, 0, 0);
            prevHP = rc.getHealth(); // update prevHP

            // try to build directly to enemy if we see one, else build randomly
            MapLocation priorityEnemyLocation = priorityEnemy != null ? priorityEnemy.getLocation() : null;

            Direction dirToBuild = (
                    priorityEnemy != null &&
                    rc.canBuildRobot(RobotType.SOLDIER, me.directionTo(priorityEnemyLocation))
                ) ?
                me.directionTo(priorityEnemyLocation) :
                directions[rng.nextInt(directions.length)];

            if (safeBuild(rc, RobotType.SOLDIER, dirToBuild)) {
                writeNumSoldiers(rc, numSoldiers + 1);
            }
        } else {
            resetFlag(rc, 0, 0);
        }

        /*
        Macro strategy implementation
        Robot build order, wiping goal in comms array, sending/checking archon heartbeat
        */

        // cannot spawn if another archon is currently priority spawner
        boolean enemArchFlag = checkFlag(rc, 0, 0);
        boolean allowedToSpawn = !enemArchFlag || isPrioritySpawner; // TODO: use

        // 1. Build order implementation

        // TODO: replace with smart spawn (from archon closest to target or explore)
        Direction soldierSpawnDir = directions[rng.nextInt(directions.length)];

        // Spawn initial miners
        // proportional to visible lead in each direction
        if (numMiners < initMiners || initMiners == -1) {
            initVisibleLeadAndMinersInDir(rc, me);

            // set heuristic
            initMiners = Math.min(MIN_MINERS, rc.getMapHeight() * rc.getMapWidth() * visibleLead / 52500);

            if (visibleLead > 0) {
                spawnMinersToLead(rc, initMiners);
            } else {
                spawnMinersToLead(rc, MIN_MINERS);
            }
        } else if (safeBuild(rc, RobotType.SOLDIER, soldierSpawnDir)) {
            writeNumSoldiers(rc, numSoldiers + 1);
        } else if (numSoldiers / numMiners < 2) {
            // spawn additional miners to meet econ needs
                // definitions
                // a = \frac{1}{m} * \frac{1}{10} * \sum_{t-9}^t \mathrm{Pb}(t)
                // # of miners to spawn = l (amt of lead we want) / a
                // assuming the current round >= 10 by the time we get here
            int l = BASE_LEAD_WANTED + readLeadRequestedForBuildings(rc);
            double a = incomeAvgQ.calcAverageVal() / (double)numMiners;
            int mapArea = rc.getMapWidth() * rc.getMapHeight();
            int minersToSpawn = (int)Math.min(Math.sqrt(mapArea) * 0.5, l/a);

            initVisibleLeadAndMinersInDir(rc, me);
            if (visibleLead > 0) {
                spawnMinersToLead(rc, minersToSpawn);
            }
        } else {
            // TODO: spawn builders and sages
        }

        // 2. Regularly wipe goal in comms array, send/check heartbeat
        if (isAlpha) {
            alphaSendHeartbeat(rc);
            int currentTurn = rc.getRoundNum();

            // Update income tracker queue
            int income = rc.getTeamLeadAmount(rc.getTeam()) - prevTurnLead;
            incomeAvgQ.enqueue(income);
            prevTurnLead = income;

            // if the comms goal has updated
            if (rc.readSharedArray(7) != prevGoal) {
                prevGoal = rc.readSharedArray(7);
                goalExpiryDate = currentTurn + ROUNDS_BEFORE_WIPING_GOAL;
            }
            // if comms goal has expired
            if (currentTurn == goalExpiryDate) {
                prevGoal = 0;
                rc.writeSharedArray(7, 0);
            }
        } else { // check that the alpha is pinging
            int alphaPing = listenAlphaHeartbeat(rc);
            if (alphaPing != rc.getRoundNum()) {
                clearIndex(rc, archIndex);
                writeOwnArchLoc(rc, 0);
            }
        }
    }

    // Yes, I know this function is not pure :(
    static void initVisibleLeadAndMinersInDir(RobotController rc, MapLocation me) throws GameActionException {
        minersInDir = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        visibleLead = 0;

        // initialize array of lead in each direction
        MapLocation[] nearbyPbLocs = rc.senseNearbyLocationsWithLead();
        int leadAtLoc;
        for (int i = 0; i < nearbyPbLocs.length; i++) {
            leadAtLoc = rc.senseLead(nearbyPbLocs[i]);
            visibleLead += leadAtLoc;
            minersInDir[dirToIndex(me, nearbyPbLocs[i])] += leadAtLoc;
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
            writeNumMiners(rc, numMiners + 1);
            minersInDir[currMinerSpawnDir]--;
        } else if (minersInDir[currMinerSpawnDir] == 0) {
            currMinerSpawnDir++;
            currMinerSpawnDir %= 8; // keeps spawning in a cyclical order
        }
    }
}