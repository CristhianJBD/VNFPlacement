package py.una.pol.service;

import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.variable.Permutation;
import py.una.pol.dto.NFVdto.Traffic;
import py.una.pol.dto.ResultGraphMap;
import py.una.pol.util.Configurations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MOEAService {
    Logger logger = Logger.getLogger(MOEAService.class);
    public void nsgaIII() {
        //VnfService vnfService = new VnfService();
        //List<ResultGraphMap> resultGraphMaps = new ArrayList<>();
        TrafficService trafficService = new TrafficService();

        try {
            Configurations.loadProperties();
            DataService.loadData();
            List<Traffic> traffics = trafficService.readTraffics();

            String[] algorithms = { "NSGAIII", "MOEAD", "RVEA"};

            //setup the experiment
            Executor executor = new Executor()
                    .withProblemClass(ProblemService.class)
                    .withMaxTime(100)
                    .distributeOnAllCores();
            //  .withMaxEvaluations(1000);

            Analyzer analyzer = new Analyzer()
                    .withProblemClass(ProblemService.class)
                    .includeHypervolume()
                    .showStatisticalSignificance();

            //run each algorithm for seeds
            for (String algorithm : algorithms)
                analyzer.addAll(algorithm, executor.withAlgorithm(algorithm).runSeeds(30));

            //print the results
            analyzer.printAnalysis();

/*
            NondominatedPopulation result = new Executor()
                    .withProblemClass(ProblemService.class)
                    .withAlgorithm("NSGAIII")
                    .distributeOnAllCores()
                    .withMaxTime(10)
                    .run();
            //display the results
            System.out.format("Nro.     Bandwidth       Energy          Delay           Distance        " +
                    "Fragmentation       Licence        LoadTrafic      MaxUseLink      NumberIntances" +
                    "    Resources     SLO        Throughput%n");

            int i = 1;
            for (Solution solution : result) {
                System.out.format(i++ + "       %.4f        %.4f        %.4f        %.4f        %.4f" +
                                "       %.4f        %.4f        %.4f        %.4f        %.4f" +
                                "       %.4f        %.4f%n",
                        solution.getObjective(0),
                        solution.getObjective(1),
                        solution.getObjective(2),
                        solution.getObjective(3),
                        solution.getObjective(4),
                        solution.getObjective(5),
                        solution.getObjective(6),
                        solution.getObjective(7),
                        solution.getObjective(8),
                        solution.getObjective(9),
                        solution.getObjective(10),
                        solution.getObjective(11));

                //Cada pareto llama de nuevo a placement para obtener las ubicaciones
        //        resultGraphMaps.add(vnfService.placementGraph(traffics, (Permutation) solution.getVariable(0)));
            }


      //      logger.info(resultGraphMaps);

 */
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }

    }
}
