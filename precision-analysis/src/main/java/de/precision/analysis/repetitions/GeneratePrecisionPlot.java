package de.precision.analysis.repetitions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * Takes a folder with sequence-executions and a precision-level as input. Tells, how many sequence-executions are needed in order to achieve the precision-level.
 * 
 * @author reichelt
 *
 */
public class GeneratePrecisionPlot implements Callable<Void> {

   private static final Logger LOG = LogManager.getLogger(GeneratePrecisionPlot.class);

   @Option(names = { "-only100k", "--only100k" }, description = "Only analyse 100.000 repetitions - for test comparison")
   private boolean only100k = false;

   @Option(names = { "-data", "--data" }, description = "Data-Folder for analysis", required = true)
   private String[] data;

   @Option(names = { "-printPicks", "--printPicks" }, description = "Print the picked values summaries (for debugging)")
   private boolean printPicks;

   @Option(names = { "-threads", "--threads" }, description = "Count of threads for analysis")
   private int threads = 2;

   @Option(names = { "-iterationResolution", "--iterationResolution" }, description = "Resolution for iteration count analysis (by default: 50 steps for iteration count)")
   private int iterationResolution = 50;

   @Option(names = { "-vmResolution", "--vmResolution" }, description = "Resolution for VM count analysis (by default: 50 steps for VM count)")
   private int vmResolution = 20;

   @Option(names = { "-statisticalTests", "--statisticalTests" }, description = "Statistical tests that should be used (either ALL or ALL_NO_BIMODA)")
   private StatisticalTestList statisticalTestList = StatisticalTestList.ALL_NO_BIMODAL_NO_CONFIDENCE;

   public static void main(final String[] args) throws JAXBException, IOException, InterruptedException {
      Configurator.setLevel("de.peass.measurement.analysis.statistics.ConfidenceIntervalInterpretion", Level.INFO);

      final GeneratePrecisionPlot command = new GeneratePrecisionPlot();
      final CommandLine commandLine = new CommandLine(command);
      commandLine.execute(args);
   }

   private void createTasks(final String[] folders, final PrecisionConfig config, final String suffix) throws IOException, JAXBException, InterruptedException {
      for (final String inputFolderName : folders) {
         final File inputFolder = new File(inputFolderName);

         final File resultFolder = new File(inputFolder.getParentFile(), inputFolder.getName() + File.separator + suffix);
         new PrecisionPlotGenerationManager(resultFolder, config).handleFolder(inputFolder);
      }
   }

   @Override
   public Void call() throws Exception {
      createTasks(data, new PrecisionConfig(only100k, false, printPicks, threads, statisticalTestList.getTests(), iterationResolution, vmResolution), "results_noOutlierRemoval");
      createTasks(data, new PrecisionConfig(only100k, true, printPicks, threads, statisticalTestList.getTests(), iterationResolution, vmResolution), "results_outlierRemoval");

      SingleFileGenerator.createSingleFiles(data);
      return null;
   }
}
