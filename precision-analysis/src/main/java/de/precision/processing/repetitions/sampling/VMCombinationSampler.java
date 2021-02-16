package de.precision.processing.repetitions.sampling;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dagere.kopeme.generated.Kopemedata.Testcases;
import de.dagere.kopeme.generated.Result;
import de.dagere.kopeme.generated.TestcaseType;
import de.peass.measurement.analysis.Relation;
import de.peass.measurement.analysis.StatisticUtil;
import de.precision.analysis.repetitions.PrecisionComparer;
import de.precision.analysis.repetitions.bimodal.CompareData;
import de.precision.processing.repetitions.misc.DetermineAverageTime;

public class VMCombinationSampler {

   private static final Logger LOG = LogManager.getLogger(VMCombinationSampler.class);

   private final int warmup, allExecutions;
   private final PrecisionComparer comparer;
   private final SamplingConfig config;

   public VMCombinationSampler(int warmup, int allExecutions, PrecisionComparer comparer, SamplingConfig config) {
      this.warmup = warmup;
      this.allExecutions = allExecutions;
      this.comparer = comparer;
      this.config = config;
   }

   /**
    * 
    * @param testclazz
    * @param versionFast
    * @param versionSlow
    * @return average VM-duration in seconds
    */
   public double sampleArtificialVMCombinations(final TestcaseType versionFast, final TestcaseType versionSlow) {
      final List<Result> fastShortened = StatisticUtil.shortenValues(versionFast.getDatacollector().get(0).getResult(), warmup, allExecutions);
      final List<Result> slowShortened = StatisticUtil.shortenValues(versionSlow.getDatacollector().get(0).getResult(), warmup, allExecutions);

      return sampleArtificialVMCombinations(fastShortened, slowShortened);
   }

   /**
    * 
    * @param fastShortened
    * @param slowShortened
    * @return average duration in seconds
    */
   public double sampleArtificialVMCombinations(final List<Result> fastShortened, final List<Result> slowShortened) {
      final double overallDuration = DetermineAverageTime.getDurationInMS(fastShortened, slowShortened);
      final double calculatedDuration = overallDuration / fastShortened.size() * config.getVms();
      
      CompareData data = new CompareData(fastShortened, slowShortened);
      for (int i = 0; i < config.getSamplingExecutions(); i++) {
         executeComparisons(config, data);
      }
      return calculatedDuration / 1000;
   }

   private void executeComparisons(final SamplingConfig config, CompareData data) {
      final SamplingExecutor samplingExecutor = new SamplingExecutor(config, data, comparer);
      samplingExecutor.executeComparisons(Relation.LESS_THAN);
      CompareData equalData = new CompareData(data.getBefore(), data.getBefore());
      final SamplingExecutor samplingExecutor2 = new SamplingExecutor(config, equalData, comparer);
      samplingExecutor2.executeComparisons(Relation.EQUAL);
   }

}
