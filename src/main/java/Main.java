import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        MaximumMatching maximumMatching = new MaximumMatching();
        Scanner scanner = new Scanner(System.in);
        System.out.print("A gráfot tartalmazó fájl neve: ");
        String fileName = scanner.nextLine();
        maximumMatching.readGraph(fileName);
        System.out.println("SCIP vagy Xpress? (S/X) ");
        String c = "0";
        while (!c.equals("s") && !c.equals("S") && !c.equals("x") && !c.equals("X")) {
            c = scanner.nextLine();
        }
        if (c.equals("s") || c.equals("S")) {
            maximumMatching.createModel(true, null);
        } else {
            maximumMatching.solveWithXpress();
        }
        int[][] edges = maximumMatching.getEdges();
        List<Integer> matching = maximumMatching.getMaximumMatching();
        System.out.println("A maximális párosítás mérete: " + matching.size());
        for (int e : matching) {
            System.out.println(Arrays.toString(edges[e]));
        }
    }
}
