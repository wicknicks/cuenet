package esl.cuenet.algorithms.firstk.personal;

public class Time {

    private final long end;
    private final long start;

    protected Time(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public static Time createFromMoment(long tstamp) {
        return new Time(tstamp, tstamp);
    }

    public static Time createFromInterval(long start, long end) {
        return new Time(start, end);
    }

    /**
     * is this before OTHER
     */
    public boolean isBefore(Time other) {
        return this.end < other.start;
    }

    /**
     * does this meet other OR does other meet this
     */
    public boolean meets(Time other) {
        return (this.start == other.end) || (this.end == other.start);
    }

    /**
     * do this and other start at the same time
     */
    public boolean starts(Time other) {
        return (this.start == other.start);
    }

    /**
     * do this and other finish at the same time
     */
    public boolean finish(Time other) {
        return (this.end == other.end);
    }

    /**
     * does this overlap with other or other overlap with this
     */
    public boolean overlaps(Time other) {
        return (this.end > other.start && other.end > this.start);
    }

    /**
     * does this contain other
     */
    public boolean contains(Time other) {
        return (this.start < other.start && this.end > other.end);
    }

    /**
     * are this and other cotemporal?
     */
    public boolean cotemporal(Time other) {
        return (this.start == other.start && this.end == other.end);
    }

    public String toString() {
        return "[" + start + " " + end + "]";
    }

}
