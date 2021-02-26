package py.una.pol.service;

import org.apache.log4j.Logger;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.variable.Permutation;
import py.una.pol.dto.NFVdto.Traffic;
import py.una.pol.dto.ResultGraphMap;
import py.una.pol.util.Configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MOEAService {
    Logger logger = Logger.getLogger(MOEAService.class);

    public void nsgaIII() {
        try {
            Configurations configurations = new Configurations();
            DataService data = new DataService();
            VnfService vnfService = new VnfService();
            TrafficService trafficService = new TrafficService();
            List<Traffic> traffics = trafficService.readTraffics();

            TournamentSelection tournamentSelection = new TournamentSelection();

            Properties properties = new Properties();
            properties.setProperty("populationSize", "20");
            properties.setProperty("sbx.swap", "true");


            NondominatedPopulation result = new Executor()
                    .withProblemClass(ProblemService.class)
                    .withAlgorithm("NSGAIII")
                    .withProperties(properties)

                    //  .withMaxEvaluations(1000)
                    .withMaxTime(10000)
                    //      .distributeOnAllCores()
                    .run();

            //Configurar algoritmo de seleccion, operador de cruce y mutacion

            //display the results
            System.out.format("Nro.     Bandwidth       Energy          Delay           Distance        " +
                    "Fragmentation       Licence        LoadTrafic      MaxUseLink      NumberIntances" +
                    "    Resources     SLO        Throughput%n");

            List<ResultGraphMap> resultGraphMaps = new ArrayList<>();
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

                //Cada pareto llamad de nuevo a placement para obtener las ubicaciones
                resultGraphMaps.add(
                        vnfService.placementGraph(traffics, (Permutation) solution.getVariable(0)));
            }

            logger.info(resultGraphMaps);
        } catch (Exception e) {
            logger.error("Error en nsgaIII: " + e.getMessage());
        }

    }
}
