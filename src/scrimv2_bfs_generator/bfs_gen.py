import numpy as np
from gen_java_defs import gen_java_defs

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

def is_adjacent(node1: str, node2: str):
    pass

def gen_java_compares(coord_array: np.ndarray, approved_circles: np.ndarray, coord_map: dict):
    fout = open('compares.java', 'w')
    checked_nodes = set()
    for i in range(1, len(approved_circles)): # i think it starts at 1?

        # make it a ring
        boundary = approved_circles[i] - approved_circles[i-1]
        coords = np.where(boundary, coord_array, 0) # map to coordinates
        nonzero_coords = coords[coords!='0']
        boundary_flat = nonzero_coords.flatten()

        inner_circle_coords = np.where(approved_circles[i-1], coord_array, 0)
        inner_circle_coords = inner_circle_coords[inner_circle_coords!='0']
        inner_flat = inner_circle_coords.flatten()
        # print(boundary_flat)
        # print(inner_flat, '\n')

        inverted_map = {v: k for k, v in coord_map.items()}

        # perform bfs comparisons
        for node in boundary_flat:
            adj_prev_locs = []
            node_x, node_y = coord_map[node]

            # find adjacent nodes that are IN inner_flat
            for dx in range(-1, 2):
                for dy in range(-1, 2):
                    test_x = node_x + dx
                    test_y = node_y + dy

                    test_loc = (test_x, test_y)

                    # check if in map
                    if test_loc in inverted_map and (inverted_map[test_loc] != node): # is it on the map?
                        # if i == 1:
                        #     print(node, inverted_map[test_loc], coord_map[node], test_loc)
                        # print(test_loc, inverted_map[test_loc], inverted_map[test_loc] in inner_flat)
                        if inverted_map[test_loc] in inner_flat:
                            # print('node, test', node, test_loc)
                            adj_prev_locs.append(inverted_map[test_loc])
            
            print('node, adjprevloc', node, adj_prev_locs)
            # TODO: use adj_prev_locs to generate java code
            # the java code makes if statements comparing the node to each of the adjacent nodes
            for loc_to_compare in adj_prev_locs: # TODO: can bytecode optimize first few
                if node not in checked_nodes:
                    # v[loc] = min(v[adj prev locs] + delay)
                    # delay is the rubble, v is time to loc
                    fout.write('if (rc.onTheMap(l{})) {{}}\n'.format(node))
                    checked_nodes.add(node)
    fout.close()



def gen_coord_map(coord_array: np.ndarray):
    coord_map = {}
    for i in range(0, len(coord_array)):
        for j in range(0, len(coord_array[i])):
            node = coord_array[i][j]

            x_str = int(node[0:2])
            y_str = int(node[2:4])

            x_sign = (x_str // 10) * -1
            y_sign = (y_str // 10) * -1

            x = (x_str % 10) * (x_sign if x_sign == -1 else 1)
            y = (y_str % 10) * (y_sign if y_sign == -1 else 1)
            coord_map[node] = (x, y)
    
    return coord_map

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
    coord_map = gen_coord_map(coords) # maps our coordinate system to x/y relative to bot
    print(coords)
    # print(coord_map)
    # gen_java_defs(coords, approvedCircleList)
    gen_java_compares(coords, approvedCircleList, coord_map)

if __name__ == '__main__':
    main()
