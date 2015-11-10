package com.safering.safebike.manager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.safering.safebike.exercisereport.ExcerciseResult;
import com.safering.safebike.exercisereport.ExerciseDayResult;
import com.safering.safebike.friend.FriendResult;
import com.safering.safebike.friend.FriendSearchResult;
import com.safering.safebike.login.LoginResult;
import com.safering.safebike.navigation.SearchPOIInfo;
import com.safering.safebike.navigation.SearchPOIInfoResult;
import com.safering.safebike.property.MyApplication;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

/**
 * Created by Tacademy on 2015-11-05.
 */
public class NetworkManager {

    private static final String SERVER = "http:...";

    /**
     * 계정
     */
    private static final String ACCOUNT_PROFILE_URL = "http:...";//서버 URL
    private static final String ACCOUNT_IMAGE_URL = "http:...";//서버 URL
    private static final String JOIN_DATE = "JOIN_DATE";
    private static final String USER_EAMIL = "uemail";
    private static final String USER_ID = "USER_ID";
    private static final String USER_PASSWORD = "USER_PASSWORD";
    private static final String USER_IMAGE = "USER_IMAGE";

    /**
     * 운동
     */
    private static final String EXCERCISE_URL = "http://52.69.133.212:3000/workout/list";//서버 URL
    private static final String EXCERCISE_DAY_URL = "http://52.69.133.212:3000/workout/";//서버 URL
    private static final String EXERCISE_REQUEST_TYPE = "EXERCISE_TYPE";
    private static final String EXCERCISE_REQUEST_DATE = "date";
    private static final String EXCERCISE_REQUEST_NUMBER = "REQUEST_NUMBER";

    /**
     * 친구
     */
    private static final String FRIEND_URL = "http://52.69.133.212:3000/user/friend";//서버 URL
    private static final String FRIEND_ADDRESS_URL = "http:...";//서버 URL
    private static final String FRIEND_ADD_URL = "http:...";//서버 URL
    private static final String FRIEND_REMOVE_URL = "http:...";//서버 URL
    private static final String FRIEND_DIRECT_URL = "http:...";//서버 URL
    private static final String FRIEND_PROFILE_URL = "http:...";//서버 URL
    private static final String FRIEND_EMAIL = "FRIEND_EMAIL";
    private static final String FRIEND_PHONE_LIST = "FRIEND_PHONE_LIST";
    private static final String FRIEND_ID = "FRIEND_ID";

    /*
    * 로그인
    * */
    private static final String LOGIN_JOIN_URL = "http:...";//서버 URL
    private static final String LOGIN_SEND_TEMP_URL = "http:...";//서버 URL
    private static final String LOGIN_AUTHOR_URL = "http:...";//서버 URL
    private static final String LOGIN_EXAM_URL = "http:...";//서버 URL


    /**
     * 네비게이션
     */
    private static final String NAVIGATION_SEND_EXCERCISE_URL = "https://apis.skplanetx.com/tmap/pois";
    private static final String NAVIGATION_SEARCH_POI_URL = "https://apis.skplanetx.com/tmap/pois";
    private static final String NAVIGATION_ADD_FAVORITE_URL = "NAVIGATION_DESTINATION";
    private static final String NAVIGATION_REMOVE_FAVORITE_URL = "NAVIGATION_DESTINATION";
    private static final String NAVIGATION_REMOVEALL_FAVORITE_URL = "NAVIGATION_DESTINATION";

    private static final String NAVIGATION_GET_FAVORITE_URL = "NAVIGATION_DESTINATION";
    private static final String NAVIGATION_KEY_HEADERS_ACCEPT = "Accept";
    private static final String NAVIGATION_KEY_HEADERS_APPKEY = "appKey";
    private static final String NAVIGATION_VALUE_HEADERS_ACCEPT = "application/json";
    private static final String NAVIGATION_VALUE_HEADERS_APPKEY = "fae4be30-90e4-3c96-b227-0086b07ae5e1";
    private static final String NAVIGATION_VALUE_POI_RESCOORDTYPE = "WGS84GEO";
    private static final String NAVIGATION_KEY_POI_VERSION = "version";
    private static final String NAVIGATION_KEY_POI_SEARCH_KEYWORD = "searchKeyword";
    private static final String NAVIGATION_KEY_POI_RESCOORDTYPE = "resCoordType";
    private static final String NAVIGATION_DESTINATION = "NAVIGATION_DESTINATION";

    private static final String NAVIGATION_CALORIE = "NAVIGATION_CALORIE";
    private static final String NAVIGATION_SPEED = "NAVIGATION_SPEED";
    private static final String NAVIGATION_DISTANCE = "NAVIGATION_DISTANCE";

    private static final int NAVIGATION_VALUE_POI_VERSION = 1;


    private static final int ON_FAIL = -1;
    private static final int ON_SUCCESS = 1;

    private static NetworkManager instance;
    AsyncHttpClient client;
    Gson gson;


    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    private NetworkManager() {
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

    /**
     * 계정
     */
    public void saveUserProfile(Context context, String email, String id, String password, File file, final OnResultListener listener) {
        RequestParams params = new RequestParams();

        try {
            params.put(USER_EAMIL, email);
            params.put(USER_ID, id);
            params.put(USER_PASSWORD, password);
            params.put(USER_IMAGE, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("Accept","application/json");
        */

        client.get(context, ACCOUNT_PROFILE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(statusCode);
            }
        });
    }
/*

    public void saveUserImage(Context context, String email, File file, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,사진파일,
        //결과값 : INT
        RequestParams params = new RequestParams();
        try {
            params.put(USER_EAMIL, email);
            params.put(USER_IMAGE, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        client.get(context, ACCOUNT_IMAGE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }
*/

    /**
     * 운동
     */

   /* public void gettestExerciseRecord(Context context, String email, String date, final OnResultListener<ExcerciseResult> listener) {
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,종류,시작날짜,개수
        //결과값 : JSON(종류에 대한 값들)
        *//*params.put(USER_EAMIL,email);
        params.put(EXCERCISE_REQUEST_DATE,date);
*//*
        params.put(USER_EAMIL,"lowgiant@gmai.com");
        params.put(EXCERCISE_REQUEST_DATE,"2015-11-03");



        client.get(context, EXCERCISE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("caloriedata fail: ", statusCode + "");

                //  listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("from server : ", responseString + "");
                ExcerciseResult result = gson.fromJson(responseString, ExcerciseResult.class);
                listener.onSuccess(result);

            }
        });
    }
*/



    public void getExerciseRecord(Context context, String email,String date, final OnResultListener<ExcerciseResult> listener) {
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,종류,시작날짜,개수
        //결과값 : JSON(종류에 대한 값들)
       /*params.put(USER_EAMIL,email);
        params.put(EXCERCISE_REQUEST_DATE,date);
        */
        params.put(USER_EAMIL,"lowgiant@gmai.com");
        params.put(EXCERCISE_REQUEST_DATE,"2015-11-03");


        client.get(context, EXCERCISE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("caloriedata : ",responseString);
                ExcerciseResult result = gson.fromJson(responseString, ExcerciseResult.class);
                listener.onSuccess(result);

            }
        });
    }






    public void getDayExerciseRecord(Context context, String email, String date, final OnResultListener<ExerciseDayResult> listener) {
        //PARAMETER : 유저 이메일,종류,날짜
        //결과값 : JSON(칼로리,속력,거리)
        RequestParams params = new RequestParams();
        /*params.put(USER_EAMIL, email);
        params.put(EXCERCISE_REQUEST_DATE, date);
        */
        params.put("uemail","lowgiant@gmai.com");
        params.put("date","2015-11-03");




        client.get(context, EXCERCISE_DAY_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("click result",responseString);
                ExerciseDayResult result = gson.fromJson(responseString, ExerciseDayResult.class);
                listener.onSuccess(result);
            }
        });

    }

    /**
     * 친구
     */
    public void getUserFriends(Context context, String email, final OnResultListener<FriendResult> listener) {
        //PARAMETER : 유저 이메일
        //결과값 : JSON(친구아이디,이메일,사진        )

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);

        client.get(context, FRIEND_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                //Toast.makeText(MyApplication.getContext(),"data : " + responseString,Toast.LENGTH_SHORT).show();
                Log.i("List ","data : " + responseString);
                FriendResult result = gson.fromJson(responseString, FriendResult.class);
                listener.onSuccess(result);
            }
        });

    }

    public void addUserFriend(Context context, String uEmail, String fEamil, String fid, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,친구 이메일, 친구 아이디
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEamil);
        params.put(FRIEND_ID, fid);

        client.get(context, FRIEND_ADD_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });

    }

    public void removeUserFriend(Context context, String uEmail, String fEamil, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,친구 이메일
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEamil);

        client.get(context, FRIEND_REMOVE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });

    }

    public void getUserFriendAddress(Context context, String email, ArrayList phoneList, final OnResultListener<FriendSearchResult> listener) {
        //PARAMETER : 유저 이메일,전화번호리스트(Array)
        //결과값 : JSON(친구아이디,이메일,사진)
        RequestParams params = new RequestParams();
        try {
            params.put(USER_EAMIL, email);
            params.put(FRIEND_PHONE_LIST, phoneList);//array보내기 질문하기
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.get(context, FRIEND_ADDRESS_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                FriendSearchResult result = gson.fromJson(responseString, FriendSearchResult.class);
                listener.onSuccess(result);
            }
        });

    }

    public void getUserFriendDirect(Context context, String uEmail, String iEmail, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,입력이메일값
        //결과값 : JSON(친구아이디,이메일,사진)
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, iEmail);
        client.get(context, FRIEND_DIRECT_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });

    }

    public void getFriendProfile(Context context, String uEmail, String fEmail, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,친구이메일,구분값(친구추가삭제 메소드랑 구분)
        //결과값 : JSON(친구아이디,이메일,가입일,활동량(칼로리,속력,거리))
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEmail);
        client.get(context, FRIEND_PROFILE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });

    }

    /**
     * 로그인
     */
    public void saveUserInform(Context context, String id, String email, String date, String password, String phone, final OnResultListener listener) {
        //PARAMETER : 유저 이름,이메일,가입일,비밀번호 ->사진은 회원가입땐 안함
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_ID, id);
        params.put(USER_EAMIL, email);
        params.put(JOIN_DATE, date);
        params.put(USER_PASSWORD, password);

        client.get(context, LOGIN_JOIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }

    public void userAuthorization(Context context, String email, String password, final OnResultListener<LoginResult> listener) {
        //PARAMETER : 이메일,비밀번호
        //결과값 : 이메일,비밀번호,패스워드,가입일

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(USER_PASSWORD, password);


        client.get(context, LOGIN_AUTHOR_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                LoginResult result = gson.fromJson(responseString, LoginResult.class);

                listener.onSuccess(result);
            }
        });
    }

    public void sendTempPassword(Context context, String email, final OnResultListener listener) {
        //PARAMETER : 유저 이메일
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);


        client.get(context, LOGIN_SEND_TEMP_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }

    public void checkEmail(Context context, String email, final  OnResultListener listener){
        //PARAMETER : 유저이메일
        //결과값 INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);

        client.get(context, LOGIN_EXAM_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });


    }

    /**
     * 네비게이션
     */
    public void searchPOI(Context context, String keyword, final OnResultListener<SearchPOIInfo> listener) {
        RequestParams params = new RequestParams();
        Header[] headers = null;

        headers = new Header[2];
        headers[0] = new BasicHeader(NAVIGATION_KEY_HEADERS_ACCEPT, NAVIGATION_VALUE_HEADERS_ACCEPT);
        headers[1] = new BasicHeader(NAVIGATION_KEY_HEADERS_APPKEY, NAVIGATION_VALUE_HEADERS_APPKEY);
        params.put(NAVIGATION_KEY_POI_VERSION, NAVIGATION_VALUE_POI_VERSION);
        params.put(NAVIGATION_KEY_POI_SEARCH_KEYWORD, keyword);
        params.put(NAVIGATION_KEY_POI_RESCOORDTYPE, NAVIGATION_VALUE_POI_RESCOORDTYPE);

        client.get(context, NAVIGATION_SEARCH_POI_URL, headers, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                SearchPOIInfoResult result = gson.fromJson(responseString, SearchPOIInfoResult.class);
                listener.onSuccess(result.searchPoiInfo);
            }
        });
    }

    public void saveFavorite(Context context, String email, String destication, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(NAVIGATION_DESTINATION, destication);

        client.get(context, NAVIGATION_ADD_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }

    public void getFavorite(Context context, String email, final OnResultListener listener) {
        //PARAMETER : 유저 이메일
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);


        client.get(context, NAVIGATION_GET_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }

    public void removeAllFavorite(Context context, String email, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        client.get(context, NAVIGATION_REMOVEALL_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }

    public void removeFavorite(Context context, String email, String destination, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(NAVIGATION_DESTINATION, destination);

        client.get(context, NAVIGATION_REMOVE_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }

    public void saveExcercise(Context context, String email, String calorie, String speed, String distance, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,칼로리,속력,거리
        //결과값 : INT
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(NAVIGATION_CALORIE, calorie);
        params.put(NAVIGATION_SPEED, speed);
        params.put(NAVIGATION_DISTANCE, distance);


        client.get(context, NAVIGATION_SEND_EXCERCISE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });
    }




    public void cancelAll(Context context) {
        client.cancelRequests(context, true);

    }

    public interface OnResultListener<T> {
        public void onSuccess(T result);

        public void onFail(int code);
    }


}
