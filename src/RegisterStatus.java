import java.util.ArrayList;
import java.util.List;

/**
 * The status table for 32 register
 * Complete
 */
public class RegisterStatus {
    private int capacity;
    public List<RegisterFileEntry> registerFileList;

    public RegisterStatus() {
        this.capacity = 32;
        this.registerFileList = new ArrayList<>();

        for(int i = 0; i < capacity; i++) {
            registerFileList.add(new RegisterFileEntry());
        }
    }

    public RegisterFileEntry getRegister(int registerNum) {
        return registerFileList.get(registerNum);
    }
}
