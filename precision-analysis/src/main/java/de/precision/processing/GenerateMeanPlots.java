package de.precision.processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dagere.kopeme.datastorage.JSONDataLoader;
import de.dagere.kopeme.kopemedata.Kopemedata;
import de.dagere.kopeme.kopemedata.TestMethod;
import de.dagere.kopeme.kopemedata.VMResult;
import de.dagere.peass.analysis.measurement.statistics.MeanCoVData;
import de.dagere.peass.analysis.measurement.statistics.MeanCoVDataContinous;

/**
 * Generates csv-files and gnuplot-commands for printing an graph of the values and the coefficient of variation of all result files in one folder
 * 
 * @author reichelt
 *
 */
public final class GenerateMeanPlots {

   private GenerateMeanPlots() {

   }

   private static final Logger LOG = LogManager.getLogger(GenerateMeanPlots.class);

   static final int AVG_COUNT = 10;
   
   static boolean useFullData = false;

   public static void main(final String[] args) throws IOException {

      printConfig();

      final Map<String, List<VMResult>> results = new TreeMap<>();
      
      final File folder = new File(args[0]);
      for (final File dataFile : FileUtils.listFiles(folder, new WildcardFileFilter("*.xml"), TrueFileFilter.INSTANCE)) {
         LOG.debug("Loading: {}", dataFile);
         final Kopemedata data = new JSONDataLoader(dataFile).getFullData();
         final TestMethod testcase = data.getMethods().get(0);
         final File testclazzFolder = dataFile.getParentFile();
         handleTestcase(data.getClazz(), testcase, testclazzFolder.getName());
         
         addResult(results, testcase, testclazzFolder);
      }
      
      for (Map.Entry<String, List<VMResult>> sizeResults : results.entrySet()) {
         MeanCoVData data = new MeanCoVData(sizeResults.getKey(), sizeResults.getValue());
         data.printAverages(new File(ProcessConstants.RESULTFOLDER_COV, sizeResults.getKey()));
      }
   }

   private static void addResult(final Map<String, List<VMResult>> results, final TestMethod testcase, final File testclazzFolder) {
      final String sizeFolderName = testclazzFolder.getParentFile().getParentFile().getName();
      List<VMResult> currentResults = results.get(sizeFolderName);
      if (currentResults == null) {
         currentResults = new LinkedList<>();
         results.put(sizeFolderName, currentResults);
      }
      currentResults.add(testcase.getDatacollectorResults().get(0).getResults().get(0));
   }

   public static void printConfig() {
      System.out.println("set datafile separator ';'");
      System.out.println("set terminal wxt size 600,400");
      System.out.println("set decimalsign locale \"de_DE.UTF-8\"");
      System.out.println("set y2range [0:2]");
      System.out.println("set y2tics");      
      System.out.println("set xlabel 'Iteration'");
      System.out.println("set ylabel 'Zeit / {/Symbol m}'");
   }

   public static void handleTestcase(final String clazzname, final TestMethod testcase, final String type) throws IOException {
      final MeanCoVData data = useFullData ? new MeanCoVDataContinous(testcase, AVG_COUNT) : new MeanCoVData(testcase, AVG_COUNT);
//      data.printTestcaseData(GenerateCoVPlots.RESULTFOLDER);

//      printVMDeviations(clazzname, testcase, testcase.getDatacollector().get(0).getResult());

      data.printAverages(ProcessConstants.RESULTFOLDER_COV, "one_" + type);
   }

   static void printVMDeviations(final String clazzname, final TestMethod testcase, final List<VMResult> results) throws IOException {
      final File summaryFile = new File(ProcessConstants.RESULTFOLDER_COV, "deviations_" + clazzname + "_" + testcase.getMethod() + "_all.csv");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(summaryFile))) {

         for (final VMResult result : results) {
            final DescriptiveStatistics statistics = new DescriptiveStatistics();
            result.getFulldata().getValues().forEach(value -> statistics.addValue(value.getValue()));
            writer.write(statistics.getMean() + ";" + statistics.getVariance() + "\n");
         }
         writer.flush();

         System.out.println("set title 'Means and Variation for " + clazzname + "." + testcase.getMethod() + "'");
         System.out.println("plot '" + summaryFile.getName() + "' u 1:2 title 'Variations'");
         System.out.println();
      }
   }
}
