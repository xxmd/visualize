package io.github.xxmd;

import java.util.Date;

public class LoadFrameTask {
    public Date date;
    public Runnable task;

    public LoadFrameTask(Runnable task) {
        date = new Date();
        this.task = task;
    }

}
