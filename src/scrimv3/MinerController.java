package scrimv3;
import battlecode.common.*;
import java.util.Arrays;

public class MinerController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static boolean isMining = false;
    static int failedMoves = 0;
    static int totalMovesMiner = 0;
    static int nearbyFriendlyBots = 0;

    private static final int ACCEPTABLE_RUBBLE = 25;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // Checks if there's too many Miners around and forces them to spread out. This one specifically looks for the first
        // miner in the area and moves away automatically.
/**
            if (totalMovesMiner > 14)
            {
               RobotInfo[] ListofNearbyRobots = rc.senseNearbyRobots(2, rc.getTeam());
                MapLocation FriendlyBot = null;
                for (RobotInfo Robot : ListofNearbyRobots)
                {
                    if (Robot.getTeam().equals(rc.getTeam()))
                    {
                        nearbyFriendlyBots++;
                        FriendlyBot = Robot.getLocation();
                    }

                }

                if (!(nearbyFriendlyBots == 0))
                {
                    Direction toFriendlyBot = me.directionTo(FriendlyBot);
                    Direction oppositeToFriendlyBot = toFriendlyBot.opposite();

                    //randomly choose in one of three opposite directions to move in
                    if (Util.rng.nextDouble() < 0.33) {
                        if (rc.canMove(oppositeToFriendlyBot) && !isObstacle(rc, oppositeToFriendlyBot)) {

                            rc.move(oppositeToFriendlyBot);
                            rc.setIndicatorString("MOVE AWAY!");
                            totalMovesMiner++;
                        }

                    }
                    else if (Util.rng.nextDouble() < 0.67) {
                        //Need a check that it can move;
                        rc.move(oppositeToFriendlyBot.rotateLeft());
                        rc.setIndicatorString("MOVE AWAY, ROTATED LEFT!");
                        totalMovesMiner++;
                    }
                    else {
                        //Need a check that it can move;
                        rc.move(oppositeToFriendlyBot.rotateRight());
                        rc.setIndicatorString("MOVE AWAY, ROTATED RIGHT!");
                        totalMovesMiner++;
                    }
                }
            }
*/

        // TODO: if we're on the other side of the map start just looking for lead/scouting
        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }


        // introduce some mutation probability
        if (rc.getRoundNum() >= 250 && Util.rng.nextDouble() <= 0.01) {
            travelDir = Util.directions[Util.rng.nextInt(Util.directions.length)];
        }

        if (failedMoves >= 3) {
            travelDir = (Util.rng.nextDouble() <= 0.02)
                    ? Util.directions[Util.rng.nextInt(Util.directions.length)]
                    : travelDir.opposite();
        }

        // communicate who is mining, will get rid of this if
        // TODO: once teh devs implement an overloaded senseNearbyLocationsWithLead
        // send a miner to the lead direction if there is a nearby loc with lead > 1
        // then set travelDir back to null to go back to swarm
        if (rc.getRobotCount() < (3 * rc.getArchonCount())) {
            MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
            if (nearbyLead.length > 0) {
                walkTowards(rc, nearbyLead[0]);
            }
        } else {
            if (Util.safeMove(rc, travelDir)) {
                failedMoves = 0;
            } else if (rc.getMovementCooldownTurns() == 0) {
                failedMoves++;
            }
        }

        // search for enemy archons (unoptimized)
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            for (RobotInfo enemy : enemies) {
                if (enemy.getType().equals(RobotType.ARCHON) && enemy.getHealth() >= 100) {
                    MapLocation enemyLoc = enemy.getLocation();
                    int waypointCode = enemyLoc.x * 100 + enemyLoc.y;
                    rc.writeSharedArray(0, waypointCode);
                } else if (enemy.getType().equals(RobotType.ARCHON) && enemy.getHealth() <= 100) {
                    rc.writeSharedArray(0, 0); // no more a destination
                }
            }
        }

        // mine
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
            }
        }
   }

    static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        // leave lead to regen
        while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
            isMining = true;
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
            isMining = true;
            rc.mineLead(mineLocation);
        }
        isMining = false;
    }

    // Bug0 pathing: TODO implement bug2 instead
    // Taken from https://github.com/battlecode/battlecode22-lectureplayer/blob/main/src/lectureplayer/Pathing.java
    static void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        if (!rc.isMovementReady() || currentLocation.equals(target)) { return; }

        Direction d = currentLocation.directionTo(target);
        if (rc.canMove(d) && !isObstacle(rc, d)) {
            // No obstacle in the way, so let's just go straight for it!
            rc.move(d);
            failedMoves = 0;
            travelDir = null;
        } else {
            // There is an obstacle in the way, so we're gonna have to go around it.
            if (travelDir == null) {
                // If we don't know what we're trying to do
                // make something up
                // And, what better than to pick as the direction we want to go in
                // the best direction towards the goal?
                travelDir = d;
            }
            // Now, try to actually go around the obstacle
            // Repeat 8 times to try all 8 possible directions.
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(travelDir) && !isObstacle(rc, travelDir)) {
                    rc.move(travelDir);
                    failedMoves = 0;
                    travelDir = travelDir.rotateLeft();
                    break;
                } else {
                    travelDir = travelDir.rotateRight();
                }
            }
        }
    }

    private static boolean isObstacle(RobotController rc, Direction d) throws GameActionException {
        MapLocation adjacentLocation = rc.getLocation().add(d);
        int rubbleOnLocation = rc.senseRubble(adjacentLocation);
        return rubbleOnLocation > ACCEPTABLE_RUBBLE;
    }
}
