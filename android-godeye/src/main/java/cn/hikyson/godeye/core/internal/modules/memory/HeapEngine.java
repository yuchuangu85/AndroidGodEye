package cn.hikyson.godeye.core.internal.modules.memory;

import java.util.concurrent.TimeUnit;

import cn.hikyson.godeye.core.internal.Engine;
import cn.hikyson.godeye.core.internal.Producer;
import cn.hikyson.godeye.core.utils.ThreadUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by kysonchao on 2017/11/23.
 */
public class HeapEngine implements Engine {
    private Producer<HeapInfo> mProducer;
    private long mIntervalMillis;
    private CompositeDisposable mCompositeDisposable;

    HeapEngine(Producer<HeapInfo> producer, long intervalMillis) {
        mProducer = producer;
        mIntervalMillis = intervalMillis;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void work() {
        mCompositeDisposable.add(Observable.interval(mIntervalMillis, TimeUnit.MILLISECONDS).map(new Function<Long, HeapInfo>() {
            @Override
            public HeapInfo apply(Long aLong) throws Exception {
                ThreadUtil.ensureWorkThread("HeapEngine apply");
                return MemoryUtil.getAppHeapInfo();
            }
        }).subscribeOn(ThreadUtil.computationScheduler())
                .observeOn(ThreadUtil.computationScheduler())
                .subscribe(new Consumer<HeapInfo>() {
                    @Override
                    public void accept(HeapInfo food) throws Exception {
                        ThreadUtil.ensureWorkThread("HeapEngine accept");
                        mProducer.produce(food);
                    }
                }));
    }

    @Override
    public void shutdown() {
        mCompositeDisposable.dispose();
    }
}
