package cn.hikyson.godeye.sample;

import java.util.ArrayList;
import java.util.List;

import cn.hikyson.godeye.core.monitor.AppInfoConext;
import cn.hikyson.godeye.core.monitor.AppInfoLabel;

/**
 * Created by kysonchao on 2017/12/9.
 */
public class AppInfoProxyImpl implements AppInfoConext {

    @Override
    public List<AppInfoLabel> getAppInfo() {
        List<AppInfoLabel> appInfoLabels = new ArrayList<>();
        appInfoLabels.add(new AppInfoLabel("Email", "kysonchao@gmail.com", "mailto:kysonchao@gmail.com"));
        appInfoLabels.add(new AppInfoLabel("ProjectUrl", "https://github.com/Kyson/AndroidGodEye", "https://github.com/Kyson/AndroidGodEye"));
        appInfoLabels.add(new AppInfoLabel("Blog", "tech.hikyson.cn", "https://tech.hikyson.cn"));
        return appInfoLabels;
    }
}
