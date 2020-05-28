package cn.hikyson.godeye.core.internal.modules.memory;

import androidx.annotation.Keep;

import java.io.Serializable;

/**
 * Created by kysonchao on 2017/11/22.
 */
@Keep
public class RamInfo implements Serializable {
    //可用RAM
    public long availMemKb;
    //手机总RAM
    public long totalMemKb;
    //内存占用满的阀值，超过即认为低内存运行状态，可能会Kill process
    public long lowMemThresholdKb;
    //是否低内存状态运行
    public boolean isLowMemory;

    public RamInfo(long availMemKb, long totalMemKb, long lowMemThresholdKb, boolean isLowMemory) {
        this.availMemKb = availMemKb;
        this.totalMemKb = totalMemKb;
        this.lowMemThresholdKb = lowMemThresholdKb;
        this.isLowMemory = isLowMemory;
    }

    public RamInfo() {
    }

    @Override
    public String toString() {
        return "RamMemoryInfo{" +
                "availMem=" + availMemKb +
                ", totalMem=" + totalMemKb +
                ", lowMemThreshold=" + lowMemThresholdKb +
                ", isLowMemory=" + isLowMemory +
                '}';
    }
}
