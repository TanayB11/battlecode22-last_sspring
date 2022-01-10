from os import close
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

def gen_java_compares(coord_array: np.ndarray, approved_circles: np.ndarray, coord_map: dict):
    fout = open('compares.java', 'w')
    fout.write('// TODO: insert the try/catch block\n')
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
            open_brace = '{'
            close_brace = '}'

            tabs = 1
            first_layer = False # we're in the first 8 nodes (right next to the center)
            fout.write(f'if (rc.onTheMap(l{node})) {open_brace}\n')

            # only need to check first 8 nodes surrounding center
            if node in ['0000', '1101', '0001', '0101', '1100', '0100', '1111', '0011', '0111']: # yes this is trash i know
                fout.write(f'\tif (!rc.isLocationOccupied(l{node})) {open_brace}\n')
                first_layer = True
                tabs = 2

            tabs = '\t' * tabs

            # set passability to rubble; cooldown only depends on rubble (base cooldown invariant of path)
            fout.write(f'{tabs}r{node} = rc.senseRubble(l{node});\n')

            for loc_to_compare in adj_prev_locs: # TODO: can bytecode optimize first few
                # v[loc] = min(v[adj prev locs] + delay)
                # delay is the rubble, v is time to loc
                # if time to this node is further than time to node plus the rubble, use the node + delay
                fout.write(f'{tabs}if (v{node} > v{loc_to_compare} + r{node}) {open_brace}\n')
                fout.write(f'{tabs}\tv{node} = v{loc_to_compare} + r{node};\n')
                if not first_layer: # update the direction
                    fout.write(f'{tabs}\td{node} = d{loc_to_compare};\n')
                else:
                    # if node in ['0000', '1101', '0001', '0101', '1100', '0100', '1111', '0011', '0111']: # yes this is trash i know
                    # if we're in the first layer, then the direction is from 0000 to the node
                    if node == '1101':
                        fout.write(f'{tabs}\td{node} = Direction.NORTHWEST;\n')
                    elif node == '0001':
                        fout.write(f'{tabs}\td{node} = Direction.NORTH;\n')
                    elif node == '0101':
                        fout.write(f'{tabs}\td{node} = Direction.NORTHEAST;\n')
                    elif node == '1100':
                        fout.write(f'{tabs}\td{node} = Direction.WEST;\n')
                    elif node == '0100':
                        fout.write(f'{tabs}\td{node} = Direction.EAST;\n')
                    elif node == '1111':
                        fout.write(f'{tabs}\td{node} = Direction.SOUTHWEST;\n')
                    elif node == '0011':
                        fout.write(f'{tabs}\td{node} = Direction.SOUTH;\n')
                    elif node == '0111':
                        fout.write(f'{tabs}\td{node} = Direction.SOUTHEAST;\n')
                    # fout.write(f'{tabs}\td{loc_to_compare} = Direction.SOMETHING;\n')
                fout.write(f'{tabs}{close_brace}\n')
                if first_layer:
                    fout.write(f'\t{close_brace}\n')
                # checked_nodes.add(node)
            fout.write(f'{close_brace}\n')
    fout.close()

def gen_switch_spam(b_vision_box: np.ndarray, coord_map: dict):
    fout = open('compute_direction.java', 'w')

    # NOTE: static might be an issue inside a method
    fout.write('static int dx = target.x - l0000.x;\n')
    fout.write('static int dy = target.y - l0000.y;\n')

    open_brace = '{'
    close_brace = '}'

    # invert coord_map
    inverted_map = {v: k for k, v in coord_map.items()}

    fout.write(f'switch(dx) {open_brace}\n')
    for dx in range(-4, 5):
        fout.write(f'\tcase {dx}:\n')
        fout.write(f'\t\tswitch(dy) {open_brace}\n')

        # get the dx-th column of b_vision_box
        # TODO: needs to be changed for different vision radius
        col = b_vision_box[:, dx+4]
        col_center = 4
        # get index of first non-zero element of col
        first_nonzero = -col_center + np.argmax(col)
        # get index of last non-zero element of col
        last_nonzero = col_center - np.argmax(col[::-1])
        # print(first_nonzero, last_nonzero)

        for dy in range(first_nonzero, last_nonzero+1):
            # TODO: include only the ones that are in the vision radius
            # can check against the droid vision box
            fout.write(f'\t\t\tcase {dy}:\n')

            # find direction to the node with dx, dy
            target = inverted_map[(dx, dy)]
            # print(target)

            fout.write(f'\t\t\t\treturn d{target};\n')

        fout.write(f'\t\t{close_brace}\n')
        fout.write(f'\t\tbreak;\n')
    fout.write(f'{close_brace}\n')

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

def compute_heuristic(b_vision_box: np.ndarray, approved_circles: np.ndarray, coord_map: dict):
    fout = open('heuristic.java', 'w')

    # invert the coordinate map
    inverted_map = {v: k for k, v in coord_map.items()}

    fout.write('Direction ans = null;\n')
    fout.write('double bestEstimation = 0;\n')
    fout.write('double initialDist = Math.sqrt(l0000.distanceSquaredTo(target));\n')

    # optimization: use Math.max
    # every point that can reach outside vision radius (outer ring + 8 extra nodes)
    # TODO: remove the sqrt?
    # TODO: make it work for different vision radius
    outer_ring = approved_circles[-1] - approved_circles[-2]
    # get indices of nonzro elements of outer_ring
    outer_ring_locs_x, outer_ring_locs_y = np.where(outer_ring == 1)
    outer_indices = [(outer_ring_locs_x[i], outer_ring_locs_y[i]) for i in range(len(outer_ring_locs_x))]
    center_coords = [(outer_indices[i][0] - 4, outer_indices[i][1] - 4) for i in range(len(outer_indices))]
    wonky_coords = [inverted_map[loc] for loc in center_coords]

    # write comparison to java file
    open_brace = '{'
    close_brace = '}'
    for coord in wonky_coords:
        fout.write(f'double dist{coord} = (initialDist - Math.sqrt(l{coord}.distanceSquaredTo(target))) / v{coord};\n')
        fout.write(f'if (dist{coord} > bestEstimation) {open_brace}\n')
        fout.write(f'\tbestEstimation = dist{coord};\n')
        fout.write(f'\tans = d{coord};\n')
        fout.write(f'{close_brace}\n')

    fout.write('return ans;\n')
    # print(outer_indices)
    print(wonky_coords)

    fout.close()
    pass

def main():
    # NOTE: will have to edit lots of hardcoded stuff to make it work with different vision radius
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
    # gen_switch_spam(b_vision_box, coord_map)
    # compute_heuristic(b_vision_box, approvedCircleList, coord_map)

if __name__ == '__main__':
    main()
