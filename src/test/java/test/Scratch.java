package test;

import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.impl.FirstKDiscoverer;
import esl.cuenet.algorithms.firstk.impl.LocalFileDataset;
import esl.cuenet.mapper.parser.ParseException;
import esl.system.ExceptionHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
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
            singleFileTest();
        } catch (Exception e) {
            exceptionHandler.handle(e);
        }
    }

    public void singleFileTest() throws IOException, ParseException, EventGraphException {
        File file = new File("/home/arjun/Dataset/vldb/DSC_0470.JPG");
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();
        firstKDiscoverer.execute(new LocalFileDataset(file));
    }

}
