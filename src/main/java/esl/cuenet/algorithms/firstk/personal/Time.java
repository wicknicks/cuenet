package esl.cuenet.algorithms.firstk.personal;

public class Time {

    private final long end;
    private final long start;

    public final boolean isMoment;

    protected Time(long start, long end) {
        isMoment = (start == end);
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public static Time createFromMoment(long tstamp) {
        return new Time(tstamp, tstamp);
    }

    public static Time createFromInterval(long start, long end) {
        return new Time(start, end);
    }

    public boolean isMoment() {
        return isMoment;
    }

    public Time subtract(long milliseconds) {
        if (milliseconds > start || milliseconds > end) throw new RuntimeException("milliseconds > start|end");
        return new Time(this.start - milliseconds, this.end - milliseconds);
    }

    public Time add(long milliseconds) {
        return new Time(this.start + milliseconds, this.end + milliseconds);
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
