package experiments;

import com.mongodb.BasicDBObject;
import esl.cuenet.query.drivers.mongodb.MongoDBHelper;
import esl.cuenet.source.accessors.Utils;
import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountAllCandidates {

    private Logger logger = Logger.getLogger(CountAllCandidates.class);
    public CountAllCandidates() {
        SysLoggerUtils.initLogger();
    }

    @Test
    public void countAllCandidatesInDB() {
        countEmails();
        countFB();
        countConfs();
    }

    private void countConfs() {
        MongoDBHelper mongoDBHelper = new MongoDBHelper();
        List<BasicDBObject> result = mongoDBHelper.query("conf_attendees", new BasicDBObject());
        HashMap<String, BasicDBObject> map = new HashMap<String, BasicDBObject>();
        int dups = 0;

        for (BasicDBObject obj: result) {
            BasicDBObject r = map.put(obj.getString("name"), obj);
            if (r != null) dups++;
        }

        logger.info("Candidates in Confs: " + map.size());
        logger.info("Duplicates found: " + dups);
    }

    private void countFB() {
        MongoDBHelper mongoDBHelper = new MongoDBHelper();
        List<BasicDBObject> result = mongoDBHelper.query("fb_users", new BasicDBObject());
        logger.info("Total number of candidates in FB: " + result.size());
    }

    private void countEmails() {
        MongoDBHelper mongoDBHelper = new MongoDBHelper();
        List<BasicDBObject> result = mongoDBHelper.query("emails", new BasicDBObject());
        HashMap<String, String> map = new HashMap<String, String>();

        String to, from, cc;
        int c = 0;
        for (BasicDBObject obj: result) {
            List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();
            to = obj.getString("To");
            if (to != null) entries.addAll(Utils.parseEmailAddresses(to));

            from = obj.getString("From");
            if (from != null) entries.addAll(Utils.parseEmailAddresses(from));

            cc = obj.getString("CC");
            if (cc != null)entries.addAll(Utils.parseEmailAddresses(cc));

            for (Map.Entry<String, String> entry: entries) {
                if (entry.getValue() == null) continue;
                String l = map.put(entry.getValue(), entry.getKey());
                if (l != null) c++;
            }


            //if (c == 10) break;
        }

        logger.info("Candidates from email: " + map.size());
        logger.info("Duplicates in email map: " + c);
    }

//    Results
//    ------------------------------------------------------------------------------------------------------------
//    0 [main] INFO experiments.CountAllCandidates  - Candidates from email: 251
//    150 [main] INFO experiments.CountAllCandidates  - Total number of candidates in FB: 405
//    194 [main] INFO experiments.CountAllCandidates  - Candidates in map: 1238
//    194 [main] INFO experiments.CountAllCandidates  - Duplicates found: 5
//    ------------------------------------------------------------------------------------------------------------
//    Total: 1894
}
