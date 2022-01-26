package baymax_seven_old;

import battlecode.common.*;
import baymax_seven_old.util.pathfinding.BFS;
import baymax_seven_old.util.pathfinding.DroidBFS;

import static baymax_seven_old.util.Communication.reportEnemy;
import static baymax_seven_old.util.SafeActions.safeBuild;

public class BuilderController {

    static BFS bfs = null;
    static boolean wantMoreLabs = false;
    static MapLocation target = null;
    static MapLocation homeArchonLoc = null;
    static MapLocation[] rubbleinArea = null;

    static void runBuilder(RobotController rc) throws GameActionException {

        MapLocation me = rc.getLocation();

        //If any enemies detected, report
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            for (int i = 0; i < nearbyEnemies.length; i++) {
                reportEnemy(rc, nearbyEnemies[i].getType(), nearbyEnemies[i].getLocation());
            }
        }

        //Send robot ID to the comms array for unique identification.
        //TODO: replace placeholder based on COMMS. RobotID won't just equal 0 and 1 and 2


        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getLocation(), -1, rc.getTeam());

        //Get Archon Location if one is not already existed
        if (homeArchonLoc.equals(null)) {

            for (RobotInfo Robot : nearbyRobots) {

                if (Robot.getType() == RobotType.ARCHON) {

                    homeArchonLoc = new MapLocation(Robot.getLocation().x, Robot.getLocation().y);
                }
            }
        }

        //TODO: NOTE THAT THIS Assumes we're only making labs so far (accounts for the two, one on each pair of edges)

        wantMoreLabs = true;

        //1 = Archon should save lead for a single lab, else not.

        //TODO: CHECK INDEX AVAILABILITY
        if (rc.readSharedArray(10) == 1) {
        } else if (wantMoreLabs) {
            rc.writeSharedArray(10, 1);
        }

        //First build the first lab near the archon.

        //Decide where we want them to move towards.
        //Below is for builders that take care of labs

        int distToNorthWall = rc.getMapHeight() - homeArchonLoc.y;
        int distToSouthWall = homeArchonLoc.y;
        int distToEastWall = rc.getMapWidth() - homeArchonLoc.x;
        int distToWestWall = homeArchonLoc.x;

        //Based on this, we decide on the two walls to check out
        //Edge cases: Archon against a wall or corner

        //Set the closest lab to be priority

        //First Builder goes to whichever target is first

        //Based on this, we decide on the two walls to check out
        //Edge cases: Archon against a wall or corner

        //First lab location if one hasn't already been set yet and this is Builder #1
        MapLocation initTarget = null;

        int RobotIDFromArrayOnceThatsSetUp = -1;

        //PART 1: TARGETS SET

        //This lab is the one that goes on the diagonal
        if (3 <= me.x && me.x <= rc.getMapWidth() - 3 && 3 <= me.y && me.y <= rc.getMapHeight()) {
            // First lab location if one hasn't already been set yet and this is Builder #1
            if (RobotIDFromArrayOnceThatsSetUp == 0 && target == null) {

                if (distToNorthWall >= distToSouthWall && distToWestWall >= distToEastWall) {
                    //SE
                    target = new MapLocation(me.x + 2, me.y - 2);
                } else if (distToNorthWall >= distToSouthWall && distToEastWall >= distToWestWall) {
                    //SW
                    target = new MapLocation(me.x - 2, me.y - 2);
                } else if (distToSouthWall >= distToNorthWall && distToWestWall >= distToEastWall) {
                    //NE
                    target = new MapLocation(me.x + 2, me.y + 2);
                } else {
                    //NW
                    target = new MapLocation(me.x - 2, me.y + 2);
                }
            }
        }

        else {
            //TODO: Find alternate first location near the archon to build
        }

        // Second lab location if one hasn't already been set yet and this is Builder #2

        if (RobotIDFromArrayOnceThatsSetUp == 1 && target == null) {

            if (distToNorthWall >= distToSouthWall && distToSouthWall > 0) {
                initTarget = new MapLocation(homeArchonLoc.x, 0);
                target = initTarget;
            } else if (distToSouthWall >= distToNorthWall && distToNorthWall > 0) {
                initTarget = new MapLocation(homeArchonLoc.x, rc.getMapHeight());
                target = initTarget;
            }
        }

        // Third lab location if one hasn't already been set yet and this is Builder #3

        else if (RobotIDFromArrayOnceThatsSetUp == 2 && target == null) {

            if (distToWestWall >= distToEastWall && distToEastWall > 0) {
                initTarget = new MapLocation(rc.getMapWidth(), homeArchonLoc.y);
                target = initTarget;
            } else if (distToEastWall >= distToWestWall && distToWestWall > 0) {
                initTarget = new MapLocation(0, homeArchonLoc.y);
                target = initTarget;
            }
        }

        //Initialize and move to priority spot
        //set goals to whatever our adjusted location is for these first three builders

        bfs = new DroidBFS(rc);
        Direction dirAdjustedLab = null;

 if (RobotIDFromArrayOnceThatsSetUp == 0 || RobotIDFromArrayOnceThatsSetUp == 1 || RobotIDFromArrayOnceThatsSetUp == 2) {
     bfs.move(target);

     //search around once in area
     //NOTE: ONLY TARGET = initialTARGET
     if (rc.getLocation().equals(initTarget)) {

         //Once we're there, check the area for a new target with lower rubble
         //TODO: Hope this doesn't break because some spots out of bounds?
         rubbleinArea = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), -1);

         //This is a temporary MapLocation that will change in the for loop
         MapLocation lowestRubble = rc.getLocation();

         //determine spot with lowest rubble by swapping
         for (MapLocation rubbleLoc : rubbleinArea) {
             if (rc.senseRubble(lowestRubble) > rc.senseRubble(rubbleLoc)) {
                 lowestRubble = rubbleLoc;
             }
         }

         //Set target to lowestRubble
         target = lowestRubble;

     }
 }

 //Not one of the first three builders?
        //TODO: Do other things that we...need to figure out


        //Reset target to null after one builder is done
        if (rc.getLocation().equals(target)) {
            target = null;
        }

        //get opposite direction for building so we don't build off the map

        dirAdjustedLab = homeArchonLoc.directionTo(target).opposite();

        if (safeBuild(rc, RobotType.LABORATORY, dirAdjustedLab) && wantMoreLabs){
            rc.buildRobot(RobotType.LABORATORY, dirAdjustedLab);
        }

        //Repair labs only
        RobotInfo[] ListofNearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

        //Check if any of those robots are repairable and if we can repair the same lab multiple times
        for (RobotInfo Robot : ListofNearbyRobots) {
            if (rc.canRepair(Robot.getLocation()) && Robot.getType() == RobotType.LABORATORY) {
                rc.repair(Robot.getLocation());
            }

        }

    }
}

