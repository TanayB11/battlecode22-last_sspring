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

        //Get Archon Location
        me = ;

        //Assumes we're only making labs so far (watchtowers later)
        // 1 = Archon should save for a single lab, else not.
        wantMoreLabs = true;

        if (rc.readSharedArray(10) == 1) {
        }

        else if (wantMoreLabs) {
            rc.writeSharedArray(10, 1);
        }

        //Decide where we want them to move towards.
        //Below is for builders that take care of labs

        int distToNorthWall = rc.getMapHeight() - me.y;
        int distToSouthWall = me.y;
        int distToEastWall = rc.getMapWidth() - me.x;
        int distToWestWall = me.x;

        //TODO: fix inefficiency
        MapLocation LocForFirstLab = null;
        MapLocation LocForSecondLab = null;

        MapLocation dirLabPriorityOne = null;
        MapLocation dirLabPriorityTwo = null;

        MapLocation PriorityOneAdjusted = null;
        MapLocation PriorityTwoAdjusted = null;

        //Based on this, we decide on the two walls to check out
        //Edge cases: Archon against a wall or corner

        //First lab(s) location
         if (distToNorthWall >= distToSouthWall && distToSouthWall > 0) {
             LocForFirstLab = new MapLocation (me.x, 0);
         }
         else if (distToSouthWall >= distToNorthWall && distToNorthWall > 0) {
             LocForFirstLab = new MapLocation (me.x, rc.getMapHeight());
         }

        //Second lab location
        if (distToWestWall >= distToEastWall && distToEastWall > 0) {
            LocForSecondLab = new MapLocation (rc.getMapWidth(), me.y);
        }
        else if (distToEastWall >= distToWestWall && distToWestWall > 0) {
            LocForSecondLab = new MapLocation (0, me.y);
        }

        //Set the closest lab to be priority. If a new target has been set to avoid target, ignore.

    if (me.distanceSquaredTo(LocForFirstLab) >= me.distanceSquaredTo(LocForSecondLab)) {
        dirLabPriorityOne = LocForSecondLab;
        dirLabPriorityTwo = LocForFirstLab;
    } else {
        dirLabPriorityTwo = LocForSecondLab;
        dirLabPriorityOne = LocForFirstLab;
    }

        //First Builder goes to whichever target is closest
        //TODO: Change builderCount so it properly counts

        //Initialize and move to priority spot
        //TODO: actually search the spot to make sure its not high rubble;
        //Adjusted terms will be modified
        //set goals to whatever our adjusted location is

        bfs = new DroidBFS(rc);

        boolean isEnemiesNear;

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());


        if (BuilderCount == 1 && wantMoreLabs){
                bfs.move(dirLabPriorityOne);
                //search around once in area
            }

            if (rc.getLocation() == dirLabPriorityOne) {
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

                if(bad rubble)
                {
                    move
                }

            }
        }

        if (BuilderCount == 2 && wantMoreLabs){
        bfs.move(dirLabPriorityOne);
        //search around once in area
    }

            if (rc.getLocation() == dirLabPriorityOne) {
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

        if(bad rubble)
        {
            set a new target
        }

    }
        //get opposite direction so we don't build off the map
    //TODO: adjust diAdjustedLab to account for rubble
        dirAdjustedLab = rc.getLocation().directionTo().PriorityOneAdjusted.opposite();

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
