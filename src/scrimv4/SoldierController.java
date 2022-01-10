package scrimv4;
import battlecode.common.*;
import scrimv4.util.BFS;
import scrimv4.util.DroidBFS;
import scrimv4.util.Util;
import java.util.Arrays;

public class SoldierController {
    //copy-and-pasted from miner v4 and soldier v3
    static int totalMovesSoldier = 0;
    static MapLocation horizReflection = null;
    static MapLocation vertReflection = null;
    static MapLocation rotationalReflection = null;
    private static final int ACCEPTABLE_RUBBLE = 25;
    static int nearbyEnemyBots = 0;
    static Direction travelDir = null;
    static MapLocation me = null, target = null;

    static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // initializing bfs
        DroidBFS bfs = new DroidBFS(rc);

        //Define the three possible locations
        horizReflection = new MapLocation(me.x, rc.getMapHeight() - me.y);
        vertReflection = new MapLocation(rc.getMapWidth() - me.x, me.y);
        rotationalReflection = new MapLocation(rc.getMapWidth() - me.x, rc.getMapHeight() - me.y);


        
        //moves soldier to locations
        if(totalMovesSoldier == 0 || totalMovesSoldier == 1) {
            rc.setIndicatorString("First two soldiers!");
            target = rotationalReflection;

        }

        if(totalMovesSoldier == 2 || totalMovesSoldier == 3) {
            rc.setIndicatorString("Third and fourth soldiers!");
            target = horizReflection;

        }

        if(totalMovesSoldier == 4 || totalMovesSoldier == 5) {
            rc.setIndicatorString("Fifth and sixth soldiers!");
            target = vertReflection;

        }
     
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
    
      


    /*   do we need this stuff from minercontroller v4?
         if (failedMoves >= 3) {
            travelDir = (Util.rng.nextDouble() <= 0.02)
                    ? Util.directions[Util.rng.nextInt(Util.directions.length)]
                    : travelDir.opposite();
        } 

if (Util.safeMove(rc, travelDir)) {
        } else if (rc.getMovementCooldownTurns() == 0) {
        } */
        
//comms copied from minercontroller
 // search for enemy archons (can be optimized)
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, opponent);
        if (enemies.length > 0) {
            for (RobotInfo enemy : enemies) {
                // TODO: make everything zero-index for readability
                int posUsed = 0;

                // TODO: Check if 100 is the optimal number
                // TODO: use less bits plz
                // this is soldier's comms btw
                if (enemy.getType().equals(RobotType.ARCHON)) {
                    MapLocation enemyLoc = enemy.getLocation();
                    // this is the form coordinates are read into the array
                    int waypointCode = enemyLoc.x * 100 + enemyLoc.y;

                    int comm_pos_one = rc.readSharedArray(0);
                    boolean oneEmpty = (comm_pos_one == 0);

                    int comm_pos_two = rc.readSharedArray(1);
                    boolean twoEmpty = (comm_pos_two == 0);

                    int comm_pos_three = rc.readSharedArray(2);
                    boolean threeEmpty = (comm_pos_three == 0);

                    int comm_pos_four = rc.readSharedArray(3);
                    boolean fourEmpty = (comm_pos_four == 0);

                    boolean isNew = true;

                    if (waypointCode == comm_pos_one || waypointCode == comm_pos_two) {
                        isNew = false;
                    } else if (waypointCode == comm_pos_three || waypointCode == comm_pos_four) {
                        isNew = false;
                    }

                    // TODO: put the archon priority status in with the same bit
                    // TODO: as the location, it def fits
                    // TODO: that would save us from doing two writes (only one needed)
                    if (isNew) {
                        if (oneEmpty) {
                            rc.writeSharedArray(0, waypointCode);
                            posUsed = 1;
                        } else if (twoEmpty) {
                            rc.writeSharedArray(1, waypointCode);
                            posUsed = 2;
                        } else if (threeEmpty) {
                            rc.writeSharedArray(2, waypointCode);
                            posUsed = 3;
                        } else if (fourEmpty) {
                            rc.writeSharedArray(3, waypointCode);
                            posUsed = 4;
                        }
                    }

                    // index 4 will contain a four digit integer of 0s or 1s
                    // 0 indicates low priority archon, 1 indicates high priority archon
                    // 0 means it is close to dead :()
                    //Can delete if too much space
                    int priorities = rc.readSharedArray(4);
                    int posUsedPriority = (int) (priorities / Math.pow(10, 4 - posUsed)) % 10;
                    if (enemy.getHealth() >= 100) {
                        // if the health is high, 0 turns to 1
                        if (posUsedPriority != 1) {
                            int valueWritten = priorities + (int) Math.pow(10, 4-posUsed);
                            rc.writeSharedArray(4, valueWritten);
                        }
                    } else {
                        // if the health is low, 1 turns to 0
                        if (posUsedPriority != 0) {
                            int valueWritten = priorities - (int) Math.pow(10, 4-posUsed);
                            rc.writeSharedArray(4, valueWritten);
                        }
                    }
                }
            }
        }

    //Search the array
    //If slot 1,2,3,or 4 empty -> don't do anything
    //if not empty -> look at index 5 and decide
    //DOUBLE CHECK?
    
    int indexCount = 0;
    for(int index : rc.readSharedArray(index))
    {
        if (rc.readSharedArray(index) == 0)
        {
            indexCount++;
        }
        else if (rc.readSharedArray(index) != 0)
        {   
            indexCount++;
            int posDetected = (int) (rc.readSharedArray(4) / Math.pow(10, 4 - indexCount)) % 10;
            break;
        }
    }

    //Make a new MapLocation w/ coords
    //BFS to wherever posPriorityAttack says to
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

    // attack!
    MapLocation toAttack = (enemies.length > 0) ? enemies[0].location : null;
    if (toAttack != null && rc.canAttack(toAttack)) { rc.attack(toAttack); }
    
    // Bug0 pathing
    // Taken from https://github.com/battlecode/battlecode22-lectureplayer/blob/main/src/lectureplayer/Pathing.java
    static void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();

        if (!rc.isMovementReady() || currentLocation.equals(target)) { return; }

        Direction d = currentLocation.directionTo(target);
        if (rc.canMove(d) && !isObstacle(rc, d)) {
            // No obstacle in the way, so let's just go straight for it!
            rc.move(d);
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