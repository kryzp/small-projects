
# minimum spanning tree using Prim–Jarník algorithm

def mst(adjacency_matrix):
	visited = [0]
	tree = []
	while True:
		coord = (-1, -1)
		minima = 999
		for y in range(len(adjacency_matrix)):
			for x in visited:
				here = adjacency_matrix[y][x]
				if here != -1 and here < minima and y not in visited:
					minima = here
					coord = (y, x)
		if coord == (-1, -1):
			break
		yy, xx = coord
		tree.append(adjacency_matrix[yy][xx])
		visited.append(yy)
	return tree

mat = [
	[-1,  5, -1, -1, -1, -1,  1,  5, -1],
	[ 5, -1,  4, -1, -1, -1, -1,  2,  6],
	[-1,  4, -1,  7, -1, -1, -1, -1,  1],
	[-1, -1,  7, -1,  4, -1, -1, -1,  5],
	[-1, -1, -1,  4, -1,  7, -1, -1,  6],
	[-1, -1, -1, -1,  7, -1,  6,  2,  3],
	[ 1, -1, -1, -1, -1,  6, -1,  3, -1],
	[ 5,  2, -1, -1, -1,  2,  3, -1,  3],
	[-1,  6,  1,  5,  6,  3, -1,  3, -1],
]

tree = mst(mat)
total = sum(tree)

print(total)
