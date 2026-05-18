package ringbuffer;

import java.util.Optional;

public final class ReadResult<T> {
    private final T item;              // can be null if nothing available
    private final long missedCount;    // number of items skipped due to overwrite

    public ReadResult(T item, long missedCount) {
        this.item = item;
        this.missedCount = missedCount;
    }

    public Optional<T> getItem() {
        return Optional.ofNullable(item);
    }

    public long getMissedCount() {
        return missedCount;
    }

    public boolean hasItem() {
        return item != null;
    }

    @Override
    public String toString() {
        return "ReadResult{item=" + item + ", missedCount=" + missedCount + "}";
    }
}
