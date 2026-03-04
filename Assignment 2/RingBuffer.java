package ringbuffer;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class RingBuffer<T> {

    private static final class Slot<T> {
        long sequence = Long.MIN_VALUE; // marks "empty"
        T value = null;
    }

    private final int capacity;
    private final Slot<T>[] slots;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Global, monotonic sequence of the next write
    private long writeSequence = 0;

    // Helps reader alignment after a miss; guarded by read/write lock usage.
    private final ThreadLocal<Long> lastServedSequence = ThreadLocal.withInitial(() -> -1L);

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.slots = (Slot<T>[]) new Slot[capacity];
        for (int i = 0; i < capacity; i++) {
            slots[i] = new Slot<>();
        }
    }

    public int capacity() {
        return capacity;
    }

    /**
     * Single writer appends one item.
     * If buffer is full, it overwrites the oldest automatically.
     */
    public void write(T item) {
        lock.writeLock().lock();
        try {
            long seq = writeSequence;
            int index = indexFor(seq);

            Slot<T> slot = slots[index];
            slot.value = item;
            slot.sequence = seq;

            writeSequence++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates a new reader starting from "now" (only future items).
     */
    public RingBufferReader<T> createReaderFromNow() {
        lock.readLock().lock();
        try {
            return new RingBufferReader<>(this, writeSequence);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Creates a new reader starting from the oldest currently available item.
     */
    public RingBufferReader<T> createReaderFromOldestAvailable() {
        lock.readLock().lock();
        try {
            long oldest = oldestAvailableSequence();
            return new RingBufferReader<>(this, oldest);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Internal read used by RingBufferReader.
     * It reads exactly one item for a given desired sequence.
     */
    ReadResult<T> readAt(long desiredSequence) {
        lock.readLock().lock();
        try {
            long currentWrite = writeSequence;
            long oldestAvailable = Math.max(0, currentWrite - capacity);

            // No data yet or reader is ahead of writer
            if (desiredSequence >= currentWrite) {
                lastServedSequence.set(-1L);
                return new ReadResult<>(null, 0);
            }

            // Reader fell behind and missed items
            if (desiredSequence < oldestAvailable) {
                long missed = oldestAvailable - desiredSequence;
                desiredSequence = oldestAvailable;

                T item = tryGet(desiredSequence);
                lastServedSequence.set(desiredSequence);
                return new ReadResult<>(item, missed);
            }

            // Normal case: desired sequence is within available window
            T item = tryGet(desiredSequence);
            lastServedSequence.set(desiredSequence);
            return new ReadResult<>(item, 0);

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Helper for reader cursor alignment after a miss.
     * The reader calls this to set nextSequenceToRead correctly.
     */
    long getLastSequenceServedToReaderPlusOne(long fallback) {
        Long served = lastServedSequence.get();
        if (served == null || served < 0) return fallback;
        return served + 1;
    }

    private T tryGet(long sequence) {
        int index = indexFor(sequence);
        Slot<T> slot = slots[index];

        // If overwritten since last read, slot.sequence won't match requested sequence
        if (slot.sequence != sequence) {
            // This can happen if writer overwrote between reads (rare due to locks),
            // or if the reader asked for a sequence that is no longer stored.
            // In our design, window checks should prevent it, but we keep it safe.
            return null;
        }
        return slot.value;
    }

    private int indexFor(long sequence) {
        // sequence is non-negative in our usage
        return (int) (sequence % capacity);
    }

    private long oldestAvailableSequence() {
        long oldest = writeSequence - capacity;
        return Math.max(0, oldest);
    }
}
