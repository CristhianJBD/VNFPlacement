package py.una.pol.service;

import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import py.una.pol.dto.NFVdto.Traffic;
import py.una.pol.util.Configurations;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MaOEAService {
    Logger logger = Logger.getLogger(MaOEAService.class);

    public void maoeaMetrics() {
        try {
            Configurations.loadProperties();
            DataService.loadData();
            TrafficService.readTraffics();

            logger.info("Inicio de ejecución: ");
            long inicioTotal = System.currentTimeMillis();

            //String[] algorithms = { "NSGAIII", "MOEAD", "RVEA"};
            String[] algorithms = { "NSGAIII"};

            //setup the experiment
            Executor executor = new Executor()
                    .withProblemClass(ProblemService.class)
                    .withMaxTime(100)
                    .distributeOnAllCores();
            //  .withMaxEvaluations(1000);

            Analyzer analyzer = new Analyzer()
                    .withProblemClass(ProblemService.class)
                    .includeHypervolume()
                    .includeContribution()
                    .showStatisticalSignificance();

            //run each algorithm for seeds
            for (String algorithm : algorithms) {
                logger.info("Inicio de ejecución " + algorithm);
                long inicio = System.currentTimeMillis();

                int seed = 1;
                List<NondominatedPopulation> results = executor.withAlgorithm(algorithm).runSeeds(1);
                for(NondominatedPopulation result : results)
                    logger.info("Frente pareto (seed) " + seed++ + ": " + result.size() + " soluciones");

                analyzer.addAll(algorithm, results);
                long fin = System.currentTimeMillis();
                logger.info("Fin de ejecución " + algorithm + " " + getTime(fin - inicio));
            }

            //print the results
            long inicio = System.currentTimeMillis();
            logger.info("Inicio Analysis");
            analyzer.printAnalysis();
            long fin = System.currentTimeMillis();
            logger.info("Fin Analysis " + getTime(fin - inicio));

            long finTotal = System.currentTimeMillis();
            logger.info("Tiempo de ejecución Total: " + getTime(finTotal - inicioTotal));

        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }

    }

    public void maoeaSolutions() throws Exception {
        Configurations.loadProperties();
        DataService.loadData();

        List<Traffic> traffics =  TrafficService.readTraffics();

        String algorithm = "NSGAIII";
        logger.info("Inicio de ejecución " + algorithm);
        long inicio = System.currentTimeMillis();

        NondominatedPopulation result = new Executor()
                    .withProblemClass(ProblemService.class)
                    .withAlgorithm(algorithm)
                    .distributeOnAllCores()
                    .withMaxTime(1200000)
                    .run();

        long fin = System.currentTimeMillis();

        logger.info("Fin de ejecución " + algorithm);
        logger.info("Frente pareto: " + result.size() + " soluciones");
        logger.info("Tiempo de ejecución: " + getTime(fin - inicio));

  /*
          VnfService vnfService = new VnfService();
          List<ResultGraphMap> resultGraphMaps = new ArrayList<>();
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
             //   resultGraphMaps.add(vnfService.placementGraph(traffics, (Permutation) solution.getVariable(0)));
            }
           // logger.info(resultGraphMaps);
   */
    }


    public String getTime(long millis){
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}




