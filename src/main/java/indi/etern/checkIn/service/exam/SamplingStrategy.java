package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.throwable.exam.generate.NotEnoughQuestionsForExamException;
import indi.etern.checkIn.throwable.exam.generate.PartitionEmptiedException;
import indi.etern.checkIn.throwable.exam.generate.PartitionMaxLimitReachedException;
import indi.etern.checkIn.utils.QuestionRealCountCounter;
import indi.etern.checkIn.utils.WeightRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public enum SamplingStrategy {
    WEIGHTED {
        @Override
        public void sampleQuestions(Set<Question> questions , Map<Partition, PartitionQuestionSampler> map, Random random, int targetQuestionAmount) throws NotEnoughQuestionsForExamException {
            WeightRandom<PartitionQuestionSampler> partitionQuestionDrawerWeightRandom = new WeightRandom<>(random);
            final List<WeightRandom.Part<PartitionQuestionSampler>> weightedParts =
                    map.values().stream().map(partitionQuestionSampler ->
                                    new WeightRandom.Part<>(partitionQuestionSampler)
                                            .useWeightCalc(partitionQuestionSampler::getAvailableCount)
                    ).toList();
            partitionQuestionDrawerWeightRandom.addItems(weightedParts);
            while (true) {
                final int count = QuestionRealCountCounter.count(questions);
                if (count == targetQuestionAmount) {
                    logger.debug("WEIGHTED: draw over, questions: {}",questions);
                    break;
                } else if (count > targetQuestionAmount) {
                    throw new IndexOutOfBoundsException(count + " > " + targetQuestionAmount);
                }
                WeightRandom.Part<PartitionQuestionSampler> partitionQuestionDrawerPart;
                try {
                    partitionQuestionDrawerPart = partitionQuestionDrawerWeightRandom.weightedRandomPart();
                } catch (IllegalStateException|ArrayIndexOutOfBoundsException e) {
                    throw new NotEnoughQuestionsForExamException(e);
                }
                
                PartitionQuestionSampler partitionQuestionSampler = partitionQuestionDrawerPart.getT();
                try {
                    final int rest = targetQuestionAmount - count;
                    final Question question = partitionQuestionSampler.drawOneCountLessThanOrEqual(rest);
                    questions.add(question);
                    logger.debug("WEIGHTED:{} added, previous has {}, rest {}", question, count, rest);
                } catch (PartitionMaxLimitReachedException|PartitionEmptiedException e) {
                    logger.warn("WEIGHTED:{} removed due to strategy or empty", partitionQuestionSampler.getPartition());
                    partitionQuestionDrawerWeightRandom.removeItem(partitionQuestionDrawerPart);
                    map.values().forEach(partitionQuestionSampler1 -> partitionQuestionSampler1.invalidAll(partitionQuestionSampler.getPartitionAllEnabledQuestions()));
                }
            }
        }
    },
    RANDOM {
        @Override
        public void sampleQuestions(Set<Question> questions, Map<Partition, PartitionQuestionSampler> map, Random random, int targetQuestionAmount) throws NotEnoughQuestionsForExamException {
            while (true) {
                final int count = QuestionRealCountCounter.count(questions);
                if (count == targetQuestionAmount) {
                    logger.debug("RANDOM: draw over, questions: {}", questions);
                    break;
                } else if (count > targetQuestionAmount) {
                    throw new IndexOutOfBoundsException(count + " > " + targetQuestionAmount);
                }
                final int randomBound = map.size();
                if (randomBound == 0) {
                    throw new NotEnoughQuestionsForExamException();
                }
                PartitionQuestionSampler partitionQuestionSampler = map.values().stream().toList().get(random.nextInt(randomBound));
                try {
                    final int rest = targetQuestionAmount - count;
                    final Question question = partitionQuestionSampler.drawOneCountLessThanOrEqual(rest);
                    questions.add(question);
                    logger.debug("RANDOM: {} added, previous has {}, rest {}", question, count, rest);
                } catch (PartitionMaxLimitReachedException | PartitionEmptiedException e) {
                    map.remove(partitionQuestionSampler.getPartition());
                    map.values().forEach(partitionQuestionSampler1 -> partitionQuestionSampler1.invalidAll(partitionQuestionSampler.getPartitionAllEnabledQuestions()));
                    logger.warn("RANDOM: {} removed due to strategy or empty", partitionQuestionSampler.getPartition());
                }
            }
        }
    };
    final Logger logger = LoggerFactory.getLogger(getClass());
    
    public abstract void sampleQuestions(Set<Question> questions, Map<Partition, PartitionQuestionSampler> map, Random random, int questionAmount) throws NotEnoughQuestionsForExamException;
}