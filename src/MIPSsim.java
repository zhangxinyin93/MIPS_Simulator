/**
 * Where main function resides
 */
public class MIPSsim {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Invalid input");
            System.out.println("First should be input filename");
            System.out.println("Second should be output filename");
            System.out.println("Third is optional, should be in the format as [-Tm:n], m->start cycle, n->end cycle");
        }

        String inputFileName = args[0];
        String outputFileName = args[1];
        String cycle = "";

        if (args.length == 3) {
            cycle = args[2];
            cycle = cycle.replace("[","");
            cycle = cycle.replace("]","");
        }
        Simulator simulator = new Simulator();
        simulator.simulate(inputFileName, outputFileName, cycle);
    }
}
