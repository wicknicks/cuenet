package test;

import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.impl.FirstKDiscoverer;
import esl.cuenet.algorithms.firstk.impl.LocalFileDataset;
import esl.cuenet.mapper.parser.ParseException;
import esl.system.ExceptionHandler;
import esl.system.ExperimentsLogger;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Scratch extends TestBase {

    private Logger logger = Logger.getLogger(Scratch.class);
    private ExceptionHandler exceptionHandler = new ExceptionHandler(ExceptionHandler.DEBUG);

    public static void main(String... args) {
//        System.out.print("Type in something to start program.... ");
//        Scanner scanner = new Scanner(System.in);
//        scanner.nextLine();
        (new Scratch()).doSingleFileTest();
    }

    public void doSingleFileTest() {
        try {
            //singleFileTest("DSC_0406.JPG");
            singleFileTest("17164616e1c011e18be00019b92f7b9d");
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }

    public void singleFileTest(String photo) throws IOException, ParseException, EventGraphException {
        ExperimentsLogger el = ExperimentsLogger.getInstance("/home/arjun/Dataset/logs/jain/turing/" + photo + ".log");

        //File file = new File("/home/arjun/Dataset/vldb/" + photo);
        File file = new File("/data/test_photos/jain/c1/" + photo);
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();

        long st = System.currentTimeMillis();

        firstKDiscoverer.execute(new LocalFileDataset(file));

        long et = System.currentTimeMillis();
        el.list("duration = " + (et-st));

        el.close();
    }

}
