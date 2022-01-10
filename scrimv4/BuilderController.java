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
        me = rc.getLocation();
        Direction dirInitial;

        //Determine distance to each wall:

        //First define each wall
        MapLocation ArchonLocation = new MapLocation(me.x, me.y);
        int distToWestWall = (new MapLocation(0, me.y).distanceSquaredTo(ArchonLocation));
        int distToEastWall = (new MapLocation(rc.getMapWidth(), me.y).distanceSquaredTo(ArchonLocation));
        int distToNorthWall = (new MapLocation(me.x, rc.getMapHeight()).distanceSquaredTo(ArchonLocation));
        int distToSouthWall = (new MapLocation(me.x, 0).distanceSquaredTo(ArchonLocation));

        boolean EW = (distToEastWall > distToWestWall);
        boolean NS = (distToNorthWall > distToSouthWall);

        //The find optimal diagonal direction!
        if (EW && NS) {
            dirInitial = Direction.NORTHEAST;
        } else if (EW && !NS) {
            dirInitial = Direction.SOUTHEAST;
        } else if (!EW && !NS) {
            dirInitial = Direction.SOUTHWEST;
        } else {
            dirInitial = Direction.NORTHWEST;
        }

        //change to be more general location based on distance from each wall
        //add randomness?
        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }
        //TODO: Make more general
        MapLocation target = ArchonLocation.add(travelDir).add(travelDir).add(travelDir);

        // initializing bfs
        DroidBFS bfs = new DroidBFS(rc);

        //uses bfs to move towards target (did we do it right?)
        if (target != null) {
            Direction optDir = bfs.getBestDir(target);
            if (optDir != null) {
                travelDir = optDir;
                if (Util.safeMove(rc, optDir)) {
                    rc.setIndicatorString("I moved " + optDir.toString());
                } else {
                    rc.setIndicatorString("I used greedy to move.");
                    // TODO: clean up this case because you can definitely
                    // TODO: avoid the whole acceptable rubble thing :vomit:
                    walkTowards(rc, target);
                }
            }
        }


        rc.setIndicatorString("TRY BUILDING PLEASE!");
        if (rc.canBuildRobot(RobotType.WATCHTOWER, dirInitial.opposite())) //how often should it build?
        {
            rc.buildRobot(RobotType.WATCHTOWER, dirInitial.opposite());

        }


        //Repair any and all watchtowers in the area

        RobotInfo [] ListofNearbyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        //Check if any of those robots are repairable and if we can repair the same watchtower multiple times
        //THIS ISN"T REPAIRING????
        for (RobotInfo Robot : ListofNearbyRobots)
        {
            rc.setIndicatorString("Will repair");
            if (rc.canRepair(Robot.getLocation()) && Robot.getTeam() == rc.getTeam())
            {
                walkTowards(rc, Robot.getLocation());
                rc.repair(Robot.getLocation());
                rc.setIndicatorString("AM REPAIRING <3!");
            }
        }

        //actually wait shouldn't builders stay near archon and the watchtowers are the ones that move around?
        //starts moving once soldiers find enemies
        //Need to formalize how many turns builder should wait first
        if(!(rc.readSharedArray(0) == 0 && totalMovesBuilder > 20)) {
            //potential edge case
            //Harvest coordinates from array that our soldiers gave us
            int posDetected;
            for(int index : rc.readSharedArray(index))
            {
                if (rc.readSharedArray(index) == 0)
                {

                }
                else if (rc.readSharedArray(index) != 0)
                {

                    posDetected = rc.readSharedArray(index);
                    break;
                }
            }

            //Make a new MapLocation w/ coords
            int xCoord = posDetected/100;
            int yCoord = posDetected - (posDetected/100)*100;
            MapLocation dangerousTarget = new MapLocation (xCoord, yCoord);

            //Somewhat safe location (weighted average) plus some randomness to mitigate clumping?
            MapLocation target = (int((0.75) * me.x + 0.25*xCoord + Math.Random() * 4 - 2), int((0.75) * me.y + 0.25*yCoord + Math.Random() * 4 - 2));

            //uses bfs to move towards target (did we do it right?)
            if (target != null) {
                Direction optDir = bfs.getBestDir(target);
                if (optDir != null) {
                    travelDir = optDir;
                    if (Util.safeMove(rc, optDir)) {
                        rc.setIndicatorString("I moved " + optDir.toString());
                    } else {
                        rc.setIndicatorString("I used greedy to move.");
                        // TODO: clean up this case because you can definitely
                        // TODO: avoid the whole acceptable rubble thing :vomit:
                        walkTowards(rc, target);
                    }
                }
            }

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


