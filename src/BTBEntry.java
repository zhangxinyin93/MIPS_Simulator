/**
 * Branch-Target Buffer
 * Used for predict branch instruction in IF stage
 */
public class BTBEntry {
    private int instructionAddress;
    private int targetAddress;
    /**
     * One bit predictor
     * 0 for non-taken, and 1 for taken
     */
    private int predictor;

    public BTBEntry(int instructionAddress, int targetAddress, int predictor) {
        this.instructionAddress = instructionAddress;
        this.targetAddress = targetAddress;
        this.predictor = predictor;
    }

    public int getInstructionAddress() {
        return instructionAddress;
    }

    public void setInstructionAddress(int instructionAddress) {
        this.instructionAddress = instructionAddress;
    }

    public int getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(int targetAddress) {
        this.targetAddress = targetAddress;
    }

    public int getPredictor() {
        return predictor;
    }

    public void setPredictor(int predictor) {
        this.predictor = predictor;
    }
}
