package org.bricolages.mys3dump;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PartitionTest {
    @Test
    void splitIntoPartitions() {
        // Split 20 rows (estimated) into 5 partitions
        List<Partition> partitions = new Partition("id", Long.valueOf(1), Long.valueOf(20)).split(5);
        assertEquals(5, partitions.size());
        assertEquals("id BETWEEN 1 AND 4", partitions.get(0).toString());
        assertEquals("id BETWEEN 5 AND 8", partitions.get(1).toString());
        assertEquals("id BETWEEN 9 AND 12", partitions.get(2).toString());
        assertEquals("id BETWEEN 13 AND 16", partitions.get(3).toString());
        assertEquals("id BETWEEN 17 AND 20", partitions.get(4).toString());
    }

    @Test
    void splitIntoPartitionsWithRemainder() {
        // Split 21 rows (estimated) into 5 partitions
        List<Partition> partitions = new Partition("id", Long.valueOf(1), Long.valueOf(21)).split(5);
        assertEquals(5, partitions.size());
        assertEquals("id BETWEEN 1 AND 5", partitions.get(0).toString());
        assertEquals("id BETWEEN 6 AND 10", partitions.get(1).toString());
        assertEquals("id BETWEEN 11 AND 15", partitions.get(2).toString());
        assertEquals("id BETWEEN 16 AND 20", partitions.get(3).toString());
        assertEquals("id BETWEEN 20 AND 21", partitions.get(4).toString());
    }
}
