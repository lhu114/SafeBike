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
public class ExerciseNetworkManager {
    private static ExerciseNetworkManager instance;
    public static ExerciseNetworkManager getInstance(){
        if(instance == null){
            instance = new ExerciseNetworkManager();
        }
        return instance;
    }

    AsyncHttpClient client;
    Gson gson;

    private ExerciseNetworkManager() {
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

    /**
     * 운동 기록을 가져온다...칼로리 속력 거리
     * */
    public void getExerciseRecord(Context context,int kind, int page, int count, final OnResultListener listener){
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,종류,시작날짜,개수
        //결과값 : JSON(종류에 대한 값들)

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

    public void getDayExerciseRecord(Context context,int kind, int page, int count, final OnResultListener listener){
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,종류,날짜
        //결과값 : JSON(칼로리,속력,거리)
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


    public void cancelAll(Context context) {
        client.cancelRequests(context, true);

    }


}
