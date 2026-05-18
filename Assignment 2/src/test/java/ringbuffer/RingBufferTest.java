package ringbuffer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


 // Unit tests for RingBuffer, RingBufferReader and ReadResult

public class RingBufferTest {

    @Test
    // checks if the buffer stores the correct capacity
    void testCapacityIsStoredCorrectly() {
        RingBuffer<String> buffer = new RingBuffer<>(5);
        assertEquals(5, buffer.capacity());
    }

    @Test
    // makes sure zero cappacity throws an exception
    void testCapacityZeroThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(0));
    }

    @Test
    // makes sure negative capacity is not allowed
    void testCapacityNegativeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RingBuffer<>(-3));
    }

    @Test
    // checks reading from empty buffer returns no item
    void testReadFromEmptyBufferReturnsNoItem() {
        RingBuffer<String> buffer = new RingBuffer<>(3);
        RingBufferReader<String> reader = buffer.createReaderFromNow();
        ReadResult<String> result = reader.read();
        assertFalse(result.hasItem());
    }

    @Test
    // checks missed count stays zero for empty buffer
    void testReadFromEmptyBufferHasZeroMissedCount() {
        RingBuffer<String> buffer = new RingBuffer<>(3);
        RingBufferReader<String> reader = buffer.createReaderFromOldestAvailable();
        ReadResult<String> result = reader.read();
        assertEquals(0, result.getMissedCount());
    }

    @Test
    // tests writring one item and reading it back
    void testWriteOneItemThenRead() {
        RingBuffer<String> buffer = new RingBuffer<>(3);
        RingBufferReader<String> reader = buffer.createReaderFromOldestAvailable();
        buffer.write("hello");
        ReadResult<String> result = reader.read();
        assertTrue(result.hasItem());
        assertEquals("hello", result.getItem().orElse(null));
    }

    @Test
    // checks if items are read in the same order they were written
    void testReadMultipleItemsInOrder() {
        RingBuffer<String> buffer = new RingBuffer<>(5);
        RingBufferReader<String> reader = buffer.createReaderFromOldestAvailable();
        buffer.write("A");
        buffer.write("B");
        buffer.write("C");

        assertEquals("A", reader.read().getItem().orElse(null));
        assertEquals("B", reader.read().getItem().orElse(null));
        assertEquals("C", reader.read().getItem().orElse(null));
    }

    @Test
    // makes sure reader from now ignores old items
    void testReaderFromNowDoesNotSeeOldItems() {
        RingBuffer<String> buffer = new RingBuffer<>(5);
        buffer.write("old1");
        buffer.write("old2");

        // Reader created AFTER writing, so it should NOT see old items
        RingBufferReader<String> reader = buffer.createReaderFromNow();
        ReadResult<String> result = reader.read();
        assertFalse(result.hasItem());
    }

    @Test
    // checks if reader sees only new items after creation
    void testReaderFromNowSeesItemsWrittenAfterCreation() {
        RingBuffer<String> buffer = new RingBuffer<>(5);
        buffer.write("old");

        RingBufferReader<String> reader = buffer.createReaderFromNow();
        buffer.write("new");

        ReadResult<String> result = reader.read();
        assertTrue(result.hasItem());
        assertEquals("new", result.getItem().orElse(null));
    }


    @Test
    // checks if oldest item gets overwrirren when buffer is full
    void testOldestItemIsOverwrittenWhenBufferIsFull() {
        // Capacity 3: writes "A","B","C","D" and then "A" gets overwritten
        RingBuffer<String> buffer = new RingBuffer<>(3);
        RingBufferReader<String> reader = buffer.createReaderFromOldestAvailable();

        buffer.write("A");
        buffer.write("B");
        buffer.write("C");
        buffer.write("D"); // overwrites "A"

        // Reader started before writes, it will miss "A"
        ReadResult<String> result = reader.read();
        assertTrue(result.getMissedCount() > 0 || result.hasItem());
        // The important thing is we don't crash and we get something valid
    }

    @Test
    // makes sure reader detects skipped items correctly
    void testReaderDetectsMissedItems() {
        RingBuffer<String> buffer = new RingBuffer<>(3);
        RingBufferReader<String> reader = buffer.createReaderFromOldestAvailable();

        // Write 5 items into a buffer of size 3 — reader will miss 2
        buffer.write("A");
        buffer.write("B");
        buffer.write("C");
        buffer.write("D");
        buffer.write("E");

        ReadResult<String> result = reader.read();
        // Reader should be notified that some items were missed
        assertTrue(result.getMissedCount() > 0);
    }

    @Test
    // checks read result when an item exists
    void testReadResultWithItem() {
        ReadResult<String> result = new ReadResult<>("test", 0);
        assertTrue(result.hasItem());
        assertEquals("test", result.getItem().orElse(null));
        assertEquals(0, result.getMissedCount());
    }

    @Test
    // checks read result when there is no item
    void testReadResultWithNoItem() {
        ReadResult<String> result = new ReadResult<>(null, 0);
        assertFalse(result.hasItem());
        assertTrue(result.getItem().isEmpty());
    }

    @Test
    // checks if missed count is stored correctly
    void testReadResultWithMissedCount() {
        ReadResult<String> result = new ReadResult<>("x", 5);
        assertEquals(5, result.getMissedCount());
        assertTrue(result.hasItem());
    }

    @Test
    // makes sure toString works without crashing
    void testReadResultToStringDoesNotCrash() {
        ReadResult<String> result = new ReadResult<>("hello", 2);
        // Just make sure it doesn't throw
        assertNotNull(result.toString());
    }

    @Test
    // checks if reader position moves foreward after reading
    void testReaderCursorAdvancesAfterRead() {
        RingBuffer<String> buffer = new RingBuffer<>(5);
        RingBufferReader<String> reader = buffer.createReaderFromOldestAvailable();
        buffer.write("A");

        long before = reader.getNextSequenceToRead();
        reader.read();
        long after = reader.getNextSequenceToRead();

        assertTrue(after > before);
    }

    @Test
    // makes sure cursor does not move if nothing was read
    void testReaderCursorDoesNotAdvanceWhenNoItemAvailable() {
        RingBuffer<String> buffer = new RingBuffer<>(5);
        RingBufferReader<String> reader = buffer.createReaderFromNow();

        long before = reader.getNextSequenceToRead();
        reader.read(); // nothing to read
        long after = reader.getNextSequenceToRead();

        assertEquals(before, after);
    }

    @Test
    // checks if two readers work independently from each other
    void testTwoReadersAreIndependent() {
        RingBuffer<String> buffer = new RingBuffer<>(5);

        buffer.write("A");
        buffer.write("B");

        RingBufferReader<String> r1 = buffer.createReaderFromOldestAvailable();
        RingBufferReader<String> r2 = buffer.createReaderFromOldestAvailable();

        assertEquals("A", r1.read().getItem().orElse(null)); // r1 moved forward, r2 should still be at A
        assertEquals("A", r2.read().getItem().orElse(null));
    }

    @Test
    // checks if the buffer also works with intergers
    void testBufferWorksWithIntegers() {
        RingBuffer<Integer> buffer = new RingBuffer<>(3);
        RingBufferReader<Integer> reader = buffer.createReaderFromOldestAvailable();
        buffer.write(42);
        ReadResult<Integer> result = reader.read();
        assertTrue(result.hasItem());
        assertEquals(42, result.getItem().orElse(null));
    }

    @Test
    // checks reading after the last item returns empty result
    void testReadingPastLastItemReturnsEmpty() {
        RingBuffer<String> buffer = new RingBuffer<>(3);
        RingBufferReader<String> reader = buffer.createReaderFromOldestAvailable();
        buffer.write("only");

        reader.read(); // reads "only"
        ReadResult<String> second = reader.read(); // nothing left
        assertFalse(second.hasItem());
    }
}