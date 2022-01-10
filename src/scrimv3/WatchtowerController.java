package scrimv3;
import javax.lang.model.util.ElementScanner6;

import battlecode.common.*;

public class WatchtowerController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static int failedMoves = 0;
    static int totalMovesWatchtower = 0;
    static int totalMovesWithoutAttack = 0;
    static MapLocation horizReflection = null;
    static MapLocation vertReflection = null;
    static MapLocation rotationalReflection = null;
    private static final int ACCEPTABLE_RUBBLE = 25;
    static int nearbyEnemyBots = 0;

    static void runWatchtower(RobotController rc) throws GameActionException {

        //if we start doing heuristics to figure out when we're gonna do watchtower strats or not, then we have to have different types of watchtowers
        //1 type is the normal "watchtower strat," the other would be more like a defensive turret or smth (for larger maps where we don't use many watchtowers in the first place)


        //we need code to make it stand up and sit down in the movement script


        // attack!
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        MapLocation toAttack = (enemies.length > 0) ? enemies[0].location : null;


        if (toAttack != null && rc.canAttack(toAttack))
        {
            rc.attack(toAttack);
            totalMovesWatchtower++;
            totalMovesWithoutAttack = 0;
        }
        else if (totalMovesWithoutAttack >= 10) //10 turns is subject to change
        {

            if (!(enemies == null)) {
                int coordXtarget = rc.readSharedArray(0)/100;
                int coordYtarget = rc.readSharedArray(0) - 100*(rc.readSharedArray(0)/100);

                //Move!
                if (rc.canTransform() && rc.getMode() == RobotMode.TURRET) {
                    MapLocation target = new MapLocation(coordXtarget, coordYtarget);
                    walkTowards(rc, target);
                    rc.setIndicatorString("WALKING TO TARGET!");
                    //move a bit until it can see an enemy.
                }
                if (nearbyEnemyBots>0 && rc.canTransform() && rc.getMode() == RobotMode.PORTABLE)
                {
                    rc.transform();
                }
            }
        }
        else
        {
            totalMovesWithoutAttack++;
        }
    }



    //Copied from MinerController
    // Bug0 pathing: TODO implement bug2 instead
    // Taken from https://github.com/battlecode/battlecode22-lectureplayer/blob/main/src/lectureplayer/Pathing.java
    static void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        if (!rc.isMovementReady() || currentLocation.equals(target)) {
            return;
        }

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

