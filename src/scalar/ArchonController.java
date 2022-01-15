package scalar;
import battlecode.common.*;
import scalar.util.Util;

public class ArchonController {
    static int heuristicMiners, heuristicBuilders;
    static int miners = 0, soldiers = 0, builders = 0, sages = 0;
    static int ArchonCurrentHealth;
    static int turnCount;
    static int totalLeadinRadius = 0;

    static void runArchon(RobotController rc) throws GameActionException {

        turnCount++;

        MapLocation me = rc.getLocation();

        int archons = rc.getArchonCount();
        miners = rc.readSharedArray(0);
        soldiers = rc.readSharedArray(1);
        builders = rc.readSharedArray(2);

        // If it can see an enemy nearby, panic and produce soldiers
        //We spawn them in our predetermined spots
        Direction defensiveSpawn = null;
        Direction offensiveSpawn = null;

        //Let's define the north/south direction as our defensive direction
        if (rc.getMapWidth() - me.y > me.y) {
            defensiveSpawn = Direction.NORTH;
        } else {
            defensiveSpawn = Direction.SOUTH;
        }

        //Let's define the east/west direction as our defensive direction

        if (rc.getMapHeight() - me.x > me.x) {
            offensiveSpawn = Direction.EAST;
        } else {
            offensiveSpawn = Direction.WEST;
        }

        //define some common things
        Direction dirToSpawn = Util.directions[Util.rng.nextInt(Util.directions.length)];
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        Util.broadcastEnemyArchonLocs(rc, nearbyEnemies);

        //soldier/sage rush if enemies are detected nearby.
        if (nearbyEnemies.length > 0) {
            if (rc.canBuildRobot(RobotType.SAGE, defensiveSpawn)) {
                rc.buildRobot(RobotType.SAGE, defensiveSpawn);
                sages++;
            } else if (rc.canBuildRobot(RobotType.SOLDIER, defensiveSpawn)) {
                rc.buildRobot(RobotType.SOLDIER, defensiveSpawn);
                soldiers++;
                rc.writeSharedArray(1, soldiers);
            }
        }

        //Archon dying? Harvest that gold by spamming miners!

        //Store old health before updating it
        int oldArchonCurrentHealth = ArchonCurrentHealth;

        //Update health
        ArchonCurrentHealth = rc.getHealth();

        int rateOfHealthloss = ArchonCurrentHealth - oldArchonCurrentHealth;

        if (rateOfHealthloss > 100 && rateOfHealthloss > 10) {
            if (rc.canBuildRobot(RobotType.MINER, dirToSpawn)) {
                rc.buildRobot(RobotType.MINER, dirToSpawn);
                rc.setIndicatorString("Damaged and spawning a miner!");
            }
        }

        /** TODO: if soldiers detect gold on the ground, need to tell archon to spawn miner if necessary

        /** TODO: erase archon when it dies, can unroll loop for bytecode if needed
         // broadcast our encoded archon locations to the shared arr
         for (int i = 6; i++ <= 9;) {
         if (rc.readSharedArray(i) == 0) {
         rc.writeSharedArray(i, me.x * 100 + me.y);
         break;
         }
         } */

        //Spawn in sages always!
        if (rc.canBuildRobot(RobotType.SAGE, offensiveSpawn)) {
            rc.buildRobot(RobotType.SAGE, offensiveSpawn);
        }

        //Now normal building. Use our heuristic to guide us.
        //The number of miners we build before soldiers is
        //-18 + 1/10 * MapLength + 4/3 * MapWidth - 1/80 * TOTALLEAD IN RADIUS - 1/60 * MapArea
        //need to calculate totaLeadinRadius
        heuristicMiners = (int) Math.ceil(-18 + 0.1 * rc.getMapHeight() + 1.3333 * rc.getMapWidth() - 0.0125 * totalLeadinRadius - 0.016667 * rc.getMapHeight() * rc.getMapWidth());

        // TODO: define ArchonDestruction rate to fit in the other heuristic (heuristicBuilders). This will probably be a combo of the destruction rate, map size, and turn number.

        //define the heuristics, these are filler values
        int SOMEVALUE = 3;
        int SOMEOTHERVALUE = 4;

        if (miners <= heuristicMiners * rc.getArchonCount() || miners <= 6) {
            if (rc.canBuildRobot(RobotType.MINER, dirToSpawn)) {
                rc.buildRobot(RobotType.MINER, dirToSpawn);
                miners++;
                rc.writeSharedArray(0, miners);
            }
        } else if (heuristicBuilders < SOMEVALUE) {
            if (rc.canBuildRobot(RobotType.SOLDIER, offensiveSpawn)) {
                rc.buildRobot(RobotType.SOLDIER, offensiveSpawn);
                soldiers++;
                rc.writeSharedArray(1, soldiers);
                //If we want we can do the whole BFS to reflection thing
            }
        }//Maybe to make sure we have enough soldiers make 1 soldier after every 2 builders?
         else if (heuristicBuilders < SOMEOTHERVALUE) {
            if (rc.canBuildRobot(RobotType.SOLDIER, dirToSpawn) && turnCount%3 == 0) {
                rc.buildRobot(RobotType.SOLDIER, dirToSpawn);
                soldiers++;
                rc.writeSharedArray(1, soldiers);
            }
            else if (rc.canBuildRobot(RobotType.BUILDER, dirToSpawn)) {
                rc.buildRobot(RobotType.BUILDER, dirToSpawn);
                builders++;
                rc.writeSharedArray(2, builders);
            }

            } else {
                // if we've met the quota, just spawn randomly - not sure if we should just do builders also (temp strat)
                RobotType randomType = (Util.rng.nextInt() == 1) ? RobotType.BUILDER : RobotType.SOLDIER;
                if (rc.canBuildRobot(randomType, dirToSpawn)) {
                    rc.buildRobot(randomType, dirToSpawn);
                    if (randomType.equals(RobotType.BUILDER)) {
                        builders++;
                        rc.writeSharedArray(2, builders);
                    } else {
                        soldiers++;
                        rc.writeSharedArray(1, soldiers);
                    }
                }
            }
        }
    }

