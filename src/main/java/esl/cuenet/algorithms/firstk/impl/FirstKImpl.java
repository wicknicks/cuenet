package esl.cuenet.algorithms.firstk.impl;

import esl.cuenet.algorithms.firstk.CorruptDatasetException;
import esl.cuenet.algorithms.firstk.FirstKAlgorithm;
import esl.cuenet.mapper.parser.ParseException;
import esl.cuenet.mapper.tree.SourceParseException;

import java.io.FileNotFoundException;

public class FirstKImpl extends FirstKAlgorithm {

    public FirstKImpl() throws FileNotFoundException, ParseException, SourceParseException {
        super();
    }

    public void execute(LocalFileDataset lds) throws CorruptDatasetException {

        LocalFilePreprocessor preprocessor = new LocalFilePreprocessor();
        preprocessor.process(lds);

    }
}
