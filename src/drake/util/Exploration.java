package drake.util;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;

public class Exploration {
    public static final Random rng = new Random();
    public static MapLocation minerExploreLoc(RobotController rc) throws GameActionException {
        int mapWidth = rc.getMapWidth(), mapHeight = rc.getMapHeight();
        // lol dumb strat go brrr
        // generate a random location between 1 and 2 vision radii away from robot
        // 5 tries to get on the map, else just go to center of map
        for (int i = 0; i++ <= 5;) {
            int x = rng.nextInt(mapWidth);
            int y = rng.nextInt(mapHeight);
            if (Math.pow(x, 2) + Math.pow(y, 2) > rc.getType().visionRadiusSquared) {
                return new MapLocation(x, y);
            }
        }
        return null;
    }
}
