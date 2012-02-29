package esl.cuenet.algorithms.costs;

import esl.cuenet.algorithms.ICostEvaluator;
import esl.cuenet.source.ISource;

public class HardcodedCostsEvaluator implements ICostEvaluator {

    @Override
    public double[] evaluate(ISource[] sources) {
        return new double[sources.length];
    }

}
