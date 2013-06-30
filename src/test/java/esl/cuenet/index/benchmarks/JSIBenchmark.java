package esl.cuenet.index.benchmarks;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import esl.system.SysLoggerUtils;
import gnu.trove.TIntProcedure;
import org.junit.Test;

import java.util.Random;


public class JSIBenchmark {

    static {
        SysLoggerUtils.initLogger();
    }

    class Interval {
        int start;
        int end;
    }

    @Test
    public void retrieveTest() {
        SpatialIndex si = new RTree();
        si.init(null);

        int lim = 10;
        for (int i=0; i < lim; i++) {
            Rectangle r = new Rectangle(1+i, 1, 22-i, 2);
            si.add(r, i);
            System.out.println("Inserting " + r + " " + i);
        }

        Rectangle query = new Rectangle((float) 7.5, 1, (float)11.5, 2);

        TIntProcedure proc = new TIntProcedure() {
            @Override
            public boolean execute(int i) {
                System.out.println(i);
                return true;
            }
        };

        si.intersects(query, proc);
    }

    public long insertbench(int intervalCount) {
        // Create and initialize an rtree
        SpatialIndex si = new RTree();
        si.init(null);

        int intervalStart = 0, intervalEnd = 1000000;
        Interval[] intervalStore = new Interval[intervalCount];

        Random generator = new Random();
        int span;
        for (int i=0; i<intervalCount; i++) {
            intervalStore[i] = new Interval();
            intervalStore[i].start = intervalStart +  generator.nextInt(intervalEnd);
            span = (int) (1000*generator.nextGaussian());
            span = Math.abs(span);
            intervalStore[i].end = intervalStore[i].start + span;
            if (intervalStore[i].end > intervalEnd) intervalStore[i].end = intervalEnd;
        }

        long startTime = System.currentTimeMillis();

        for (int i=0; i<intervalCount; i++)
            si.add(new Rectangle(intervalStore[i].start, 1, intervalStore[i].end, 2), i);

        long diff = (System.currentTimeMillis() - startTime);
        System.out.println("Time taken to add " + intervalCount + " intervals " + diff);

        return diff;
    }

    public void findbench(int intervalCount, int queries) {

        Random generator = new Random();

        SpatialIndex si = new RTree();
        si.init(null);

        int intervalStart = 0, intervalEnd = 10000000;
        Interval[] intervalStore = new Interval[intervalCount];

        int span = (intervalEnd- intervalStart)/intervalCount;
        for (int i=0; i<intervalCount; i++) {
            intervalStore[i] = new Interval();
            intervalStore[i].start = i * span;
            intervalStore[i].end = (i+1) * span;
        }

        for (int i=0; i<intervalCount; i++)
            si.add(new Rectangle(intervalStore[i].start, 1, intervalStore[i].end, 2), i);

        System.out.println("Insertion Complete (" + intervalCount + ")");

        long start = System.currentTimeMillis();
        int s, e;
        final int[] count = {0};

        Rectangle query = new Rectangle();
        for (int i=0; i<queries; i++) {
            s = generator.nextInt(intervalEnd);
            e = generator.nextInt(intervalEnd);
            if (e > s) query.set(s, 1, e, 2);
            else query.set(e, 1, s, 2);

            count[0] = 0;
            si.intersects(query, new TIntProcedure() {
                @Override
                public boolean execute(int i) {
                    count[0]++;
                    if (count[0] > 100) return false;
                    return true;
                }
            });

            //System.out.println(Math.abs(e-s) + " " + count[0] * span);
        }

        long diff = System.currentTimeMillis() - start;
        System.out.println("Time taken to query: " + diff);
        System.out.println("Average time for one query: " + (double) diff/queries);
        System.out.println();
    }

    @Test
    public void insertbench() {
        insertbench(1000);
        insertbench(10000);
        insertbench(100000);
        insertbench(1000000);
        insertbench(10000000);
    }

    @Test
    public void findbench() {
        findbench(10000000, 10000);
        findbench(1000, 100);
        findbench(10000, 1000);
        findbench(100000, 10000);
        findbench(1000000, 10000);
    }
}
