package py.una.pol.service;

import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.indicator.*;
import org.moeaframework.problem.DTLZ.DTLZ2;
import py.una.pol.dto.NFVdto.Traffic;
import py.una.pol.util.Configurations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

            String[] algorithms = {"NSGAIII", "MOEAD", "RVEA"};

            //setup the experiment
            Executor executor = new Executor()
                    .withProblemClass(ProblemService.class)
                    .withMaxTime(600000)
                    .distributeOnAllCores();
            //  .withMaxEvaluations(1000);

            Analyzer analyzer = new Analyzer()
                    .withProblemClass(ProblemService.class)
                    .includeGenerationalDistance()
                    .includeInvertedGenerationalDistance()
                    .includeMaximumParetoFrontError()
                    .includeAdditiveEpsilonIndicator()
                    .includeSpacing()
                    .includeR1()
                    .includeR2()
                    .includeR3()
                    .showStatisticalSignificance();

            Problem problem = new ProblemService();

            //run each algorithm for seeds
            for (String algorithm : algorithms) {
                logger.info("Inicio de ejecución " + algorithm);
                long inicio = System.currentTimeMillis();

                int seed = 0;
                List<NondominatedPopulation> results = executor.withAlgorithm(algorithm).runSeeds(3);

                //Se crean los puntos de referencia
                NondominatedPopulation referenceSet = new NondominatedPopulation(new ParetoDominanceComparator());
                for (NondominatedPopulation set : results)
                    referenceSet.addAll(set);

                File targetFile = new File("/home/cbenitez/Desktop/NFV-Placement/Solutions/"
                        + Configurations.networkPackage + "Traffic_" + Configurations.numberTraffic + "/" + algorithm);
                targetFile.mkdirs();

                String objetivesTitle = "Bandwidth;EnergyCost;DelayCost;Distance;Fragmentation;LicencesCost;LoadTraffic;MaxUseLink;NumberInstances;ResourcesCost;SloCost;Throughput\n";
                String metricsTitle = "GenerationalDistance;InvertedGenerationalDistance;MaximumParetoFrontError;AdditiveEpsilonIndicator;Spacing;R1Indicator;R2Indicator;R3Indicator \n";

                File paretoFile;
                String metrics = "";
                for (NondominatedPopulation result : results) {
                    seed = seed + 1;
                    logger.info("Frente pareto (seed) " + seed + ": " + result.size() + " soluciones");
                    paretoFile = new File(targetFile + "/pareto" + seed);
                    paretoFile.mkdirs();

                    String objetivesNormalized = "";
                    String objetives = "";
                    for (int i = 0; i < result.size(); i++) {
                        objetives = objetives.concat(getObjectives(result.get(i)));
                        if(result.get(i).getAttribute("Normalized Objectives") != null)
                            objetivesNormalized = objetivesNormalized.concat(getObjectivesNormalized((double[]) result.get(i).getAttribute("Normalized Objectives")));
                    }

                    writeFile(paretoFile + "/solutions_"+ result.size() + ".csv", objetivesTitle.concat(objetives));
                    writeFile(paretoFile + "/solutionsNormalized_"+ result.size() + ".csv", objetivesTitle.concat(objetivesNormalized));

                    double generationalDistance = new GenerationalDistance(problem, referenceSet).evaluate(result);
                    double invertedGenerationalDistance = new InvertedGenerationalDistance(problem, referenceSet).evaluate(result);
                    double maximumParetoFrontError = new MaximumParetoFrontError(problem, referenceSet).evaluate(result);
                    double additiveEpsilonIndicator = new AdditiveEpsilonIndicator(problem, referenceSet).evaluate(result);
                    double spacing = new Spacing(problem).evaluate(result);
                    double includeR1 = new R1Indicator(problem, R1Indicator.getDefaultSubdivisions(problem), referenceSet).evaluate(result);
                    double includeR2 = new R2Indicator(problem, R2Indicator.getDefaultSubdivisions(problem), referenceSet).evaluate(result);
                    double includeR3 = new R3Indicator(problem, R3Indicator.getDefaultSubdivisions(problem), referenceSet).evaluate(result);

                    metrics = metrics.concat(generationalDistance + ";" + invertedGenerationalDistance + ";" + maximumParetoFrontError + ";" +
                            additiveEpsilonIndicator + ";" + spacing + ";" + includeR1 + ";" + includeR2 + ";" + includeR3 + "\n");

                }
                writeFile(targetFile + "/metrics.csv", metricsTitle.concat(metrics));

                analyzer.addAll(algorithm, results);
                long fin = System.currentTimeMillis();
                logger.info("Fin de ejecución " + algorithm + " " + getTime(fin - inicio));
            }

            //print the results
            long inicio = System.currentTimeMillis();
            logger.info("Inicio Analysis");
            Analyzer analyzerResult = analyzer.printAnalysis();
            logger.info(analyzerResult.toString());
            long fin = System.currentTimeMillis();
            logger.info("Fin Analysis " + getTime(fin - inicio));

            long finTotal = System.currentTimeMillis();
            logger.info("Tiempo de ejecución Total: " + getTime(finTotal - inicioTotal));

        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }

    }

    private String getObjectives(Solution solution) {

        return solution.getObjective(0) + ";" +
                solution.getObjective(1) + ";" +
                solution.getObjective(2) + ";" +
                solution.getObjective(3) + ";" +
                solution.getObjective(4) + ";" +
                solution.getObjective(5) + ";" +
                solution.getObjective(6) + ";" +
                solution.getObjective(7) + ";" +
                solution.getObjective(8) + ";" +
                solution.getObjective(9) + ";" +
                solution.getObjective(10) + ";" +
                solution.getObjective(11) + "\n";

    }

    private String getObjectivesNormalized(double[] objetivesNormalizedVector) {

        return objetivesNormalizedVector[0] + ";" +
                objetivesNormalizedVector[1] + ";" +
                objetivesNormalizedVector[2] + ";" +
                objetivesNormalizedVector[3] + ";" +
                objetivesNormalizedVector[4] + ";" +
                objetivesNormalizedVector[5] + ";" +
                objetivesNormalizedVector[6] + ";" +
                objetivesNormalizedVector[7] + ";" +
                objetivesNormalizedVector[8] + ";" +
                objetivesNormalizedVector[9] + ";" +
                objetivesNormalizedVector[10] + ";" +
                objetivesNormalizedVector[11] + "\n";

    }

    private void writeFile(String src, String object) throws Exception {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(src);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);

        } catch (Exception e) {
            logger.error("Error al escribir en el archivo de traficos");
            throw new Exception();
        } finally {
            if (objectOutputStream != null)
                objectOutputStream.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }

    public void maoeaSolutions() throws Exception {
        Configurations.loadProperties();
        DataService.loadData();

        List<Traffic> traffics = TrafficService.readTraffics();

        String algorithm = "NSGAIII";
        logger.info("Inicio de ejecución " + algorithm);
        long inicio = System.currentTimeMillis();

        NondominatedPopulation result = new Executor()
                .withProblemClass(ProblemService.class)
                .withAlgorithm(algorithm)
                .distributeOnAllCores()
                .withMaxTime(600000)
                .run();

        long fin = System.currentTimeMillis();

        logger.info("Fin de ejecución " + algorithm);
        logger.info("Frente pareto: " + result.size() + " soluciones");
        logger.info("Tiempo de ejecución: " + getTime(fin - inicio));

        logger.info("Throughput: ");
        int i = 1;
        for (Solution solution : result) {
            logger.info(i++ + ") " + solution.getObjective(11));
        }


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


    public String getTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}




