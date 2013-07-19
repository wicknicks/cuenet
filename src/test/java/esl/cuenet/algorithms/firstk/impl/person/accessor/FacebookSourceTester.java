package esl.cuenet.algorithms.firstk.impl.person.accessor;

import esl.cuenet.algorithms.firstk.personal.EventContextNetwork;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.cuenet.algorithms.firstk.personal.accessor.Candidates;
import esl.cuenet.algorithms.firstk.personal.accessor.Email;
import esl.cuenet.algorithms.firstk.personal.accessor.Facebook;
import esl.cuenet.algorithms.firstk.personal.accessor.PConstants;
import esl.system.SysLoggerUtils;
import org.junit.Test;

import javax.mail.internet.MailDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class FacebookSourceTester {

    static {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void unit() throws ParseException {

        Candidates candidateSet = Candidates.getInstance();

        Facebook facebook = Facebook.getInstance();
        Email email = Email.getInstance();

        List<Candidates.CandidateReference> f = facebook.knows (candidateSet.search(Candidates.EMAIL_KEY, PConstants.EMAIL));
        System.out.println(f.size());


        MailDateFormat format = new MailDateFormat();
        Date d = format.parse("Mon, 21 May 2011 08:22:28 -0800 (PST)");
        List<EventContextNetwork> nets = email.knowsAtTime(candidateSet.search(Candidates.EMAIL_KEY, PConstants.EMAIL),
                Time.createFromMoment(d.getTime()));
        System.out.println(nets.size());
    }

}
