package scrimv0;
import battlecode.common.*;
import java.util.Arrays;

public class ArchonController {
    static int miners = 0, soldiers = 0, builders = 0;
    static MapLocation me = null;

    static void runArchon(RobotController rc) throws GameActionException {
        if (me != null) { me = rc.getLocation(); }

        if (miners < 10) {
            Util.safeSpawn(rc, RobotType.MINER, Util.directions[Util.rng.nextInt(Util.directions.length)]);
//            MapLocation[] nearbyPb = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
//            Arrays.sort(nearbyPb, (a, b) -> me.distanceSquaredTo(a) - me.distanceSquaredTo(b));
//            for (MapLocation pb : nearbyPb) {
//                Util.safeSpawn(rc, RobotType.MINER, me.directionTo(pb));
//            }
            miners++;
        }

    }
}
