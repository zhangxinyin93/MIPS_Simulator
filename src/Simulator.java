import java.util.*;

/**
 * Tomasulo Algorithm simulator
 */
public class Simulator {
    private Queue<Instruction> instructionQueue;
    private ReservationStation reservationStation;
    private ReorderBuffer reorderBuffer;
    private RegisterStatus registerStatus;
    private BTB btb;
    private Set<String> branchInstructions;
    private Map<Integer,Instruction> allInstructions;
    private int reorderBufferId;

    private int cycle = 0;
    private int pc = 600;
    private boolean isFinish = false;

    public Simulator() {
        this.instructionQueue = new LinkedList<>();
        this.reservationStation = new ReservationStation();
        this.reorderBuffer = new ReorderBuffer();
        this.registerStatus = new RegisterStatus();
        this.btb = new BTB();
        this.branchInstructions = new HashSet<>();
        this.allInstructions = new HashMap<>();

        branchInstructions.add("J");
        branchInstructions.add("BEQ");
        branchInstructions.add("BNE");
        branchInstructions.add("BGEZ");
        branchInstructions.add("BGTZ");
        branchInstructions.add("BLEZ");
        branchInstructions.add("BLTZ");
    }

    public void simulate(String inputFileName, String outputFileName, String requiredCycle) {
        allInstructions = DisassembleUtil.disassemble(inputFileName, 1);
        for(int addr: allInstructions.keySet()) {
            Instruction i = allInstructions.get(addr);
            System.out.println(i.getOperation()+" "+i.getAddress()+" " +i.getRd()+" "+i.getRs()+" "+i.getRt()+" "+i.getSa()+" "+i.getImmValue());
        }

        // Handle the input arguments

        if(requiredCycle == "") {
            while (!isFinish) {
                pipeline();
                // TODO: write to file
            }
            return;
        }

        requiredCycle = requiredCycle.replace("-T","");
        String[] cycles = requiredCycle.split(":");
        int start = Integer.parseInt(cycles[0]);
        int end = Integer.parseInt(cycles[1]);

        if(start == 0 && end == 0) {
            while (!isFinish) {
                pipeline();
                // TODO: write to file
            }
        } else if(start == 0) {
            while (cycle <= end && !isFinish) {
                pipeline();
                // TODO: write to file
            }
        } else if(start > 0) {
            while (cycle <= start - 1) {
                pipeline();
            }
            while (cycle <= end && !isFinish) {
                pipeline();
                // TODO: write to file
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void pipeline() {
        Commit();
        WriteBack();
        Execute();
        Issue();
        IF();
        cycle++;
    }

    private void IF() {
        Instruction instruction = allInstructions.get(pc);
        // After BREAK, instructionQueue will be empty when BREAK issued
        if(instruction == null) {
            return;
        }

        String operation = instruction.getOperation();
        instructionQueue.add(instruction);

        // Check for branch instruction
        // PC == instruction.getAddress()
        if (!branchInstructions.contains(operation)) {
            pc = pc + 4;
            return;
        }

        // When this branch is not in btb, it will enter btb in execute stage
        // And this will be treated as not taken
        if(!btb.containsInstruction(pc)) {
            instruction.setPredictor(0);
            pc = pc + 4;
            return;
        }

        // When btb contains this instruction, then based on the predictor to update pc
        pc = btb.getTargetAddress(pc);
        instruction.setPredictor(btb.getPrediction(pc));
    }

    private void Issue() {
        if (instructionQueue.isEmpty()) {
            return;
        }

        Instruction instruction = instructionQueue.poll();
        if(instruction.getOperation().equals("NOP") || instruction.getOperation().equals("BREAK") && !reorderBuffer.isFull()) {
            issue(instruction);
        }
        if (!reservationStation.isFull() && !reorderBuffer.isFull()) {
            issue(instruction);
        }
    }

    private void issue(Instruction instruction) {
        
        String operation = instruction.getOperation();

        if (operation.equals("NOP") || operation.equals("BREAK")) {
            reorderBuffer.add(reorderBufferId, new ReorderBufferEntry(reorderBufferId, instruction));
            reorderBuffer.getROBEntry(reorderBufferId).setReady(true);
            reorderBufferId++;
            return;
        }

        // For all the other instructions

        // destination
        int rd = instruction.getRd();
        // source
        int rs = instruction.getRs();
        // target
        int rt = instruction.getRt();
        int sa = instruction.getSa();
        ReservationStationEntry  rsEntry = new ReservationStationEntry(instruction);
        ReorderBufferEntry robEntry = new ReorderBufferEntry(reorderBufferId, instruction);
        reservationStation.add(rsEntry);
    }

    private void Execute() {

    }

    private void WriteBack() {

    }

    private void Commit() {

    }
}