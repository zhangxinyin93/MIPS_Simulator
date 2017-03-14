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
    private Map<Integer,Integer> dataSegment;

    private int reorderBufferId = 1;
    private int cycle = 0;
    private int pc = 600;
    private boolean isFinish = false;
    private boolean isFlushed = false;

    public Simulator() {
        this.instructionQueue = new LinkedList<>();
        this.reservationStation = new ReservationStation();
        this.reorderBuffer = new ReorderBuffer();
        this.registerStatus = new RegisterStatus();
        this.btb = new BTB();
        this.branchInstructions = new HashSet<>();
        this.allInstructions = new HashMap<>();
        this.dataSegment = new HashMap<>();

        branchInstructions.add("J");
        branchInstructions.add("BEQ");
        branchInstructions.add("BNE");
        branchInstructions.add("BGEZ");
        branchInstructions.add("BGTZ");
        branchInstructions.add("BLEZ");
        branchInstructions.add("BLTZ");
    }

    public void simulate(String inputFileName, String outputFileName, String requiredCycle) throws Exception {
        WriteUtil writeUtil = new WriteUtil(outputFileName);
        allInstructions = DisassembleUtil.disassemble(inputFileName, 1, dataSegment);

        // Handle the input arguments
        if(requiredCycle == "") {
            while (!isFinish) {
                pipeline();
                writeUtil.write(cycle, instructionQueue, reservationStation,
                        reorderBuffer, btb, registerStatus, dataSegment);
            }
            writeUtil.close();
            return;
        }

        requiredCycle = requiredCycle.replace("-T","");
        String[] cycles = requiredCycle.split(":");
        int start = Integer.parseInt(cycles[0]);
        int end = Integer.parseInt(cycles[1]);

        if(start == 0 && end == 0) {
            while (!isFinish) {
                pipeline();
                writeUtil.write(cycle, instructionQueue, reservationStation,
                        reorderBuffer, btb, registerStatus, dataSegment);
            }
        } else if(start == 0) {
            while (cycle <= end && !isFinish) {
                pipeline();
                writeUtil.write(cycle, instructionQueue, reservationStation,
                        reorderBuffer, btb, registerStatus, dataSegment);
            }
        } else if(start > 0) {
            while (cycle <= start - 1) {
                pipeline();
            }
            while (cycle <= end && !isFinish) {
                pipeline();
                writeUtil.write(cycle, instructionQueue, reservationStation,
                        reorderBuffer, btb, registerStatus, dataSegment);
            }
        } else {
            throw new IllegalArgumentException();
        }
        writeUtil.close();
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

        // If just flushed this cycle because of wrong predict
        // We just can fetch from next cycle
        if(isFlushed) {
            isFlushed = false;
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
        if(!btb.containsInstruction(instruction.getAddress())) {
            instruction.setPredictor(0);
            pc = pc + 4;
            return;
        }

        // When btb contains this instruction, then based on the predictor to update pc
        pc = btb.getTargetAddress(instruction.getAddress());
        instruction.setPredictor(btb.getPrediction(instruction.getAddress()));
    }

    private void Issue() {
        if (instructionQueue.isEmpty()) {
            return;
        }

        if(reservationStation.isFull() || reorderBuffer.isFull()) {
            return;
        }

        if(reorderBuffer.isReclaim()) {
            reorderBuffer.setReclaim(false);
            return;
        }

        Instruction instruction = instructionQueue.poll();
        issue(instruction);
    }

    private void issue(Instruction instruction) {
        int currentReorderBufferId = reorderBufferId;
        String operation = instruction.getOperation();
        ReorderBufferEntry robEntry = new ReorderBufferEntry(currentReorderBufferId, instruction);
        // This is for next reorder buffer id
        reorderBufferId++;

        // NOP and BREAK are ready to commit after issued
        if (operation.equals("NOP") || operation.equals("BREAK")) {
            reorderBuffer.add(currentReorderBufferId,robEntry);
            robEntry.setReady(true);
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
        reservationStation.add(rsEntry);
        reorderBuffer.add(currentReorderBufferId,robEntry);
        // The calculated result will store in this reorder buffer entry
        rsEntry.setDestination(currentReorderBufferId);

        if (!instruction.getOperation().equals("J") && !instruction.getOperation().equals("SLL") &&
                !instruction.getOperation().equals("SRL") && !instruction.getOperation().equals("SRA")) {
            if(registerStatus.getRegister(rs).isBusy()) {
                // Get the location of reorder buffer entry
                int reorderBufferLocation = registerStatus.getRegister(rs).getReorderBufferNum();

                if (reorderBuffer.getROBEntry(reorderBufferLocation).isReady()) {
                    rsEntry.setVj(reorderBuffer.getROBEntry(reorderBufferLocation).getValue());
                    rsEntry.setQj(0);
                } else {
                    rsEntry.setQj(reorderBufferLocation);
                }
            } else {
                rsEntry.setVj(registerStatus.getRegister(rs).getValue());
                rsEntry.setQj(0);
            }
        }

        if (instruction.getOperation().equals("SLL") || instruction.getOperation().equals("SRL") || instruction.getOperation().equals("SRA")) {
            if(registerStatus.getRegister(rt).isBusy()) {
                int reorderBufferLocation = registerStatus.getRegister(rt).getReorderBufferNum();
                if(reorderBuffer.getROBEntry(reorderBufferLocation).isReady()) {
                    rsEntry.setVk(reorderBuffer.getROBEntry(reorderBufferLocation).getValue());
                    rsEntry.setQk(0);
                } else {
                    rsEntry.setQk(reorderBufferLocation);
                }
            } else {
                rsEntry.setVk(registerStatus.getRegister(rt).getValue());
                rsEntry.setQk(0);
            }
        }

        switch (instruction.getOperation()) {
            case "ADDI":case "ADDIU":case "SLTI":case "LW":
                robEntry.setDestination(registerStatus.getRegister(rt));
                // 最后存放结果的寄存器要把结果写回到当前这条instruction所在的reorder buffer entry
                registerStatus.getRegister(rt).setReorderBufferNum(currentReorderBufferId);
                registerStatus.getRegister(rt).setBusy(true);
                rsEntry.setImmediateValue(instruction.getImmValue());
                break;

            case "SLL":case "SRL":case "SRA":
                robEntry.setDestination(registerStatus.getRegister(rd));
                registerStatus.getRegister(rd).setBusy(true);
                registerStatus.getRegister(rd).setReorderBufferNum(currentReorderBufferId);
                rsEntry.setImmediateValue(sa);
                break;

            case "SW":case "BEQ":case "BNE":
                if(registerStatus.getRegister(rt).isBusy()) {
                    int reorderBufferLocation = registerStatus.getRegister(rt).getReorderBufferNum();
                    if(reorderBuffer.getROBEntry(reorderBufferLocation).isReady()) {
                        rsEntry.setVk(reorderBuffer.getROBEntry(reorderBufferLocation).getValue());
                        rsEntry.setQk(0);
                    } else {
                        rsEntry.setQk(reorderBufferLocation);
                    }
                } else {
                    rsEntry.setVk(registerStatus.getRegister(rt).getValue());
                    rsEntry.setQk(0);
                }
                rsEntry.setImmediateValue(instruction.getImmValue());
                break;

            // Only rs and immValue
            // Jump doesn't have rs
            case "J":case "BGEZ":case "BGTZ":case "BLEZ":case "BLTZ":
                rsEntry.setImmediateValue(instruction.getImmValue());
                break;

            default:
                if (registerStatus.getRegister(rt).isBusy()) {
                    int reorderBufferLocation = registerStatus.getRegister(rt).getReorderBufferNum();
                    if(reorderBuffer.getROBEntry(reorderBufferLocation).isReady()) {
                        rsEntry.setVk(reorderBuffer.getROBEntry(reorderBufferLocation).getValue());
                        rsEntry.setQk(0);
                    } else {
                        rsEntry.setQk(reorderBufferLocation);
                    }
                } else {
                    rsEntry.setVk(registerStatus.getRegister(rt).getValue());
                    rsEntry.setQk(0);
                }

                robEntry.setDestination(registerStatus.getRegister(rd));
                registerStatus.getRegister(rd).setReorderBufferNum(robEntry.getBufferId());
                registerStatus.getRegister(rd).setBusy(true);
                break;
        }
    }

    private void Execute() {
        for(ReservationStationEntry entry : reservationStation.getReservationQueue()) {
            if(entry.isBusy()) {
                execute(entry);
            }
        }
    }

    private void execute(ReservationStationEntry entry) {
        Instruction instruction = entry.getInstruction();
        String operation = instruction.getOperation();

        if(instruction.needExecuteJNextCycle()) {
            instruction.setExecuteJNextCycle(false);
            return;
        }

        if(instruction.needExecuteKNextCycle()) {
            instruction.setExecuteKNextCycle(false);
            return;
        }

        int branchOutcome;
        int targetAddress;

        switch (operation) {
            case "BEQ":
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    branchOutcome = (entry.getVj() == entry.getVk()) ? 1 : 0;

                    // Branch instruction enters btb
                    if (!btb.containsInstruction(instruction.getAddress())) {
                        targetAddress = instruction.getAddress() + 4 + instruction.getImmValue();
                        btb.put(new BTBEntry(instruction.getAddress(), targetAddress, branchOutcome));
                    }

                    if (instruction.getPredictor() != branchOutcome) {
                        instruction.isWrongPredicted = true;
                    } else {
                        instruction.isWrongPredicted = false;
                    }
                    // Update predictor
                    btb.getEntry(instruction.getAddress()).setPredictor(branchOutcome);

                    // Ready to commit
                    entry.setBusy(false);
                    reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
                }
                break;

            case "BNE":
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    branchOutcome = (entry.getVj() == entry.getVk()) ? 0 : 1;

                    // Branch instruction enters btb
                    if (!btb.containsInstruction(instruction.getAddress())) {
                        targetAddress = instruction.getAddress() + 4 + instruction.getImmValue();
                        btb.put(new BTBEntry(instruction.getAddress(), targetAddress, branchOutcome));
                    }

                    if (instruction.getPredictor() != branchOutcome) {
                        instruction.isWrongPredicted = true;
                    } else {
                        instruction.isWrongPredicted = false;
                    }
                    // Update predictor
                    btb.getEntry(instruction.getAddress()).setPredictor(branchOutcome);

                    // Ready to commit
                    entry.setBusy(false);
                    reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
                }
                break;

            // Enter BTB in commit stage
            case "J":
                if(instruction.getPredictor() != 1) {
                    instruction.isWrongPredicted = true;
                } else {
                    instruction.isWrongPredicted = false;
                }
                //btb.getEntry(instruction.getAddress()).setPredictor(1);
                entry.setBusy(false);
                reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
                break;

            case "BGEZ":
                if (entry.getQj() == 0) {
                    branchOutcome = (entry.getVj() >= 0) ? 1 : 0;

                    if (!btb.containsInstruction(instruction.getAddress())) {
                        targetAddress = instruction.getAddress() + 4 + instruction.getImmValue();
                        btb.put(new BTBEntry(instruction.getAddress(), targetAddress, branchOutcome));
                    }

                    if (instruction.getPredictor() != branchOutcome) {
                        instruction.isWrongPredicted = true;
                    } else {
                        instruction.isWrongPredicted = false;
                    }
                    // Update predictor
                    btb.getEntry(instruction.getAddress()).setPredictor(branchOutcome);

                    // Ready to commit
                    entry.setBusy(false);
                    reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
                }
                break;

            case "BLTZ":
                if (entry.getQj() == 0) {
                    branchOutcome = (entry.getVj() < 0) ? 1 : 0;
                    if (!btb.containsInstruction(instruction.getAddress())) {
                        targetAddress = instruction.getAddress() + 4 + instruction.getImmValue();
                        btb.put(new BTBEntry(instruction.getAddress(), targetAddress, branchOutcome));
                    }

                    if (instruction.getPredictor() != branchOutcome) {
                        instruction.isWrongPredicted = true;
                    } else {
                        instruction.isWrongPredicted = false;
                    }
                    // Update predictor
                    btb.getEntry(instruction.getAddress()).setPredictor(branchOutcome);

                    // Ready to commit
                    entry.setBusy(false);
                    reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
                }
                break;

            case "BLEZ":
                if (entry.getQj() == 0) {
                    branchOutcome = (entry.getVj() <= 0) ? 1 : 0;

                    if (!btb.containsInstruction(instruction.getAddress())) {
                        targetAddress = instruction.getAddress() + 4 + instruction.getImmValue();
                        btb.put(new BTBEntry(instruction.getAddress(), targetAddress, branchOutcome));
                    }

                    if (instruction.getPredictor() != branchOutcome) {
                        instruction.isWrongPredicted = true;
                    } else {
                        instruction.isWrongPredicted = false;
                    }
                    // Update predictor
                    btb.getEntry(instruction.getAddress()).setPredictor(branchOutcome);

                    // Ready to commit
                    entry.setBusy(false);
                    reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
                }
                break;

            case "BGTZ":
                if (entry.getQj() == 0) {
                    branchOutcome = (entry.getVj() > 0) ? 1 : 0;

                    if (!btb.containsInstruction(instruction.getAddress())) {
                        targetAddress = instruction.getAddress() + 4 + instruction.getImmValue();
                        btb.put(new BTBEntry(instruction.getAddress(), targetAddress, branchOutcome));
                    }

                    if (instruction.getPredictor() != branchOutcome) {
                        instruction.isWrongPredicted = true;
                    } else {
                        instruction.isWrongPredicted = false;
                    }
                    // Update predictor
                    btb.getEntry(instruction.getAddress()).setPredictor(branchOutcome);

                    // Ready to commit
                    entry.setBusy(false);
                    reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
                }
                break;

            case "ADDI":case "ADDIU":
                if(entry.getQj() == 0) {
                    entry.setImmediateValue(entry.getVj() + entry.getImmidateValue());
                    entry.setBusy(false);
                }
                break;

            case "ADD":case "ADDU":
                //System.out.println(entry.getInstruction().getAddress() + " " + cycle);
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    entry.setImmediateValue(entry.getVj() + entry.getVk());
                    entry.setBusy(false);
                }
                break;

            case "SUB":case "SUBU":
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    entry.setImmediateValue(entry.getVj() - entry.getVk());
                    entry.setBusy(false);
                }
                break;

            case "SLT":case "SLTU":
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    if (entry.getVj() < entry.getVk()) {
                        entry.setImmediateValue(1);
                    } else {
                        entry.setImmediateValue(0);
                    }
                    entry.setBusy(false);
                }
                break;

            case "AND":
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    entry.setImmediateValue(entry.getVj() & entry.getVk());
                    entry.setBusy(false);
                }
                break;

            case "OR":
                if(entry.getQj() == 0 && entry.getQk() == 0 ) {
                    entry.setImmediateValue(entry.getVj() | entry.getVk());
                    entry.setBusy(false);
                }
                break;

            case "XOR":
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    entry.setImmediateValue(entry.getVj() ^ entry.getVk());
                    entry.setBusy(false);
                }
                break;

            case "NOR":
                if(entry.getQj() == 0 && entry.getQk() == 0) {
                    entry.setImmediateValue(~(entry.getVj() | entry.getVk()));
                    entry.setBusy(false);
                }
                break;

            case "SLL":
                if(entry.getQk() == 0) {
                    entry.setImmediateValue(entry.getVk() << entry.getImmidateValue());
                    entry.setBusy(false);
                }
                break;

            case "SRL":
                if(entry.getQk() == 0) {
                    entry.setImmediateValue(entry.getVk() >>> entry.getImmidateValue());
                    entry.setBusy(false);
                }
                break;

            case "SRA":
                if(entry.getQk() == 0) {
                    entry.setImmediateValue(entry.getVk() >>> entry.getImmidateValue());
                    entry.setBusy(false);
                }
                break;

            case "LW":
                if(entry.getQj() == 0 && !existLoadStoreDependency(instruction)) {
                    entry.setImmediateValue(entry.getImmidateValue() + entry.getVj());
                    entry.setBusy(false);
                }
                break;

            case "SW":
                if(entry.getQj() == 0 && !existLoadStoreDependency(instruction)) {
                    entry.setBusy(false);
                    int memoryAddress = entry.getVj() + entry.getImmidateValue();
                    entry.setImmediateValue(memoryAddress);
                    reorderBuffer.getROBEntry(entry.getDestination()).setMemoryAddress(memoryAddress);
                }
                break;
        }
    }

    private void WriteBack() {
        for(ReservationStationEntry rsEntry : reservationStation.getReservationQueue()) {
            String operation = rsEntry.getInstruction().getOperation();
            // Bypass Store and branch instruction, NOP and BREAK
            if(branchInstructions.contains(operation) ||
                    operation.equals("NOP") || operation.equals("BREAK")) {
                continue;
            }
            if(!rsEntry.isBusy() && !rsEntry.hasWrittenBack()) {
                writeBack(rsEntry);
            }
        }
    }

    private void writeBack(ReservationStationEntry entry) {
        Instruction instruction = entry.getInstruction();
        String operation = instruction.getOperation();

        if(operation.equals("LW") && existEarlyStoreInROB(entry)) {
            return;
        }

         // For Load instruction, first cycle to access memory
        if(operation.equals("LW") && !instruction.finishedFirstCycle()) {
            entry.setImmediateValue(dataSegment.get(entry.getImmidateValue()));
            instruction.setFirstCycle(true);
            return;
        }

        if(operation.equals("LW")) {
            instruction.setFirstCycle(false);
        }

        if(operation.equals("SW") && entry.getQk() != 0) {
            return;
        }

        if (operation.equals("SW")) {
            reorderBuffer.getROBEntry(entry.getDestination()).setValue(entry.getVk());
            reorderBuffer.getROBEntry(entry.getDestination()).setReady(true);
            entry.setWrittenBack(true);
            return;
        }

            int destinationInROB = entry.getDestination();
            // write to ROB
            reorderBuffer.getROBEntry(destinationInROB).setValue(entry.getImmidateValue());
            reorderBuffer.getROBEntry(destinationInROB).setReady(true);

            // Broadcast on CDB
            for (ReservationStationEntry rsEntry : reservationStation.getReservationQueue()) {
                int count = 0;
                if (rsEntry.getQj() == destinationInROB) {
                    rsEntry.setVj(entry.getImmidateValue());
                    rsEntry.setQj(0);
                    rsEntry.getInstruction().setExecuteJNextCycle(true);
                    count++;
                }
                if (rsEntry.getQk() == destinationInROB) {
                    rsEntry.setVk(entry.getImmidateValue());
                    rsEntry.setQk(0);
                    rsEntry.getInstruction().setExecuteKNextCycle(true);
                    count++;
                }
                if(count == 2) {
                    instruction.writtenInSameCycle = true;
                }
            }

            entry.setWrittenBack(true);
    }

    // If mispredict, then remove all
    private void Commit() {
        if(reorderBuffer.isFull()) {
            reorderBuffer.setReclaim(true);
        }

        ReorderBufferEntry robEntry = reorderBuffer.getReorderBufferQueue().peek();
        if(robEntry == null || !robEntry.isReady()) {
            return;
        }

        // clear the entry for rob and rs
        robEntry = reorderBuffer.poll();
        reservationStation.poll();

        Instruction instruction = robEntry.getInstruction();

        if (instruction.getOperation().equals("J") && !btb.containsInstruction(instruction.getAddress())) {
            btb.put(new BTBEntry(instruction.getAddress(), instruction.getImmValue(), 1));
        }

        if(instruction.isWrongPredicted) {
            removeAll();
            if(instruction.getPredictor() == 0) {
                pc = btb.getTargetAddress(instruction.getAddress());
            }
            if(instruction.getPredictor() == 1) {
                pc = instruction.getAddress() + 4;
            }
        }


        switch (instruction.getOperation()) {
            case "SW":
                dataSegment.replace(robEntry.getMemoryAddress(),robEntry.getValue());
                break;

            case "BREAK":
                isFinish = true;
                break;

            case "J":case "BGEZ":case "BGTZ":case "BLEZ":case "BLTZ":case "BNE":case "BEQ":case "NOP":
                break;

            default:
                robEntry.getDestination().setValue(robEntry.getValue());
                robEntry.getDestination().setBusy(false);
                break;
        }

        robEntry = reorderBuffer.getReorderBufferQueue().peek();
        if(robEntry != null && robEntry.isReady() && robEntry.isBusy()) {
            robEntry.setBusy(false);
        }

    }

    private boolean existLoadStoreDependency(Instruction instruction) {
        for(ReservationStationEntry rsEntry : reservationStation.getReservationQueue()) {
            if(rsEntry.getInstruction().equals(instruction)) {
                break;
            }
            if(rsEntry.getInstruction().getOperation().equals("LW") ||
                    rsEntry.getInstruction().getOperation().equals("SW")) {
                if(rsEntry.isBusy()) {
                    return true;
                }
            }
        }
        return false;
    }

    // Check if there is early store instruction with same memory address in ROB
    private boolean existEarlyStoreInROB(ReservationStationEntry entry) {
        for(ReservationStationEntry rsEntry : reservationStation.getReservationQueue()) {
            if(rsEntry.equals(entry)) {
                break;
            }
            if(rsEntry.getInstruction().getOperation().equals("SW") && !rsEntry.isBusy() && rsEntry.getImmidateValue() == entry.getImmidateValue()) {
                return true;
            }
        }
        return false;
    }


    // Remove all instruction behind this branch or Jump instruction
    private void removeAll() {
        instructionQueue.clear();
        reservationStation = new ReservationStation();
        reorderBuffer = new ReorderBuffer();
        isFlushed = true;
    }
}