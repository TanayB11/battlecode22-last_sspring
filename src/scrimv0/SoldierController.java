package scrimv0;
import battlecode.common.*;

public class SoldierController {
    static Direction travelDir = null;
    static MapLocation hurtMiners = null;

    static void runSoldier(RobotController rc) throws GameActionException {
        int movementCooldownTurns = rc.getMovementCooldownTurns();

        //Only created when miner takes damage. Create a relevant maplocation.
        int xCoordDamaged = rc.readSharedArray(0);
        int yCoordDamaged = rc.readSharedArray(1);
        hurtMiners = new MapLocation(xCoordDamaged, yCoordDamaged);

        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }
        //MOVE

        {
            // TODO: stop the robots from oscillating
            // TODO: check if the lead is gone from our destination
            rc.setIndicatorString("Pathfinding to " + hurtMiners.toString());
            if (movementCooldownTurns == 0) {
                travelDir = Util.greedyNextMove(rc, hurtMiners);
                if (!Util.safeMove(rc, travelDir)) {
                    for (int i = 0; i < Util.directions.length; i++) {
                        if (!Util.directions[i].equals(travelDir)) {
                            // if moved successfully, cooldown kicks in and loop will break
                            Util.safeMove(rc, Util.directions[i]);
                        }
                    }
                }
            }

        }
    }
}