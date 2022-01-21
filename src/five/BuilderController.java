package five;
import battlecode.common.*;
import static five.util.Communication.*;
import static five.util.Miscellaneous.*;
import static five.util.SafeActions.safeBuild;
import five.util.pathfinding.BFS;
import five.util.pathfinding.DroidBFS;

public class BuilderController {
    static MapLocation me = null;
    static int BuilderCount = 0;
    static BFS bfs = null;
    static boolean wantMoreLabs = false;

    static void runBuilder(RobotController rc) throws GameActionException {
        BuilderCount++;

        //TODO: Get Archon Location
        me = ;

        //Assumes we're only making labs so far (watchtowers later)
        // 1 = Archon should save for a single lab, else not.
        wantMoreLabs = true;

        if (rc.readSharedArray(10) == 1)
        {

        }

        else if (wantMoreLabs)
        {
            rc.writeSharedArray(10, 1);
        }
        //Decide where we want them to move towards.
        //Below is for builders that take care of labs

        int distToNorthWall = rc.getMapHeight() - me.y;
        int distToSouthWall = me.y;
        int distToEastWall = rc.getMapWidth() - me.x;
        int distToWestWall = me.x;

        //TODO: fix inefficiency
        MapLocation dirForFirstLab = null;
        MapLocation dirForSecondLab = null;

        MapLocation dirLabPriorityOne = null;
        MapLocation dirLabPriorityTwo = null;

        MapLocation PriorityOneAdjusted = null;
        MapLocation PriorityTwoAdjusted = null;
        //Based on this, we decide on the two walls to check out
        //Edge cases: Archon against a wall or corner

        //First lab(s) location
         if (distToNorthWall >= distToSouthWall && distToSouthWall > 0) {
             dirForFirstLab = new MapLocation (me.x, 0);
         }
         else if (distToSouthWall >= distToNorthWall && distToNorthWall > 0) {
             dirForFirstLab = new MapLocation (me.x, rc.getMapHeight());
         }

        //Second lab location
        if (distToWestWall >= distToEastWall && distToEastWall > 0) {
            dirForSecondLab = new MapLocation (rc.getMapWidth(), me.y);
        }
        else if (distToEastWall >= distToWestWall && distToWestWall > 0) {
            dirForSecondLab = new MapLocation (0, me.y);
        }

        //Set the closest lab to be priority

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        senseRubble(MapLocation)
        if (me.distanceSquaredTo(dirForFirstLab) >= me.distanceSquaredTo(dirForSecondLab))
        {
            dirLabPriorityOne = dirForSecondLab;
            dirLabPriorityTwo = dirForFirstLab;
        }
        else
        {
            dirLabPriorityTwo = dirForSecondLab;
            dirLabPriorityOne = dirForFirstLab;
        }


        //First Builder goes to whichever target is closest

        //Initialize and move to priority spot
        //TODO: actually search the spot to make sure its not high rubble;
        //Adjusted terms will be modified
        //set goals to whatever our adjusted location is

        bfs = new DroidBFS(rc);
        Direction dirAdjustedLab = null;

        if (BuilderCount == 1 && wantMoreLabs){
            bfs.move(dirLabPriorityOne);
            //search around once in area

            if (rc.getLocation() == dirLabPriorityOne) {
                //Once we're there, check the area for a new target with lower rubble

            }
        }
        //dirAdjustedLab = rc.getLocation().directionTo().PriorityOneAdjusted.opposite();
        else if (BuilderCount == 2 && wantMoreLabs){
            bfs.move(dirLabPriorityTwo);
            //search around once in area
        }
        //dirAdjustedLab = rc.getLocation().directionTo().PriorityOneAdjusted.opposite();


        if ()

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
}
