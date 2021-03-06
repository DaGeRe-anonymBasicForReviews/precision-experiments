package de.precision.analysis.repetitions;

import java.util.Map;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dagere.peass.config.StatisticsConfig;
import de.dagere.peass.measurement.statistics.ConfidenceIntervalInterpretion;
import de.dagere.peass.measurement.statistics.Relation;
import de.dagere.peass.measurement.statistics.bimodal.BimodalityTester;
import de.dagere.peass.measurement.statistics.bimodal.CompareData;

public class TestExecutors {
   private static final Logger LOG = LogManager.getLogger(TestExecutors.class);

   public static void getMeanRelation(final Map<String, Relation> relations, final CompareData data) {
      final double minChange = 0.997;
      if (data.getAvgBefore() < data.getAvgAfter() * minChange) {
         relations.put(StatisticalTests.MEAN, Relation.LESS_THAN);
      } else {
         if (data.getAvgAfter() * 0.99 > data.getAvgBefore()) {
            relations.put(StatisticalTests.MEAN, Relation.GREATER_THAN);
         } else {
            relations.put(StatisticalTests.MEAN, Relation.EQUAL);
         }
      }
   }

   public static boolean getTTestRelation(final Map<String, Relation> relations, final CompareData data, final StatisticsConfig config) {
      final boolean tchange = new TTest().homoscedasticTTest(data.getBefore(), data.getAfter(), config.getType1error());
      // final boolean tchange = new TTest().homoscedasticTTest(values.get(0), values.get(1), 0.01);
      if (tchange) {
         relations.put(StatisticalTests.TTEST, data.getAvgBefore() < data.getAvgAfter() ? Relation.LESS_THAN : Relation.GREATER_THAN);
      } else {
         relations.put(StatisticalTests.TTEST, Relation.EQUAL);
      }
      return tchange;
   }

   public static boolean getTTestRelationBimodal(final Map<String, Relation> relations, final CompareData data, final StatisticsConfig statisticsConfig) {
      final BimodalityTester tester = new BimodalityTester(data);
      final boolean tchange = tester.isTChange(statisticsConfig.getType1error());
      if (tchange) {
         final Relation relation = tester.getRelation();
         relations.put(StatisticalTests.TTEST2, relation);
      } else {
         relations.put(StatisticalTests.TTEST2, Relation.EQUAL);
      }
      return tchange;
   }

   public static void getMannWhitneyRelation(final Map<String, Relation> relations, final CompareData data, final StatisticsConfig config) {
      final double statistic = new MannWhitneyUTest().mannWhitneyUTest(data.getBefore(), data.getAfter());
      LOG.trace(statistic);
      final boolean mannchange = statistic < config.getType1error(); // 2.33 - critical value for confidence level 0.99
      if (mannchange) {
         relations.put(StatisticalTests.MANNWHITNEY, data.getAvgBefore() < data.getAvgAfter() ? Relation.LESS_THAN : Relation.GREATER_THAN);
      } else {
         relations.put(StatisticalTests.MANNWHITNEY, Relation.EQUAL);
      }
   }

   public static void getConfidenceRelation(final CompareData cd, final Map<String, Relation> relations) {
      final Relation confidence = ConfidenceIntervalInterpretion.compare(cd, 90);
      relations.put(StatisticalTests.CONFIDENCE, confidence);
      LOG.trace("Confidence: " + confidence);
   }
}
