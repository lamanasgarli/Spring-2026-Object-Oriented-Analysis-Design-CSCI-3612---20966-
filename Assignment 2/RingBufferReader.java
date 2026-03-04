package ringbuffer;

public final class RingBufferReader<T> {
    private final RingBuffer<T> buffer;
    private long nextSequenceToRead;

    RingBufferReader(RingBuffer<T> buffer, long startSequence) {
        this.buffer = buffer;
        this.nextSequenceToRead = startSequence;
    }

    public ReadResult<T> read() {
        ReadResult<T> result = buffer.readAt(nextSequenceToRead);
        if (result.hasItem()) {
            nextSequenceToRead++; // consume exactly one sequence step
        } else {
            // if nothing available, nextSequenceToRead remains unchanged
            // so a future write can make data available later
        }

        // If we missed items, buffer.readAt() already advanced us to oldest available
        // by returning missedCount and delivering first readable item.
        if (result.getMissedCount() > 0 && result.hasItem()) {
            // Important detail:
            // buffer.readAt() returns item at adjusted sequence.
            // We must align the local cursor to (adjustedSequence + 1).
            // The buffer encodes that by updating internally through return shape.
            // We handle it by asking buffer for the "current sequence used".
            nextSequenceToRead = buffer.getLastSequenceServedToReaderPlusOne(nextSequenceToRead);
        }

        return result;
    }

    public long getNextSequenceToRead() {
        return nextSequenceToRead;
    }
}
