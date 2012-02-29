package esl.cuenet.algorithms;

import esl.cuenet.source.ISource;

public interface ICostEvaluator {

    double[] evaluate(ISource[] sources);

}
