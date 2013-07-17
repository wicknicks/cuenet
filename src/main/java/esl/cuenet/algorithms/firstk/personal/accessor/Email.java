package esl.cuenet.algorithms.firstk.personal.accessor;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import esl.cuenet.algorithms.firstk.personal.Location;
import esl.cuenet.algorithms.firstk.personal.Time;
import esl.cuenet.query.drivers.mongodb.MongoDB;
import esl.cuenet.source.accessors.Utils;
import org.apache.log4j.Logger;

import javax.mail.internet.MailDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Email implements Source {

    private Logger logger = Logger.getLogger(Email.class);
    private SimpleDateFormat rfc2822DateFormatter = new MailDateFormat();
    private final List<EmailObject> emails;
    private Candidates candidateList = Candidates.getInstance();


    public Email() {
        MailLoader loader = new MailLoader();
        this.emails = loader.load();
        logger.info("Loaded " + emails.size() + " emails.");
    }

    @Override
    public List<Object> eventsContaining(Object person, Time interval, Location location) {
        return null;
    }

    @Override
    public List<Object> participants(Object event) {
        return null;
    }

    @Override
    public List<Object> subevents(Object event) {
        return null;
    }

    @Override
    public List<Object> knows(Object person) {
        return null;
    }

    @Override
    public List<Object> knowsAtTime(Object person, Time time) {
        return null;
    }

    public class EmailObject {
        List<Map.Entry<String, String>> nameMailPairs;
        Time time;
    }

    public class MailLoader extends MongoDB {

        public MailLoader() {
            super(PConstants.DBNAME);
        }

        public List<EmailObject> load() {
            MongoDB.DBReader reader = startReader("emails");
            reader.getAll(new BasicDBObject());

            String to, from, cc, date;
            List<EmailObject> emails = new ArrayList<EmailObject>();
            while (reader.hasNext()) {
                BasicDBObject obj = (BasicDBObject) reader.next();

                EmailObject email = new EmailObject();
                email.nameMailPairs = Lists.newArrayList();

                to = obj.getString("to");
                if (to != null) email.nameMailPairs.addAll(Utils.parseEmailAddresses(to));

                from = obj.getString("from");
                if (from != null) email.nameMailPairs.addAll(Utils.parseEmailAddresses(from));

                cc = obj.getString("cc");
                if (cc != null) email.nameMailPairs.addAll(Utils.parseEmailAddresses(cc));

                date = obj.getString("date");
                email.time = getDate(date);

                checkCandidates(email.nameMailPairs);

                emails.add(email);
            }
            close();
            return emails;
        }

        private void checkCandidates(List<Map.Entry<String, String>> nameMailPairs) {
            for (Map.Entry<String, String> pair: nameMailPairs) {
                String email = pair.getKey().toLowerCase();
                String name = pair.getValue();

                if (name != null) name = name.toLowerCase();

                Candidates.CandidateReference cReference = null;
                cReference = candidateList.search(Candidates.EMAIL_KEY, email);
                if (cReference == Candidates.UNKNOWN && name != null) cReference = candidateList.search(Candidates.NAME_KEY, name);

                if (cReference == Candidates.UNKNOWN) candidateList.createCandidate(Candidates.EMAIL_KEY, email);
                candidateList.add(cReference, Candidates.EMAIL_KEY, email);
                if (name != null) candidateList.add(cReference, Candidates.NAME_KEY, name);
            }
        }

        private Time getDate(String date) {
            long ms = 0;
            if (date != null) {
                try {
                    ms = (rfc2822DateFormatter.parse(date)).getTime();
                } catch (ParseException e) {
                    logger.error("RFC2822 Date Parsing failed: " + date + " " + e.getMessage());
                }
            } else {
                logger.info("Date is null!");
            }

            return Time.createFromMoment(ms);
        }
    }
}
