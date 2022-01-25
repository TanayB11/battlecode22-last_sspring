package baymax_seven.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Exploration {
    public static MapLocation minerExploreLoc(RobotController rc) throws GameActionException {
        int mapWidth = rc.getMapWidth(), mapHeight = rc.getMapHeight();
        // lol dumb strat go brrr
        // generate a random location between 1 and 2 vision radii away from robot
        // 5 tries to get on the map, else just go to center of map

        boolean coinFlip = Miscellaneous.rng.nextBoolean();

        if (coinFlip) {
            for (int i = 0; i++ <= 5;) {
                int x = Miscellaneous.rng.nextInt(mapWidth);
                int y = Miscellaneous.rng.nextInt(mapHeight);
                if (Math.pow(x, 2) + Math.pow(y, 2) > rc.getType().visionRadiusSquared) {
                    return new MapLocation(x, y);
                }
            }
        } else {
            int choice = Miscellaneous.rng.nextInt(5);

            if (choice == 0) {
                return new MapLocation(mapWidth / 2, mapHeight / 2);
            } else if (choice == 1){
                return new MapLocation(0, 0);
            } else if (choice == 2) {
                return new MapLocation(0, mapHeight-1);
            } else if (choice == 3) {
                return new MapLocation(mapHeight-1, 0);
            } else {
                return new MapLocation(mapHeight-1, mapHeight-1);
            }
        }

        return null;
    }

    // TODO: replace this with an initial location based on symmetry
    public static MapLocation soldierExploreLoc(RobotController rc) throws GameActionException {
        int mapWidth = rc.getMapWidth(), mapHeight = rc.getMapHeight();
        // lol dumb strat go brrr
        // generate a random location between 1 and 2 vision radii away from robot
        // 5 tries to get on the map, else just go to center of map
        for (int i = 0; i++ <= 5;) {
            int x = Miscellaneous.rng.nextInt(mapWidth);
            int y = Miscellaneous.rng.nextInt(mapHeight);
            if (Math.pow(x, 2) + Math.pow(y, 2) > rc.getType().visionRadiusSquared) {
                return new MapLocation(x, y);
            }
        }
        return null;
    }
}
