import java.util.ArrayDeque;
import java.util.Deque;

public final class MyStringBuilder {

    private char[] value;
    private int count;
    private final Deque<Snapshot> history = new ArrayDeque<>();

    public MyStringBuilder() {
        value = new char[16];
        count = 0;
    }

    private record Snapshot(char[] value, int count) {
        Snapshot {
            value = value.clone();
        }
    }

    private void saveSnapshot() {
        history.push(new Snapshot(value, count));
    }

    public void undo() {
        if (history.isEmpty()) {
            return;
        }
        Snapshot snapshot = history.pop();
        this.value = snapshot.value.clone();
        this.count = snapshot.count;
    }

    public MyStringBuilder append(String s) {
        saveSnapshot();
        ensureCapacity(count + s.length());
        for (int i = 0; i < s.length(); i++) {
            value[count++] = s.charAt(i);
        }
        return this;
    }

    private void ensureCapacity(int minCapacity) {
        if (value.length < minCapacity) {
            char[] newValue = new char[Math.max(value.length * 2, minCapacity)];
            System.arraycopy(value, 0, newValue, 0, count);
            value = newValue;
        }
    }

    @Override
    public String toString() {
        return new String(value, 0, count);
    }
}



