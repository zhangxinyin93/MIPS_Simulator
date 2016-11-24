/**
 * Reservation Stations' Entry -> Page 188
 * 10 ALU entries
 */
public class ReservationStationEntry {
    private boolean busy;
    private int Vj; // operand1
    private int Vk; // operand2
    private int Qj; // source1 should be initialized as 0
    private int Qk; // source2 should be initialized as 0
    private int destination; // #ReorderBuffer
    private int immediateValue; // for load and store to calculate the mem address
    private Instruction instruction;

    // Only need to be written back once
    private boolean writtenBack;

    public ReservationStationEntry(Instruction instruction) {
        this.busy = true;
        this.writtenBack = false;
        this.Vj = Integer.MIN_VALUE;
        this.Vk = Integer.MIN_VALUE;
        this.destination = -1;
        this.instruction = instruction;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public int getVj() {
        return Vj;
    }

    public void setVj(int Vj) {
        this.Vj = Vj;
    }

    public int getVk() {
        return Vk;
    }

    public void setVk(int Vk) {
        this.Vk = Vk;
    }


    public int getQj() {
        return Qj;
    }

    public void setQj(int Qj) {
        this.Qj = Qj;
    }

    public int getQk() {
        return Qk;
    }

    public void setQk(int Qk) {
        this.Qk = Qk;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getImmidateValue() {
        return immediateValue;
    }

    public void setImmediateValue(int immediateValue) {
        this.immediateValue = immediateValue;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public boolean hasWrittenBack() {
        return writtenBack;
    }

    public void setWrittenBack(boolean writtenBack) {
        this.writtenBack = writtenBack;
    }
}
