package scalar;
import battlecode.common.*;
import scalar.util.BFS;
import scalar.util.DroidBFS;
import scalar.util.Util;

public class BuilderController {
    static MapLocation me = null;
    static void runBuilder(RobotController rc) throws GameActionException {
        me = rc.getLocation();


        //TODO: BFS to a nearby location first
        int distToNorthWall = rc.getMapHeight() - me.y;
        int distToSouthWall = me.y;

        int distToEastWall = rc.getMapWidth() - me.x;
        int distToWestWall = me.x;

        int yCoordmin = Math.min(distToSouthWall, (int) (0.3 * rc.getMapHeight());
        int xCoordmin = Math.min(distToWestWall, (int) (0.3 * rc.getMapWidth());
        int yCoordmax = Math.min(distToNorthWall, (int) (0.3 * rc.getMapHeight());
        int xCoordmax = Math.min(distToEastWall, (int) (0.3 * rc.getMapWidth());

        int xCoordRandom = Util.rng.nextInt(me.x-xCoordmin, me.x-xCoordmax);
        int yCoordRandom = Util.rng.nextInt(me.y - yCoordmin, me.y-yCoordmax);

        MapLocation target = new MapLocation(xCoordRandom, yCoordRandom);

        //TODO: BFS to this location. Add a check that its at least 5 squares from the Archon at nearly all times

        //Now, repair anything nearby.

        RobotInfo [] ListofNearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        //Check if any of those robots are repairable and if we can repair the same watchtower multiple times
        for (RobotInfo Robot : ListofNearbyRobots)
        {
            if (rc.canRepair(Robot.getLocation()))
            {
                rc.repair(Robot.getLocation());
            }

        }

        //Mutate watchtowers only to level 2
        for (RobotInfo Robot : ListofNearbyRobots)
        {
            if (rc.canMutate(Robot.getLocation()) && rc.getType() == RobotType.WATCHTOWER && rc.getLevel() == 1)
            {
                rc.mutate(Robot.getLocation());
            }

        }
    }
}
