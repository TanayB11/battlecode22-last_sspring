package scrimv4.util;

import battlecode.common.MapLocation;

// TODO: Fix this because it's broken/not ideal.
// TODO: Potentially rewrite from scratch
public class mapTracker {
    final static int MAX_MAP_SIZE = 60;
    final static int INT_BITS = 32;
    final static int ARRAY_SIZE = 120;

    static int[] visitedLocations = new int[ARRAY_SIZE];

    mapTracker(){}

    static void reset(){
        visitedLocations = new int[ARRAY_SIZE];
    }

    static void add(MapLocation loc){
        int arrayPos = (loc.x%MAX_MAP_SIZE)*(1+(loc.y%MAX_MAP_SIZE)/INT_BITS);
        int bitPos = loc.y%INT_BITS;
        visitedLocations[arrayPos] |= (1 << bitPos);
    }

    static boolean check(MapLocation loc){
        int arrayPos = (loc.x%MAX_MAP_SIZE)*(1 + (loc.y%MAX_MAP_SIZE) / INT_BITS);
        int bitPos = loc.y%INT_BITS;
        return ((visitedLocations[arrayPos] & (1 << bitPos)) > 0);
    }
}
