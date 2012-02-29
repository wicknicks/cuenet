package esl.cuenet.algorithms.costs;

import esl.cuenet.algorithms.ICostEvaluator;
import esl.cuenet.source.ISource;

import java.util.Random;

public class RandomCostEvaluator implements ICostEvaluator {

    private Random generator = new Random(System.currentTimeMillis());

    public double[] evaluate(ISource[] sources) {

        double results[] = new double[sources.length];

        for (int i=0; i<results.length; i++)
            results[i] = generator.nextDouble();

        return results;
    }

}
