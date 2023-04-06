import sys

# Recursive function to print the path of a given vertex from source vertex
def getPath(parent, vertex):
	if vertex < 0:
		return []
	return getPath(parent, parent[vertex]) + [vertex]

# Function to run the Bellman–Ford algorithm from a given source
def bellmanFord(edges, source, n):
	# distance[] and parent[] stores the shortest path (least cost/path) info
	distance = [sys.maxsize] * n
	parent = [-1] * n

	# Initially, all vertices except source vertex weight INFINITY and no parent
	distance[source] = 0

	# relaxation step (run V-1 times)
	for k in range(n - 1):
		# edge from `u` to `v` having weight `w`
		for (u, v, w) in edges:
			# if the distance to destination `v` can be shortened by taking edge (u, v)
			if distance[u] != sys.maxsize and distance[u] + w < distance[v]:
				# update distance to the new lower value
				distance[v] = distance[u] + w
				# set v's parent as `u`
				parent[v] = u

	# run relaxation step once more for n'th time to check for negative-weight cycles
	for (u, v, w) in edges:  # edge from `u` to `v` having weight `w`
		# if the distance to destination `u` can be shortened by taking edge (u, v)
		if distance[u] != sys.maxsize and distance[u] + w < distance[v]:
			print('Negative-weight cycle is found!!')
			return

	for i in range(n):
		if i != source and distance[i] < sys.maxsize:
			print(f'The distance of vertex {i} from vertex {source} is {distance[i]}. '
				  f'Its path is', getPath(parent, i))


if __name__ == '__main__':

	# of graph edges as per the above diagram
	edges = [
		# (x, y, w) —> edge from `x` to `y` having weight `w`
		(0, 1, -1), (0, 2, 4), (1, 2, 3), (1, 3, 2),
		(1, 4, 2), (3, 2, 5), (3, 1, 1), (4, 3, -3)
	]

	# set the maximum number of nodes in the graph
	n = 5

	# run the Bellman–Ford algorithm from every node
	for source in range(n):
		bellmanFord(edges, source, n)
