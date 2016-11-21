/**
 * Reorder Buffer for in-order commit
 */
public class ReorderBufferEntry {
    private int bufferId;
    private boolean busy;
    private Instruction instruction;
    private boolean ready;
    private RegisterFileEntry destination; // target register
    private int value; // memory address or calculated value

    public ReorderBufferEntry(int bufferId, Instruction instruction) {
        this.bufferId = bufferId;
        this.busy = true;
        this.instruction = instruction;
    }

    public int getBufferId() {
        return bufferId;
    }

    public void setBufferId(int bufferId) {
        this.bufferId = bufferId;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public RegisterFileEntry getDestination() {
        return destination;
    }

    public void setDestination(RegisterFileEntry destination) {
        this.destination = destination;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
