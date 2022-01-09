// TODO: insert the try/catch block
if (rc.onTheMap(l1101)) {
	if (!rc.isLocationOccupied(l1101)) {
		r1101 = rc.senseRubble(l1101);
		if (v0000 > v1101 + r0000) {
			v0000 = v1101 + r0000;
			d1101 = Direction.NORTHWEST;
		}
	}
}
if (rc.onTheMap(l0001)) {
	if (!rc.isLocationOccupied(l0001)) {
		r0001 = rc.senseRubble(l0001);
		if (v0000 > v0001 + r0000) {
			v0000 = v0001 + r0000;
			d0001 = Direction.NORTH;
		}
	}
}
if (rc.onTheMap(l0101)) {
	if (!rc.isLocationOccupied(l0101)) {
		r0101 = rc.senseRubble(l0101);
		if (v0000 > v0101 + r0000) {
			v0000 = v0101 + r0000;
			d0101 = Direction.NORTHEAST;
		}
	}
}
if (rc.onTheMap(l1100)) {
	if (!rc.isLocationOccupied(l1100)) {
		r1100 = rc.senseRubble(l1100);
		if (v0000 > v1100 + r0000) {
			v0000 = v1100 + r0000;
			d1100 = Direction.WEST;
		}
	}
}
if (rc.onTheMap(l0100)) {
	if (!rc.isLocationOccupied(l0100)) {
		r0100 = rc.senseRubble(l0100);
		if (v0000 > v0100 + r0000) {
			v0000 = v0100 + r0000;
			d0100 = Direction.EAST;
		}
	}
}
if (rc.onTheMap(l1111)) {
	if (!rc.isLocationOccupied(l1111)) {
		r1111 = rc.senseRubble(l1111);
		if (v0000 > v1111 + r0000) {
			v0000 = v1111 + r0000;
			d1111 = Direction.SOUTHWEST;
		}
	}
}
if (rc.onTheMap(l0011)) {
	if (!rc.isLocationOccupied(l0011)) {
		r0011 = rc.senseRubble(l0011);
		if (v0000 > v0011 + r0000) {
			v0000 = v0011 + r0000;
			d0011 = Direction.SOUTH;
		}
	}
}
if (rc.onTheMap(l0111)) {
	if (!rc.isLocationOccupied(l0111)) {
		r0111 = rc.senseRubble(l0111);
		if (v0000 > v0111 + r0000) {
			v0000 = v0111 + r0000;
			d0111 = Direction.SOUTHEAST;
		}
	}
}
if (rc.onTheMap(l1102)) {
	r1102 = rc.senseRubble(l1102);
	if (v1101 > v1102 + r1101) {
		v1101 = v1102 + r1101;
		d1101 = d1102;
	}
	if (v0001 > v1102 + r0001) {
		v0001 = v1102 + r0001;
		d0001 = d1102;
	}
}
if (rc.onTheMap(l0002)) {
	r0002 = rc.senseRubble(l0002);
	if (v1101 > v0002 + r1101) {
		v1101 = v0002 + r1101;
		d1101 = d0002;
	}
	if (v0001 > v0002 + r0001) {
		v0001 = v0002 + r0001;
		d0001 = d0002;
	}
	if (v0101 > v0002 + r0101) {
		v0101 = v0002 + r0101;
		d0101 = d0002;
	}
}
if (rc.onTheMap(l0102)) {
	r0102 = rc.senseRubble(l0102);
	if (v0001 > v0102 + r0001) {
		v0001 = v0102 + r0001;
		d0001 = d0102;
	}
	if (v0101 > v0102 + r0101) {
		v0101 = v0102 + r0101;
		d0101 = d0102;
	}
}
if (rc.onTheMap(l1201)) {
	r1201 = rc.senseRubble(l1201);
	if (v1100 > v1201 + r1100) {
		v1100 = v1201 + r1100;
		d1100 = d1201;
	}
	if (v1101 > v1201 + r1101) {
		v1101 = v1201 + r1101;
		d1101 = d1201;
	}
}
if (rc.onTheMap(l0201)) {
	r0201 = rc.senseRubble(l0201);
	if (v0100 > v0201 + r0100) {
		v0100 = v0201 + r0100;
		d0100 = d0201;
	}
	if (v0101 > v0201 + r0101) {
		v0101 = v0201 + r0101;
		d0101 = d0201;
	}
}
if (rc.onTheMap(l1200)) {
	r1200 = rc.senseRubble(l1200);
	if (v1111 > v1200 + r1111) {
		v1111 = v1200 + r1111;
		d1111 = d1200;
	}
	if (v1100 > v1200 + r1100) {
		v1100 = v1200 + r1100;
		d1100 = d1200;
	}
	if (v1101 > v1200 + r1101) {
		v1101 = v1200 + r1101;
		d1101 = d1200;
	}
}
if (rc.onTheMap(l0200)) {
	r0200 = rc.senseRubble(l0200);
	if (v0111 > v0200 + r0111) {
		v0111 = v0200 + r0111;
		d0111 = d0200;
	}
	if (v0100 > v0200 + r0100) {
		v0100 = v0200 + r0100;
		d0100 = d0200;
	}
	if (v0101 > v0200 + r0101) {
		v0101 = v0200 + r0101;
		d0101 = d0200;
	}
}
if (rc.onTheMap(l1211)) {
	r1211 = rc.senseRubble(l1211);
	if (v1111 > v1211 + r1111) {
		v1111 = v1211 + r1111;
		d1111 = d1211;
	}
	if (v1100 > v1211 + r1100) {
		v1100 = v1211 + r1100;
		d1100 = d1211;
	}
}
if (rc.onTheMap(l0211)) {
	r0211 = rc.senseRubble(l0211);
	if (v0111 > v0211 + r0111) {
		v0111 = v0211 + r0111;
		d0111 = d0211;
	}
	if (v0100 > v0211 + r0100) {
		v0100 = v0211 + r0100;
		d0100 = d0211;
	}
}
if (rc.onTheMap(l1112)) {
	r1112 = rc.senseRubble(l1112);
	if (v1111 > v1112 + r1111) {
		v1111 = v1112 + r1111;
		d1111 = d1112;
	}
	if (v0011 > v1112 + r0011) {
		v0011 = v1112 + r0011;
		d0011 = d1112;
	}
}
if (rc.onTheMap(l0012)) {
	r0012 = rc.senseRubble(l0012);
	if (v1111 > v0012 + r1111) {
		v1111 = v0012 + r1111;
		d1111 = d0012;
	}
	if (v0011 > v0012 + r0011) {
		v0011 = v0012 + r0011;
		d0011 = d0012;
	}
	if (v0111 > v0012 + r0111) {
		v0111 = v0012 + r0111;
		d0111 = d0012;
	}
}
if (rc.onTheMap(l0112)) {
	r0112 = rc.senseRubble(l0112);
	if (v0011 > v0112 + r0011) {
		v0011 = v0112 + r0011;
		d0011 = d0112;
	}
	if (v0111 > v0112 + r0111) {
		v0111 = v0112 + r0111;
		d0111 = d0112;
	}
}
if (rc.onTheMap(l1103)) {
	r1103 = rc.senseRubble(l1103);
	if (v1102 > v1103 + r1102) {
		v1102 = v1103 + r1102;
		d1102 = d1103;
	}
	if (v0002 > v1103 + r0002) {
		v0002 = v1103 + r0002;
		d0002 = d1103;
	}
}
if (rc.onTheMap(l0003)) {
	r0003 = rc.senseRubble(l0003);
	if (v1102 > v0003 + r1102) {
		v1102 = v0003 + r1102;
		d1102 = d0003;
	}
	if (v0002 > v0003 + r0002) {
		v0002 = v0003 + r0002;
		d0002 = d0003;
	}
	if (v0102 > v0003 + r0102) {
		v0102 = v0003 + r0102;
		d0102 = d0003;
	}
}
if (rc.onTheMap(l0103)) {
	r0103 = rc.senseRubble(l0103);
	if (v0002 > v0103 + r0002) {
		v0002 = v0103 + r0002;
		d0002 = d0103;
	}
	if (v0102 > v0103 + r0102) {
		v0102 = v0103 + r0102;
		d0102 = d0103;
	}
}
if (rc.onTheMap(l1202)) {
	r1202 = rc.senseRubble(l1202);
	if (v1201 > v1202 + r1201) {
		v1201 = v1202 + r1201;
		d1201 = d1202;
	}
	if (v1101 > v1202 + r1101) {
		v1101 = v1202 + r1101;
		d1101 = d1202;
	}
	if (v1102 > v1202 + r1102) {
		v1102 = v1202 + r1102;
		d1102 = d1202;
	}
}
if (rc.onTheMap(l0202)) {
	r0202 = rc.senseRubble(l0202);
	if (v0101 > v0202 + r0101) {
		v0101 = v0202 + r0101;
		d0101 = d0202;
	}
	if (v0102 > v0202 + r0102) {
		v0102 = v0202 + r0102;
		d0102 = d0202;
	}
	if (v0201 > v0202 + r0201) {
		v0201 = v0202 + r0201;
		d0201 = d0202;
	}
}
if (rc.onTheMap(l1301)) {
	r1301 = rc.senseRubble(l1301);
	if (v1200 > v1301 + r1200) {
		v1200 = v1301 + r1200;
		d1200 = d1301;
	}
	if (v1201 > v1301 + r1201) {
		v1201 = v1301 + r1201;
		d1201 = d1301;
	}
}
if (rc.onTheMap(l0301)) {
	r0301 = rc.senseRubble(l0301);
	if (v0200 > v0301 + r0200) {
		v0200 = v0301 + r0200;
		d0200 = d0301;
	}
	if (v0201 > v0301 + r0201) {
		v0201 = v0301 + r0201;
		d0201 = d0301;
	}
}
if (rc.onTheMap(l1300)) {
	r1300 = rc.senseRubble(l1300);
	if (v1211 > v1300 + r1211) {
		v1211 = v1300 + r1211;
		d1211 = d1300;
	}
	if (v1200 > v1300 + r1200) {
		v1200 = v1300 + r1200;
		d1200 = d1300;
	}
	if (v1201 > v1300 + r1201) {
		v1201 = v1300 + r1201;
		d1201 = d1300;
	}
}
if (rc.onTheMap(l0300)) {
	r0300 = rc.senseRubble(l0300);
	if (v0211 > v0300 + r0211) {
		v0211 = v0300 + r0211;
		d0211 = d0300;
	}
	if (v0200 > v0300 + r0200) {
		v0200 = v0300 + r0200;
		d0200 = d0300;
	}
	if (v0201 > v0300 + r0201) {
		v0201 = v0300 + r0201;
		d0201 = d0300;
	}
}
if (rc.onTheMap(l1311)) {
	r1311 = rc.senseRubble(l1311);
	if (v1211 > v1311 + r1211) {
		v1211 = v1311 + r1211;
		d1211 = d1311;
	}
	if (v1200 > v1311 + r1200) {
		v1200 = v1311 + r1200;
		d1200 = d1311;
	}
}
if (rc.onTheMap(l0311)) {
	r0311 = rc.senseRubble(l0311);
	if (v0211 > v0311 + r0211) {
		v0211 = v0311 + r0211;
		d0211 = d0311;
	}
	if (v0200 > v0311 + r0200) {
		v0200 = v0311 + r0200;
		d0200 = d0311;
	}
}
if (rc.onTheMap(l1212)) {
	r1212 = rc.senseRubble(l1212);
	if (v1211 > v1212 + r1211) {
		v1211 = v1212 + r1211;
		d1211 = d1212;
	}
	if (v1112 > v1212 + r1112) {
		v1112 = v1212 + r1112;
		d1112 = d1212;
	}
	if (v1111 > v1212 + r1111) {
		v1111 = v1212 + r1111;
		d1111 = d1212;
	}
}
if (rc.onTheMap(l0212)) {
	r0212 = rc.senseRubble(l0212);
	if (v0112 > v0212 + r0112) {
		v0112 = v0212 + r0112;
		d0112 = d0212;
	}
	if (v0111 > v0212 + r0111) {
		v0111 = v0212 + r0111;
		d0111 = d0212;
	}
	if (v0211 > v0212 + r0211) {
		v0211 = v0212 + r0211;
		d0211 = d0212;
	}
}
if (rc.onTheMap(l1113)) {
	r1113 = rc.senseRubble(l1113);
	if (v1112 > v1113 + r1112) {
		v1112 = v1113 + r1112;
		d1112 = d1113;
	}
	if (v0012 > v1113 + r0012) {
		v0012 = v1113 + r0012;
		d0012 = d1113;
	}
}
if (rc.onTheMap(l0013)) {
	r0013 = rc.senseRubble(l0013);
	if (v1112 > v0013 + r1112) {
		v1112 = v0013 + r1112;
		d1112 = d0013;
	}
	if (v0012 > v0013 + r0012) {
		v0012 = v0013 + r0012;
		d0012 = d0013;
	}
	if (v0112 > v0013 + r0112) {
		v0112 = v0013 + r0112;
		d0112 = d0013;
	}
}
if (rc.onTheMap(l0113)) {
	r0113 = rc.senseRubble(l0113);
	if (v0012 > v0113 + r0012) {
		v0012 = v0113 + r0012;
		d0012 = d0113;
	}
	if (v0112 > v0113 + r0112) {
		v0112 = v0113 + r0112;
		d0112 = d0113;
	}
}
if (rc.onTheMap(l1204)) {
	r1204 = rc.senseRubble(l1204);
	if (v1103 > v1204 + r1103) {
		v1103 = v1204 + r1103;
		d1103 = d1204;
	}
}
if (rc.onTheMap(l1104)) {
	r1104 = rc.senseRubble(l1104);
	if (v1103 > v1104 + r1103) {
		v1103 = v1104 + r1103;
		d1103 = d1104;
	}
	if (v0003 > v1104 + r0003) {
		v0003 = v1104 + r0003;
		d0003 = d1104;
	}
}
if (rc.onTheMap(l0004)) {
	r0004 = rc.senseRubble(l0004);
	if (v1103 > v0004 + r1103) {
		v1103 = v0004 + r1103;
		d1103 = d0004;
	}
	if (v0003 > v0004 + r0003) {
		v0003 = v0004 + r0003;
		d0003 = d0004;
	}
	if (v0103 > v0004 + r0103) {
		v0103 = v0004 + r0103;
		d0103 = d0004;
	}
}
if (rc.onTheMap(l0104)) {
	r0104 = rc.senseRubble(l0104);
	if (v0003 > v0104 + r0003) {
		v0003 = v0104 + r0003;
		d0003 = d0104;
	}
	if (v0103 > v0104 + r0103) {
		v0103 = v0104 + r0103;
		d0103 = d0104;
	}
}
if (rc.onTheMap(l0204)) {
	r0204 = rc.senseRubble(l0204);
	if (v0103 > v0204 + r0103) {
		v0103 = v0204 + r0103;
		d0103 = d0204;
	}
}
if (rc.onTheMap(l1303)) {
	r1303 = rc.senseRubble(l1303);
	if (v1202 > v1303 + r1202) {
		v1202 = v1303 + r1202;
		d1202 = d1303;
	}
}
if (rc.onTheMap(l1203)) {
	r1203 = rc.senseRubble(l1203);
	if (v1202 > v1203 + r1202) {
		v1202 = v1203 + r1202;
		d1202 = d1203;
	}
	if (v1102 > v1203 + r1102) {
		v1102 = v1203 + r1102;
		d1102 = d1203;
	}
	if (v1103 > v1203 + r1103) {
		v1103 = v1203 + r1103;
		d1103 = d1203;
	}
}
if (rc.onTheMap(l0203)) {
	r0203 = rc.senseRubble(l0203);
	if (v0102 > v0203 + r0102) {
		v0102 = v0203 + r0102;
		d0102 = d0203;
	}
	if (v0103 > v0203 + r0103) {
		v0103 = v0203 + r0103;
		d0103 = d0203;
	}
	if (v0202 > v0203 + r0202) {
		v0202 = v0203 + r0202;
		d0202 = d0203;
	}
}
if (rc.onTheMap(l0303)) {
	r0303 = rc.senseRubble(l0303);
	if (v0202 > v0303 + r0202) {
		v0202 = v0303 + r0202;
		d0202 = d0303;
	}
}
if (rc.onTheMap(l1402)) {
	r1402 = rc.senseRubble(l1402);
	if (v1301 > v1402 + r1301) {
		v1301 = v1402 + r1301;
		d1301 = d1402;
	}
}
if (rc.onTheMap(l1302)) {
	r1302 = rc.senseRubble(l1302);
	if (v1301 > v1302 + r1301) {
		v1301 = v1302 + r1301;
		d1301 = d1302;
	}
	if (v1201 > v1302 + r1201) {
		v1201 = v1302 + r1201;
		d1201 = d1302;
	}
	if (v1202 > v1302 + r1202) {
		v1202 = v1302 + r1202;
		d1202 = d1302;
	}
}
if (rc.onTheMap(l0302)) {
	r0302 = rc.senseRubble(l0302);
	if (v0201 > v0302 + r0201) {
		v0201 = v0302 + r0201;
		d0201 = d0302;
	}
	if (v0202 > v0302 + r0202) {
		v0202 = v0302 + r0202;
		d0202 = d0302;
	}
	if (v0301 > v0302 + r0301) {
		v0301 = v0302 + r0301;
		d0301 = d0302;
	}
}
if (rc.onTheMap(l0402)) {
	r0402 = rc.senseRubble(l0402);
	if (v0301 > v0402 + r0301) {
		v0301 = v0402 + r0301;
		d0301 = d0402;
	}
}
if (rc.onTheMap(l1401)) {
	r1401 = rc.senseRubble(l1401);
	if (v1300 > v1401 + r1300) {
		v1300 = v1401 + r1300;
		d1300 = d1401;
	}
	if (v1301 > v1401 + r1301) {
		v1301 = v1401 + r1301;
		d1301 = d1401;
	}
}
if (rc.onTheMap(l0401)) {
	r0401 = rc.senseRubble(l0401);
	if (v0300 > v0401 + r0300) {
		v0300 = v0401 + r0300;
		d0300 = d0401;
	}
	if (v0301 > v0401 + r0301) {
		v0301 = v0401 + r0301;
		d0301 = d0401;
	}
}
if (rc.onTheMap(l1400)) {
	r1400 = rc.senseRubble(l1400);
	if (v1311 > v1400 + r1311) {
		v1311 = v1400 + r1311;
		d1311 = d1400;
	}
	if (v1300 > v1400 + r1300) {
		v1300 = v1400 + r1300;
		d1300 = d1400;
	}
	if (v1301 > v1400 + r1301) {
		v1301 = v1400 + r1301;
		d1301 = d1400;
	}
}
if (rc.onTheMap(l0400)) {
	r0400 = rc.senseRubble(l0400);
	if (v0311 > v0400 + r0311) {
		v0311 = v0400 + r0311;
		d0311 = d0400;
	}
	if (v0300 > v0400 + r0300) {
		v0300 = v0400 + r0300;
		d0300 = d0400;
	}
	if (v0301 > v0400 + r0301) {
		v0301 = v0400 + r0301;
		d0301 = d0400;
	}
}
if (rc.onTheMap(l1411)) {
	r1411 = rc.senseRubble(l1411);
	if (v1311 > v1411 + r1311) {
		v1311 = v1411 + r1311;
		d1311 = d1411;
	}
	if (v1300 > v1411 + r1300) {
		v1300 = v1411 + r1300;
		d1300 = d1411;
	}
}
if (rc.onTheMap(l0411)) {
	r0411 = rc.senseRubble(l0411);
	if (v0311 > v0411 + r0311) {
		v0311 = v0411 + r0311;
		d0311 = d0411;
	}
	if (v0300 > v0411 + r0300) {
		v0300 = v0411 + r0300;
		d0300 = d0411;
	}
}
if (rc.onTheMap(l1412)) {
	r1412 = rc.senseRubble(l1412);
	if (v1311 > v1412 + r1311) {
		v1311 = v1412 + r1311;
		d1311 = d1412;
	}
}
if (rc.onTheMap(l1312)) {
	r1312 = rc.senseRubble(l1312);
	if (v1311 > v1312 + r1311) {
		v1311 = v1312 + r1311;
		d1311 = d1312;
	}
	if (v1212 > v1312 + r1212) {
		v1212 = v1312 + r1212;
		d1212 = d1312;
	}
	if (v1211 > v1312 + r1211) {
		v1211 = v1312 + r1211;
		d1211 = d1312;
	}
}
if (rc.onTheMap(l0312)) {
	r0312 = rc.senseRubble(l0312);
	if (v0212 > v0312 + r0212) {
		v0212 = v0312 + r0212;
		d0212 = d0312;
	}
	if (v0211 > v0312 + r0211) {
		v0211 = v0312 + r0211;
		d0211 = d0312;
	}
	if (v0311 > v0312 + r0311) {
		v0311 = v0312 + r0311;
		d0311 = d0312;
	}
}
if (rc.onTheMap(l0412)) {
	r0412 = rc.senseRubble(l0412);
	if (v0311 > v0412 + r0311) {
		v0311 = v0412 + r0311;
		d0311 = d0412;
	}
}
if (rc.onTheMap(l1313)) {
	r1313 = rc.senseRubble(l1313);
	if (v1212 > v1313 + r1212) {
		v1212 = v1313 + r1212;
		d1212 = d1313;
	}
}
if (rc.onTheMap(l1213)) {
	r1213 = rc.senseRubble(l1213);
	if (v1212 > v1213 + r1212) {
		v1212 = v1213 + r1212;
		d1212 = d1213;
	}
	if (v1113 > v1213 + r1113) {
		v1113 = v1213 + r1113;
		d1113 = d1213;
	}
	if (v1112 > v1213 + r1112) {
		v1112 = v1213 + r1112;
		d1112 = d1213;
	}
}
if (rc.onTheMap(l0213)) {
	r0213 = rc.senseRubble(l0213);
	if (v0113 > v0213 + r0113) {
		v0113 = v0213 + r0113;
		d0113 = d0213;
	}
	if (v0112 > v0213 + r0112) {
		v0112 = v0213 + r0112;
		d0112 = d0213;
	}
	if (v0212 > v0213 + r0212) {
		v0212 = v0213 + r0212;
		d0212 = d0213;
	}
}
if (rc.onTheMap(l0313)) {
	r0313 = rc.senseRubble(l0313);
	if (v0212 > v0313 + r0212) {
		v0212 = v0313 + r0212;
		d0212 = d0313;
	}
}
if (rc.onTheMap(l1214)) {
	r1214 = rc.senseRubble(l1214);
	if (v1113 > v1214 + r1113) {
		v1113 = v1214 + r1113;
		d1113 = d1214;
	}
}
if (rc.onTheMap(l1114)) {
	r1114 = rc.senseRubble(l1114);
	if (v1113 > v1114 + r1113) {
		v1113 = v1114 + r1113;
		d1113 = d1114;
	}
	if (v0013 > v1114 + r0013) {
		v0013 = v1114 + r0013;
		d0013 = d1114;
	}
}
if (rc.onTheMap(l0014)) {
	r0014 = rc.senseRubble(l0014);
	if (v1113 > v0014 + r1113) {
		v1113 = v0014 + r1113;
		d1113 = d0014;
	}
	if (v0013 > v0014 + r0013) {
		v0013 = v0014 + r0013;
		d0013 = d0014;
	}
	if (v0113 > v0014 + r0113) {
		v0113 = v0014 + r0113;
		d0113 = d0014;
	}
}
if (rc.onTheMap(l0114)) {
	r0114 = rc.senseRubble(l0114);
	if (v0013 > v0114 + r0013) {
		v0013 = v0114 + r0013;
		d0013 = d0114;
	}
	if (v0113 > v0114 + r0113) {
		v0113 = v0114 + r0113;
		d0113 = d0114;
	}
}
if (rc.onTheMap(l0214)) {
	r0214 = rc.senseRubble(l0214);
	if (v0113 > v0214 + r0113) {
		v0113 = v0214 + r0113;
		d0113 = d0214;
	}
}
