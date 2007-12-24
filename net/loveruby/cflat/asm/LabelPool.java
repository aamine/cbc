package net.loveruby.cflat.asm;

public class LabelPool {
    long seq;

    public LabelPool() {
        seq = 0;
    }

    public Label newLabel() {
        seq++;
        return new Label(seq, ".L" + seq);
    }
}
