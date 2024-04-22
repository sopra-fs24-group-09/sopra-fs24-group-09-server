package ch.uzh.ifi.hase.soprafs24.model;

public class TimestampedRequest<T> {
    private long timestamp;
    private T message;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }
}

