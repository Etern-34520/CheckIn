package indi.etern.checkIn.utils;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WeightRandom<T> {
    Logger logger = LoggerFactory.getLogger(getClass());
    @FunctionalInterface
    public interface WeightProcessor {
        int process();
    }
    @Getter
    public static class Part<T> {
        private final T t;
        private WeightProcessor weightProcessor;
        public Part (T t) {
            this.t = t;
        }
        public Part<T> useWeightCalc(WeightProcessor weightProcessor) {
            this.weightProcessor = weightProcessor;
            return this;
        }
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Part<?> part = (Part<?>) o;
            return Objects.equals(t, part.t);
        }
        public int hashCode() {
            return Objects.hash(t);
        }
        public String toString() {
            return "WeightPart(" + t.toString() + ")";
        }
    }
    private boolean dirty = false;
    Random random;
    Set<Part<T>> partsSet = new LinkedHashSet<>();
    int[] weightsArray;
    Part<T>[] partsArray;
    public WeightRandom(Random random) {
        this.random = random;
    }
    public void addItem(Part<T> part) {
        boolean dirty = !partsSet.contains(part);
        partsSet.add(part);
        if (!dirty) {
            logger.warn("part \"{}\" exists",part);
        } else {
            logger.debug("part \"{}\" added",part);
        }
        this.dirty = dirty;
    }
    public void addItems(Collection<Part<T>> parts) {
        parts.forEach(this::addItem);
    }
    public void removeItem(Part<T> part) {
        boolean dirty = partsSet.remove(part);
        if (!dirty) {
            logger.warn("part \"{}\"  not exists",part);
        } else {
            logger.debug("part  \"{}\" removed",part);
        }
        this.dirty = dirty;
    }
    public void flush() {
        AtomicInteger maxWeight = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);
        final List<Part<T>> partList = partsSet.stream().filter(part -> part.weightProcessor.process() > 0).toList();
        int[] weights = new int[partList.size() + 1];
        //noinspection rawtypes
        Part[] partsArray = new Part[partList.size()];
        partList.forEach(part -> {
            int weight = part.weightProcessor.process();
            weights[index.get()] = maxWeight.getAndAdd(weight);
            partsArray[index.get()] = part;
            index.incrementAndGet();
        });
        weights[weights.length - 1] = maxWeight.get();
        this.weightsArray = weights;
        //noinspection unchecked
        this.partsArray = partsArray;
        if (logger.isDebugEnabled()) {
            logger.debug("weight flushed: {}", Arrays.toString(weights));
            logger.debug("parts flushed: {}", Arrays.toString(partsArray));
        }
        dirty = false;
    }
    public T weightedRandomOne() {
        Part<T> part = weightedRandomPart();
        return part.getT();
    }
    public Part<T> weightedRandomPart() {
        if (dirty || weightsArray == null) flush();
        final int bound = weightsArray[weightsArray.length - 1];
        if (bound == 0) {
            logger.warn("no part available");
            throw new IllegalStateException("no part available");
        }
        final int randomInt = bound > 1 ? random.nextInt(1, bound) : 1;
        final int searchResult = Arrays.binarySearch(weightsArray, randomInt);
        int partIndex = searchResult >= 0 ? searchResult : -searchResult - 2;
        if (partsArray.length == partIndex) {
            partIndex--;
        }
        Part<T> part = partsArray[partIndex];
        logger.debug("get random part, index: \"{}\"", partIndex);
        logger.debug("part target: \"{}\"",part.t);
        return part;
    }
}