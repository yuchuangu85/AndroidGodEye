package cn.hikyson.godeye.core.internal.modules.crash;

import java.util.List;

import cn.hikyson.godeye.core.internal.Install;
import cn.hikyson.godeye.core.internal.ProduceableSubject;
import cn.hikyson.godeye.core.utils.L;
import cn.hikyson.godeye.core.utils.ReflectUtil;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

/**
 * crash collector
 * not immediate:send crash info when crash if emergency(file store error) or crash module installed if not
 * immediate:send crash info when crash
 * <p>
 * Created by kysonchao on 2017/12/18.
 */
public class Crash extends ProduceableSubject<List<CrashInfo>> implements Install<CrashConfig> {
    private boolean mInstalled;
    private CrashConfig mConfig;

    @Override
    public synchronized boolean install(final CrashConfig crashContext) {
        if (mInstalled) {
            L.d("Crash already installed, ignore.");
            return true;
        }
        Consumer<List<CrashInfo>> consumer = this::produce;
        // auto detect crash collector provider
        try {
            ReflectUtil.invokeStaticMethodUnSafe("cn.hikyson.android.godeye.xcrash.GodEyePluginXCrash", "init",
                    new Class<?>[]{CrashConfig.class, Consumer.class}, new Object[]{crashContext, consumer});
        } catch (Exception e) {
            L.d("Crash can not be installed:", e.getLocalizedMessage());
            return false;
        }
        mConfig = crashContext;
        mInstalled = true;
        L.d("Crash installed.");
        return true;
    }

    @Override
    public synchronized void uninstall() {
        if (!mInstalled) {
            L.d("Crash already uninstalled, ignore.");
            return;
        }
        mConfig = null;
        mInstalled = false;
        L.d("Crash uninstalled.");
    }

    @Override
    public synchronized boolean isInstalled() {
        return mInstalled;
    }

    @Override
    public CrashConfig config() {
        return mConfig;
    }

    @Override
    protected Subject<List<CrashInfo>> createSubject() {
        return ReplaySubject.create();
    }
}
