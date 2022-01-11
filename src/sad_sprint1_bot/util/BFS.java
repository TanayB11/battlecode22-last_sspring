package sad_sprint1_bot.util;

import battlecode.common.*;

public abstract class BFS {
    final int BYTECODE_REMAINING = 1000;
    //static final int BYTECODE_BFS = 5000;
    final int GREEDY_TURNS = 4;

    static RobotController rc;

    int turnsGreedy = 0;

//    int turnsGreedy = 0;
    MapLocation currentTarget = null;

    public BFS(RobotController rc){
        this.rc = rc;
    }

    void reset(){
        turnsGreedy = 0;
        mapTracker.reset();
    }

    public void move(MapLocation target) throws GameActionException {
        move(target, false);
    }

    void update(MapLocation target){
        if (currentTarget == null || target.distanceSquaredTo(currentTarget) > 0){
            reset();
        } else --turnsGreedy;
        currentTarget = target;
        mapTracker.add(rc.getLocation());
    }

    void move(MapLocation target, boolean greedy) throws GameActionException {
        if (target == null) return;
        if (!rc.isMovementReady()) return;
        if (rc.getLocation().distanceSquaredTo(target) == 0) return;

        update(target);

        if (!greedy && turnsGreedy <= 0){
            Direction dir = getBestDir(target);
            if (dir != null && !mapTracker.check(rc.getLocation().add(dir))){
                Util.safeMove(rc, dir);
                return;
            } else activateGreedy();
        }

        if (Clock.getBytecodesLeft() >= BYTECODE_REMAINING) {
            Util.walkTowards(rc, target);
            --turnsGreedy;
        }
    }

    void activateGreedy(){
        turnsGreedy = GREEDY_TURNS;
    }

    abstract Direction getBestDir(MapLocation target);
}
