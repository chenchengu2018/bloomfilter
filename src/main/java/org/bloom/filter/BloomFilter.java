package org.bloom.filter;

import com.google.common.hash.Hashing;

import java.util.BitSet;

import static com.google.common.base.Preconditions.checkArgument;

public class BloomFilter<T> {
    private int numHashFunctions;
    private double fpp;
    private int bitSize; // TODO: support long
    private BitSet bitSet; // TODO: support multi-threading

    /**
     * @param expectedItems It is important that we provide a reasonably accurate value for the expected number of elements.
     *                      Otherwise, our filter will return false positives at a much higher rate than desired.
     * @param fpp           False positive rate.
     */
    public BloomFilter(long expectedItems, double fpp) {
        checkArguments(expectedItems, fpp);
        this.fpp = fpp;
        this.bitSize = calculateSize(expectedItems);
        this.numHashFunctions = calculateNumOfHashFunctions(expectedItems);
        this.bitSet = new BitSet(bitSize);
    }

    public boolean put(T item) {

        for (int i = 1; i <= numHashFunctions; i++) {
            int idx = Hashing.murmur3_128(i).hashInt(item.hashCode()).asInt() % bitSize;
            if (idx < 0) {
                idx = ~idx;
            }
            bitSet.set(idx);
        }
        return true;
    }

    public boolean mightContain(T item) {

        for (int i = 1; i <= numHashFunctions; i++) {
            int idx = Hashing.murmur3_128(i).hashInt(item.hashCode()).asInt() % bitSize;
            if (idx < 0) {
                idx = ~idx;
            }
            if (!bitSet.get(idx)) {
                return false;
            }
        }
        return true;
    }

    private void checkArguments(long expectedItems, double fpp) {
        checkArgument(expectedItems >= 0, "Expected items (%s) must be >= 0", expectedItems);
        checkArgument(fpp > 0.0, "False positive probability (%s) must be > 0.0", fpp);
        checkArgument(fpp < 1.0, "False positive probability (%s) must be < 1.0", fpp);
    }

    private int calculateSize(long expectedItems) {
        return (int) (-expectedItems * Math.log(fpp) / (Math.log(2) * Math.log(2)));
    }

    private int calculateNumOfHashFunctions(long expectedItems) {
        return (int) (bitSize / expectedItems * Math.log(2));
    }
}
