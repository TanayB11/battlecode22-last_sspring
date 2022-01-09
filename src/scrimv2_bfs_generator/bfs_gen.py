import numpy as np

# https://stackoverflow.com/q/44865023/10981207
def gen_circle_mask(vision_box: np.ndarray, center: tuple, rad: float):
    height, width = vision_box.shape[0], vision_box.shape[1]
    x, y = np.ogrid[:height, :width] # coords of each pixel
    dist = np.sqrt(pow(x - center[0] + 1, 2) + pow(y - center[1] + 1, 2))

    return dist <= rad

def compute_vision_box(box_dims: tuple, radius: float, box_rad: tuple):
    center = (box_dims[0] // 2 + 1, box_dims[1] // 2 + 1)
    vision_box = np.zeros(box_dims)
    circle_mask = gen_circle_mask(vision_box, center, radius)

    vision_box = np.where(circle_mask, 1, 0)
    return vision_box

def get_concentric_circles(b_vis_rad_sq, box_dims, b_vision_box_radius):
    # iterate from r = 0 to r = sqrt(b_vis_rad_sq)
    # pass in dr_vision_box
    # this is to figure out which ones we actually want
    listOfCircles = []
    for r in range(0, b_vis_rad_sq+1):
        # this is the circle we use to init variables
        gen_circle = compute_vision_box(box_dims, np.sqrt(r), b_vision_box_radius)
        listOfCircles.append(gen_circle)
    
    return np.array(listOfCircles)

def assign_coords(vision_box: np.ndarray):
    height, width = vision_box.shape[0], vision_box.shape[1]
    center = (height // 2, width // 2)

    coordArray = []

    for x in range(0, width):
        row = []
        for y in range(0, height):
            adjustedWidth = x - center[0]
            adjustedHeight = y - center[1]
            
            if adjustedWidth < 0:
                adjustedWidth = "1" + str(abs(adjustedWidth))
            else:
                adjustedWidth = "0" + str(abs(adjustedWidth))

            if adjustedHeight < 0:
                adjustedHeight = "1" + str(abs(adjustedHeight))
            else:
                adjustedHeight = "0" + str(abs(adjustedHeight))

            row.append(adjustedWidth + adjustedHeight)
        coordArray.append(row)

    coordArray = np.array(coordArray)
    coordArray = np.rot90(coordArray)
    return coordArray

def gen_java_defs(coord_array: np.ndarray, approved_circles: np.ndarray):
    with open('test.java', 'w') as fout:
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

def is_adjacent(node1: str, node2: str):
    pass

# TODO : go through the entire algorithm and then just print out
def gen_java_compares(coord_array: np.ndarray, approved_circles: np.ndarray):
    for i in range(1, len(approved_circles)): # i think it starts at 1?

        # make it a ring
        boundary = approved_circles[i] - approved_circles[i-1]
        coords = np.where(boundary, coord_array, 0) # map to coordinates
        nonzero_coords = coords[coords!='0']
        flat = nonzero_coords.flatten()

        inner_circle_coords = np.where(approved_circles[i-1], coord_array, 0)
        inner_circle_coords = inner_circle_coords[inner_circle_coords!='0']
        inner_flat = inner_circle_coords.flatten()
        print(flat, inner_flat)

        # perform bfs comparisons
        for node in flat:
            adj_prev_locs = []
            # parse the coordinates from the string
            x_coord = int(node[0:2])
            y_coord = int(node[2:4])
            x_dir = x_coord // 10
            y_dir = y_coord // 10
            x_coord = x_coord % 10
            y_coord = y_coord % 10

            for inner_node in inner_circle_coords:
                inner_x_coord = int(inner_node[0:2])
                inner_y_coord = int(inner_node[2:4])
                inner_x_dir = inner_x_coord // 10
                inner_y_dir = inner_y_coord // 10
                inner_x_coord = inner_x_coord % 10
                inner_y_coord = inner_y_coord % 10

                print(node, inner_node)
                # TODO: check if the coordinates are adjacent

                # for inner_node in inner_circle:
                #     inner_x_coord = int(inner_node[0:2])
                #     inner_y_coord = int(inner_node[2:4])
                #     inner_x_dir = inner_x_coord // 10
                #     inner_y_dir = inner_y_coord // 10
                #     inner_x_coord = inner_x_coord % 10
                #     inner_y_coord = inner_y_coord % 10

                #     print(node, inner_node)

                # print(x_coord, y_coord, node)

                # find nodes in approved_circles[i-1] that are adjacent to node
                # adjacency means a distance in array of 1
                # for dx in range(-1,2):
                #     for dy in range(-1,2):
                #         if dx == 0 and dy == 0:
                #             continue
                        # we only want to subtract x/y coords because we want to go inwards
                        # it's adjacent if euclidean distance is 1


                        # shift by EAST/WEST/NORTH/SOUTH
                        # dx = 0, dy = 1 -> NORTH
                        # dx = 0, dy = -1 -> SOUTH
                        # dx = 1, dy = 0 -> EAST
                        # dx = -1, dy = 0 -> WEST
                        # convert dx to our coordinate system: xDir = dx // 10, x = dx%10

                        # adj_prev_locs.append(node + dx + dy*10)
                # print(node, adj_prev_locs, 'adj prev locs')


def main():
    b_vision_box_radius = (5, 5) # contains entire circle for droid
    b_vis_rad_sq = 20
    b_vis_rad = np.sqrt(b_vis_rad_sq)
    box_dims = int(np.floor(b_vis_rad) * 2) + 1, int(np.floor(b_vis_rad) * 2) + 1
    
    b_vision_box = compute_vision_box(box_dims, b_vis_rad, b_vision_box_radius)
    print(b_vision_box)
    
    # modify this to pick from the above
    circleList = get_concentric_circles(b_vis_rad_sq, box_dims, b_vision_box_radius)
    concentric_locs = [0, 2, 5, 10, 20]
    
    approvedCircleList = []
    for i in concentric_locs:
        approvedCircleList.append(circleList[i])
        # print(circleList[i])

    approvedCircleList = np.array(approvedCircleList)

    coords = assign_coords(b_vision_box)
    print(coords)
    # gen_java_defs(coords, approvedCircleList)
    gen_java_compares(coords, approvedCircleList)

if __name__ == '__main__':
    main()
