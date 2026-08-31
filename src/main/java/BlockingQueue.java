import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void enqueue(T t) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();
        }
        queue.add(t);
        notifyAll();
    }

    public synchronized T dequeue() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        T poll = queue.poll();
        notifyAll();
        return poll;
    }

    public synchronized int size() {
        return queue.size();
    }
}
