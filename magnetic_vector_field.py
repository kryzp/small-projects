import numpy as np
import matplotlib.pyplot as plot

N = 1
falloff = 5
flip = [1, -1]
positions = [(0, 5), (0, -5)]
sizes = [(1, 1), (1, 1)]

def W(x, y):
	final = (0, 0)
	for i in range(len(positions)):
		invpos = tuple(-x for x in positions[i])
		coord = tuple(map(sum, zip((x, y), invpos)))
		px, py = coord
		sx, sy = sizes[i]
		px /= sx
		py /= sy
		xi = flip[i%2] * np.exp(-(px**2 + py**2)**0.5/falloff)
		val = (-py, px) * xi
		final = tuple(map(sum, zip(final, val)))
	return final

SP = 25
DS = 10

x, y = np.meshgrid(np.linspace(-DS, DS, SP), np.linspace(-DS, DS, SP))
u, v = W(x, y)

plot.quiver(x, y, u, v)
# plot.grid()
plot.show()
