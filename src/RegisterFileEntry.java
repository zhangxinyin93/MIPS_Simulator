/**
 * Register File (destination in reorder buffer)
 * toString?
 */
public class RegisterFileEntry {
    private int reorderBufferNum;
    private int value;
    private boolean busy;

    public RegisterFileEntry() {
        this.busy = false;
        this.value = 0;
        this.reorderBufferNum = -1;
    }

    public int getReorderBufferNum() {
        return reorderBufferNum;
    }

    public void setReorderBufferNum(int reorderBufferNum) {
        this.reorderBufferNum = reorderBufferNum;
        busy = true;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

}
