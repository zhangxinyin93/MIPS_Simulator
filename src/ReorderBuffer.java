import java.util.HashMap;
import java.util.Map;

/**
 * Reorder buffer with 6 entries
 */
public class ReorderBuffer {
    private int capacity;
    /**
     * Integer -> EntryId
     */
    private Map<Integer, ReorderBufferEntry> reorderBufferMap;

    public ReorderBuffer() {
        this.capacity = 6;
        this.reorderBufferMap = new HashMap<>();
    }

    public boolean isFull() {
        return reorderBufferMap.size() == capacity;
    }

    public boolean isEmpty() {
        return reorderBufferMap.size() == 0;
    }

    public ReorderBufferEntry getROBEntry(int entryId) {
        return reorderBufferMap.get(entryId);
    }

    public boolean add(int entryId, ReorderBufferEntry entry) {
        if(isFull()) {
            return false;
        }
        reorderBufferMap.put(entryId, entry);
        return true;
    }
}
