package scalar;
import battlecode.common.*;
import scalar.util.Util;

public class ArchonController {
    static final int MINERS_PER_ARCHON = 12;
    static final int SOLDIERS_PER_ARCHON = 15;
    static int miners = 0, soldiers = 0, builders = 0;
    static int ArchonCurrentHealth;

    static void runArchon(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();

        int archons = rc.getArchonCount();
        miners = rc.readSharedArray(0);
        soldiers = rc.readSharedArray(1);
        builders = rc.readSharedArray(2);
        // If it can see an enemy nearby, panic and produce soldiers
        //We spawn them in our predetermined spots
        Direction defensiveSpawn = null;

        //Let's define the north/south direction as our defensive direction
        if(rc.getMapWidth() - me.y > me.y)
        {
            defensiveSpawn = Direction.NORTH;
        }
        else
        {
            defensiveSpawn = Direction.SOUTH;
        }

        //Now we spam soldiers if we see them too close
        while(rc.senseNearbyRobots(-1, rc.getTeam().opponent()) != null)
        {
            if (rc.canBuildRobot(RobotType.SAGE, defensiveSpawn))
            {
                rc.buildRobot(RobotType.SAGE, defensiveSpawn);
            }

            else if (rc.canBuildRobot(RobotType.SOLDIER, defensiveSpawn))
            {
                rc.buildRobot(RobotType.SOLDIER, defensiveSpawn);
            }
        }

        //Archon dying? Harvest that gold by spamming miners!

        //Store old health before updating it
        int oldArchonCurrentHealth = ArchonCurrentHealth;

        //Update health
        ArchonCurrentHealth = rc.getHealth();

        int rateOfHealthloss = ArchonCurrentHealth - oldArchonCurrentHealth;

        if (rateOfHealthloss > 100 && rateOfHealthloss > 10)
        {
            for(Direction dir : Direction.allDirections())
            {
                if (rc.canBuildRobot(RobotType.MINER, dir))
                {
                    rc.buildRobot(RobotType.MINER, dir);
                    rc.setIndicatorString("Damaged and spawning a miner!");
                }
            }
        }

        // TODO: erase archon when it dies, can unroll loop for bytecode if needed
        // broadcast our encoded archon locations to the shared arr
        for (int i = 6; i++ <= 9;) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, me.x * 100 + me.y);
                break;
            }
        }

        // spawn bots
        Direction dirToSpawn = Util.directions[Util.rng.nextInt(Util.directions.length)];
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        Util.broadcastEnemyArchonLocs(rc, nearbyEnemies);

        // if enemy is near us, soldier rush
        if (nearbyEnemies.length > 0) {
            if (rc.canBuildRobot(RobotType.SOLDIER, dirToSpawn)) {
                rc.buildRobot(RobotType.SOLDIER, dirToSpawn);
                soldiers++;
                rc.writeSharedArray(1, soldiers);
            }
        }

        if (miners < MINERS_PER_ARCHON * archons) {
            if (rc.canBuildRobot(RobotType.MINER, dirToSpawn)) {
                rc.buildRobot(RobotType.MINER, dirToSpawn);
                miners++;
                rc.writeSharedArray(0, miners);
            }
        } else if (soldiers < SOLDIERS_PER_ARCHON * archons){
            if (rc.canBuildRobot(RobotType.SOLDIER, dirToSpawn)) {
                rc.buildRobot(RobotType.SOLDIER, dirToSpawn);
                soldiers++;
                rc.writeSharedArray(1, soldiers);
            }
        } else {
            // if we've met the quota, just spawn randomly (temp strat)
            RobotType randomType = (Util.rng.nextInt() == 1) ? RobotType.MINER : RobotType.SOLDIER;
            if (rc.canBuildRobot(randomType, dirToSpawn)) {
                rc.buildRobot(randomType, dirToSpawn);
                if (randomType.equals(RobotType.MINER)) {
                    miners++;
                    rc.writeSharedArray(1, miners);
                } else {
                    soldiers++;
                    rc.writeSharedArray(1, soldiers);
                }
            }
        }
    }
}