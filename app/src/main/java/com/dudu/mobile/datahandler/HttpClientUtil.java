package com.dudu.mobile.datahandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * HttpClient工具类
 * 提供通过HttpClient访问网络的各种方法
 */
public class HttpClientUtil {

    /**
     * 通用异常信息
     */
    private final static String ERROR = "出现异常";

    /**
     * HttpClient对象
     */
    private static DefaultHttpClient httpclient;

    /**
     * HttpContext
     */
    private static HttpContext context;

    /** 初始化HttpClient的各个配置项 */
    static {

        HttpParams params = new BasicHttpParams();

        // 设置允许链接的做多链接数目
        ConnManagerParams.setMaxTotalConnections(params, 10);
        // 设置超时时间.
        ConnManagerParams.setTimeout(params, 2000);
        // 连接超时
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        // 请求超时
        HttpConnectionParams.setSoTimeout(params, 30000);

        // 设置每个路由的最多链接数量是20
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);

        // 设置链接使用的版本
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        // 设置链接使用的内容的编码
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        httpclient = new DefaultHttpClient(cm, params);

        context = new BasicHttpContext();
    }

    public static String getUrl(String baseUrl, String json) {
        try {
            if (json != null && !"".equals(json)) {
                JSONObject jsonObject = new JSONObject(json);
                Iterator keys = jsonObject.keys();
                boolean first = true;
                String key;
                while (keys.hasNext()) {
                    key = keys.next().toString();
                    if (first) {
                        baseUrl += "?" + key + "=" + jsonObject.get(key);
                        first = false;
                    } else {
                        baseUrl += "&" + key + "=" + jsonObject.get(key);
                    }
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return baseUrl;
    }

    public static ResultWebapi PostExecute(String url, String paramJson) {

        ResultWebapi resultWebapi = new ResultWebapi();
        try {
            // 设置URL
            HttpPost httpost = new HttpPost(url);
            httpost.setEntity(new StringEntity(paramJson));
            HttpResponse response = httpclient.execute(httpost, context);

            // 从response中获取http响应实体
            HttpEntity entity = response.getEntity();

            // 数据量较少时，服务器不会进行压缩，直接解析
            String jsonStr = EntityUtils.toString(entity);

            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has("Code")) {
                int code = jsonObject.getInt("Code");
                resultWebapi.setRetCode(code);
                if (code != 0 && jsonObject.has("Desc")) {
                    resultWebapi.setDescription(jsonObject.getString("Desc"));
                }
                resultWebapi.setRetObject(jsonStr);
            } else {
                resultWebapi.setRetCode(-1);
                resultWebapi.setDescription("接口异常!");
            }
        } catch (Exception e) {
            resultWebapi.setRetCode(-1);
            resultWebapi.setDescription("接口异常!");
        }

        return resultWebapi;
    }

    public static ResultWebapi GetExecute(String url) {

        ResultWebapi resultWebapi = new ResultWebapi();
        try {
            // 设置URL
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget, context);

            // 从response中获取http响应实体
            HttpEntity entity = response.getEntity();

            // 数据量较少时，服务器不会进行压缩，直接解析
            String jsonStr = EntityUtils.toString(entity);

            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has("code")) {
                int code = jsonObject.getInt("code");
                resultWebapi.setRetCode(code);
                if (code != 0 && jsonObject.has("desc")) {
                    resultWebapi.setDescription(jsonObject.getString("desc"));
                }
                if(jsonObject.has("returnobject")){
                    resultWebapi.setRetObject(jsonObject.getString("returnobject"));
                }
            } else {
                resultWebapi.setRetCode(-1);
                resultWebapi.setDescription("接口异常!");
            }
        } catch (Exception e) {
            resultWebapi.setRetCode(-1);
            resultWebapi.setDescription("接口异常!");
        }

        return resultWebapi;
    }
}
