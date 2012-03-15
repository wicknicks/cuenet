package esl.cuenet.algorithms.costs;

import esl.cuenet.algorithms.ICostEvaluator;
import esl.cuenet.source.ISource;

public class FixedCostsEvaluator implements ICostEvaluator {

    @Override
    public double[] evaluate(ISource[] sources) {
        return new double[sources.length];
    }

}
