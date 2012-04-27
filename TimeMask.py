def MakeDeltaT(time_mask, delta_t):
    """ This function takes in a time_mask and the original delta_t, and
    returns a modified delta_t that has been adjusted to use with this
    time_mask"""

    ndelta_t = [0] * (time_mask.count(1) - 1) # Make our new delta_t
    mask = 0
    last_time = None

    for i, bit in enumerate(time_mask):
        if bit == 1:
            # Keep this time point
            if last_time == None:
                last_time = i
                continue

            time_elapsed = 0
            for j in xrange(last_time, i):
                time_elapsed += delta_t[j]
            ndelta_t[mask] = time_elapsed
            last_time = i
            mask += 1
    print ndelta_t

    return ndelta_t
