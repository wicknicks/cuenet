package esl.cuenet.index;

import esl.cuenet.index.exceptions.RTreeOverflowException;

import java.nio.LongBuffer;

public class RTreeIndex {

    private int _bufferCount = 200;
    private int _bufferFilledTill = 0;
    private int _bufferCapacity = 512; //store 512 longs

    private LongBuffer[] buffers = null;

    public RTreeIndex() {
        buffers = new LongBuffer[_bufferCount];
        addNewBuffer();
    }

    private void addNewBuffer() {
        if (_bufferFilledTill < _bufferCount)
            buffers[_bufferFilledTill] = LongBuffer.allocate(_bufferCapacity);
        else throw new RTreeOverflowException("Exceeded Buffer Space");
        _bufferFilledTill++;
    }

    public void add() {
        LongBuffer root = buffers[0];
    }

}
