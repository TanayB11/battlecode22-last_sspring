package scrimv1;
import battlecode.common.*;

public class SoldierController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static int failedMoves = 0;

    static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // waypoint navigation
        int currWaypoint = rc.readSharedArray(0);
         if (currWaypoint != 0) {
             rc.setIndicatorString("Moving to waypoint");
             int waypointX = currWaypoint / 100;
             int waypointY = currWaypoint - waypointX;
             Direction dirToWaypoint = me.directionTo(new MapLocation(waypointX, waypointY));
             if (rc.canMove(dirToWaypoint)) {
                 rc.move(dirToWaypoint);
             }
         }

        // TODO: if we're on the other side of the map start just looking for lead/scouting
        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }

        if (failedMoves >= 3) {
            travelDir = (Util.rng.nextDouble() <= 0.02)
                    ? Util.directions[Util.rng.nextInt(Util.directions.length)]
                    : travelDir.opposite();
        }


        if (Util.safeMove(rc, travelDir)) {
            failedMoves = 0;
        } else if (rc.getMovementCooldownTurns() == 0) {
            failedMoves++;
        }

        // attack!
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        MapLocation toAttack = (enemies.length > 0) ? enemies[0].location : null;
        if (toAttack != null && rc.canAttack(toAttack)) { rc.attack(toAttack); }
    }
}
