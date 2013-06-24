package esl.cuenet.index.benchmarks;

import esl.system.SysLoggerUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

public class NIOBenchmarks {

    static {
        SysLoggerUtils.initLogger();
    }

    Logger logger = Logger.getLogger(NIOBenchmarks.class);

    @Test
    public void writeBytes() throws Exception {
        File temp1 = File.createTempFile("byteArrayWriteTest1", null);
        File temp2 = File.createTempFile("byteArrayWriteTest2", null);
        File temp3 = File.createTempFile("byteArrayWriteTest3", null);

        logger.info(temp1.getAbsolutePath());

        temp1.deleteOnExit();
        temp2.deleteOnExit();
        temp3.deleteOnExit();


        FileOutputStream stream = new FileOutputStream(temp1);


        int N = 4096;
        Random generator = new Random();
        byte[] bytes = new byte[N];
        generator.nextBytes(bytes);

        long _start_time = System.currentTimeMillis();

        for (int i=0; i<1000; i++) {
            //byte[] bytes = new byte[N];
            //generator.nextBytes(bytes);
            stream.write(bytes);
            stream.flush();
        }

        stream.close();

        logger.info("Time to save Byte Array (1000):  " + (System.currentTimeMillis() - _start_time));

        stream = new FileOutputStream(temp2);

        _start_time = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            //byte[] bytes = new byte[N];
            //generator.nextBytes(bytes);
            stream.write(bytes);
            stream.flush();
        }

        stream.close();

        logger.info("Time to save Byte Array (10000): " + (System.currentTimeMillis() - _start_time));


        stream = new FileOutputStream(temp3);

        _start_time = System.currentTimeMillis();
        for (int i=0; i<100000; i++) {
            //byte[] bytes = new byte[N];
            //generator.nextBytes(bytes);
            stream.write(bytes);
            stream.flush();
        }

        stream.close();

        logger.info("Time to save Byte Array (100000): " + (System.currentTimeMillis() - _start_time));
    }

    @Test
    public void writeByteBuffers() throws Exception {

        File temp1 = File.createTempFile("ByteBuffersWriteTest1", null);
        File temp2 = File.createTempFile("ByteBuffersWriteTest2", null);
        File temp3 = File.createTempFile("ByteBuffersWriteTest3", null);

        logger.info(temp1.getAbsolutePath());

        int N = 4096;
        Random generator = new Random();
        byte[] bytes = new byte[N];
        generator.nextBytes(bytes);

        FileOutputStream stream = new FileOutputStream(temp1);
        FileChannel channel = stream.getChannel();

        long _start_time = System.currentTimeMillis();

        for (int i=0; i<1000; i++) {

            ByteBuffer buffer = ByteBuffer.allocate(N);
            buffer.put(bytes);
            //for (int j=0; j<N; j++) buffer.put(j, (byte)generator.nextInt());
            channel.write(buffer);
        }
        stream.close();

        logger.info("Time to save ByteBuffer (1000):  " + (System.currentTimeMillis() - _start_time));

        stream = new FileOutputStream(temp2);
        channel = stream.getChannel();

        _start_time = System.currentTimeMillis();

        for (int i=0; i<10000; i++) {

            ByteBuffer buffer = ByteBuffer.allocate(N);
            buffer.put(bytes);
            //for (int j=0; j<N; j++) buffer.put(j, (byte)generator.nextInt());
            channel.write(buffer);
        }
        stream.close();

        logger.info("Time to save ByteBuffer (10000):  " + (System.currentTimeMillis() - _start_time));

        stream = new FileOutputStream(temp3);
        channel = stream.getChannel();

        _start_time = System.currentTimeMillis();

        for (int i=0; i<100000; i++) {

            ByteBuffer buffer = ByteBuffer.allocate(N);
            buffer.put(bytes);
            //for (int j=0; j<N; j++) buffer.put(j, (byte)generator.nextInt());
            channel.write(buffer);
        }
        stream.close();

        logger.info("Time to save ByteBuffer (100000):  " + (System.currentTimeMillis() - _start_time));

        temp1.deleteOnExit();
        temp2.deleteOnExit();
        temp3.deleteOnExit();
    }

    @Test
    public void writeLongBuffers() throws Exception {

        int N = 4096;
        Random generator = new Random();

        LongBuffer buffer = LongBuffer.allocate(N);
        for (int i=0; i<N; i++) {
            buffer.put(i, generator.nextLong());
        }

        long _start_time = System.currentTimeMillis();

        int r=0;
        int w=0;
        long p;
        for (int j=0; j<10*1000*1000; j++) {
            if (generator.nextBoolean()) {
                p = buffer.get(generator.nextInt(N));
                r++;
            }
            else {
                buffer.put(generator.nextInt(N), generator.nextLong());
                w++;
            }
        }

        logger.info("LongBuffer " + (System.currentTimeMillis() - _start_time) + "ms for " + r + " reads, " + w + " writes.");

        long[] numbers = new long[N];
        for (int i=0; i<N; i++) numbers[i] = generator.nextLong();


        for (int j=0; j<10*1000*1000; j++) {
            if (generator.nextBoolean()) {
                p = numbers[generator.nextInt(N)];
                r++;
            }
            else {
                numbers[generator.nextInt(N)] = generator.nextLong();
                w++;
            }
        }

        logger.info("Long Array " + (System.currentTimeMillis() - _start_time) + "ms for " + r + " reads, " + w + " writes.");

    }

    @Test
    public void writeDoubleBuffers() throws Exception {

        int N = 4096;
        Random generator = new Random();

        DoubleBuffer buffer = DoubleBuffer.allocate(N);
        for (int i=0; i<N; i++) {
            buffer.put(i, generator.nextInt(360) * generator.nextDouble() - 180);
        }

        long _start_time = System.currentTimeMillis();

        int r=0;
        int w=0;
        double p;
        for (int j=0; j<10*1000*1000; j++) {
            if (generator.nextBoolean()) {
                p = buffer.get(generator.nextInt(N));
                r++;
            }
            else {
                buffer.put(generator.nextInt(N), generator.nextInt(360) * generator.nextDouble() - 180);
                w++;
            }
        }

        logger.info("DoubleBuffer " + (System.currentTimeMillis() - _start_time) + "ms for " + r + " reads, " + w + " writes.");

        double[] numbers = new double[N];
        for (int i=0; i<N; i++) numbers[i] = generator.nextInt(360) * generator.nextDouble() - 180;


        for (int j=0; j<10*1000*1000; j++) {
            if (generator.nextBoolean()) {
                p = numbers[generator.nextInt(N)];
                r++;
            }
            else {
                numbers[generator.nextInt(N)] = generator.nextInt(360) * generator.nextDouble() - 180;
                w++;
            }
        }

        logger.info("Double Array " + (System.currentTimeMillis() - _start_time) + "ms for " + r + " reads, " + w + " writes.");

    }

}
