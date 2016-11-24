import java.util.HashMap;

/**
 * This class can dissemble a binary file for MIPS
 */
public class Disassembler {
    public static void main(String[] args) throws Exception {
        for(int fileNumber = 1; fileNumber <= args.length; fileNumber++) {
            DisassembleUtil.disassemble(args[fileNumber-1], fileNumber, new HashMap<>());
        }
    }
}
