package esl.cuenet.algorithms.firstk.personal;

import esl.cuenet.algorithms.firstk.impl.LocalFilePreprocessor;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Email;
import esl.cuenet.algorithms.firstk.personal.accessor.Facebook;
import esl.cuenet.algorithms.firstk.personal.accessor.PConstants;
import esl.system.SysLoggerUtils;

import java.io.IOException;

public class Main {

    public static void load () {
        LocalFilePreprocessor.ExifExtractor extractor = new LocalFilePreprocessor.ExifExtractor();
        LocalFilePreprocessor.Exif exif;
        try {
            exif = extractor.extractExif(PConstants.IMAGE);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println(exif);

    }

    public static void main(String[] args) {
        SysLoggerUtils.initLogger();
//        new Facebook();
//        new Email();
//        Candidates.getInstance().logistics(false);

        load();
        (new Ontology()).printAll();
    }

}
