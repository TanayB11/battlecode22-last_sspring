package sad_sprint1_bot;
import battlecode.common.*;
import sad_sprint1_bot.util.DroidBFS;
import sad_sprint1_bot.util.Util;
import java.util.Arrays;

public class MinerController {
    // initialize everything :)
    static Direction travelDir = null;
    static MapLocation me = null, target = null;
    static boolean isMining = false;
    static final int ACCEPTABLE_RUBBLE = 25;

    static void runMiner(RobotController rc) throws GameActionException {
        me = rc.getLocation();

        // initializing bfs
        DroidBFS bfs = new DroidBFS(rc);

        // sets initial direction using our weird symmetry tricks
        if (travelDir == null) {
            travelDir = Util.initDir(rc);
        }

        // sense lead -> if lead found, set target
        MapLocation[] nearbyLead = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared, 2);
        // TODO: find out if the target == null is necessary
        if (target == null && nearbyLead.length > 0 && !isMining) {
            rc.setIndicatorString("Searching for a destination!");
            Arrays.sort(nearbyLead, (a, b) -> me.distanceSquaredTo(a) - me.distanceSquaredTo(b));
            // TODO: find out if this is necessary
            for (MapLocation potentialDest : nearbyLead) {
                if (!rc.isLocationOccupied(potentialDest)) {
                    target = potentialDest;
                    break;
                }
            }
        }

        // if it has a target, move toward it with bfs if bfs is not null
        // if bfs is null, greedy toward it
        if (target != null) {
            bfs.move(target);
        } else {
            // if it doesn't have a target, just safe move toward initial direction
            // TODO: add some extra explore code so that it doesn't all just go
            // TODO: toward an archon, bc outward exploration is good :D
            if (Util.safeMove(rc, travelDir)) {
                rc.setIndicatorString("GG moved " + travelDir.toString());
            } else {
                travelDir = null;
            }
        }

        // mine
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                safeMine(rc, mineLocation);
            }
        }

        if (target != null) {
            if (rc.canSenseLocation(target) && rc.senseLead(target) < 2) {
                target = null;
            }
        }

        // search for enemy archons (can be optimized)
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, opponent);

        if (enemies.length > 0) {
            for (RobotInfo enemy : enemies) {
                // TODO: make everything zero-index for readability
                int posUsed = 0;

                // TODO: Check if 100 is the optimal number
                // TODO: use less bits plz
                // this is miner's comms btw
                if (enemy.getType().equals(RobotType.ARCHON)) {
                    MapLocation enemyLoc = enemy.getLocation();
                    // this is the form coordinates are read into the array
                    int waypointCode = enemyLoc.x * 100 + enemyLoc.y;

                    int comm_pos_one = rc.readSharedArray(0);
                    boolean oneEmpty = (comm_pos_one == 0);

                    int comm_pos_two = rc.readSharedArray(1);
                    boolean twoEmpty = (comm_pos_two == 0);

                    int comm_pos_three = rc.readSharedArray(2);
                    boolean threeEmpty = (comm_pos_three == 0);

                    int comm_pos_four = rc.readSharedArray(3);
                    boolean fourEmpty = (comm_pos_four == 0);

                    boolean isNew = true;

                    if (waypointCode == comm_pos_one || waypointCode == comm_pos_two) {
                        isNew = false;
                    } else if (waypointCode == comm_pos_three || waypointCode == comm_pos_four) {
                        isNew = false;
                    }

                    // TODO: put the archon priority status in with the same bit
                    // TODO: as the location, it def fits
                    // TODO: that would save us from doing two writes (only one needed)
                    if (isNew) {
                        if (oneEmpty) {
                            rc.writeSharedArray(0, waypointCode);
                            posUsed = 1;
                        } else if (twoEmpty) {
                            rc.writeSharedArray(1, waypointCode);
                            posUsed = 2;
                        } else if (threeEmpty) {
                            rc.writeSharedArray(2, waypointCode);
                            posUsed = 3;
                        } else if (fourEmpty) {
                            rc.writeSharedArray(3, waypointCode);
                            posUsed = 4;
                        }
                    }

                    // index 4 will contain a four digit integer of 0s or 1s
                    // 0 indicates low priority archon, 1 indicates high priority archon
                    // 0 means it is close to dead :()
                    int priorities = rc.readSharedArray(4);
                    int posUsedPriority = (int) (priorities / Math.pow(10, 4 - posUsed)) % 10;
                    if (enemy.getHealth() >= 100) {
                        // if the health is high, 0 turns to 1
                        if (posUsedPriority != 1) {
                            int valueWritten = priorities + (int) Math.pow(10, 4-posUsed);
                            rc.writeSharedArray(4, valueWritten);
                        }
                    } else {
                        // if the health is low, 1 turns to 0
                        if (posUsedPriority != 0) {
                            int valueWritten = priorities - (int) Math.pow(10, 4-posUsed);
                            rc.writeSharedArray(4, valueWritten);
                        }
                    }
                }
            }
        }
    }

    static void safeMine(RobotController rc, MapLocation mineLocation) throws GameActionException {
        // leave lead to regen
        while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
            isMining = true;
            rc.mineGold(mineLocation);
        }
        while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
            isMining = true;
            rc.mineLead(mineLocation);
        }
        isMining = false;
    }
}