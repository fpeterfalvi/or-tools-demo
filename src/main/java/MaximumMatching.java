import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaximumMatching {
    private int M;
    private int N;
    private int E;
    private int[][] edges;
    private List<Integer> maximumMatching;

    public MaximumMatching() {
        loadORTools();
    }

    public void loadORTools() {
        for (String directory : System.getProperty("java.library.path").split(";")) {
            File file = new File(directory, "jniortools.dll");
            if (file.exists()) {
                System.load(file.getPath());
                return;
            }
        }
        Loader.loadNativeLibraries();
    }

    public void readGraph(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String[] parameters = reader.readLine().split(" ");
        M = Integer.parseInt(parameters[0]);
        N = Integer.parseInt(parameters[1]);
        E = Integer.parseInt(parameters[2]);
        edges = new int[E][2];
        for (int e = 0; e < E; e++) {
            String[] ends = reader.readLine().split(" ");
            edges[e][0] = Integer.parseInt(ends[0]);
            edges[e][1] = Integer.parseInt(ends[1]);
        }
        reader.close();
    }

    public boolean createModel(boolean solve, String modelFileName) {
        String solverName = "SCIP";
        //String solverName = "CBC";
        MPSolver solver = MPSolver.createSolver(solverName);
        if (solver == null) {
            System.out.println("Could not create solver " + solverName);
            return false;
        }

        MPVariable[] variables = new MPVariable[E];
        for (int e = 0; e < E; e++) {
            variables[e] = solver.makeIntVar(0.0, 1.0,  "e" + e);
        }

        for (int i = 0; i < M; i++) {
            MPConstraint constraint = solver.makeConstraint(0.0, 1.0, "cu" + i);
            for (int e = 0; e < E; e++) {
                if (edges[e][0] == i) {
                    constraint.setCoefficient(variables[e], 1);
                }
            }
        }
        for (int j = 0; j < N; j++) {
            MPConstraint constraint = solver.makeConstraint(0.0, 1.0, "cv" + j);
            for (int e = 0; e < E; e++) {
                if (edges[e][1] == j) {
                    constraint.setCoefficient(variables[e], 1);
                }
            }
        }

        MPObjective objective = solver.objective();
        for (int e = 0; e < E; e++) {
            objective.setCoefficient(variables[e], 1);
        }
        objective.setMaximization();

        if (modelFileName != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(modelFileName));
                writer.write(solver.exportModelAsLpFormat());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (solve) {
            solver.setTimeLimit(10000L);
            final MPSolver.ResultStatus resultStatus = solver.solve();

            if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
                System.out.println("Solution:");
                System.out.println("Objective value = " + solver.objective().value());
                System.out.println("\nAdvanced usage:");
                System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
                System.out.println("Problem solved in " + solver.iterations() + " iterations");
                System.out.println("Problem solved in " + solver.nodes() + " branch-and-bound nodes");
                System.out.println();

                maximumMatching = new ArrayList<>();
                for (int e = 0; e < E; e++) {
                    if (variables[e].solutionValue() == 1) {
                        maximumMatching.add(e);
                    }
                }
                return true;
            } else {
                System.err.println("The problem does not have an optimal solution!");
                return false;
            }
        }
        return false;
    }

    public void solveWithXpress() {
        createModel(false, "demo.lp");
        String command = "LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/xpressmp/lib/ && export LD_LIBRARY_PATH && cd /opt/xpressmp/bin && ./optimizer @/home/ferenc.peterfalvi/demoscript.txt";
        String xpressLog = XpressUtil.makeXpressSSHCommunication(command, "demo.lp", "demo.slx");
        Map<String, Integer> xpressVariables = XpressUtil.loadVariablesFromSlx("demo.slx");
        System.out.println(xpressVariables);
        if (xpressVariables.isEmpty()) {
            System.out.println("Nincs megengedett megoldás!");
        } else {
            maximumMatching = new ArrayList<>();
            for (int e = 0; e < E; e++) {
                int val = xpressVariables.get("e" + e);
                if (val == 1) {
                    maximumMatching.add(e);
                }
            }
        }
    }

    public int[][] getEdges() {
        return edges;
    }

    public List<Integer> getMaximumMatching() {
        return maximumMatching;
    }
}
