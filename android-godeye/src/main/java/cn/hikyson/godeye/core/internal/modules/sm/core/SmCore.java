package cn.hikyson.godeye.core.internal.modules.sm.core;

import android.content.Context;
import android.os.Looper;

import java.util.List;
import java.util.Map;

import cn.hikyson.godeye.core.helper.AndroidDebug;
import cn.hikyson.godeye.core.internal.modules.cpu.CpuInfo;
import cn.hikyson.godeye.core.internal.modules.memory.MemoryUtil;
import cn.hikyson.godeye.core.utils.ThreadUtil;

public final class SmCore {

    private Context mContext;
    private LooperMonitor mMonitor;
    private StackSampler stackSampler;
    private CpuSampler cpuSampler;

    private BlockListener mBlockListener;

    private long mLongBlockThresholdMillis;

    public SmCore(final Context context, long longBlockThresholdMillis, long shortBlockThresholdMillis, long dumpIntervalMillis) {
        this.mContext = context;
        this.mLongBlockThresholdMillis = longBlockThresholdMillis;
        this.stackSampler = new StackSampler(
                Looper.getMainLooper().getThread(), dumpIntervalMillis, getSampleDelay());
        this.cpuSampler = new CpuSampler(dumpIntervalMillis, getSampleDelay());
        this.mMonitor = new LooperMonitor(new LooperMonitor.BlockListener() {

            @Override
            public void onEventStart(long startTime) {
                startDump();
            }

            @Override
            public void onEventEnd(long endTime) {
                stopDump();
            }

            @Override
            public void onBlockEvent(final long blockTimeMillis, final long threadBlockTimeMillis, final boolean longBlock, final long eventStartTimeMilliis, final long eventEndTimeMillis, long longBlockThresholdMillis, long shortBlockThresholdMillis) {
                ThreadUtil.computationScheduler().scheduleDirect(() -> {
                    if (AndroidDebug.isDebugging()) {// if debugging, then ignore
                        return;
                    }
                    if (longBlock) {
                        //如果是长卡顿，那么需要记录很多信息
                        final boolean cpuBusy = cpuSampler.isCpuBusy(eventStartTimeMilliis, eventEndTimeMillis);
                        //这里短卡顿基本是dump不到数据的，因为dump延时一般都会比短卡顿时间久
                        final List<CpuInfo> cpuInfos = cpuSampler.getCpuRateInfo(eventStartTimeMilliis, eventEndTimeMillis);
                        final Map<Long, List<StackTraceElement>> threadStackEntries = stackSampler.getThreadStackEntries(eventStartTimeMilliis, eventEndTimeMillis);
                        final MemoryInfo memoryInfo = new MemoryInfo(MemoryUtil.getAppHeapInfo(), MemoryUtil.getAppPssInfo(mContext), MemoryUtil.getRamInfo(mContext));
                        LongBlockInfo blockBaseinfo = new LongBlockInfo(eventStartTimeMilliis, eventEndTimeMillis, threadBlockTimeMillis,
                                blockTimeMillis, cpuBusy, cpuInfos, threadStackEntries, memoryInfo);
                        if (mBlockListener != null) {
                            mBlockListener.onLongBlock(context, blockBaseinfo);
                        }
                    } else {
                        final MemoryInfo memoryInfo = new MemoryInfo(MemoryUtil.getAppHeapInfo(), MemoryUtil.getAppPssInfo(mContext), MemoryUtil.getRamInfo(mContext));
                        ShortBlockInfo shortBlockInfo = new ShortBlockInfo(eventStartTimeMilliis, eventEndTimeMillis, threadBlockTimeMillis,
                                blockTimeMillis, memoryInfo);
                        if (mBlockListener != null) {
                            mBlockListener.onShortBlock(context, shortBlockInfo);
                        }
                    }
                });
            }
        }, longBlockThresholdMillis, shortBlockThresholdMillis);
    }

    private void startDump() {
        if (null != stackSampler) {
            stackSampler.start();
        }
        if (null != cpuSampler) {
            cpuSampler.start();
        }
    }

    private void stopDump() {
        if (null != stackSampler) {
            stackSampler.stop();
        }
        if (null != cpuSampler) {
            cpuSampler.stop();
        }
    }

    public void setBlockListener(BlockListener blockListener) {
        mBlockListener = blockListener;
    }

    public void install() {
        ThreadUtil.createIfNotExistHandler(AbstractSampler.SM_DO_DUMP);
        Looper.getMainLooper().setMessageLogging(mMonitor);
    }

    public void uninstall() {
        Looper.getMainLooper().setMessageLogging(null);
        stopDump();
        ThreadUtil.destoryHandler(AbstractSampler.SM_DO_DUMP);
    }

    /**
     * 获取dump信息的延时时间
     * 认为1秒是长卡顿，那么延时0.9s开始dump信息
     * 换句话说，短卡顿是dump不到信息的
     *
     * @return
     */
    private long getSampleDelay() {
        return (long) (this.mLongBlockThresholdMillis * 0.9f);
    }

    public Context getContext() {
        return mContext;
    }
}
