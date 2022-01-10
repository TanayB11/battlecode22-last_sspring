package scrimv3;
import battlecode.common.*;

public class SoldierController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static int failedMoves = 0;
    static int totalMovesSoldier = 0;
    static MapLocation horizReflection = null;
    static MapLocation vertReflection = null;
    static MapLocation rotationalReflection = null;
    private static final int ACCEPTABLE_RUBBLE = 25;
    static int nearbyEnemyBots = 0;

    static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        //Define the three possible locations
        horizReflection = new MapLocation(me.x, rc.getMapHeight() - me.y);
        vertReflection = new MapLocation(rc.getMapWidth() - me.x, me.y);
        rotationalReflection = new MapLocation(rc.getMapWidth() - me.x, rc.getMapHeight() - me.y);

        if(totalMovesSoldier == 0 || totalMovesSoldier == 1) {
            rc.setIndicatorString("First two soldiers!");
            walkTowards(rc, rotationalReflection);
            totalMovesSoldier++;
        }

        if(totalMovesSoldier == 2 || totalMovesSoldier == 3) {
            rc.setIndicatorString("Third and fourth soldiers!");
            walkTowards(rc, horizReflection);
            totalMovesSoldier++;
        }

        if(totalMovesSoldier == 4 || totalMovesSoldier == 5) {
            rc.setIndicatorString("Fifth and sixth soldiers!");
            walkTowards(rc, vertReflection);
            totalMovesSoldier++;
        }
        //Define nearby robots
       RobotInfo [] ListofNearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        //Check if any of those robots are enemy ones
       for (RobotInfo Robot : ListofNearbyRobots)
       {
           if (Robot.getTeam().equals(rc.getTeam().opponent()))
           {
               nearbyEnemyBots++;
           }

       }
        if(!(nearbyEnemyBots == 0))
        {
            rc.writeSharedArray(1, me.x * 100 + me.y);
            rc.setIndicatorString("Enemy detected.");
        }

        // waypoint navigation
        int currWaypoint = rc.readSharedArray(0);
         if (currWaypoint != 0) {
             //rc.setIndicatorString("Moving to waypoint");
             int waypointX = currWaypoint / 100;
             int waypointY = currWaypoint - waypointX;
             Direction dirToWaypoint = me.directionTo(new MapLocation(waypointX, waypointY));
             if (rc.canMove(dirToWaypoint)) {
                 rc.move(dirToWaypoint);
             }
         }

        // TODO: if we're on the other side of the map start just looking for lead/scouting
        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }

        if (failedMoves >= 3) {
            travelDir = (Util.rng.nextDouble() <= 0.02)
                    ? Util.directions[Util.rng.nextInt(Util.directions.length)]
                    : travelDir.opposite();
        }


        if (Util.safeMove(rc, travelDir)) {
            failedMoves = 0;
        } else if (rc.getMovementCooldownTurns() == 0) {
            failedMoves++;
        }

        // attack!
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        MapLocation toAttack = (enemies.length > 0) ? enemies[0].location : null;
        if (toAttack != null && rc.canAttack(toAttack)) { rc.attack(toAttack); }
    }

    //Copied from MinerController
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
