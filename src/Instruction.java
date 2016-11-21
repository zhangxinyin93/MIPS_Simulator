/**
 *
 */
public class Instruction {
    private String operation;
    private int address;
    private int immValue;
    private int rs;
    private int rt;
    private int rd;
    private int sa;

    // For branch instruction
    private int predictor;

    public Instruction() {

    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getImmValue() {
        return immValue;
    }

    public void setImmValue(int immValue) {
        this.immValue = immValue;
    }

    public int getRs() {
        return rs;
    }

    public void setRs(int rs) {
        this.rs = rs;
    }

    public int getRt() {
        return rt;
    }

    public void setRt(int rt) {
        this.rt = rt;
    }

    public int getRd() {
        return rd;
    }

    public void setRd(int rd) {
        this.rd = rd;
    }

    public int getSa() {
        return sa;
    }

    public void setSa(int sa) {
        this.sa = sa;
    }

    public int getPredictor() {
        return predictor;
    }

    public void setPredictor(int predictor) {
        this.predictor = predictor;
    }
}
