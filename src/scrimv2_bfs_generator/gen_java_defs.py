import numpy as np

def gen_java_defs(coord_array: np.ndarray, approved_circles: np.ndarray):
    with open('definitions.java', 'w') as fout:
        shape = np.shape(coord_array)
        center = (coord_array.shape[0] // 2, coord_array.shape[1] // 2)
        
        centerCoord = coord_array[center[0]][center[1]]
        fout.write("l" + centerCoord + " = rc.getLocation();\n")
        fout.write("v" + centerCoord + " = 0;" + '\n\n')

        for i in range(1, approved_circles.shape[0]):
            # check if it's a square, so we can perform edge tests
            circle_nonzero_els = np.count_nonzero(approved_circles[i])
            side_length = np.sqrt(circle_nonzero_els)
            print(approved_circles[i], circle_nonzero_els, side_length)
            is_square = side_length % 1 == 0
            
            boundary = approved_circles[i] - approved_circles[i-1]
            boundary = np.where(boundary, coord_array, 0)

            boundary = boundary.flatten()
            boundary = boundary[boundary!='0']

            print(boundary)
            # perform our initialization
            # do a check to see if it's a square
            # index 0 is a corner, index s - 1 is a corner (s = side length)
            # index l is a corner (l = last), index l - s + 1 is a corner
            corners = []
            if (is_square and len(corners) == 0):
                side_length = int(side_length)
                corners = [
                    boundary[0],
                    boundary[side_length - 1],
                    boundary[-1],
                    boundary[-side_length],
                ]
                # todo: process these separately
                print('corners', corners)
                
            # # intermediate variable for computing the "radius"
            # maxYDistance = 0
            # for edge_loc in boundary:
            #     yDistance = int(edge_loc) % 10
            #     if yDistance > maxYDistance:
            #         yDistance = modTen
            
            # radius = maxYDistance
            
            for edge_loc in boundary:
                x_coord = int(edge_loc[0:2])
                y_coord = int(edge_loc[2:4])
                directionNeeded = ""
                previousCoordNeeded = ""
                
                # corners!
                if edge_loc in corners:
                    whichX = x_coord // 10
                    whichY = y_coord // 10

                    if whichX == 0 and whichY == 0:
                        directionNeeded = "NORTHEAST"
                    elif whichX == 0 and whichY == 1:
                        directionNeeded = "SOUTHEAST"
                    elif whichX == 1 and whichY == 0:
                        directionNeeded = "NORTHWEST"
                    else:
                        directionNeeded = "SOUTHWEST"
                else:
                    # process this case, normal
                    # if abs(y coord) > abs(x coord), we're on top or bottom
                    if (y_coord % 10) > (x_coord % 10):
                        # sign indicator 0 or 1
                        whichY = y_coord // 10
                        # if sign of y coord is positive, it's on the top
                        if whichY == 0:
                            directionNeeded = "NORTH"
                        else:
                            # otherwise it's on the bottom :)
                            directionNeeded = "SOUTH"
                    else:
                        whichX = x_coord // 10
                        if whichX == 0:
                            directionNeeded = "EAST"
                        else:
                            directionNeeded = "WEST"

                if i == 1:
                    l_str = 'l'+edge_loc+' = rc.adjacentLocation(Direction.{});'.format(directionNeeded)
                else:
                    previous_x_string = ""
                    previous_y_string = ""
                    
                    # compute the coordinate of the previous node
                    # shift in both x dir and y dir: corner :()
                    if edge_loc in corners:
                        # as you go toward center, you decrease both bc abs()
                        previous_x = x_coord - 1
                        previous_y = y_coord - 1

                        # xDir = previous_x // 10
                        # yDir = previous_y // 10

                        if previous_x == 0:
                            previous_x_string = "00"
                        elif previous_x == 1:
                            previous_x_string = "01"
                        elif previous_x < 10:
                            # one digit things
                            previous_x_string = "0" + str(previous_x)
                        elif previous_x == 10:
                            # wonky edge case things, going from 11 to 00
                            previous_x_string = "00"
                        elif previous_x > 10:
                            # already fine things
                            previous_x_string = str(previous_x)
                        
                        if previous_y == 0:
                            previous_y_string = "00"
                        elif previous_y == 1:
                            previous_y_string = "01"
                        elif previous_y < 10:
                            # see previous comments
                            previous_y_string = "0" + str(previous_y)
                        elif previous_y == 10:
                            previous_y_string = "00"
                        elif previous_y > 10:
                            previous_y_string = str(previous_y)
                        
                    elif directionNeeded == "EAST" or directionNeeded == "WEST":
                        # shift in x dir: EAST/WEST is directionNeeded -> WEST/EAST
                        # case 1: you have 11 -> 00
                        # case 2: you have 1x -> 1x
                        # case 3: you have 0x -> 0x

                        previous_x = x_coord - 1
                        previous_y = y_coord

                        if previous_x == 0:
                            previous_x_string = "00"
                        elif previous_x == 1:
                            previous_x_string = "01"
                        elif previous_x < 10:
                            # one digit things
                            previous_x_string = "0" + str(previous_x)
                        elif previous_x == 10:
                            # wonky edge case things, going from 11 to 00
                            previous_x_string = "00"
                        elif previous_x > 10:
                            # already fine things
                            previous_x_string = str(previous_x)
                        
                        if previous_y == 0:
                            previous_y_string = "00"
                        elif previous_y == 1:
                            previous_y_string = "01"
                        elif previous_y < 10:
                            # see previous comments
                            previous_y_string = "0" + str(previous_y)
                        elif previous_y == 10:
                            previous_y_string = "00"
                        elif previous_y > 10:
                            previous_y_string = str(previous_y)
                    else:
                        # shift in y dir: NORTH/SOUTH is directionNeeded -> SOUTH/NORTH
                        # case 1: you have 11 -> 00
                        # case 2: you have 1x -> 1x
                        # case 3: you have 0x -> 0x
                        previous_y = y_coord - 1
                        previous_x = x_coord

                        if previous_x == 0:
                            previous_x_string = "00"
                        elif previous_x == 1:
                            previous_x_string = "01"
                        elif previous_x < 10:
                            # one digit things
                            previous_x_string = "0" + str(previous_x)
                        elif previous_x == 10:
                            # wonky edge case things, going from 11 to 00
                            previous_x_string = "00"
                        elif previous_x > 10:
                            # already fine things
                            previous_x_string = str(previous_x)
                        
                        if previous_y == 0:
                            previous_y_string = "00"
                        elif previous_y == 1:
                            previous_y_string = "01"
                        elif previous_y < 10:
                            # see previous comments
                            previous_y_string = "0" + str(previous_y)
                        elif previous_y == 10:
                            previous_y_string = "00"
                        elif previous_y > 10:
                            previous_y_string = str(previous_y)

                        previous_x_string = str(previous_x)

                    previousCoordNeeded = previous_x_string + previous_y_string
                    l_str = 'l'+edge_loc+' = l{}.add(Direction.{});'.format(previousCoordNeeded, directionNeeded)
                v_str = 'v'+edge_loc+' = 1000000;'
                d_str = 'd'+edge_loc+' = null;'

                fout.write(l_str + '\n')
                fout.write(v_str + '\n')
                fout.write(d_str + '\n\n')