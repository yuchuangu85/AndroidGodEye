package cn.hikyson.godeye.core.internal.modules.sm.core;

import androidx.annotation.Keep;

import java.io.Serializable;

import cn.hikyson.godeye.core.internal.modules.memory.HeapInfo;
import cn.hikyson.godeye.core.internal.modules.memory.PssInfo;
import cn.hikyson.godeye.core.internal.modules.memory.RamInfo;

/**
 * Created by kysonchao on 2017/11/22.
 */
@Keep
public class MemoryInfo implements Serializable {
    public HeapInfo heapInfo;
    public PssInfo pssInfo;
    public RamInfo ramInfo;

    public MemoryInfo(HeapInfo heapInfo, PssInfo pssInfo, RamInfo ramInfo) {
        this.heapInfo = heapInfo;
        this.pssInfo = pssInfo;
        this.ramInfo = ramInfo;
    }

    @Override
    public String toString() {
        return "MemoryInfo{" +
                "heapInfo=" + heapInfo +
                ", pssInfo=" + pssInfo +
                ", ramInfo=" + ramInfo +
                '}';
    }
}
