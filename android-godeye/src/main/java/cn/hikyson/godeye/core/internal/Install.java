package cn.hikyson.godeye.core.internal;

/**
 * Created by kysonchao on 2017/11/24.
 */
public interface Install<T> {
    boolean install(T config);

    void uninstall();

    boolean isInstalled();

    T config();
}
