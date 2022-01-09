package scrimv2.util;
import battlecode.common.*;

public class DroidBFS extends BFS {
    public DroidBFS(RobotController rc){
        super(rc);
    }
    // 69 locs to check
    // first digit: x dir, second digit: y dir, 0 means positive, 1 negative
    // measuring from center
    // TODO: optimize onTheMap

    // leftmost line vertical in vision radius
    // https://discord.com/channels/@me/927343523836076102/929468448575217684
    static MapLocation l1412;
    static double v1412;
    static Direction d1412;
    static double r1412;

    static MapLocation l1411;
    static double v1411;
    static Direction d1411;
    static double r1411;

    static MapLocation l1400;
    static double v1400;
    static Direction d1400;
    static double r1400;

    static MapLocation l1401;
    static double v1401;
    static Direction d1401;
    static double r1401;

    static MapLocation l1402;
    static double v1402;
    static Direction d1402;
    static double r1402;

    // second leftmost vertical line in vision radius
    static MapLocation l1313;
    static double v1313;
    static Direction d1313;
    static double r1313;

    static MapLocation l1312;
    static double v1312;
    static Direction d1312;
    static double r1312;

    static MapLocation l1311;
    static double v1311;
    static Direction d1311;
    static double r1311;

    static MapLocation l1300;
    static double v1300;
    static Direction d1300;
    static double r1300;

    static MapLocation l1301;
    static double v1301;
    static Direction d1301;
    static double r1301;

    static MapLocation l1302;
    static double v1302;
    static Direction d1302;
    static double r1302;

    static MapLocation l1303;
    static double v1303;
    static Direction d1303;
    static double r1303;

    // third from left vertical line
    static MapLocation l1214;
    static double v1214;
    static Direction d1214;
    static double r1214;

    static MapLocation l1213;
    static double v1213;
    static Direction d1213;
    static double r1213;

    static MapLocation l1212;
    static double v1212;
    static Direction d1212;
    static double r1212;

    static MapLocation l1211;
    static double v1211;
    static Direction d1211;
    static double r1211;

    static MapLocation l1200;
    static double v1200;
    static Direction d1200;
    static double r1200;

    static MapLocation l1201;
    static double v1201;
    static Direction d1201;
    static double r1201;

    static MapLocation l1202;
    static double v1202;
    static Direction d1202;
    static double r1202;

    static MapLocation l1203;
    static double v1203;
    static Direction d1203;
    static double r1203;

    static MapLocation l1204;
    static double v1204;
    static Direction d1204;
    static double r1204;

    // fourth from left vertical line
    static MapLocation l1114;
    static double v1114;
    static Direction d1114;
    static double r1114;

    static MapLocation l1113;
    static double v1113;
    static Direction d1113;
    static double r1113;

    static MapLocation l1112;
    static double v1112;
    static Direction d1112;
    static double r1112;

    static MapLocation l1111;
    static double v1111;
    static Direction d1111;
    static double r1111;

    static MapLocation l1100;
    static double v1100;
    static Direction d1100;
    static double r1100;

    static MapLocation l1101;
    static double v1101;
    static Direction d1101;
    static double r1101;

    static MapLocation l1102;
    static double v1102;
    static Direction d1102;
    static double r1102;

    static MapLocation l1103;
    static double v1103;
    static Direction d1103;
    static double r1103;

    static MapLocation l1104;
    static double v1104;
    static Direction d1104;
    static double r1104;

    // middle vertical line
    static MapLocation l0014;
    static double v0014;
    static Direction d0014;
    static double r0014;

    static MapLocation l0013;
    static double v0013;
    static Direction d0013;
    static double r0013;

    static MapLocation l0012;
    static double v0012;
    static Direction d0012;
    static double r0012;

    static MapLocation l0011;
    static double v0011;
    static Direction d0011;
    static double r0011;

    static MapLocation l0000;
    static double v0000;
    static Direction d0000;
    static double r0000;

    static MapLocation l0001;
    static double v0001;
    static Direction d0001;
    static double r0001;

    static MapLocation l0002;
    static double v0002;
    static Direction d0002;
    static double r0002;

    static MapLocation l0003;
    static double v0003;
    static Direction d0003;
    static double r0003;

    static MapLocation l0004;
    static double v0004;
    static Direction d0004;
    static double r0004;

    // immediately right of middle vertical line
    static MapLocation l0114;
    static double v0114;
    static Direction d0114;
    static double r0114;

    static MapLocation l0113;
    static double v0113;
    static Direction d0113;
    static double r0113;

    static MapLocation l0112;
    static double v0112;
    static Direction d0112;
    static double r0112;

    static MapLocation l0111;
    static double v0111;
    static Direction d0111;
    static double r0111;

    static MapLocation l0100;
    static double v0100;
    static Direction d0100;
    static double r0100;

    static MapLocation l0101;
    static double v0101;
    static Direction d0101;
    static double r0101;

    static MapLocation l0102;
    static double v0102;
    static Direction d0102;
    static double r0102;

    static MapLocation l0103;
    static double v0103;
    static Direction d0103;
    static double r0103;

    static MapLocation l0104;
    static double v0104;
    static Direction d0104;
    static double r0104;

    // two right of middle vertical line
    static MapLocation l0214;
    static double v0214;
    static Direction d0214;
    static double r0214;

    static MapLocation l0213;
    static double v0213;
    static Direction d0213;
    static double r0213;

    static MapLocation l0212;
    static double v0212;
    static Direction d0212;
    static double r0212;

    static MapLocation l0211;
    static double v0211;
    static Direction d0211;
    static double r0211;

    static MapLocation l0200;
    static double v0200;
    static Direction d0200;
    static double r0200;

    static MapLocation l0201;
    static double v0201;
    static Direction d0201;
    static double r0201;

    static MapLocation l0202;
    static double v0202;
    static Direction d0202;
    static double r0202;

    static MapLocation l0203;
    static double v0203;
    static Direction d0203;
    static double r0203;

    static MapLocation l0204;
    static double v0204;
    static Direction d0204;
    static double r0204;

    // three right of middle vertical line
    static MapLocation l0313;
    static double v0313;
    static Direction d0313;
    static double r0313;

    static MapLocation l0312;
    static double v0312;
    static Direction d0312;
    static double r0312;

    static MapLocation l0311;
    static double v0311;
    static Direction d0311;
    static double r0311;

    static MapLocation l0300;
    static double v0300;
    static Direction d0300;
    static double r0300;

    static MapLocation l0301;
    static double v0301;
    static Direction d0301;
    static double r0301;

    static MapLocation l0302;
    static double v0302;
    static Direction d0302;
    static double r0302;

    static MapLocation l0303;
    static double v0303;
    static Direction d0303;

     // four right of middle vertical line
    static MapLocation l0412;
    static double v0412;
    static Direction d0412;
    static double r0412;

    static MapLocation l0411;
    static double v0411;
    static Direction d0411;
    static double r0411;

    static MapLocation l0400;
    static double v0400;
    static Direction d0400;
    static double r0400;

    static MapLocation l0401;
    static double v0401;
    static Direction d0401;
    static double r0401;

    static MapLocation l0402;
    static double v0402;
    static Direction d0402;
    static double r0402;

    // ACTUAL BFS
    // this is basically a dijkstra
    // implement BFS class
    Direction getBestDir(MapLocation target){
      l0000 = rc.getLocation();
      v0000 = 0;

      l1101 = rc.adjacentLocation(Direction.NORTHWEST);
      v1101 = 1000000;
      d1101 = null;

      l0001 = rc.adjacentLocation(Direction.NORTH);
      v0001 = 1000000;
      d0001 = null;

      l0101 = rc.adjacentLocation(Direction.NORTHEAST);
      v0101 = 1000000;
      d0101 = null;

      l1100 = rc.adjacentLocation(Direction.WEST);
      v1100 = 1000000;
      d1100 = null;

      l0100 = rc.adjacentLocation(Direction.EAST);
      v0100 = 1000000;
      d0100 = null;

      l1111 = rc.adjacentLocation(Direction.SOUTHWEST);
      v1111 = 1000000;
      d1111 = null;

      l0011 = rc.adjacentLocation(Direction.SOUTH);
      v0011 = 1000000;
      d0011 = null;

      l0111 = rc.adjacentLocation(Direction.SOUTHEAST);
      v0111 = 1000000;
      d0111 = null;

      l1102 = l1101.add(Direction.NORTH);
      v1102 = 1000000;
      d1102 = null;

      l0002 = l0001.add(Direction.NORTH);
      v0002 = 1000000;
      d0002 = null;

      l0102 = l0101.add(Direction.NORTH);
      v0102 = 1000000;
      d0102 = null;

      l1201 = l1101.add(Direction.WEST);
      v1201 = 1000000;
      d1201 = null;

      l0201 = l0101.add(Direction.EAST);
      v0201 = 1000000;
      d0201 = null;

      l1200 = l1100.add(Direction.WEST);
      v1200 = 1000000;
      d1200 = null;

      l0200 = l0100.add(Direction.EAST);
      v0200 = 1000000;
      d0200 = null;

      l1211 = l1111.add(Direction.WEST);
      v1211 = 1000000;
      d1211 = null;

      l0211 = l0111.add(Direction.EAST);
      v0211 = 1000000;
      d0211 = null;

      l1112 = l1111.add(Direction.SOUTH);
      v1112 = 1000000;
      d1112 = null;

      l0012 = l0011.add(Direction.SOUTH);
      v0012 = 1000000;
      d0012 = null;

      l0112 = l0111.add(Direction.SOUTH);
      v0112 = 1000000;
      d0112 = null;

      l1103 = l1102.add(Direction.NORTH);
      v1103 = 1000000;
      d1103 = null;

      l0003 = l0002.add(Direction.NORTH);
      v0003 = 1000000;
      d0003 = null;

      l0103 = l0102.add(Direction.NORTH);
      v0103 = 1000000;
      d0103 = null;

      l1202 = l1102.add(Direction.WEST);
      v1202 = 1000000;
      d1202 = null;

      l0202 = l0102.add(Direction.EAST);
      v0202 = 1000000;
      d0202 = null;

      l1301 = l1201.add(Direction.WEST);
      v1301 = 1000000;
      d1301 = null;

      l0301 = l0201.add(Direction.EAST);
      v0301 = 1000000;
      d0301 = null;

      l1300 = l1200.add(Direction.WEST);
      v1300 = 1000000;
      d1300 = null;

      l0300 = l0200.add(Direction.EAST);
      v0300 = 1000000;
      d0300 = null;

      l1311 = l1211.add(Direction.WEST);
      v1311 = 1000000;
      d1311 = null;

      l0311 = l0211.add(Direction.EAST);
      v0311 = 1000000;
      d0311 = null;

      l1212 = l1112.add(Direction.WEST);
      v1212 = 1000000;
      d1212 = null;

      l0212 = l0112.add(Direction.EAST);
      v0212 = 1000000;
      d0212 = null;

      l1113 = l1112.add(Direction.SOUTH);
      v1113 = 1000000;
      d1113 = null;

      l0013 = l0012.add(Direction.SOUTH);
      v0013 = 1000000;
      d0013 = null;

      l0113 = l0112.add(Direction.SOUTH);
      v0113 = 1000000;
      d0113 = null;

      l1204 = l1203.add(Direction.NORTH);
      v1204 = 1000000;
      d1204 = null;

      l1104 = l1103.add(Direction.NORTH);
      v1104 = 1000000;
      d1104 = null;

      l0004 = l0003.add(Direction.NORTH);
      v0004 = 1000000;
      d0004 = null;

      l0104 = l0103.add(Direction.NORTH);
      v0104 = 1000000;
      d0104 = null;

      l0204 = l0203.add(Direction.NORTH);
      v0204 = 1000000;
      d0204 = null;

      l1303 = l1203.add(Direction.WEST);
      v1303 = 1000000;
      d1303 = null;

      l1203 = l1202.add(Direction.NORTH);
      v1203 = 1000000;
      d1203 = null;

      l0203 = l0202.add(Direction.NORTH);
      v0203 = 1000000;
      d0203 = null;

      l0303 = l0203.add(Direction.EAST);
      v0303 = 1000000;
      d0303 = null;

      l1402 = l1302.add(Direction.WEST);
      v1402 = 1000000;
      d1402 = null;

      l1302 = l1202.add(Direction.WEST);
      v1302 = 1000000;
      d1302 = null;

      l0302 = l0202.add(Direction.EAST);
      v0302 = 1000000;
      d0302 = null;

      l0402 = l0302.add(Direction.EAST);
      v0402 = 1000000;
      d0402 = null;

      l1401 = l1301.add(Direction.WEST);
      v1401 = 1000000;
      d1401 = null;

      l0401 = l0301.add(Direction.EAST);
      v0401 = 1000000;
      d0401 = null;

      l1400 = l1300.add(Direction.WEST);
      v1400 = 1000000;
      d1400 = null;

      l0400 = l0300.add(Direction.EAST);
      v0400 = 1000000;
      d0400 = null;

      l1411 = l1311.add(Direction.WEST);
      v1411 = 1000000;
      d1411 = null;

      l0411 = l0311.add(Direction.EAST);
      v0411 = 1000000;
      d0411 = null;

      l1412 = l1312.add(Direction.WEST);
      v1412 = 1000000;
      d1412 = null;

      l1312 = l1212.add(Direction.WEST);
      v1312 = 1000000;
      d1312 = null;

      l0312 = l0212.add(Direction.EAST);
      v0312 = 1000000;
      d0312 = null;

      l0412 = l0312.add(Direction.EAST);
      v0412 = 1000000;
      d0412 = null;

      l1313 = l1213.add(Direction.WEST);
      v1313 = 1000000;
      d1313 = null;

      l1213 = l1212.add(Direction.SOUTH);
      v1213 = 1000000;
      d1213 = null;

      l0213 = l0212.add(Direction.SOUTH);
      v0213 = 1000000;
      d0213 = null;

      l0313 = l0213.add(Direction.EAST);
      v0313 = 1000000;
      d0313 = null;

      l1214 = l1213.add(Direction.SOUTH);
      v1214 = 1000000;
      d1214 = null;

      l1114 = l1113.add(Direction.SOUTH);
      v1114 = 1000000;
      d1114 = null;

      l0014 = l0013.add(Direction.SOUTH);
      v0014 = 1000000;
      d0014 = null;

      l0114 = l0113.add(Direction.SOUTH);
      v0114 = 1000000;
      d0114 = null;

      l0214 = l0213.add(Direction.SOUTH);
      v0214 = 1000000;
      d0214 = null;

      try {

      } catch (Exception e) {
          e.printStackTrace();
      }
      return null;
    }
}
