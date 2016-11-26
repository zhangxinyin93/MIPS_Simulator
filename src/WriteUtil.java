import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Queue;

/**
 * Write to file as the required format
 */
public class WriteUtil {
    private String outputFileName;
    private BufferedWriter writeToFile;

    public WriteUtil(String outputFileName) throws Exception{
        this.outputFileName = outputFileName;
        this.writeToFile = new BufferedWriter(new FileWriter(new File(outputFileName)));
    }

    public void write(int cycle, Queue<Instruction> instructionQueue, ReservationStation reservationStation, ReorderBuffer reorderBuffer, BTB btb) throws Exception{
        // Write cycle
        String cycleString = "Cycle" + " " + "<" + cycle + ">" + ":";
        writeToFile.write(cycleString);
        writeToFile.newLine();

        // Write IQ
        writeToFile.write("IQ:");
        writeToFile.newLine();
        for (Instruction instruction : instructionQueue) {
            writeToFile.write("[" + instruction.getCommand() + "]");
            writeToFile.newLine();
        }

        // Write RS
        writeToFile.write("RS:");
        writeToFile.newLine();
        for (ReservationStationEntry rs : reservationStation.getReservationQueue()) {
            writeToFile.write("[" + rs.getInstruction().getCommand() + "]");
            writeToFile.newLine();
        }

        // Write ROB
        writeToFile.write("ROB:");
        writeToFile.newLine();
        for (ReorderBufferEntry rob : reorderBuffer.getReorderBufferQueue()) {
            writeToFile.write("[" + rob.getInstruction().getCommand() + "]");
            writeToFile.newLine();
        }
    }
}
