import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        MaximumMatching maximumMatching = new MaximumMatching();
        Scanner scanner = new Scanner(System.in);
        System.out.print("A gráfot tartalmazó fájl neve: ");
        String fileName = scanner.nextLine();
        maximumMatching.readGraph(fileName);
        maximumMatching.createModel(true);

        int[][] edges = maximumMatching.getEdges();
        List<Integer> matching = maximumMatching.getMaximumMatching();
        System.out.println("A maximális párosítás mérete: " + matching.size());
        for (int e : matching) {
            System.out.println(Arrays.toString(edges[e]));
        }
    }
}
