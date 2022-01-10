package scrimv2;
import battlecode.common.*;
import scrimv2.util.Util;

public class SoldierController {
    static Direction travelDir = null;
    static MapLocation me = null;
    static int failedMoves = 0;

    static void runSoldier(RobotController rc) throws GameActionException {
        me = rc.getLocation();

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
