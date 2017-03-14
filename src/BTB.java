import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Branch Target Buffer with 16 entries for branch prediction
 */
public class BTB {
    private int capacity;
    private Map<Integer, BTBEntry> btb;
    private Queue<BTBEntry> btbQueue;

    public BTB() {
        this.capacity = 16;
        this.btb = new HashMap<>();
        this.btbQueue = new LinkedList<>();
    }

    public void put(BTBEntry entry) {
        btb.put(entry.getInstructionAddress(),entry);
        btbQueue.add(entry);
    }

    public boolean containsInstruction(int instructionAddress) {
        return btb.containsKey(instructionAddress);
    }

    public int getTargetAddress(int instructionAddress) {
        BTBEntry entry = btb.get(instructionAddress);
        int predictor = entry.getPredictor();
        if(predictor == 1) {
            return entry.getTargetAddress();
        }
        return instructionAddress + 4;
    }

    public int getPrediction(int instructionAddress) {
        return btb.get(instructionAddress).getPredictor();
    }

    public BTBEntry getEntry(int instructionAddress) {
        return btb.get(instructionAddress);
    }

    public Queue<BTBEntry> getBtbQueue() {
        return btbQueue;
    }
}
