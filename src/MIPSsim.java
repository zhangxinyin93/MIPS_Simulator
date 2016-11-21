/**
 * Where main function resides
 */
public class MIPSsim {
    public static void main(String[] args) {
        Simulator simulator = new Simulator();
        simulator.simulate("fibonacci_bin.bin","output","");
    }
}
