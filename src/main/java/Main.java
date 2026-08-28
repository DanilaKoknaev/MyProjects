import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        String[] words = {"a", "b", "a", "c", "a", "b" , "d"};
        System.out.println(countEl(words));

        Integer[] numbers = {1, 2, 2, 3, 3, 3, 4};
        System.out.println(countEl(numbers));

        Person [] persons = {new Person ("Иван"), new Person ("Марина"), new Person ("Иван")};
        System.out.println(countEl(persons));
    }

    public static class Person {
        String name;
        public Person(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static <T> Map<T, Integer> countEl(T[] array) {
        Map<T, Integer> map = new HashMap<>();
        for (T item : array) {
            if (map.containsKey(item)) {
                map.put(item, map.get(item) + 1);
            } else {
                map.put(item, 1);
            }
            //map.merge(item, 1, Integer::sum); - тоже самое (запомнить!)
        }
        return map;
    }
}
