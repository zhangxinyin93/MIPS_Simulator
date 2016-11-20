/**
 * Reservation Stations' Entry -> Page 188
 * 10 ALU entries
 */
public class ReservationStationEntry {
    private boolean busy;
    private String operation;
    private int Vj; // operand1
    private int Vk; // operand2
    private int Qj; // source1
    private int Qk; // source2
    private int destination; // #ReorderBuffer
    private int immediateValue; // for load and store to calculate the mem address
    private Instruction instruction;

    public ReservationStationEntry(Instruction instruction) {
        this.busy = true;
        this.Vj = -1;
        this.Vk = -1;
        this.operation = instruction.operation;
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

    public void setImmidateValue(int immediateValue) {
        this.immediateValue = immediateValue;
    }
}
