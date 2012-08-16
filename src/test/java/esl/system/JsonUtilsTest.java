package esl.system;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.junit.Test;

public class JsonUtilsTest {

    @Test
    public void test1() {
        BasicDBObject dbo = (BasicDBObject) JSON.parse("{'a': {'b': {'c': 42, 'd': 0.8}, 'e': true}, 'f': \"some string\"}");

        try {
            System.out.println(dbo.containsField("a.b.c"));
            System.out.println(JsonUtils.contains(dbo, "a.b.c"));
        } catch(Exception e) {
            System.out.println("Got " + e.getClass() + ", message: " + e.getMessage());
        }

        System.out.println(JsonUtils.unnest(dbo, "a.b.c"));
        System.out.println(JsonUtils.unnest(dbo, "a.b.d"));
        System.out.println(JsonUtils.unnest(dbo, "a.e"));
        System.out.println(JsonUtils.unnest(dbo, "f"));
        System.out.println(JsonUtils.unnest(dbo, "a"));

        System.out.println(JsonUtils.unnest(dbo, "a.b.c", Integer.class));
        System.out.println(JsonUtils.unnest(dbo, "a.b.d", Double.class));
        System.out.println(JsonUtils.unnest(dbo, "a.e", Boolean.class));
        System.out.println(JsonUtils.unnest(dbo, "f", String.class));
        System.out.println(JsonUtils.unnest(dbo, "a", BasicDBObject.class));
    }

}
