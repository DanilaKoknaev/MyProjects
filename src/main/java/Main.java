
public class Main {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new BlockingQueue<>(10);


        Thread prod = new Thread(() -> {
            int i = 0;
            try {
                while (true) {
                    queue.enqueue(i);
                    System.out.println("Положил" + i);
                    i++;
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });


        Thread cons = new Thread(() -> {
            try {
                while (true) {
                    Integer value = queue.dequeue();
                    System.out.println("Взял " + value);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        prod.start();
        cons.start();


    }
}
