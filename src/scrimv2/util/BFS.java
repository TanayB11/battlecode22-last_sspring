package scrimv2.util;
import battlecode.common.*;

public class BFS {
    final int BYTECODE_REMAINING = 1000;
    //static final int BYTECODE_BFS = 5000;
    final int GREEDY_TURNS = 4;

    static RobotController rc;

    int turnsGreedy = 0;
    MapLocation currentTarget = null;

    BFS(RobotController rc){
        this.rc = rc;
    }
}
