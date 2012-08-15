package test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class FasterIteration {

    @Test
    public void junk() {
        int sz = 100000;
        Random generator = new Random();
        ArrayList<Integer> ints = new ArrayList<Integer>();
        for (int i=0; i<sz; i++) ints.add(generator.nextInt());

        long d = System.currentTimeMillis();
        int tmp= 0;
        for (int i=0; i<sz; i++) tmp = ints.get(i) + 1;
        long diff = System.currentTimeMillis() - d;
        System.out.println("Time (Index Based): " + diff);

        d = System.currentTimeMillis();
        for (int _i: ints) _i += 1;
        diff = System.currentTimeMillis() - d;
        System.out.println("Time (Iterator Based): " + diff);

    }

}
