package drake;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static drake.util.Communication.*;


public class ArchonController {
    static int archIndex = -1, miners = 0, soldiers = 0;
    static boolean isAlpha = false;
    static int prevEnemySeven = 0, prevEnemyEight = 0, prevEnemyNine = 0, prevEnemyTen = 0, turnsSeven = 0, turnsEight = 0, turnsNine = 0, turnsTen = 0;

    public static void runArchon(RobotController rc) throws GameActionException {
        if (archIndex == -1) {
            archIndex = firstArchIndexEmpty(rc);
            writeOwnArchLoc(rc, firstArchIndexEmpty(rc));
            if (archIndex == 0) {
                isAlpha = true;
            }
        }

        if (rc.getHealth() <= 60) {
            throwFlag(rc, archIndex, 3);
        }

        int archons = rc.getArchonCount();
        miners = readNumMiners(rc);
        soldiers = readNumSoldiers(rc);

        if (isAlpha) {
            int currentTurn = rc.getRoundNum();

            if (rc.readSharedArray(7) != prevEnemySeven) {
                turnsSeven = currentTurn + 20;
            }

            if (rc.readSharedArray(8) != prevEnemyEight) {
                turnsEight = currentTurn + 20;
            }

            if (rc.readSharedArray(9) != prevEnemyNine) {
                turnsNine = currentTurn + 20;
            }

            if (rc.readSharedArray(10) != prevEnemyTen) {
                turnsTen = currentTurn + 20;
            }

            if (currentTurn == turnsSeven) {
                rc.writeSharedArray(7, 0);
            }

            if (currentTurn == turnsEight) {
                rc.writeSharedArray(8, 0);
            }

            if (currentTurn == turnsNine) {
                rc.writeSharedArray(9, 0);
            }

            if (currentTurn == turnsTen) {
                rc.writeSharedArray(10, 0);
            }
        }

        // TODO: Build order heuristic (must take into account case when archon is initially visible)
    }
}