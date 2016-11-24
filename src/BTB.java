import java.util.HashMap;
import java.util.Map;

/**
 * Branch Target Buffer with 16 entries for branch prediction
 */
public class BTB {
    private int capacity;
    private Map<Integer, BTBEntry> btb;

    public BTB() {
        this.capacity = 16;
        this.btb = new HashMap<>();
    }

    public void put(BTBEntry entry) {
        btb.put(entry.getInstructionAddress(),entry);
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
}
