package com.safering.safebike.manager;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.safering.safebike.property.MyApplication;

import org.apache.http.Header;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by Tacademy on 2015-11-04.
 */
public class AccountNetworkManager {
    public static AccountNetworkManager instance;
    public static AccountNetworkManager getInstance(){
        if(instance == null){
            instance = new AccountNetworkManager();
        }
        return instance;
    }

    AsyncHttpClient client;
    Gson gson;

    private AccountNetworkManager() {
        try {
            KeyStore trueStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trueStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trueStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client = new AsyncHttpClient();
            client.setSSLSocketFactory(socketFactory);
            client.setCookieStore(new PersistentCookieStore(MyApplication.getContext()));

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        gson = new Gson();
        client.setCookieStore(new PersistentCookieStore(MyApplication.getContext()));
    }

    public interface OnResultListener{
        public void onSuccess(int result);
        public void onFail(int code);
    }

    private static final String SERVER = "http:...";//서버 주소
    private static final String LOGIN_URL = "http:...";//서버 URL

    public void saveUserProfile(Context context, int page, int count, final OnResultListener listener) {
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,이름,비밀번호
        //결과값 : INT

        client.get(context, LOGIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                int fail = -1;
                listener.onFail(fail);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                int success = 1;
                listener.onSuccess(success);
            }
        });
    }

    public void saveUserImage(Context context, int page, int count, final OnResultListener listener) {
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,사진파일,
        //결과값 : INT
        client.get(context, LOGIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                int fail = -1;
                listener.onFail(fail);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                int success = 1;
                listener.onSuccess(success);
            }
        });
    }

    public void cancelAll(Context context) {
        client.cancelRequests(context, true);

    }
}
