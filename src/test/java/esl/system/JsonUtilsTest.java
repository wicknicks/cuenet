package esl.system;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class JsonUtilsTest {

    @Test
    public void test1() {
        BasicDBObject dbo = (BasicDBObject) JSON.parse("{'a': {'b': {'c': 42, 'd': 0.8}, 'e': true}, 'f': \"some string\"}");

        try {
            assertEquals(false, dbo.containsField("a.b.c"));
            assertEquals(true, JsonUtils.contains(dbo, "a.b.c"));
        } catch(Exception e) {
            System.out.println("Got " + e.getClass() + ", message: " + e.getMessage());
        }


        assertEquals(42, JsonUtils.unnest(dbo, "a.b.c"));
        assertEquals(0.8D, (Double)JsonUtils.unnest(dbo, "a.b.d"), 0.001);
        assertEquals(true, JsonUtils.unnest(dbo, "a.e"));
        assertEquals("some string", JsonUtils.unnest(dbo, "f"));
        assertEquals("{ \"b\" : { \"c\" : 42 , \"d\" : 0.8} , \"e\" : true}", (JsonUtils.unnest(dbo, "a")).toString());
        assertEquals(null, JsonUtils.unnest(dbo, "a.x.y.z"));

        assertEquals(42, (int)JsonUtils.unnest(dbo, "a.b.c", Integer.class));
        assertEquals(0.8, JsonUtils.unnest(dbo, "a.b.d", Double.class), 0.001);
        assertEquals(true, JsonUtils.unnest(dbo, "a.e", Boolean.class));
        assertEquals("some string", JsonUtils.unnest(dbo, "f", String.class));
        assertEquals("{ \"b\" : { \"c\" : 42 , \"d\" : 0.8} , \"e\" : true}", JsonUtils.unnest(dbo, "a", BasicDBObject.class).toString());
        assertEquals(null, JsonUtils.unnest(dbo, "a.x.y.z", BasicDBObject.class));
    }

}
