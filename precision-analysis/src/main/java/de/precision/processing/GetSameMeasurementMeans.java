package de.precision.processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;

import de.dagere.kopeme.datastorage.JSONDataLoader;
import de.dagere.kopeme.kopemedata.Kopemedata;
import de.dagere.kopeme.kopemedata.VMResult;
import de.dagere.peass.measurement.statistics.StatisticUtil;

public class GetSameMeasurementMeans {

   static Map<File, DescriptiveStatistics> data = new LinkedHashMap<>();

   public static void main(final String[] args)  {

      GetSameMeasurementMeans getSameMeasurementMeans = new GetSameMeasurementMeans();
      for (File job : new File(args[0]).listFiles()) {
         if (job.isDirectory()) {
            DescriptiveStatistics currentStatistics = new DescriptiveStatistics();
            data.put(job, currentStatistics);
            getSameMeasurementMeans.getSizeStatistics(job, currentStatistics);
         }
      }
      DescriptiveStatistics first = data.values().iterator().next();
      for (Map.Entry<File, DescriptiveStatistics> entry : data.entrySet()) {
         System.out.println(entry.getKey().getName() + " " +
               entry.getValue().getMean() + " " +
               entry.getValue().getStandardDeviation() + " " + 
               new TTest().t(first, entry.getValue()));
      }
   }

   private int getSize(final String[] parts) {
      int size;
      if (parts.length == 3) {
         size = Integer.parseInt(parts[1]);
      } else {
         size = Integer.parseInt(parts[2]);
      }
      return size;
   }

   private void getSizeStatistics(final File folder, final DescriptiveStatistics stat)  {
      File valueCSV = new File(folder.getParentFile(), folder.getName() + ".csv");
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(valueCSV))) {
         for (File file : folder.listFiles()) {
            if (file.getName().startsWith("wl_")) {
               System.out.println("File: " + file.getName());
               String[] parts = file.getName().split("_");
               int size = getSize(parts);
               // int index = Integer.parseInt(parts[1]);
               if (parts.length == 3) {
                  final VMResult result = readShortenedResult(file);
                  stat.addValue(result.getValue());
                  writer.write(result.getValue() + "\n");
                  // dataManager.addMean(size, folder.getName(), result.getValue());
               }
            }
         }
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   private VMResult readShortenedResult(final File file)  {
      System.out.println(Arrays.toString(file.listFiles()));
      File measurementFile = file.listFiles((FileFilter) new WildcardFileFilter("*.xml"))[0];
      final Kopemedata loadData = JSONDataLoader.loadData(measurementFile);

      final VMResult basicResult = loadData.getFirstResult();
      final VMResult result = StatisticUtil.shortenResult(basicResult);
      return result;
   }
}
