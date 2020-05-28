package cn.hikyson.godeye.monitor.server;

import android.text.TextUtils;

import androidx.collection.ArrayMap;

import com.koushikdutta.async.http.WebSocket;

import org.json.JSONObject;

import java.util.Map;

public class WebSocketBizRouter implements WebSocketProcessor {
    private Map<String, WebSocketProcessor> mRouterMap;

    WebSocketBizRouter() {
        mRouterMap = new ArrayMap<>();
        mRouterMap.put("clientOnline", new WebSocketClientOnlineProcessor());
        mRouterMap.put("appInfo", new WebSocketAppinfoProcessor());
        mRouterMap.put("methodCanary", new WebSocketMethodCanaryProcessor());
        mRouterMap.put("reinstallBlock", new WebSocketChangeBlockConfigProcessor());
    }

    @Override
    public void process(WebSocket webSocket, JSONObject msgJSONObject) throws Exception {
        if (msgJSONObject == null) {
            throw new UnhandledException("msgJSONObject == null");
        }
        final String moduleName = msgJSONObject.optString("moduleName");
        if (TextUtils.isEmpty(moduleName)) {
            throw new UnhandledException("TextUtils.isEmpty(moduleName)");
        }
        WebSocketProcessor webSocketProcessor = mRouterMap.get(moduleName);
        if (webSocketProcessor == null) {
            throw new UnhandledException("can not find module to process [" + moduleName + "]");
        }
        webSocketProcessor.process(webSocket, msgJSONObject);
    }
}
