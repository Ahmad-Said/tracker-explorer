package said.ahmad.javafx.util;


public class Holder<T> {
    private T value;

    public Holder() {
    }

    // Constructor
    public Holder(T value) {
        this.value = value;
    }

    // Getter for value
    public T getValue() {
        return value;
    }

    // Setter for value
    public T setValue(T value) {
        this.value = value;
        return value;
    }

    @Override
    public String toString() {
        return "Holder{" + "value=" + value + '}';
    }
}
