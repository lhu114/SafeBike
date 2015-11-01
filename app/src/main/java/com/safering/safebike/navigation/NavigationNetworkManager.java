package com.safering.safebike.navigation;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.safering.safebike.property.MyApplication;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by lhu on 2015-10-31.
 */
public class NavigationNetworkManager {
    private static NavigationNetworkManager instance;

    private static final String KEY_HEADERS_ACCEPT = "Accept";
    private static final String KEY_HEADERS_APPKEY = "appKey";

    private static final String VALUE_HEADERS_ACCEPT = "application/json";
    private static final String VALUE_HEADERS_APPKEY = "fae4be30-90e4-3c96-b227-0086b07ae5e1";

    AsyncHttpClient client;
    Gson gson;
    Header[] headers;

    private NavigationNetworkManager() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client = new AsyncHttpClient();
            client.setSSLSocketFactory(socketFactory);
            client.setCookieStore(new PersistentCookieStore(MyApplication.getContext()));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }

        gson = new Gson();
        client.setCookieStore(new PersistentCookieStore(MyApplication.getContext()));

        headers = new Header[2];
        headers[0] = new BasicHeader(KEY_HEADERS_ACCEPT, VALUE_HEADERS_ACCEPT);
        headers[1] = new BasicHeader(KEY_HEADERS_APPKEY, VALUE_HEADERS_APPKEY);
    }

    public static NavigationNetworkManager getInstance() {
        if (instance == null) {
            instance = new NavigationNetworkManager();
        }

        return instance;
    }

    public HttpClient getHttpClient() {
        return client.getHttpClient();
    }

    public interface OnResultListener<T> {
        public void onSuccess(T result);
        public void onFail(int code);
    }

    public void cancelAll(Context context) {
        client.cancelRequests(context, true);
    }

    public static final String SEARCH_POI_URL = "https://apis.skplanetx.com/tmap/pois";

    private static final String KEY_POI_VERSION = "version";
    private static final String KEY_POI_SEARCHKEYWORD = "searchKeyword";
    private static final String KEY_POI_RESCOORDTYPE = "resCoordType";

    private static final int VALUE_POI_VERSION = 1;
    private static final String VALUE_POI_RESCOORDTYPE = "WGS84GEO";

    public void searchPOI(Context context, String keyword, final OnResultListener<SearchPOIInfo> listener) {
        RequestParams params = new RequestParams();
        params.put(KEY_POI_VERSION, VALUE_POI_VERSION);
        params.put(KEY_POI_SEARCHKEYWORD, keyword);
        params.put(KEY_POI_RESCOORDTYPE, VALUE_POI_RESCOORDTYPE);

        client.get(context, SEARCH_POI_URL, headers, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                /*
                 * fail에 따른 statusCode 처리
                 */
                listener.onFail(statusCode);

                String code = Integer.toString(statusCode);
                Log.d("safebike", code);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                /*
                 * success에 따른 statusCode 처리
                 */
                SearchPOIInfoResult result = gson.fromJson(responseString, SearchPOIInfoResult.class);
                listener.onSuccess(result.searchPoiInfo);

                String code = Integer.toString(statusCode);
                Log.d("safebike", code);
            }
        });
    }
}
