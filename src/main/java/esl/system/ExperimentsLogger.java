package esl.system;

import com.mongodb.BasicDBObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ExperimentsLogger {

    private BufferedWriter writer = null;
    private int iterCount = 0;

    private ExperimentsLogger(String filename) {
        try {
            writer = new BufferedWriter(new FileWriter(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ExperimentsLogger instance = null;

    public static ExperimentsLogger getInstance(String filename) {
        instance = new ExperimentsLogger(filename);
        return instance;
    }

    public static ExperimentsLogger getInstance() {
        if (instance == null) throw new RuntimeException("ExperimentsLogger not initialized");
        return instance;
    }

    public void incrementIteration() {
        iterCount++;
        BasicDBObject msg = new BasicDBObject("iter", iterCount);
        msg.put("msg", "Incrementing Iteration Count");
        write(msg);
    }

    public void discoveryStart(String type, String name) {
        BasicDBObject msg = new BasicDBObject("discovering", type);
        msg.put("label", name);
        msg.put("msg", "iter: " + iterCount + " discovering " + name);
        write(msg);
    }

    public void recognized(String name, double confidence) {
        BasicDBObject msg = new BasicDBObject("recognizing", name);
        msg.put("conf", confidence);
        msg.put("msg", "iter: " + iterCount + " verified " + name + " with confidence " + confidence);
        write(msg);
    }

    public void list(String s) {
        try {
            writer.write(s + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(BasicDBObject msg) {
        try {
            writer.write(msg.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void voterScore(String entityID, double score) {
        BasicDBObject msg = new BasicDBObject("voting", entityID);
        msg.put("score", score);
        write(msg);
    }
}
