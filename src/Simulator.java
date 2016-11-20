import java.util.LinkedList;
import java.util.Queue;

/**
 * Tomasulo Algorithm simulator
 */
public class Simulator {
    private Queue<Instruction> instructionQueue;
    private ReservationStation reservationStation;
    private ReorderBuffer reorderBuffer;
    private RegisterStatus registerStatus;
    private BTB btb;

    private int cycle = 0;
    private int btbId = 1;
    private int reorderBufferId = 1;

    public Simulator() {
        this.instructionQueue = new LinkedList<>();
        this.reservationStation = new ReservationStation();
        this.reorderBuffer = new ReorderBuffer();
        this.registerStatus = new RegisterStatus();
        this.btb = new BTB();
    }

    public void simulator(String inputFileName, String outputFileName, String requiredCycle) {

    }
}