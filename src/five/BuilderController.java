package five;
import battlecode.common.*;
import static five.util.Communication.*;
import static five.util.Miscellaneous.*;
import static five.util.SafeActions.safeBuild;
import five.util.pathfinding.BFS;
import five.util.pathfinding.DroidBFS;

public class BuilderController {
    static MapLocation Archon = null;
    static BFS bfs = null;
    static boolean wantMoreLabs = false;
    static MapLocation target = null;
    static MapLocation homeArchonLoc = null;

    static void runBuilder(RobotController rc) throws GameActionException {

        //Send robot ID to the comms array for unique identification.
        //TODO: replace placeholder based on COMMS:

        int RobotID;

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getLocation(), -1, rc.getTeam());

        //Get Archon Location if one is not already existed
        if (homeArchonLoc.equals(null)) {

            for (RobotInfo Robot: nearbyRobots) {

                if (Robot.getType() == RobotType.ARCHON) {

                    homeArchonLoc = new MapLocation(Robot.getLocation().x, Robot.getLocation().y);
                }
            }
        }

        //TODO: NOTE THAT THIS Assumes we're only making labs so far (watchtowers later)

        wantMoreLabs = true;

        //1 = Archon should save lead for a single lab, else not.
        //Read array when possible to save bytecode

        if (rc.readSharedArray(10) == 1) { }

        else if (wantMoreLabs) {
            rc.writeSharedArray(10, 1);
        }
        //Decide where we want them to move towards.
        //Below is for builders that take care of labs

        int distToNorthWall = rc.getMapHeight() - homeArchonLoc.y;
        int distToSouthWall = homeArchonLoc.y;
        int distToEastWall = rc.getMapWidth() - homeArchonLoc.x;
        int distToWestWall = homeArchonLoc.x;

        //Based on this, we decide on the two walls to check out
        //Edge cases: Archon against a wall or corner

        //Set the closest lab to be priority

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        //First Builder goes to whichever target is first

        //Based on this, we decide on the two walls to check out
        //Edge cases: Archon against a wall or corner

        //First lab location if one hasn't already been set yet and this is Builder #1
         if (RobotID == 1 && target == null){

            if (distToNorthWall >= distToSouthWall && distToSouthWall > 0) {
                target = new MapLocation (homeArchonLoc.x, 0);
            }
            else if (distToSouthWall >= distToNorthWall && distToNorthWall > 0) {
                target = new MapLocation (homeArchonLoc.x, rc.getMapHeight());
            }
        }
        //Second lab location if one hasn't already been set yet and this is Builder #2
        else if (RobotID == 2 && target == null) {

             if (distToWestWall >= distToEastWall && distToEastWall > 0) {
                 target = new MapLocation(rc.getMapWidth(), homeArchonLoc.y);
             }
             else if (distToEastWall >= distToWestWall && distToWestWall > 0) {
                 target = new MapLocation(0, homeArchonLoc.y);
             }
         }

        //Initialize and move to priority spot
        //TODO: actually search the spot once there to make sure its not high rubble;
        //Adjusted terms will be modified
        //set goals to whatever our adjusted location is

        bfs = new DroidBFS(rc);

        Direction dirAdjustedLab = null;

        if (BuilderCount == 1 && wantMoreLabs){
            bfs.move(dirLabPriorityOne);
            //search around once in area

        boolean isEnemiesNear;

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if (RobotID == 1 && wantMoreLabs){
                bfs.move(target);
            }

            if (rc.getLocation().equals(target)) {
                //Once we're there, check the area for a new target with lower rubble

            }
        }

        //dirAdjustedLab = rc.getLocation().directionTo().PriorityOneAdjusted.opposite();
        else if (BuilderCount == 2 && wantMoreLabs){
            bfs.move(dirLabPriorityTwo);
            //search around once in area

        if (BuilderCount == 2 && wantMoreLabs){
        bfs.move(target);
        //search around once in area
    }

            if (rc.getLocation().equals(target)) {
        //Once we're there, check the area for a new target with lower rubble

        if (nearbyEnemies != null)
        {
            isEnemiesNear = true;
            //TODO: Send a flag
        }
        else
        {
            isEnemiesNear = false;

        }
        //dirAdjustedLab = rc.getLocation().directionTo().PriorityOneAdjusted.opposite();


        if ()

    }
        //get opposite direction so we don't build off the map
    //TODO: adjust diAdjustedLab to account for rubble
        dirAdjustedLab = Archon.directionTo().target.opposite();


        if (rc.getLocation() == safeBuild(rc, RobotType.LABORATORY, dirAdjustedLab) && wantMoreLabs);
        {
            rc.buildRobot(RobotType.LABORATORY, dirAdjustedLab);
        }

        //Repair labs only
        RobotInfo [] ListofNearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());

        //Check if any of those robots are repairable and if we can repair the same lab multiple times
        for (RobotInfo Robot : ListofNearbyRobots)
        {
            if (rc.canRepair(Robot.getLocation()) && Robot.getType() == RobotType.LABORATORY)
            {
                rc.repair(Robot.getLocation());
            }

        }

/*        //Mutate watchtowers only to level 2
        for (RobotInfo Robot : ListofNearbyRobots)
        {
            if (rc.canMutate(Robot.getLocation()) && (rc.getType() == RobotType.WATCHTOWER || rc.getType() == RobotType.LABORATORY) && rc.getLevel() == 1)
            {
                rc.mutate(Robot.getLocation());
            }

        }*/

    }

