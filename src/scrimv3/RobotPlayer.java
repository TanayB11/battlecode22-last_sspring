package scrimv3;

import battlecode.common.*;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    static int turnCount = 0;

    /** * run() is the method that is called when a robot is instantiated in the Battlecode world. **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());

            try {
                switch (rc.getType()) {
                    case ARCHON:
                        ArchonController.runArchon(rc); break;
                    case MINER:
                        MinerController.runMiner(rc); break;
                    case SOLDIER:
                        SoldierController.runSoldier(rc); break;
                    case LABORATORY:
                        LabController.runLab(rc); break;
                    case WATCHTOWER:
                        WatchtowerController.runWatchtower(rc); break;
                    case BUILDER:
                        BuilderController.runBuilder(rc); break;
                    case SAGE:
                        SageController.runSage(rc); break;
                }
            } catch (GameActionException e) { // We did something illegal
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } catch (Exception e) { // Probably a bug
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } finally {
                Clock.yield(); // End our turn
            }
        }
        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
}