import numpy as np
import matplotlib.pyplot as plot

falloff = 3
alpha = [1, 1]
positions = [(0, 5), (0, -5)]

def d(coord):
	return sum(x**2 for x in coord)**0.5

def n(coord):
	return (-coord[1], coord[0])

def B(x, y):
	graph = (x, y)
	final = (0, 0)
	for i in range(len(positions)):
		coord = tuple(graph[j] - positions[i][j] for j in range(len(graph)))
		final = tuple(map(sum, zip(final, n(coord) * alpha[i] * np.exp(-d(coord)/falloff))))
	return final

SP = 25
DS = 10

x, y = np.meshgrid(np.linspace(-DS, DS, SP), np.linspace(-DS, DS, SP))
u, v = B(x, y)

plot.quiver(x, y, u, v)
# plot.grid()
plot.show()
