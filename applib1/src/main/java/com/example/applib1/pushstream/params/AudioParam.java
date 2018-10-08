package com.example.applib1.pushstream.params;

/**
 * Created by lh on 2018/9/30.
 */

public class AudioParam {

    //采样率
    private int sampleRateInHz = 44100;
    //声道个数
    private int channel = 1;

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
