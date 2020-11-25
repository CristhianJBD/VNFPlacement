package py.una.pol.service;

import org.apache.log4j.Logger;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import py.una.pol.util.Configurations;

public class MOEAService {
    Logger logger = Logger.getLogger(MOEAService.class);

    public void nsgaIII (){
        try {
            Configurations configurations = new Configurations();
            TrafficService trafficService = new TrafficService();
            DataService data = new DataService();

            NondominatedPopulation result = new Executor()
                    .withProblemClass(ProblemService.class)
                    .withAlgorithm("NSGAIII")
                  //  .withMaxEvaluations(1000)
                    .withMaxTime(10000)
                    .distributeOnAllCores()
                    .run();

            //Configurar algoritmo de seleccion, operador de cruce y mutacion

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
            }
        }catch (Exception e){
            logger.error("Error en nsgaIII: " + e.getMessage());
        }

    }
}
