import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;

/**
 * Write to file as the required format
 */
public class WriteUtil {
    private BufferedWriter writeToFile;

    public WriteUtil(String outputFileName) throws Exception{
        this.writeToFile = new BufferedWriter(new FileWriter(new File(outputFileName)));
    }

    public void write(int cycle, Queue<Instruction> instructionQueue, ReservationStation reservationStation,
                      ReorderBuffer reorderBuffer, BTB btb, RegisterStatus registerStatus,
                      Map<Integer,Integer> datasegment) throws Exception{
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

        // Write BTB
        writeToFile.write("BTB:");
        writeToFile.newLine();
        int count = 0;
        for (BTBEntry btbEntry : btb.getBtbQueue()) {
            count++;
            writeToFile.write("[Entry" + count + "]");
            writeToFile.write("<" + btbEntry.getInstructionAddress() + "," + btbEntry.getTargetAddress() + "," + btbEntry.getPredictor() + ">");
            writeToFile.newLine();
        }

        // Write Register
        writeToFile.write("Registers:");
        writeToFile.newLine();
        for (int i = 0; i < 32; i++) {
            if (i % 8 == 0) {
                if (i - 10 < 0) {
                    writeToFile.write("R0" + i + ":" + '\t');
                } else {
                    writeToFile.write("R" + i + ":" + '\t');
                }
            }

            // Write Value
            writeToFile.write(Integer.toString(registerStatus.getRegister(i).getValue()));

            if (i % 8 == 7) {
                writeToFile.newLine();
            } else {
                writeToFile.write('\t');
            }
        }

        // Write data segment
        writeToFile.write("Data Segment:");
        writeToFile.newLine();
        writeToFile.write("716:" + "\t");
        for (int i = 0; i < datasegment.size(); i++) {
            int address = i * 4 + 716;
            writeToFile.write(Integer.toString(datasegment.get(address)));
            if(i % 10 == 9) {
                writeToFile.newLine();
            } else {
                writeToFile.write("\t");
            }
        }
        writeToFile.flush();
    }

    public void close() throws IOException {
        writeToFile.close();
    }
}
