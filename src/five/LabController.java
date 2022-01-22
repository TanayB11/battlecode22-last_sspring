package five;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static five.util.Communication.readNumLabs;
import static five.util.Communication.writeNumLabs;
import static five.util.Miscellaneous.incomeAvgQ;

public class LabController {
    static boolean isInitialized = false;
    static int labIndex = 0;

    static void runLab(RobotController rc) throws GameActionException {
        if (!isInitialized) {
            labIndex = readNumLabs(rc);
            writeNumLabs(rc, labIndex+1);
        }

        double rollingProductionAverage = incomeAvgQ.calcAverageVal();

        if (labIndex == 0 && rc.canTransmute() && rc.getTransmutationRate() <= 4 && rollingProductionAverage >= 13) {
            rc.transmute();
        }

        if (labIndex == 1 && rc.canTransmute() && rc.getTransmutationRate() <= 4 && rollingProductionAverage >= 19) {
            rc.transmute();
        }

        if (labIndex == 2 && rc.canTransmute() && rc.getTransmutationRate() <= 4 && rollingProductionAverage >= 24) {
            rc.transmute();
        }
    }
}
