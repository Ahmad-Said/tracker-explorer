package said.ahmad.javafx.util;

public class ArrayListHelper {
    /**
     * Return valid number in the range from 0 to array size -1 Index sent by
     * parameter can be negative. Example:
     * <ul>
     * <li>sent -> return (x element)</li>
     * <li>0 -> 0 (first element)</li>
     * <li>1 -> 0 (first element)</li>
     * <li>2 -> 1 (second element)</li>
     * <li>3 -> 2 (third element)</li>
     * <li>... Relative number</li>
     * <li>-1 -> array size - 1 (last element)</li>
     * <li>-2 -> array size - 2 (before the last element)</li>
     * </ul>
     *
     * @param indexRelativeStartFrom1 can be negative
     * @param arraySize Usually selections size of files
     * @return valid index in range of 0 to arraySize - 1
     */
    public static int getCyclicIndex(int indexRelativeStartFrom1, int arraySize) {
        if (arraySize == 0) {
            return 0;
        }
        if (indexRelativeStartFrom1 > 0) {
            indexRelativeStartFrom1--;
        }
        return (indexRelativeStartFrom1 + (indexRelativeStartFrom1 / arraySize + 1) * arraySize) % arraySize;
    }
}
