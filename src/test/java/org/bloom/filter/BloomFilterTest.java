package org.bloom.filter;

import com.google.common.io.Resources;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class BloomFilterTest {

    @Test
    public void testBloomFilter_smallInput() throws Exception {
        for (int i = 0; i < 100_000; i++) {
            BloomFilter<Integer> filter = new BloomFilter<>(10, 0.01);
            filter.put(1);
            filter.put(2);
            filter.put(3);
            assertThat(filter.mightContain(1)).isTrue();
            assertThat(filter.mightContain(2)).isTrue();
            assertThat(filter.mightContain(3)).isTrue();
            assertThat(filter.mightContain(100)).isFalse();
        }
    }

    /**
     * ~340K words.
     */
    @Test
    public void testBloomFilter_mediumInput() throws Exception {
        List<String> words = Files.readAllLines(Paths.get(Resources.getResource("bloomfilter/wordlist_medium.txt").getPath()), UTF_8);
        BloomFilter<String> filter = new BloomFilter<>(350_000, 0.01);
        for (String word : words) {
            filter.put(word);
        }
        assertThat(filter.mightContain("événements")).isTrue();
        assertThat(filter.mightContain("you've")).isTrue();
        // 你好 does not exist in word list
        assertThat(filter.mightContain("你好")).isFalse();
    }

    @Test
    public void testBloomFilter_overSaturated() throws Exception {
        BloomFilter<Integer> filter = new BloomFilter<>(5, 0.01);
        IntStream.range(0, 100_000).forEach(filter::put);
        assertThat(filter.mightContain(1)).isTrue();
        // filter becomes over-saturated and has a much higher fpp
        assertThat(filter.mightContain(200_000)).isTrue();
    }
}
