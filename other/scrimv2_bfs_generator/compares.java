// TODO: insert the try/catch block
if (rc.onTheMap(l1101)) {
	if (!rc.isLocationOccupied(l1101)) {
		r1101 = rc.senseRubble(l1101);
		if (v1101 > v0000 + r1101) {
			v1101 = v0000 + r1101;
			d1101 = Direction.NORTHWEST;
		}
	}
}
if (rc.onTheMap(l0001)) {
	if (!rc.isLocationOccupied(l0001)) {
		r0001 = rc.senseRubble(l0001);
		if (v0001 > v0000 + r0001) {
			v0001 = v0000 + r0001;
			d0001 = Direction.NORTH;
		}
	}
}
if (rc.onTheMap(l0101)) {
	if (!rc.isLocationOccupied(l0101)) {
		r0101 = rc.senseRubble(l0101);
		if (v0101 > v0000 + r0101) {
			v0101 = v0000 + r0101;
			d0101 = Direction.NORTHEAST;
		}
	}
}
if (rc.onTheMap(l1100)) {
	if (!rc.isLocationOccupied(l1100)) {
		r1100 = rc.senseRubble(l1100);
		if (v1100 > v0000 + r1100) {
			v1100 = v0000 + r1100;
			d1100 = Direction.WEST;
		}
	}
}
if (rc.onTheMap(l0100)) {
	if (!rc.isLocationOccupied(l0100)) {
		r0100 = rc.senseRubble(l0100);
		if (v0100 > v0000 + r0100) {
			v0100 = v0000 + r0100;
			d0100 = Direction.EAST;
		}
	}
}
if (rc.onTheMap(l1111)) {
	if (!rc.isLocationOccupied(l1111)) {
		r1111 = rc.senseRubble(l1111);
		if (v1111 > v0000 + r1111) {
			v1111 = v0000 + r1111;
			d1111 = Direction.SOUTHWEST;
		}
	}
}
if (rc.onTheMap(l0011)) {
	if (!rc.isLocationOccupied(l0011)) {
		r0011 = rc.senseRubble(l0011);
		if (v0011 > v0000 + r0011) {
			v0011 = v0000 + r0011;
			d0011 = Direction.SOUTH;
		}
	}
}
if (rc.onTheMap(l0111)) {
	if (!rc.isLocationOccupied(l0111)) {
		r0111 = rc.senseRubble(l0111);
		if (v0111 > v0000 + r0111) {
			v0111 = v0000 + r0111;
			d0111 = Direction.SOUTHEAST;
		}
	}
}
if (rc.onTheMap(l1102)) {
	r1102 = rc.senseRubble(l1102);
	if (v1102 > v1101 + r1102) {
		v1102 = v1101 + r1102;
		d1102 = d1101;
	}
	if (v1102 > v0001 + r1102) {
		v1102 = v0001 + r1102;
		d1102 = d0001;
	}
}
if (rc.onTheMap(l0002)) {
	r0002 = rc.senseRubble(l0002);
	if (v0002 > v1101 + r0002) {
		v0002 = v1101 + r0002;
		d0002 = d1101;
	}
	if (v0002 > v0001 + r0002) {
		v0002 = v0001 + r0002;
		d0002 = d0001;
	}
	if (v0002 > v0101 + r0002) {
		v0002 = v0101 + r0002;
		d0002 = d0101;
	}
}
if (rc.onTheMap(l0102)) {
	r0102 = rc.senseRubble(l0102);
	if (v0102 > v0001 + r0102) {
		v0102 = v0001 + r0102;
		d0102 = d0001;
	}
	if (v0102 > v0101 + r0102) {
		v0102 = v0101 + r0102;
		d0102 = d0101;
	}
}
if (rc.onTheMap(l1201)) {
	r1201 = rc.senseRubble(l1201);
	if (v1201 > v1100 + r1201) {
		v1201 = v1100 + r1201;
		d1201 = d1100;
	}
	if (v1201 > v1101 + r1201) {
		v1201 = v1101 + r1201;
		d1201 = d1101;
	}
}
if (rc.onTheMap(l0201)) {
	r0201 = rc.senseRubble(l0201);
	if (v0201 > v0100 + r0201) {
		v0201 = v0100 + r0201;
		d0201 = d0100;
	}
	if (v0201 > v0101 + r0201) {
		v0201 = v0101 + r0201;
		d0201 = d0101;
	}
}
if (rc.onTheMap(l1200)) {
	r1200 = rc.senseRubble(l1200);
	if (v1200 > v1111 + r1200) {
		v1200 = v1111 + r1200;
		d1200 = d1111;
	}
	if (v1200 > v1100 + r1200) {
		v1200 = v1100 + r1200;
		d1200 = d1100;
	}
	if (v1200 > v1101 + r1200) {
		v1200 = v1101 + r1200;
		d1200 = d1101;
	}
}
if (rc.onTheMap(l0200)) {
	r0200 = rc.senseRubble(l0200);
	if (v0200 > v0111 + r0200) {
		v0200 = v0111 + r0200;
		d0200 = d0111;
	}
	if (v0200 > v0100 + r0200) {
		v0200 = v0100 + r0200;
		d0200 = d0100;
	}
	if (v0200 > v0101 + r0200) {
		v0200 = v0101 + r0200;
		d0200 = d0101;
	}
}
if (rc.onTheMap(l1211)) {
	r1211 = rc.senseRubble(l1211);
	if (v1211 > v1111 + r1211) {
		v1211 = v1111 + r1211;
		d1211 = d1111;
	}
	if (v1211 > v1100 + r1211) {
		v1211 = v1100 + r1211;
		d1211 = d1100;
	}
}
if (rc.onTheMap(l0211)) {
	r0211 = rc.senseRubble(l0211);
	if (v0211 > v0111 + r0211) {
		v0211 = v0111 + r0211;
		d0211 = d0111;
	}
	if (v0211 > v0100 + r0211) {
		v0211 = v0100 + r0211;
		d0211 = d0100;
	}
}
if (rc.onTheMap(l1112)) {
	r1112 = rc.senseRubble(l1112);
	if (v1112 > v1111 + r1112) {
		v1112 = v1111 + r1112;
		d1112 = d1111;
	}
	if (v1112 > v0011 + r1112) {
		v1112 = v0011 + r1112;
		d1112 = d0011;
	}
}
if (rc.onTheMap(l0012)) {
	r0012 = rc.senseRubble(l0012);
	if (v0012 > v1111 + r0012) {
		v0012 = v1111 + r0012;
		d0012 = d1111;
	}
	if (v0012 > v0011 + r0012) {
		v0012 = v0011 + r0012;
		d0012 = d0011;
	}
	if (v0012 > v0111 + r0012) {
		v0012 = v0111 + r0012;
		d0012 = d0111;
	}
}
if (rc.onTheMap(l0112)) {
	r0112 = rc.senseRubble(l0112);
	if (v0112 > v0011 + r0112) {
		v0112 = v0011 + r0112;
		d0112 = d0011;
	}
	if (v0112 > v0111 + r0112) {
		v0112 = v0111 + r0112;
		d0112 = d0111;
	}
}
if (rc.onTheMap(l1103)) {
	r1103 = rc.senseRubble(l1103);
	if (v1103 > v1102 + r1103) {
		v1103 = v1102 + r1103;
		d1103 = d1102;
	}
	if (v1103 > v0002 + r1103) {
		v1103 = v0002 + r1103;
		d1103 = d0002;
	}
}
if (rc.onTheMap(l0003)) {
	r0003 = rc.senseRubble(l0003);
	if (v0003 > v1102 + r0003) {
		v0003 = v1102 + r0003;
		d0003 = d1102;
	}
	if (v0003 > v0002 + r0003) {
		v0003 = v0002 + r0003;
		d0003 = d0002;
	}
	if (v0003 > v0102 + r0003) {
		v0003 = v0102 + r0003;
		d0003 = d0102;
	}
}
if (rc.onTheMap(l0103)) {
	r0103 = rc.senseRubble(l0103);
	if (v0103 > v0002 + r0103) {
		v0103 = v0002 + r0103;
		d0103 = d0002;
	}
	if (v0103 > v0102 + r0103) {
		v0103 = v0102 + r0103;
		d0103 = d0102;
	}
}
if (rc.onTheMap(l1202)) {
	r1202 = rc.senseRubble(l1202);
	if (v1202 > v1201 + r1202) {
		v1202 = v1201 + r1202;
		d1202 = d1201;
	}
	if (v1202 > v1101 + r1202) {
		v1202 = v1101 + r1202;
		d1202 = d1101;
	}
	if (v1202 > v1102 + r1202) {
		v1202 = v1102 + r1202;
		d1202 = d1102;
	}
}
if (rc.onTheMap(l0202)) {
	r0202 = rc.senseRubble(l0202);
	if (v0202 > v0101 + r0202) {
		v0202 = v0101 + r0202;
		d0202 = d0101;
	}
	if (v0202 > v0102 + r0202) {
		v0202 = v0102 + r0202;
		d0202 = d0102;
	}
	if (v0202 > v0201 + r0202) {
		v0202 = v0201 + r0202;
		d0202 = d0201;
	}
}
if (rc.onTheMap(l1301)) {
	r1301 = rc.senseRubble(l1301);
	if (v1301 > v1200 + r1301) {
		v1301 = v1200 + r1301;
		d1301 = d1200;
	}
	if (v1301 > v1201 + r1301) {
		v1301 = v1201 + r1301;
		d1301 = d1201;
	}
}
if (rc.onTheMap(l0301)) {
	r0301 = rc.senseRubble(l0301);
	if (v0301 > v0200 + r0301) {
		v0301 = v0200 + r0301;
		d0301 = d0200;
	}
	if (v0301 > v0201 + r0301) {
		v0301 = v0201 + r0301;
		d0301 = d0201;
	}
}
if (rc.onTheMap(l1300)) {
	r1300 = rc.senseRubble(l1300);
	if (v1300 > v1211 + r1300) {
		v1300 = v1211 + r1300;
		d1300 = d1211;
	}
	if (v1300 > v1200 + r1300) {
		v1300 = v1200 + r1300;
		d1300 = d1200;
	}
	if (v1300 > v1201 + r1300) {
		v1300 = v1201 + r1300;
		d1300 = d1201;
	}
}
if (rc.onTheMap(l0300)) {
	r0300 = rc.senseRubble(l0300);
	if (v0300 > v0211 + r0300) {
		v0300 = v0211 + r0300;
		d0300 = d0211;
	}
	if (v0300 > v0200 + r0300) {
		v0300 = v0200 + r0300;
		d0300 = d0200;
	}
	if (v0300 > v0201 + r0300) {
		v0300 = v0201 + r0300;
		d0300 = d0201;
	}
}
if (rc.onTheMap(l1311)) {
	r1311 = rc.senseRubble(l1311);
	if (v1311 > v1211 + r1311) {
		v1311 = v1211 + r1311;
		d1311 = d1211;
	}
	if (v1311 > v1200 + r1311) {
		v1311 = v1200 + r1311;
		d1311 = d1200;
	}
}
if (rc.onTheMap(l0311)) {
	r0311 = rc.senseRubble(l0311);
	if (v0311 > v0211 + r0311) {
		v0311 = v0211 + r0311;
		d0311 = d0211;
	}
	if (v0311 > v0200 + r0311) {
		v0311 = v0200 + r0311;
		d0311 = d0200;
	}
}
if (rc.onTheMap(l1212)) {
	r1212 = rc.senseRubble(l1212);
	if (v1212 > v1211 + r1212) {
		v1212 = v1211 + r1212;
		d1212 = d1211;
	}
	if (v1212 > v1112 + r1212) {
		v1212 = v1112 + r1212;
		d1212 = d1112;
	}
	if (v1212 > v1111 + r1212) {
		v1212 = v1111 + r1212;
		d1212 = d1111;
	}
}
if (rc.onTheMap(l0212)) {
	r0212 = rc.senseRubble(l0212);
	if (v0212 > v0112 + r0212) {
		v0212 = v0112 + r0212;
		d0212 = d0112;
	}
	if (v0212 > v0111 + r0212) {
		v0212 = v0111 + r0212;
		d0212 = d0111;
	}
	if (v0212 > v0211 + r0212) {
		v0212 = v0211 + r0212;
		d0212 = d0211;
	}
}
if (rc.onTheMap(l1113)) {
	r1113 = rc.senseRubble(l1113);
	if (v1113 > v1112 + r1113) {
		v1113 = v1112 + r1113;
		d1113 = d1112;
	}
	if (v1113 > v0012 + r1113) {
		v1113 = v0012 + r1113;
		d1113 = d0012;
	}
}
if (rc.onTheMap(l0013)) {
	r0013 = rc.senseRubble(l0013);
	if (v0013 > v1112 + r0013) {
		v0013 = v1112 + r0013;
		d0013 = d1112;
	}
	if (v0013 > v0012 + r0013) {
		v0013 = v0012 + r0013;
		d0013 = d0012;
	}
	if (v0013 > v0112 + r0013) {
		v0013 = v0112 + r0013;
		d0013 = d0112;
	}
}
if (rc.onTheMap(l0113)) {
	r0113 = rc.senseRubble(l0113);
	if (v0113 > v0012 + r0113) {
		v0113 = v0012 + r0113;
		d0113 = d0012;
	}
	if (v0113 > v0112 + r0113) {
		v0113 = v0112 + r0113;
		d0113 = d0112;
	}
}
if (rc.onTheMap(l1204)) {
	r1204 = rc.senseRubble(l1204);
	if (v1204 > v1103 + r1204) {
		v1204 = v1103 + r1204;
		d1204 = d1103;
	}
}
if (rc.onTheMap(l1104)) {
	r1104 = rc.senseRubble(l1104);
	if (v1104 > v1103 + r1104) {
		v1104 = v1103 + r1104;
		d1104 = d1103;
	}
	if (v1104 > v0003 + r1104) {
		v1104 = v0003 + r1104;
		d1104 = d0003;
	}
}
if (rc.onTheMap(l0004)) {
	r0004 = rc.senseRubble(l0004);
	if (v0004 > v1103 + r0004) {
		v0004 = v1103 + r0004;
		d0004 = d1103;
	}
	if (v0004 > v0003 + r0004) {
		v0004 = v0003 + r0004;
		d0004 = d0003;
	}
	if (v0004 > v0103 + r0004) {
		v0004 = v0103 + r0004;
		d0004 = d0103;
	}
}
if (rc.onTheMap(l0104)) {
	r0104 = rc.senseRubble(l0104);
	if (v0104 > v0003 + r0104) {
		v0104 = v0003 + r0104;
		d0104 = d0003;
	}
	if (v0104 > v0103 + r0104) {
		v0104 = v0103 + r0104;
		d0104 = d0103;
	}
}
if (rc.onTheMap(l0204)) {
	r0204 = rc.senseRubble(l0204);
	if (v0204 > v0103 + r0204) {
		v0204 = v0103 + r0204;
		d0204 = d0103;
	}
}
if (rc.onTheMap(l1303)) {
	r1303 = rc.senseRubble(l1303);
	if (v1303 > v1202 + r1303) {
		v1303 = v1202 + r1303;
		d1303 = d1202;
	}
}
if (rc.onTheMap(l1203)) {
	r1203 = rc.senseRubble(l1203);
	if (v1203 > v1202 + r1203) {
		v1203 = v1202 + r1203;
		d1203 = d1202;
	}
	if (v1203 > v1102 + r1203) {
		v1203 = v1102 + r1203;
		d1203 = d1102;
	}
	if (v1203 > v1103 + r1203) {
		v1203 = v1103 + r1203;
		d1203 = d1103;
	}
}
if (rc.onTheMap(l0203)) {
	r0203 = rc.senseRubble(l0203);
	if (v0203 > v0102 + r0203) {
		v0203 = v0102 + r0203;
		d0203 = d0102;
	}
	if (v0203 > v0103 + r0203) {
		v0203 = v0103 + r0203;
		d0203 = d0103;
	}
	if (v0203 > v0202 + r0203) {
		v0203 = v0202 + r0203;
		d0203 = d0202;
	}
}
if (rc.onTheMap(l0303)) {
	r0303 = rc.senseRubble(l0303);
	if (v0303 > v0202 + r0303) {
		v0303 = v0202 + r0303;
		d0303 = d0202;
	}
}
if (rc.onTheMap(l1402)) {
	r1402 = rc.senseRubble(l1402);
	if (v1402 > v1301 + r1402) {
		v1402 = v1301 + r1402;
		d1402 = d1301;
	}
}
if (rc.onTheMap(l1302)) {
	r1302 = rc.senseRubble(l1302);
	if (v1302 > v1301 + r1302) {
		v1302 = v1301 + r1302;
		d1302 = d1301;
	}
	if (v1302 > v1201 + r1302) {
		v1302 = v1201 + r1302;
		d1302 = d1201;
	}
	if (v1302 > v1202 + r1302) {
		v1302 = v1202 + r1302;
		d1302 = d1202;
	}
}
if (rc.onTheMap(l0302)) {
	r0302 = rc.senseRubble(l0302);
	if (v0302 > v0201 + r0302) {
		v0302 = v0201 + r0302;
		d0302 = d0201;
	}
	if (v0302 > v0202 + r0302) {
		v0302 = v0202 + r0302;
		d0302 = d0202;
	}
	if (v0302 > v0301 + r0302) {
		v0302 = v0301 + r0302;
		d0302 = d0301;
	}
}
if (rc.onTheMap(l0402)) {
	r0402 = rc.senseRubble(l0402);
	if (v0402 > v0301 + r0402) {
		v0402 = v0301 + r0402;
		d0402 = d0301;
	}
}
if (rc.onTheMap(l1401)) {
	r1401 = rc.senseRubble(l1401);
	if (v1401 > v1300 + r1401) {
		v1401 = v1300 + r1401;
		d1401 = d1300;
	}
	if (v1401 > v1301 + r1401) {
		v1401 = v1301 + r1401;
		d1401 = d1301;
	}
}
if (rc.onTheMap(l0401)) {
	r0401 = rc.senseRubble(l0401);
	if (v0401 > v0300 + r0401) {
		v0401 = v0300 + r0401;
		d0401 = d0300;
	}
	if (v0401 > v0301 + r0401) {
		v0401 = v0301 + r0401;
		d0401 = d0301;
	}
}
if (rc.onTheMap(l1400)) {
	r1400 = rc.senseRubble(l1400);
	if (v1400 > v1311 + r1400) {
		v1400 = v1311 + r1400;
		d1400 = d1311;
	}
	if (v1400 > v1300 + r1400) {
		v1400 = v1300 + r1400;
		d1400 = d1300;
	}
	if (v1400 > v1301 + r1400) {
		v1400 = v1301 + r1400;
		d1400 = d1301;
	}
}
if (rc.onTheMap(l0400)) {
	r0400 = rc.senseRubble(l0400);
	if (v0400 > v0311 + r0400) {
		v0400 = v0311 + r0400;
		d0400 = d0311;
	}
	if (v0400 > v0300 + r0400) {
		v0400 = v0300 + r0400;
		d0400 = d0300;
	}
	if (v0400 > v0301 + r0400) {
		v0400 = v0301 + r0400;
		d0400 = d0301;
	}
}
if (rc.onTheMap(l1411)) {
	r1411 = rc.senseRubble(l1411);
	if (v1411 > v1311 + r1411) {
		v1411 = v1311 + r1411;
		d1411 = d1311;
	}
	if (v1411 > v1300 + r1411) {
		v1411 = v1300 + r1411;
		d1411 = d1300;
	}
}
if (rc.onTheMap(l0411)) {
	r0411 = rc.senseRubble(l0411);
	if (v0411 > v0311 + r0411) {
		v0411 = v0311 + r0411;
		d0411 = d0311;
	}
	if (v0411 > v0300 + r0411) {
		v0411 = v0300 + r0411;
		d0411 = d0300;
	}
}
if (rc.onTheMap(l1412)) {
	r1412 = rc.senseRubble(l1412);
	if (v1412 > v1311 + r1412) {
		v1412 = v1311 + r1412;
		d1412 = d1311;
	}
}
if (rc.onTheMap(l1312)) {
	r1312 = rc.senseRubble(l1312);
	if (v1312 > v1311 + r1312) {
		v1312 = v1311 + r1312;
		d1312 = d1311;
	}
	if (v1312 > v1212 + r1312) {
		v1312 = v1212 + r1312;
		d1312 = d1212;
	}
	if (v1312 > v1211 + r1312) {
		v1312 = v1211 + r1312;
		d1312 = d1211;
	}
}
if (rc.onTheMap(l0312)) {
	r0312 = rc.senseRubble(l0312);
	if (v0312 > v0212 + r0312) {
		v0312 = v0212 + r0312;
		d0312 = d0212;
	}
	if (v0312 > v0211 + r0312) {
		v0312 = v0211 + r0312;
		d0312 = d0211;
	}
	if (v0312 > v0311 + r0312) {
		v0312 = v0311 + r0312;
		d0312 = d0311;
	}
}
if (rc.onTheMap(l0412)) {
	r0412 = rc.senseRubble(l0412);
	if (v0412 > v0311 + r0412) {
		v0412 = v0311 + r0412;
		d0412 = d0311;
	}
}
if (rc.onTheMap(l1313)) {
	r1313 = rc.senseRubble(l1313);
	if (v1313 > v1212 + r1313) {
		v1313 = v1212 + r1313;
		d1313 = d1212;
	}
}
if (rc.onTheMap(l1213)) {
	r1213 = rc.senseRubble(l1213);
	if (v1213 > v1212 + r1213) {
		v1213 = v1212 + r1213;
		d1213 = d1212;
	}
	if (v1213 > v1113 + r1213) {
		v1213 = v1113 + r1213;
		d1213 = d1113;
	}
	if (v1213 > v1112 + r1213) {
		v1213 = v1112 + r1213;
		d1213 = d1112;
	}
}
if (rc.onTheMap(l0213)) {
	r0213 = rc.senseRubble(l0213);
	if (v0213 > v0113 + r0213) {
		v0213 = v0113 + r0213;
		d0213 = d0113;
	}
	if (v0213 > v0112 + r0213) {
		v0213 = v0112 + r0213;
		d0213 = d0112;
	}
	if (v0213 > v0212 + r0213) {
		v0213 = v0212 + r0213;
		d0213 = d0212;
	}
}
if (rc.onTheMap(l0313)) {
	r0313 = rc.senseRubble(l0313);
	if (v0313 > v0212 + r0313) {
		v0313 = v0212 + r0313;
		d0313 = d0212;
	}
}
if (rc.onTheMap(l1214)) {
	r1214 = rc.senseRubble(l1214);
	if (v1214 > v1113 + r1214) {
		v1214 = v1113 + r1214;
		d1214 = d1113;
	}
}
if (rc.onTheMap(l1114)) {
	r1114 = rc.senseRubble(l1114);
	if (v1114 > v1113 + r1114) {
		v1114 = v1113 + r1114;
		d1114 = d1113;
	}
	if (v1114 > v0013 + r1114) {
		v1114 = v0013 + r1114;
		d1114 = d0013;
	}
}
if (rc.onTheMap(l0014)) {
	r0014 = rc.senseRubble(l0014);
	if (v0014 > v1113 + r0014) {
		v0014 = v1113 + r0014;
		d0014 = d1113;
	}
	if (v0014 > v0013 + r0014) {
		v0014 = v0013 + r0014;
		d0014 = d0013;
	}
	if (v0014 > v0113 + r0014) {
		v0014 = v0113 + r0014;
		d0014 = d0113;
	}
}
if (rc.onTheMap(l0114)) {
	r0114 = rc.senseRubble(l0114);
	if (v0114 > v0013 + r0114) {
		v0114 = v0013 + r0114;
		d0114 = d0013;
	}
	if (v0114 > v0113 + r0114) {
		v0114 = v0113 + r0114;
		d0114 = d0113;
	}
}
if (rc.onTheMap(l0214)) {
	r0214 = rc.senseRubble(l0214);
	if (v0214 > v0113 + r0214) {
		v0214 = v0113 + r0214;
		d0214 = d0113;
	}
}
