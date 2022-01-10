package scrimv3;
import battlecode.common.*;

public class BuilderController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static boolean isBuilding = false;
    static int failedMoves = 0;
    static int totalMovesBuilder = 0;
    static int nearbyFriendlyBots = 0;
    private static final int ACCEPTABLE_RUBBLE = 25;
    static MapLocation target = null; 

    static void runBuilder(RobotController rc) throws GameActionException {
           

            //define opposite direction so that watchtower is protected!
            //Direction oppositeOfBuilder = me.directionTo(target).opposite();
            //test this
            rc.setIndicatorString("Alive?");
            MapLocation ArchonLocation = new MapLocation(me.x, me.y);

            rc.setIndicatorString("archonlocation found");
            MapLocation BuilderPartOne = new MapLocation(me.x + 10, me.y + 10);

            rc.setIndicatorString("MapLocation set");
            walkTowards(rc, BuilderPartOne);
            rc.setIndicatorString("Moved");
;            
            rc.setIndicatorString("TRY BUILDING PLEASE!");
            
       // if(rc.canBuildRobot(RobotType.WATCHTOWER, oppositeOfBuilder))
           // {
            //    rc.buildRobot(RobotType.WATCHTOWER, oppositeOfBuilder);
         //       rc.setIndicatorString("BUILDING!");
           // }
          //  else
          //  {
         //       rc.setIndicatorString("NOT BUILDING!");
           // }

        //Repair any and all watchtowers in the area
                
        RobotInfo [] ListofNearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        //Check if any of those robots are repairable and if we can repair the same watchtower multiple times
        for (RobotInfo Robot : ListofNearbyRobots)
        {
        if (rc.canRepair(Robot.getLocation()))
        {
            rc.setIndicatorString("PLZ REPAIR <3!");
            rc.repair(Robot.getLocation());
        }

        }

        //Need to formalize how many turns builder should wait first
        if(!(rc.readSharedArray(0) == 0 && totalMovesBuilder > 20)) {
            //potential edge case
            //Harvest coordinates from array that our soldiers gave us
            int xCoordDetected = rc.readSharedArray(0) / 100;
            int yCoordDetected = rc.readSharedArray(0) - (rc.readSharedArray(0) / 100) * 100;
            //Move!
            MapLocation target = new MapLocation(xCoordDetected, yCoordDetected);
            walkTowards(rc, target);
            rc.setIndicatorString("WALKING TO TARGET!");
        
        }

        totalMovesBuilder++;
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


