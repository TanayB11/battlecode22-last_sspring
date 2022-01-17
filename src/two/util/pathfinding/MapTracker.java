package two.util.pathfinding;

import battlecode.common.MapLocation;

// TODO: Fix this because it's broken/not ideal.
// TODO: Potentially rewrite from scratch
public class MapTracker {
    final static int INT_BITS = 32;
    final static int ARRAY_SIZE = 120;

    static int[] visitedLocations = new int[ARRAY_SIZE];

    MapTracker(){}

    static void reset(){
        visitedLocations = new int[ARRAY_SIZE];
    }

    // one map square -> 1 bit
    // a pair of 2 array locs stores 1 X location
    // the bit stores the y location
    static void add(MapLocation loc){
        int arrayPos = loc.x / 2 + (loc.y < INT_BITS ? 0 : 1);
        int bitPos = loc.y % INT_BITS;
        visitedLocations[arrayPos] |= (1 << bitPos);
    }

    static boolean check(MapLocation loc){
        int arrayPos = loc.x / 2 + (loc.y < INT_BITS ? 0 : 1);
        int bitPos = loc.y % INT_BITS;
        return ((visitedLocations[arrayPos] & (1 << bitPos)) > 0);
    }
}