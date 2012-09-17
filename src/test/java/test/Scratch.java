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
            //VLDB CONFERENCE
            //singleFileTest("DSC_0406.JPG");

            //TURING AWARD PHOTOS
            /////singleFileTest("17164616e1c011e18be00019b92f7b9d");
            //singleFileTest("17160c5ae1c011e18be00019b92f7b9d");
            //singleFileTest("17165da4e1c011e18be00019b92f7b9d");
            //singleFileTest("17161f60e1c011e18be00019b92f7b9d");
            //singleFileTest("17169378e1c011e18be00019b92f7b9d");
            //singleFileTest("1715a03ae1c011e18be00019b92f7b9d");

            //SETAREH PARTY PHOTOS
            singleFileTest("af948a5ae64411e195ea0019b92f7b9d");
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }

    public void singleFileTest(String photo) throws IOException, ParseException, EventGraphException {
        //ExperimentsLogger el = ExperimentsLogger.getInstance("/home/arjun/Dataset/logs/jain/turing/" + photo + ".log");
        ExperimentsLogger el = ExperimentsLogger.getInstance("/home/arjun/Dataset/logs/setareh/party/" + photo + ".log");

        //File file = new File("/home/arjun/Dataset/vldb/" + photo);
        //File file = new File("/data/test_photos/jain/c1/" + photo);
        File file = new File("/data/test_photos/setareh/" + photo);
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();

        long st = System.currentTimeMillis();

        firstKDiscoverer.execute(new LocalFileDataset(file));

        long et = System.currentTimeMillis();
        el.list("duration = " + (et-st));

        el.close();
    }

}
