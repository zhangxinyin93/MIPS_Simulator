import java.util.HashMap;
import java.util.Map;

/**
 * The status table for 32 register
 * Complete
 */
public class RegisterStatus {
    private Map<Integer,RegisterFileEntry> registersMap;

    public RegisterStatus() {
        this.registersMap = new HashMap<>();

        for(int i = 0; i < 32; i++) {
            registersMap.put(i,new RegisterFileEntry());
        }
    }

    public RegisterFileEntry getRegister(int registerNum) {
        return registersMap.get(registerNum);
    }

    public void setValue(int registerNum, int value) {
        registersMap.get(registerNum).setValue(value);
    }
}
