import numpy as np
import matplotlib.pyplot as plotter

SECONDS_LAST = 1
SAMPLING_RATE = 256

data = []

for i in range(SAMPLING_RATE * SECONDS_LAST):
    n = 0.0
    t = i / SAMPLING_RATE * SECONDS_LAST
    t2 = 2.0 * np.pi * t
    n += 1.5 * np.cos(t2 * 1.0)
    n += 5.0 * np.sin(t2 * 5.0)
    n += 2.5 * np.sin(t2 * 50.0)
    n += 2.0 * np.cos(t2 * 100.0)
    data.append(n)

mean = sum(data) / len(data)
data = [x - mean for x in data]

time = np.arange(0, SECONDS_LAST, 1.0 / SAMPLING_RATE)

def fft(data):
    n = len(data)
    if n == 1:
        return data
    data_even = data[::2]
    data_odd  = data[1::2]
    res_even = fft(data_even)
    res_odd  = fft(data_odd)
    res = [0] * n
    for i in range(int(n / 2)):
        theta = 2.0 * np.pi / n * i
        omega = np.cos(theta) + 1j * np.sin(theta)
        res[i]              = res_even[i] + omega * res_odd[i]
        res[i + int(n / 2)] = res_even[i] - omega * res_odd[i]
    return res

fft_output = fft(data)
N = len(fft_output)

frequencies = np.arange(N) * N / SAMPLING_RATE

plotter.title("Time Space")
plotter.plot(time, data, 'r-')
plotter.xlabel("Time / s")

plotter.figure()
plotter.title("Frequency Space")
plotter.stem(frequencies, [abs(z) for z in fft_output], 'b')
plotter.xlabel("Frequency / Hz")

plotter.show()
