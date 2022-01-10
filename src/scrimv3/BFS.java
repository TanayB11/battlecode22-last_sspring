package scrimv3;
import battlecode.common.*;
import java.util.ArrayDeque;

public class BFS {
    public BFS() {}

    static void BFS(RobotController rc, MapLocation goal) {
        ArrayDeque<BFSNode> q = new ArrayDeque<BFSNode>();
        BFSNode currLoc = new BFSNode(rc.getLocation());
        currLoc.isExplored = true;

        while (!q.isEmpty()) {
            BFSNode node = q.removeLast();
            if (node.loc.equals(goal)) {
                // TODO
            }
        }
    }
}

class BFSNode {
    public MapLocation loc = null;
    public boolean isExplored;

    public BFSNode(MapLocation location) {
        loc = location;
        isExplored = false;
    }
}