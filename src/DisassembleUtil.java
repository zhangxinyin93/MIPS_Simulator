import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

/**
 * This class is an util class to interpret bin file
 * and construct to assemble language for MIPS
 */
public class DisassembleUtil {

    /**
     * A map to map first six bits to correspond operation
     */
    private static Map<String,String> firstSixBitsMap;

    /**
     * A map to map tail six bits to correspond operation
     */
    private static Map<String,String> tailSixBitsMap;

    /**
     * InputStream to read binary file
     */
    private static DataInputStream readBinaryFile;

    /**
     * Writer to write the interpreted file
     */
    private static BufferedWriter writeInterpretedFile;

    /**
     * Flag to decide if it needs space between binary string
     */
    private static boolean needSpace = true;

    /**
     * The space to be used as delimiter
     */
    private static char SPACE = ' ';

    /**
     * Initialize map of opcode and operation
     */
    static{
        firstSixBitsMap = new HashMap<String,String>() {{
            put("001000","ADDI"); put("001001","ADDIU");
            put("000100","BEQ"); put("000101","BNE"); put("000111","BGTZ"); put("000110","BLEZ"); put("000010","J");
            put("001010","SLTI");
            put("101011","SW"); put("100011","LW");
        }};

        tailSixBitsMap = new HashMap<String,String>() {{
            put("001101","BREAK");
            put("101010","SLT"); put("101011","SLTU");
            put("000000","SLL"); put("000010","SRL"); put("000011","SRA");
            put("100010","SUB"); put("100011","SUBU"); put("100000","ADD"); put("100001","ADDU");
            put("100100","AND"); put("100101","OR"); put("100110","XOR"); put("100111","NOR");
        }};
    }

    /**
     * This method is to disassemble the binary String
     * @param filename
     *          The binary file name
     */

    private static Map<Integer,Instruction> allInstructions = new HashMap<>();

    public static Map<Integer, Instruction> disassemble(String filename, int fileNumber) {
        try{
            String outputFileName = String.format("output_%d.txt", fileNumber);
            int instructionCount = 0;
            boolean needInterpret = true;
            File binaryFile = new File(filename);
            File outputFile = new File(outputFileName);
            outputFile.createNewFile();

            readBinaryFile = new DataInputStream(new FileInputStream(binaryFile));
            writeInterpretedFile = new BufferedWriter(new FileWriter(outputFile));

            int fileByte;
            String instruction = "";
            String operation = "";
            String operand = "0";

            String[] instructionPieces = new String[6];

            int byteCount = 0;
            int instructionAddress;

            for (int i = 0; i < binaryFile.length(); i++) {
                fileByte = readBinaryFile.readUnsignedByte();
                instruction = instruction + (String.format("%8s", Integer.toBinaryString(fileByte))).replace(' ', '0');
                byteCount++;

                // Every 4 bytes is an instruction for MIPS
                if (byteCount == 4) {
                
                    // Cut instruction to 6 5 5 5 5 6 format
                    instructionPieces[0] = instruction.substring(0, 6);
                    instructionPieces[5] = instruction.substring(26, 32);
                    for (int piece = 1; piece <= 4; piece++) {
                        instructionPieces[piece] = instruction.substring(piece * 5 + 1, piece * 5 + 6);
                    }

                    // construct instruction for part2
                    Instruction instructionObject = new Instruction();

                    // Calculate instruction address
                    instructionAddress = 600 + 4 * instructionCount;
                    instructionObject.setAddress(instructionAddress);

                    // Interpret binary string
                    if (needInterpret) {
                        // Interpret operation
                        operation = interpretOperation(instructionPieces);
                        operand = interpretOperand(operation, instructionPieces, instructionObject);

                        instructionObject.setOperation(operation);

                        if (operation.equals("BREAK")) {
                            needInterpret = false;
                        }
                    }
                    // The data region starts from 716
                    else if(instructionAddress >= 716) {
                        operand = Integer.toString(getSignedData(instruction));
                    }

                    // Write to file
                    if(operation.equals("")) {
                        needSpace = false;
                    }

                    allInstructions.put(instructionAddress,instructionObject);
                    instruction = format(instructionAddress,instructionPieces,operation,operand);
                    writeInterpretedFile.write(instruction);
                    if(i != binaryFile.length()-1) {
                        writeInterpretedFile.newLine();
                    }
                    writeInterpretedFile.flush();


                    // Clean
                    instructionCount++;
                    byteCount = 0;
                    instruction = "";
                    operation = "";
                    operand = "0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try{
                System.out.println("Close inputStream and Writer");
                readBinaryFile.close();
                writeInterpretedFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return allInstructions;
    }

    /**
     * This method is to interpret operation
     * @param instructionPieces
     *          Divided instruction binary string as 6 5 5 5 5 6
     * @return Operation name in String format
     */
    private static String interpretOperation(String[] instructionPieces) {
        String operation;
        if(instructionPieces[0].equals("000000")) {
            operation = tailSixBitsMap.get(instructionPieces[5]);
            if(operation.equals("SLL") && instructionPieces[2].equals("00000")) {
                operation = "NOP";
            }
        } else {
            operation = firstSixBitsMap.get(instructionPieces[0]);
            if(operation.equals("") && instructionPieces[0].equals("000001")) {
                if(instructionPieces[2].equals("00001")) {
                    operation = "BGEZ";
                }
                if(instructionPieces[2].equals("00000")) {
                    operation = "BLTZ";
                }
            }
        }
        return operation;
    }

    /**
     * This method is to interpret operand part
     * @param operation
     *          Correspond operation name
     * @param instructionPieces
     *          Dived instruction
     * @return The whole operand in String format
     */
    private static String interpretOperand(String operation,String[] instructionPieces,Instruction instructionObject) {
        String operand;
        int immValue;
        int rs = Integer.parseInt(instructionPieces[1],2);
        int rt = Integer.parseInt(instructionPieces[2],2);
        int rd = Integer.parseInt(instructionPieces[3],2);
        int sa = Integer.parseInt(instructionPieces[4],2);

        switch (operation) {
            case "ADD":case "ADDU":case "AND":case "OR":case "NOR":
            case "SLT":case "SLTU":case "SUB":case "SUBU":case "XOR":
               operand = "R"+ rd+", "
                       + "R"+ rs+", "
                       + "R"+ rt;
                instructionObject.setRs(rs);
                instructionObject.setRd(rd);
                instructionObject.setRt(rt);
                break;

            case "ADDI":case "ADDIU":case "SLTI":
                immValue = getSignedNum(instructionPieces);
                operand = "R"+ rt+", "
                        + "R"+ rs+", "
                        + "#"+ immValue;
                instructionObject.setRt(rt);
                instructionObject.setRs(rs);
                instructionObject.setImmValue(immValue);
                break;

            case "BEQ":case "BNE":
                immValue = getShiftedSignedNum(instructionPieces);
                operand = "R" + rs + ", "
                        + "R" + rt + ", "
                        + "#" + immValue;
                instructionObject.setImmValue(immValue);
                instructionObject.setRs(rs);
                instructionObject.setRt(rt);
                break;

            case "SW":case "LW":
                immValue = getSignedNum(instructionPieces);
                operand = "R" + rt + ", "
                        + immValue + "(" + "R" + rs + ")";
                instructionObject.setImmValue(immValue);
                instructionObject.setRt(rt);
                break;

            case "BREAK":case "NOP":
                operand = "";
                break;

            case "J":
                operand = "#";

                String offset = instructionPieces[1]+instructionPieces[2]+instructionPieces[3]+instructionPieces[4]+instructionPieces[5]+"00";
                int offsetNum = parseInt(offset,2);

                instructionObject.setImmValue(offsetNum);

                operand = operand+offsetNum;
                break;

            case "BGEZ":case "BGTZ":case "BLEZ":case "BLTZ":
                immValue = getShiftedSignedNum(instructionPieces);
                operand = "R" + rs + ", "
                        + "#" + immValue;
                instructionObject.setImmValue(immValue);
                instructionObject.setRs(rs);
                break;

            case "SLL":case "SRA":case "SRL":
                operand = "R" + rd + ", "
                        + "R" + rt + ", "
                        + sa;
                instructionObject.setRd(rd);
                instructionObject.setRt(rt);
                instructionObject.setSa(sa);
                break;

            default:
                operand = "The instruction doesn't exist";
                break;
        }

        return operand;
    }

    /**
     * This method is to calculate non-shift signed number
     * @param instructionPieces
     *          Divided instruction
     * @return  Signed int
     */
    private static int getSignedNum(String[] instructionPieces) {
        int num;
        String offset;
        if(!instructionPieces[3].substring(0,2).equals("11")) {
            offset = instructionPieces[3]+instructionPieces[4]+instructionPieces[5];
            num = parseInt(offset,2);
        } else {
            offset = "1111111111111111"+instructionPieces[3]+instructionPieces[4]+instructionPieces[5];
            num = (int)Long.parseLong(offset,2);
        }
        return num;
    }

    private static int getSignedData(String instruction) {
        int data;
        if(!instruction.substring(0,2).equals("11")) {
            data = Integer.parseInt(instruction);
        } else {
            data = (int)Long.parseLong(instruction);
        }
        return data;
    }

    /**
     * This method is to calculate shifted signed number
     * @param instructionPieces
     *          Divided instruction
     * @return Signed int
     */
    private static int getShiftedSignedNum(String[] instructionPieces) {
        int num;
        String offset;
        if(!instructionPieces[3].substring(0,2).equals("11")) {
            offset = instructionPieces[3]+instructionPieces[4]+instructionPieces[5]+"00";
            num = parseInt(offset,2);
        } else {
            offset = "11111111111111"+instructionPieces[3]+instructionPieces[4]+instructionPieces[5]+"00";
            num = (int)Long.parseLong(offset,2);
        }
        return num;
    }

    /**
     * This method is to format the instruction to required format
     * @param instructionAddress
     *          The address for instruction
     * @param instructionPieces
     *          Divided instruction
     * @param operation
     *          Name of operation
     * @param operand
     *          Whole operand
     * @return The whole assemble sentence with address
     */
    private static String format(int instructionAddress, String[] instructionPieces, String operation, String operand) {
        String instruction = "";

        // Write binary part to instruction string
        for(int i=0; i<instructionPieces.length; i++) {
            instruction = instruction + instructionPieces[i];
            if(needSpace) {
                instruction = instruction + SPACE;
            }
            if(i == instructionPieces.length-1 && !needSpace) {
                instruction = instruction + SPACE;
            }
        }

        // Write address
        instruction = instruction + Integer.toString(instructionAddress) + SPACE;

        // Write operation
        if(operation.equals("")) {
            instruction = instruction + operand;
            return instruction;
        }
        instruction = instruction + operation;
        if(!operation.equals("BREAK") && !operation.equals("NOP")) {
            instruction = instruction + SPACE + operand;
        }
        return instruction;
    }
}