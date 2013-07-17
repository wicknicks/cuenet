package esl.cuenet.algorithms.firstk.personal;

import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Email;
import esl.cuenet.algorithms.firstk.personal.accessor.Facebook;
import esl.system.SysLoggerUtils;

public class Main {

    public static void main(String[] args) {
        SysLoggerUtils.initLogger();
//        new Facebook();
//        new Email();
//        Candidates.getInstance().logistics(false);

        (new Ontology()).printAll();

    }

}
