package py.una.pol.service;


import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.problem.AbstractProblem;
import py.una.pol.dto.NFVdto.Traffic;
import py.una.pol.dto.SolutionTraffic;
import py.una.pol.util.Configurations;
import java.util.List;


public class ProblemService extends AbstractProblem {

        private List<Traffic> traffics;
    /**
         * Constructs a new instance of the DTLZ2 function, defining it
         * to include 11 decision variables and 2 objectives.
         */
        public ProblemService() throws Exception {
            super(1, 12);
            TrafficService trafficService = new TrafficService();
            traffics = trafficService.readTraffics();
        }

        /**
         * Constructs a new solution and defines the bounds of the decision
         * variables.
         */
        @Override
        public Solution newSolution() {
            Solution solution = new Solution(getNumberOfVariables(),
                    getNumberOfObjectives());

            Permutation permutation = new Permutation(Configurations.numberTraffic);
            for (int i = 0; i < getNumberOfVariables(); i++) {
               // permutation.randomize();
                solution.setVariable(i, permutation);
            }


            return solution;
        }

        /**
         * Extracts the decision variables from the solution, evaluates the
         * Rosenbrock function, and saves the resulting objective value back to
         * the solution.
         */
        @Override
        public void evaluate(Solution solution) {
            VnfService vnfService = new VnfService();
            double[] f = new double[numberOfObjectives];
            Permutation permutation = (Permutation) solution.getVariable(0);
            SolutionTraffic solutions = vnfService.placement(traffics, permutation);

            f[0] = solutions.getBandwidth();
            f[1] = solutions.getEnergyCost();
            f[2] = solutions.getDelayCost();
            f[3] = solutions.getDistance();
            f[4] = solutions.getFragmentation();
            f[5] = solutions.getLicencesCost();
            f[6] = solutions.getLoadTraffic();
            f[7] = solutions.getMaxUseLink();
            f[8] = solutions.getNumberInstances();
            f[9] = solutions.getResourcesCost();
            f[10] = solutions.getSloCost();
            f[11] = solutions.getThroughput();

            solution.setObjectives(f);
    // TODO crear otra clase solucionPlacement para la ubicacion de los vnfs, que extienda de solution

        }

}
