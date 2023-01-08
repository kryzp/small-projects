
def export_move(start, end):
	print("Moved one disk from " + str(start) + " to " + str(end))

def towers_of_hanoi(n, start, end):
	if n == 1:
		export_move(start, end)
		return
	free = 6 - start - end
	towers_of_hanoi(n - 1, start, free)
	export_move(start, end)
	towers_of_hanoi(n - 1, free, end)

towers_of_hanoi(2, 1, 3)
print("Finished!")
