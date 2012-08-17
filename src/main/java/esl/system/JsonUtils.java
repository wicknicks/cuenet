package esl.system;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class JsonUtils {

    public synchronized static boolean contains(BasicDBObject json, String expr) {
        String[] parts = expr.split("\\.");
        return checkForField(json, parts, 0);
    }

    private static boolean checkForField(BasicDBObject json, String[] arrPath, int pos) {
        if (json.containsField(arrPath[pos])) {
            Object o = json.get(arrPath[pos]);
            if (pos == arrPath.length-1) return true;
            if (o instanceof BasicDBList) throw new RuntimeException("Lists not yet supported");
            else if (o instanceof BasicDBObject) return checkForField((BasicDBObject) o, arrPath, pos + 1);
            else throw new RuntimeException("Attempting to unnest: " + json.toString());
        }
        return false;
    }

    public synchronized static Object unnest(BasicDBObject json, String expr) {
        String[] parts = expr.split("\\.");
        return extract(json, parts, 0);
    }

    private static Object extract(BasicDBObject json, String[] arrPath, int pos) {
        if (json.containsField(arrPath[pos])) {
            Object o = json.get(arrPath[pos]);
            if (pos == arrPath.length-1) return o;
            if (o instanceof BasicDBList) throw new RuntimeException("Lists not yet supported");
            else if (o instanceof BasicDBObject) return extract((BasicDBObject) o, arrPath, pos+1);
            else throw new RuntimeException("Attempting to unnest: " + json.toString());
        }
        return null;
    }

    public synchronized static <T> T unnest(BasicDBObject json, String expr, Class<T> type) {
        Object o = unnest(json, expr);
        if (o == null) return null;
        return ((T)o);
    }

}
