package test;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.cuenet.algorithms.firstk.impl.FirstKDiscoverer;
import esl.cuenet.algorithms.firstk.impl.LocalFileDataset;
import esl.cuenet.mapper.parser.ParseException;
import esl.system.ExceptionHandler;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Scratch extends TestBase {

    private Logger logger = Logger.getLogger(Scratch.class);
    private ExceptionHandler exceptionHandler = new ExceptionHandler(ExceptionHandler.DEBUG);

//    @Test
    public void dateTest() throws ParseException {
//
//        long start = 1251173399000L;
//        long end =  1251173399000L;
//        BasicDBObject o = (BasicDBObject) JSON.parse(String.format("{\"start-date\" : { \"$lt\" : %d} , \"end-date\" : { \"$gt\" : %d}}", start, end));
//        logger.info(o.toMap());

//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date f = sdf.parse("2009-08-24 00:00:00");
//        logger.info(f.getTime());
//        f = sdf.parse("2009-08-28 00:00:00");
//        logger.info(f.getTime());
//
//        logger.info((1251442800000L > 1251173399000L) + " " + (1251173399000L > 1251097200000L));
//
//        Date d = new Date(1248470621238L);
//        logger.info( (d.getYear()+1900) + " " + (d.getMonth()+1) + " "+ d.getDate() + "   " + d.getHours() + " " + d.getMinutes() + " " + d.getSeconds());
    }

//    @Test
//    public void sa() throws java.text.ParseException { //
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
//        Date f = sdf.parse("2009:08:27 18:00:00");  //1251421200000
//        logger.info(f.getTime());
//        f = sdf.parse("2009:08:27 23:59:59");  //1251442799000
//        logger.info(f.getTime());
//    }

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
        //File file = new File("/home/arjun/Dataset/ramesh/confs/DSCN4265.JPG");
        File file = new File("/home/arjun/Dataset/vldb/DSC_0470.JPG");
        FirstKDiscoverer firstKDiscoverer = new FirstKDiscoverer();
        firstKDiscoverer.execute(new LocalFileDataset(file));
    }

}
