package esl.cuenet.algorithms.firstk.personal.accessor;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PConstants {

    private static BasicDBObject obj;
    static {
        try {
            obj = (BasicDBObject) JSON.parse(FileUtils.readFileToString(new File("cuenet.conf")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static String DBNAME;
    public final static String IMAGE;
    public final static String USERNAME;
    public final static String EMAIL;
    static {
        DBNAME = obj.getString("dbname");
        IMAGE = obj.getString("image");
        USERNAME = obj.getString("name").toLowerCase();
        EMAIL = obj.getString("email").toLowerCase();
    }



}
