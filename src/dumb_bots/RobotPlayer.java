package dumb_bots;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());
        boolean waypointFlag = false;

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());

            // Reset waypoint every 150 turns, will set to 0 at start
            if (rc.getRoundNum() % 150 == 0) { rc.writeSharedArray(0, 0); }

            // Once we get to the waypoint, clear it
            int waypointCode = rc.readSharedArray(0);
            if (rc.getLocation().equals(new MapLocation(waypointCode/100, waypointCode - waypointCode/100))) {
                rc.writeSharedArray(0, 0);
                waypointFlag = true;
            }

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try { // Called for all robots
                switch (rc.getType()) {
                    case ARCHON:     runArchon(rc, waypointFlag);  break;
                    case MINER:      runMiner(rc, waypointFlag);   break;
                    case SOLDIER:    runSoldier(rc, waypointFlag); break;
                    case LABORATORY: // Examplefuncsplayer doesn't use any of these robot types below.
                    case WATCHTOWER: // You might want to give them a try!
                    case BUILDER:
                    case SAGE:       break;
                }
            } catch (GameActionException e) { // We did illegal thing
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } catch (Exception e) { // We did a bad thing, probably a bug
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } finally { // End turn
                Clock.yield();
            }
        }
        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    // Run a single turn for an Archon.
    static void runArchon(RobotController rc, boolean waypointFlag) throws GameActionException {
        // Spawn miners in random directions
        Direction spawnDir = directions[rng.nextInt(directions.length)];
        int currWaypoint = rc.readSharedArray(0);

        if ((rng.nextBoolean() || rc.getRoundNum() < 250) && (currWaypoint == 0 && !waypointFlag)) {
            rc.setIndicatorString("Trying to build a miner");
            if (rc.canBuildRobot(RobotType.MINER, spawnDir)) {
                rc.buildRobot(RobotType.MINER, spawnDir);
            }
        } else {
            rc.setIndicatorString("Trying to build a soldier");
            if (rc.canBuildRobot(RobotType.SOLDIER, spawnDir)) {
                rc.buildRobot(RobotType.SOLDIER, spawnDir);
            }
        }
    }

    // Run a single turn for a Miner.
    static void runMiner(RobotController rc, boolean waypointFlag) throws GameActionException {
        MapLocation myLoc = rc.getLocation();
        int currWaypoint = rc.readSharedArray(0);

        // Check if can mine within vision radius
        MapLocation[] visibleLocs = rc.getAllLocationsWithinRadiusSquared(myLoc, RobotType.MINER.visionRadiusSquared);
        ArrayList<MapLocation> nearbyPbLocs = new ArrayList<MapLocation>();
        MapLocation nearestPb = null;
        double nearestPbDistanceSq = Double.POSITIVE_INFINITY;

        for (MapLocation loc : visibleLocs) {
            if (rc.senseLead(loc) > 0) {
                nearbyPbLocs.add(loc);
                double distSq = Math.pow(loc.x, 2) + Math.pow(loc.y, 2);
                if (distSq < nearestPbDistanceSq) { nearestPb = loc; }
                nearestPbDistanceSq = Math.min(nearestPbDistanceSq, distSq);
            }
        }

        if (nearestPb != null)  { // Dumb path finding to nearest Pb
            Direction dirToClosestPb = myLoc.directionTo(nearestPb);
            if (rc.canMove(dirToClosestPb)) { rc.move(dirToClosestPb); }
        } else if (currWaypoint != 0 && !waypointFlag) { // Move to the waypoint direction
            int waypointX = currWaypoint / 100;
            int waypointY = currWaypoint - waypointX;
            Direction dirToWaypoint  = myLoc.directionTo(new MapLocation(waypointX, waypointY));
            if (rc.canMove(dirToWaypoint)) { rc.move(dirToWaypoint); }
        } else { // move randomly
            Direction dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir)) { rc.move(dir); }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                while (rc.canMineGold(mineLocation)) { rc.mineGold(mineLocation); }
                while (rc.canMineLead(mineLocation)) { rc.mineLead(mineLocation); }
            }
        }

        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation enemyLoc = enemies[0].location;
            int waypointCode = enemyLoc.x * 100 + enemyLoc.y;
            rc.writeSharedArray(0, waypointCode);
        }
    }

    // Run a single turn for a Soldier.
    static void runSoldier(RobotController rc, boolean waypointFlag) throws GameActionException {
        MapLocation myLoc = rc.getLocation();

        int currWaypoint = rc.readSharedArray(0);
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        MapLocation toAttack = (enemies.length > 0) ? enemies[0].location : null;

        if (currWaypoint != 0 && !waypointFlag) {
            rc.setIndicatorString("Moving to waypoint");
            int waypointX = currWaypoint / 100;
            int waypointY = currWaypoint - waypointX;
            Direction dirToWaypoint  = myLoc.directionTo(new MapLocation(waypointX, waypointY));
            if (rc.canMove(dirToWaypoint)) { rc.move(dirToWaypoint); }
        } else if (enemies.length == 0 || waypointFlag) {
            rc.setIndicatorString("No enemies found! Moving randomly");
            Direction dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir)) { rc.move(dir); }
        } else {
            rc.setIndicatorString("Found an enemy! Marking a waypoint");
            int waypointCode = toAttack.x * 100 + toAttack.y; // 1st 2 digits = waypoint x, last 2 digits = waypoint y
            rc.writeSharedArray(0, waypointCode);
       }

        if (toAttack != null && rc.canAttack(toAttack)) { rc.attack(toAttack); }
    }
}
