package ringbuffer;

public final class Demo {

    public static void main(String[] args) {
        RingBuffer<String> buffer = new RingBuffer<>(3);

        // Reader A starts from oldest available (will read from beginning)
        RingBufferReader<String> readerA = buffer.createReaderFromOldestAvailable();

        buffer.write("A");
        buffer.write("B");

        // Reader B starts from now (won't see A,B)
        RingBufferReader<String> readerB = buffer.createReaderFromNow();

        System.out.println("ReaderA: " + readerA.read()); // A
        System.out.println("ReaderA: " + readerA.read()); // B
        System.out.println("ReaderB: " + readerB.read()); // null

        buffer.write("C");
        buffer.write("D"); // overwrites oldest if needed (capacity=3)
        buffer.write("E"); // likely overwrites

        System.out.println("ReaderA: " + readerA.read()); // may miss depending on timing
        System.out.println("ReaderB: " + readerB.read()); // should read C then D then E (unless overwritten)
    }
}
