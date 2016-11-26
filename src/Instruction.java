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
    private String command;
    // The result from rob only can be used in next cycle
    private boolean executeJNextCycle;
    private boolean executeKNextCycle;

    // For branch instruction
    private int predictor;
    public boolean isWrongPredicted;
    public boolean writtenInSameCycle;

    // For Load instruction, at write back stage needs two cycle
    private boolean firstCycle;

    public Instruction() {
        this.isWrongPredicted = false;
        this.firstCycle = false;
        this.executeJNextCycle = false;
        this.executeKNextCycle = false;
        this.writtenInSameCycle = false;
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

    public boolean finishedFirstCycle() {
        return firstCycle;
    }

    public void setFirstCycle(boolean firstCycle) {
        this.firstCycle = firstCycle;
    }

    public boolean needExecuteJNextCycle() {
        return executeJNextCycle;
    }

    public void setExecuteJNextCycle(boolean executeJNextCycle) {
        this.executeJNextCycle = executeJNextCycle;
    }

    public boolean needExecuteKNextCycle() {
        return executeKNextCycle;
    }

    public void setExecuteKNextCycle(boolean executeKNextCycle) {
        this.executeKNextCycle = executeKNextCycle;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
