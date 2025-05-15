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

public enum DrawingStrategy {
    WEIGHTED {
        @Override
        public void drawQuestions(Set<Question> questions , Map<Partition, PartitionQuestionDrawer> map, Random random, int targetQuestionAmount) throws NotEnoughQuestionsForExamException {
            WeightRandom<PartitionQuestionDrawer> partitionQuestionDrawerWeightRandom = new WeightRandom<>(random);
            final List<WeightRandom.Part<PartitionQuestionDrawer>> weightedParts =
                    map.values().stream().map(partitionQuestionDrawer ->
                                    new WeightRandom.Part<>(partitionQuestionDrawer)
                                            .useWeightCalc(partitionQuestionDrawer::getAvailableCount)
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
                WeightRandom.Part<PartitionQuestionDrawer> partitionQuestionDrawerPart;
                try {
                    partitionQuestionDrawerPart = partitionQuestionDrawerWeightRandom.weightedRandomPart();
                } catch (IllegalStateException|ArrayIndexOutOfBoundsException e) {
                    throw new NotEnoughQuestionsForExamException(e);
                }
                
                PartitionQuestionDrawer partitionQuestionDrawer = partitionQuestionDrawerPart.getT();
                try {
                    final int rest = targetQuestionAmount - count;
                    final Question question = partitionQuestionDrawer.drawOneCountLessThanOrEqual(rest);
                    questions.add(question);
                    logger.debug("WEIGHTED:{} added, previous has {}, rest {}", question, count, rest);
                } catch (PartitionMaxLimitReachedException|PartitionEmptiedException e) {
                    logger.warn("WEIGHTED:{} removed due to strategy or empty", partitionQuestionDrawer.getPartition());
                    partitionQuestionDrawerWeightRandom.removeItem(partitionQuestionDrawerPart);
                    map.values().forEach(partitionQuestionDrawer1 -> partitionQuestionDrawer1.invalidAll(partitionQuestionDrawer.getPartitionAllEnabledQuestions()));
                }
            }
        }
    },
    RANDOM {
        @Override
        public void drawQuestions(Set<Question> questions, Map<Partition, PartitionQuestionDrawer> map, Random random, int targetQuestionAmount) throws NotEnoughQuestionsForExamException {
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
                PartitionQuestionDrawer partitionQuestionDrawer = map.values().stream().toList().get(random.nextInt(randomBound));
                try {
                    final int rest = targetQuestionAmount - count;
                    final Question question = partitionQuestionDrawer.drawOneCountLessThanOrEqual(rest);
                    questions.add(question);
                    logger.debug("RANDOM: {} added, previous has {}, rest {}", question, count, rest);
                } catch (PartitionMaxLimitReachedException | PartitionEmptiedException e) {
                    map.remove(partitionQuestionDrawer.getPartition());
                    map.values().forEach(partitionQuestionDrawer1 -> partitionQuestionDrawer1.invalidAll(partitionQuestionDrawer.getPartitionAllEnabledQuestions()));
                    logger.warn("RANDOM: {} removed due to strategy or empty", partitionQuestionDrawer.getPartition());
                }
            }
        }
    };
    final Logger logger = LoggerFactory.getLogger(getClass());
    
    public abstract void drawQuestions(Set<Question> questions, Map<Partition, PartitionQuestionDrawer> map, Random random, int questionAmount) throws NotEnoughQuestionsForExamException;
}