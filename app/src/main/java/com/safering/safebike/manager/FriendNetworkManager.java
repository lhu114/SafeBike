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
public class FriendNetworkManager {
    public static FriendNetworkManager instance;
    public static FriendNetworkManager getInstance(){
        if(instance == null){
            instance = new FriendNetworkManager();
        }
        return instance;
    }

    AsyncHttpClient client;
    Gson gson;

    private FriendNetworkManager() {
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


    public void cancelAll(Context context) {
        client.cancelRequests(context, true);

    }

    public void getUserFriends(Context context,int kind, int page, int count, final OnResultListener listener){
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일
        //결과값 : JSON(친구아이디,이메일,사진)
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

    public void addUserFriend(Context context,int kind, int page, int count, final OnResultListener listener){
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,친구 이메일
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
                //json이나 array로 받아서 파싱할것
                listener.onSuccess(success);
                //리스너에 던져주고 구현하는 클래스가 데이터 사용하기
            }
        });

    }

    public void getUserFriendAddress(Context context,int kind, int page, int count, final OnResultListener listener){
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,전화번호리스트(Array)
        //결과값 : JSON(친구아이디,이메일,사진)
        client.get(context, LOGIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                int fail = -1;
                listener.onFail(fail);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                int success = 1;
                //json이나 array로 받아서 파싱할것
                listener.onSuccess(success);
                //리스너에 던져주고 구현하는 클래스가 데이터 사용하기
            }
        });

    }

    public void getUserFriendDirect(Context context,int kind, int page, int count, final OnResultListener listener){
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,입력이메일값
        //결과값 : JSON(친구아이디,이메일,사진)
        client.get(context, LOGIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                int fail = -1;
                listener.onFail(fail);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                int success = 1;
                //json이나 array로 받아서 파싱할것
                listener.onSuccess(success);
                //리스너에 던져주고 구현하는 클래스가 데이터 사용하기
            }
        });

    }

    public void getFriendProfile(Context context,int kind, int page, int count, final OnResultListener listener){
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,친구이메일,구분값(친구추가삭제 메소드랑 구분)
        //결과값 : JSON(친구아이디,이메일,가입일,활동량(칼로리,속력,거리))
        client.get(context, LOGIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                int fail = -1;
                listener.onFail(fail);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                int success = 1;
                //json이나 array로 받아서 파싱할것
                listener.onSuccess(success);
                //리스너에 던져주고 구현하는 클래스가 데이터 사용하기
            }
        });

    }




}
