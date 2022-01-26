package baymax_seven;

import battlecode.common.*;
import baymax_seven.util.pathfinding.BFS;
import baymax_seven.util.pathfinding.DroidBFS;

import java.util.Arrays;
import java.util.Comparator;

import static baymax_seven.util.Communication.*;
import static baymax_seven.util.Miscellaneous.*;
import static baymax_seven.util.SafeActions.safeBuild;


public class ArchonController {
    // archon info
    static int archIndex;
    static boolean isAlpha = false;
    static boolean isPrioritySpawner = false;


    // archon movement
    static BFS bfs = null;
    static MapLocation bestArchLoc = null;
    static int turnsSincePortable = 0;

    // values updated regularly
    static int prevHP = Integer.MIN_VALUE;
    static final int MIN_MINERS = 4;

    // rolling average queue for income
    static int prevTurnLead = 0;
    static int enemyPrevTurnLead = 0;
    static boolean isInitialized = false;

    // determine directions to spawn miners
    static int visibleLead = -1;
    static int currMinerSpawnDir = 0;
    static int[] minersInDir = {0, 0, 0, 0, 0, 0, 0, 0};

    static int prevTarget = 0, turnsSinceEnemyEstablished = 0;

    // regularly resetting comms array goal
    static final int ROUNDS_BEFORE_WIPING_TARGET = 40;
    static MapLocation goal = null;

    public static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        goal = getGoal(rc);

        // TODO: 50 turn time limit for transformation
        // FIX TRANSFORMATION on chess

        /*
        Initialization
        */

        // initialize
        archonReport(rc);
        isAlpha = (readArchonCount(rc) == rc.getArchonCount());

        if (!isInitialized) {
            bfs = new DroidBFS(rc);
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

        // 1. If we're on too high of a rubble square, move to nearest acceptable location
        if (bestArchLoc == null) {
            bestArchLoc = getBestBuildingLoc(rc);
        }

        if (bestArchLoc != null && me.equals(bestArchLoc)) {
            if (rc.isTransformReady() && rc.getMode().equals(RobotMode.PORTABLE)) {
                rc.transform();
                bestArchLoc = null;
            }
        }

        // 2. If we see an enemy archon, soldier rush
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

        // 3. If we're losing health or if we see an enemy archon, throw flag
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

        // Build order
//        int initLabHeuristic = Math.max(MIN_MINERS, rc.getMapWidth() * rc.getMapHeight() / 375);
        boolean spawnedSuccessfully = false;
        Direction randomDir = directions[rng.nextInt(directions.length)];

        if (numMiners < MIN_MINERS) {
            initVisibleLeadAndMinersInDir(rc, me);
            if (spawnMinersToLead(rc, MIN_MINERS)) {
                spawnedSuccessfully = true;
                minerReport(rc);
            }

            if (!spawnedSuccessfully) {
                if (safeBuild(rc, RobotType.MINER, randomDir)) {
                    spawnedSuccessfully = true;
                    minerReport(rc);
                }
            }
        } else if (readNumBuilders(rc) == 0 && (rc.getRobotCount() - rc.getArchonCount()) < MIN_MINERS + 1) {
            if (safeBuild(rc, RobotType.BUILDER, randomDir)) {
                spawnedSuccessfully = true;
            }
        } else if (readNumLabs(rc) > 0) {
            if (smartSpawnAtkUnit(rc, me, RobotType.SAGE)) {
                rc.setIndicatorString("SPAWN A SAGE");
                spawnedSuccessfully = true;
            } else if (numMiners < 3.5 * MIN_MINERS) {
                rc.setIndicatorString("NOT ENOUGH MINRES SPAWNING MINER YAY");
                if (numMiners % rc.getArchonCount() == (archIndex % rc.getArchonCount())) {
                    if(safeBuild(rc, RobotType.MINER, randomDir)){
                        minerReport(rc);
                        spawnedSuccessfully = true;
                    }
                }
            } else if (liveNumMiners(rc) < 3 * MIN_MINERS) {
                rc.setIndicatorString("NOT ENOUGH MINRES SPAWNING MINER YAY");
                if (numMiners % rc.getArchonCount() == (archIndex % rc.getArchonCount())) {
                    if(safeBuild(rc, RobotType.MINER, randomDir)){
                        minerReport(rc);
                        spawnedSuccessfully = true;
                    }
                }
            } else if (numSoldiers < MIN_MINERS) {
                rc.setIndicatorString("NOT ENOUGH SOLDIERS");
                if (smartSpawnAtkUnit(rc, me, RobotType.SOLDIER)) {
                    soldierReport(rc);
                    spawnedSuccessfully = true;
                }
            } else if (rc.getMapWidth() > 32 && rc.getMapHeight() > 32) {
                if (readNumLabs(rc) > ((numSoldiers - 3) / 13)+1) {
                    rc.setIndicatorString("PLENTY OF LABS");
                    if (numMiners < numSoldiers) {
                        if (numMiners % rc.getArchonCount() == (archIndex % rc.getArchonCount())) {
                            if(safeBuild(rc, RobotType.MINER, randomDir)){
                                minerReport(rc);
                                spawnedSuccessfully = true;
                            }
                        }
                    } else {
                        rc.setIndicatorString("SOLDIER IG");
                        if (smartSpawnAtkUnit(rc, me, RobotType.SOLDIER)) {
                            soldierReport(rc);
                            spawnedSuccessfully = true;
                        }
                    }
                }
            } else {
                if (readNumLabs(rc) > ((numSoldiers - 3) / 26)+1) {
                    rc.setIndicatorString("PLENTY OF LABS");
                    if (numMiners < numSoldiers) {
                        if (numMiners % rc.getArchonCount() == (archIndex % rc.getArchonCount())) {
                            if(safeBuild(rc, RobotType.MINER, randomDir)){
                                minerReport(rc);
                                spawnedSuccessfully = true;
                            }
                        }
                    } else {
                        rc.setIndicatorString("SOLDIER IG");
                        if (smartSpawnAtkUnit(rc, me, RobotType.SOLDIER)) {
                            soldierReport(rc);
                            spawnedSuccessfully = true;
                        }
                    }
                }
            }
        }


//        if (numMiners < initLabHeuristic) {
//            initVisibleLeadAndMinersInDir(rc, me);
//            spawnedSuccessfully = spawnMinersToLead(rc, initLabHeuristic);
//            if (!spawnedSuccessfully) {
//                if (safeBuild(rc, RobotType.MINER, randomDir)) {
//                    spawnedSuccessfully = true;
//                }
//            }
//        } else if (smartSpawnAtkUnit(rc, me, RobotType.SAGE)) {
//            spawnedSuccessfully = true;
//        } else if (readNumBuilders(rc) == 0 && (rc.getRobotCount() - rc.getArchonCount()) < numMiners + 1) {
//            if (safeBuild(rc, RobotType.BUILDER, randomDir)) {
//                spawnedSuccessfully = true;
//            }
//        } else if (bestArchLoc != null && !me.equals(bestArchLoc)) {
//            if (rc.getMode().equals(RobotMode.TURRET)) {
//                if (rc.isTransformReady()) {
//                    rc.transform();
//                }
//            } else {
//                bfs.move(bestArchLoc);
//            }
//        } else if (readNumLabs(rc) > ((double) rc.getRoundNum() / 2000) * initLabHeuristic) {
//            if (numMiners < numSoldiers) {
//                if (numMiners % rc.getArchonCount() == (archIndex % rc.getArchonCount()) && safeBuild(rc, RobotType.MINER, randomDir)) {
//                    spawnedSuccessfully = true;
//                }
//            } else {
//                if (smartSpawnAtkUnit(rc, me, RobotType.SOLDIER)) {
//                    spawnedSuccessfully = true;
//                }
//            }
//        }

        // healing if we couldn't spawn anything this turn
        if (!spawnedSuccessfully) {
            healNearbyAllies(rc);
        }

        /*
        Alpha archon responsibilities:
        Clear troop counts, update average income queue
        Goal expiry handling
        */

        if (isAlpha) {
            clearIndex(rc, ARCHON_COUNT_INDEX);
            clearIndex(rc, BUILDER_COUNT_INDEX);
            clearIndex(rc, SAGE_COUNT_INDEX);
            clearIndex(rc, ALIVE_MINER_COUNT);

            // Update income tracker queue
            int income = rc.getTeamLeadAmount(rc.getTeam()) - prevTurnLead;
            incomeAvgQ.enqueue(income);
            prevTurnLead = income;

            int enemyIncome = rc.getTeamLeadAmount(rc.getTeam()) - prevTurnLead;
            enemyIncomeAvgQ.enqueue(income);
            enemyPrevTurnLead = income;

            int currentTurn = rc.getRoundNum();
            if (rc.readSharedArray(TARGET_ENEMY_INDEX) != prevTarget) {
                prevTarget = rc.readSharedArray(TARGET_ENEMY_INDEX);
                turnsSinceEnemyEstablished = currentTurn + ROUNDS_BEFORE_WIPING_TARGET;
            }
            if (currentTurn == turnsSinceEnemyEstablished) {
                prevTarget = 0;
                rc.writeSharedArray(TARGET_ENEMY_INDEX, 0);
            }
        }
    }

    private static boolean smartSpawnAtkUnit(RobotController rc, MapLocation me, RobotType type) throws GameActionException {
        rc.setIndicatorString("SPAWNING A THINGY AT " + type.toString());
        int teamResource = (type.equals(RobotType.SAGE)) ? rc.getTeamGoldAmount(rc.getTeam()) : rc.getTeamLeadAmount(rc.getTeam());

        int cost = (type.equals(RobotType.SAGE)) ? type.buildCostGold : type.buildCostLead;

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
        if (teamResource < 2 * cost) {
            // spawn 1 unit from closest arch
            if (archs[0].equals(me)) {
                return safeBuild(rc, type, dirToSpawn);
            }
        } else if (teamResource < 3 * cost) {
            // spawn 2 units from 2 closest archs
            if (
                    (archs[0] != null && archs[0].equals(me)) ||
                            (archs[1] != null && archs[1].equals(me))
            ) {
                return safeBuild(rc, type, dirToSpawn);
            }
        } else if (teamResource < 4 * cost) {
            // spawn 3 units from 3 closest archs
            if (
                    (archs[0] != null && archs[0].equals(me)) ||
                            (archs[1] != null && archs[1].equals(me)) ||
                            (archs[2] != null && archs[2].equals(me))
            ) {
                return safeBuild(rc, type, dirToSpawn);
            }
        }
        // spawn 4 units from all archs
        return safeBuild(rc, type, dirToSpawn);
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

    static boolean spawnMinersToLead(RobotController rc, int numMinersToSpawn) throws GameActionException {
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
            return true;
        } else if (minersInDir[currMinerSpawnDir] == 0) {
            currMinerSpawnDir++;
            currMinerSpawnDir %= 8; // keeps spawning in a cyclical order
        }
        return false;
    }

    static void healNearbyAllies(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (nearbyAllies.length > 0) {
            Arrays.sort(nearbyAllies, Comparator.comparingInt(bot -> {
                try {
                    return bot.getHealth();
                } catch (Exception e) {
                    // prioritize highest HP robots
                    return Integer.MIN_VALUE;
                }
            }));

            for (int i = nearbyAllies.length - 1; i > 0; i--) {
                if (
                        (nearbyAllies[i].getType().equals(RobotType.SOLDIER) &&
                                nearbyAllies[i].getHealth() < .92*RobotType.SOLDIER.health) || (nearbyAllies[i].getType().equals(RobotType.SAGE) &&
                                nearbyAllies[i].getHealth() < .91*RobotType.SAGE.health)
                ) {
                    rc.setIndicatorString("Hello, I'm Baymax, your personal healthcare companion!");
                    if (rc.canRepair(nearbyAllies[i].getLocation())) {
                        rc.repair(nearbyAllies[i].getLocation());
                    }
                }
            }
        }
    }
}