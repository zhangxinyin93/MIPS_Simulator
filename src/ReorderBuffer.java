import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Reorder buffer with 6 entries
 */
public class ReorderBuffer {
    private int capacity;
    /**
     * Integer -> EntryId
     */
    private Map<Integer, ReorderBufferEntry> reorderBufferMap;
    private Queue<ReorderBufferEntry> reorderBufferQueue;
    private boolean reclaim;

    public ReorderBuffer() {
        this.capacity = 6;
        this.reorderBufferMap = new HashMap<>();
        this.reorderBufferQueue = new LinkedList<>();
        this.reclaim = false;
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

    /**
     * Add reorder buffer entry into reorder buffer
     * @param entry
     *          Reorder buffer entry
     * @return reorder buffer Id
     */
    public boolean add(int entryId, ReorderBufferEntry entry) {
        if(isFull()) {
            return false;
        }
        reorderBufferMap.put(entryId, entry);
        reorderBufferQueue.add(entry);
        return true;
    }

    public Queue<ReorderBufferEntry>  getReorderBufferQueue() {
        return reorderBufferQueue;
    }

    public ReorderBufferEntry poll() {
        if(isEmpty()) return null;
        ReorderBufferEntry robEntry = reorderBufferQueue.poll();
        reorderBufferMap.remove(robEntry.getBufferId());
        return robEntry;
    }

    public boolean isReclaim() {
        return reclaim;
    }

    public void setReclaim(boolean reclaim) {
        this.reclaim = reclaim;
    }
}
