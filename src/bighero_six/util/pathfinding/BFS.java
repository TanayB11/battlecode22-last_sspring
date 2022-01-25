package bighero_six.util.pathfinding;

import battlecode.common.*;

import static bighero_six.util.SafeActions.safeMove;

public abstract class BFS {
    static final int BYTECODE_REMAINING = 1000;
    //static final int BYTECODE_BFS = 5000;
    static final int GREEDY_TURNS = 4;
    static final int ACCEPTABLE_RUBBLE = 25;
    private static Direction travelDir = null;

    static RobotController rc;

    int turnsGreedy = 0;

    //    int turnsGreedy = 0;
    MapLocation currentTarget = null;

    public BFS(RobotController rc){
        this.rc = rc;
    }

    void reset(){
        turnsGreedy = 0;
        MapTracker.reset();
    }

    public void move(MapLocation target) throws GameActionException {
        move(target, false);
    }

    void update(MapLocation target){
        if (currentTarget == null || target.distanceSquaredTo(currentTarget) > 0){
            reset();
        } else --turnsGreedy;
        currentTarget = target;
        MapTracker.add(rc.getLocation());
    }

    public void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
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

    void move(MapLocation target, boolean greedy) throws GameActionException {
        if (target == null) return;
        if (!rc.isMovementReady()) return;
        if (rc.getLocation().distanceSquaredTo(target) == 0) return;

        update(target);

        if (!greedy && turnsGreedy <= 0){
            Direction dir = getBestDir(target);
            if (dir != null && !MapTracker.check(rc.getLocation().add(dir))){
                safeMove(rc, dir);
                return;
            } else activateGreedy();
        }

        if (Clock.getBytecodesLeft() >= BYTECODE_REMAINING) {
            walkTowards(rc, target);
            --turnsGreedy;
        }
    }

    void activateGreedy(){
        turnsGreedy = GREEDY_TURNS;
    }

    abstract Direction getBestDir(MapLocation target);
}
