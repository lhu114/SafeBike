package com.safering.safebike.manager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.safering.safebike.exercisereport.ExcerciseResult;
import com.safering.safebike.exercisereport.ExerciseDayResult;
import com.safering.safebike.exercisereport.ExerciseRecentResult;
import com.safering.safebike.friend.FriendAddressFragment;
import com.safering.safebike.friend.FriendDirectSearchResult;
import com.safering.safebike.friend.FriendProfileResult;
import com.safering.safebike.friend.FriendResult;
import com.safering.safebike.friend.FriendSearchResult;
import com.safering.safebike.login.LoginResult;
import com.safering.safebike.navigation.FavoriteResult;
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
    private static final String ACCOUNT_PROFILE_URL = "http://52.69.133.212:3000/user/photoedit";
    private static final String ACCOUNT_PROFILE_NOFILE_URL = "http://52.69.133.212:3000/user/edit";
    private static final String JOIN_DATE = "date";
    private static final String USER_EAMIL = "uemail";
    private static final String USER_ID = "name";
    private static final String USER_PASSWORD = "pwd";
    private static final String USER_IMAGE = "photo";
    private static final String USER_PHONE = "phone";

/*
    params.put(USER_ID, id);
    params.put(USER_EAMIL, email);
    params.put(JOIN_DATE, date);
    params.put(USER_PASSWORD, password);*/

    /**
     * 운동
     */
    private static final String NAVIGATION_SAVE_EXCERCISE_URL = "http://52.69.133.212:3000/workout/add";
    private static final String EXCERCISE_URL = "http://52.69.133.212:3000/workout/list";
    private static final String EXCERCISE_DAY_URL = "http://52.69.133.212:3000/workout/";

    private static final String EXCERCISE_REQUEST_DATE = "date";
    private static final String EXCERCISE_RECENT_URL = "http://52.69.133.212:3000/workout/one";
    private static final String LOGOUT_URL = "http://52.69.133.212:3000/user/logout";

    /**
     * 친구
     */

    private static final String FRIEND_URL = "http://52.69.133.212:3000/user/friend";//서버 URL
    private static final String FRIEND_ADDRESS_URL = "http://52.69.133.212:3000/user/psearch";//서버 URL
    private static final String FRIEND_ADD_URL = "http://52.69.133.212:3000/user/friend/add";//서버 URL
    private static final String FRIEND_REMOVE_URL = "http://52.69.133.212:3000/user/friend/delete";//서버 URL
    private static final String FRIEND_DIRECT_URL = "http://52.69.133.212:3000/user/esearch";//서버 URL
    private static final String FRIEND_PROFILE_URL = "http://52.69.133.212:3000/user/friend/profile";//서버 URL
    private static final String FRIEND_EMAIL = "pemail";
    private static final String FRIEND_ID = "pname";

    /*
    * 로그인
    * */
    private static final String LOGIN_JOIN_URL = "http://52.69.133.212:3000/user/add";//서버 URL
    private static final String LOGIN_SEND_TEMP_URL = "http://52.69.133.212:3000/user/ps";//서버 URL
    private static final String LOGIN_AUTHOR_URL = "http://52.69.133.212:3000/user/login";//서버 URL
    private static final String LOGIN_EXAM_URL = "http://52.69.133.212:3000/user/email";//서버 URL


    /**
     * 네비게이션
     */

    private static final String NAVIGATION_LATITUDE = "favoriteslatitude";
    private static final String NAVIGATION_LONGITUDE = "favoriteslongitude";

    private static final String NAVIGATION_ADD_FAVORITE_URL = "http://52.69.133.212:3000/favorites/add";
    private static final String NAVIGATION_REMOVE_FAVORITE_URL = "http://52.69.133.212:3000/favorites/delete";
    private static final String NAVIGATION_REMOVEALL_FAVORITE_URL = "http://52.69.133.212:3000/favorites/alldelete";
    private static final String NAVIGATION_GET_FAVORITE_URL = "http://52.69.133.212:3000/favorites";
    private static final String NAVIGATION_GET_MATCH_FAVORITE_URL = "http://52.69.133.212:3000/favorites/one";
    private static final String NAVIGATION_KEY_HEADERS_ACCEPT = "Accept";
    private static final String NAVIGATION_KEY_HEADERS_APPKEY = "appKey";
    private static final String NAVIGATION_VALUE_HEADERS_ACCEPT = "application/json";
    private static final String NAVIGATION_VALUE_HEADERS_APPKEY = "fae4be30-90e4-3c96-b227-0086b07ae5e1";
    private static final String NAVIGATION_VALUE_POI_RESCOORDTYPE = "WGS84GEO";
    private static final String NAVIGATION_KEY_POI_VERSION = "version";
    private static final String NAVIGATION_KEY_POI_SEARCH_KEYWORD = "searchKeyword";
    private static final String NAVIGATION_KEY_POI_RESCOORDTYPE = "resCoordType";

    private static final String NAVIGATION_DESTINATION = "favoritesname";
    private static final String NAVIGATION_CALORIE = "calorie";
    private static final String NAVIGATION_SPEED = "speed";
    private static final String NAVIGATION_DISTANCE = "road";

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
        boolean hasFile = true;
        RequestParams params = new RequestParams();

        try {

            params.put(USER_IMAGE, file);
        } catch (FileNotFoundException e) {
            hasFile = false;

        } finally {
            if(hasFile){
                params.put(USER_EAMIL, email);
                params.put(USER_ID, id);
                params.put(USER_PASSWORD, password);
                client.post(context, ACCOUNT_PROFILE_URL, params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        listener.onSuccess(responseString);

                    }
                });
            }else{

                params.put(USER_EAMIL, email);
                params.put(USER_ID, id);
                params.put(USER_PASSWORD, password);

                client.post(context, ACCOUNT_PROFILE_NOFILE_URL, params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        listener.onSuccess(responseString);

                    }
                });
            }

        }




    }


    /**
     * 운동
     */
    public void getExerciseRecord(Context context, String email, ArrayList<String> dateList, final OnResultListener<ExcerciseResult> listener) {
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        for(int i = 0; i < dateList.size(); i++){
            params.add(EXCERCISE_REQUEST_DATE,dateList.get(i));
        }

        client.get(context, EXCERCISE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                ExcerciseResult result = gson.fromJson(responseString, ExcerciseResult.class);
                listener.onSuccess(result);

            }
        });
    }

    public void getRecentExerciseDate(Context context,String email,final OnResultListener<ExerciseRecentResult> listener){
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        client.get(context, EXCERCISE_RECENT_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                ExerciseRecentResult result = gson.fromJson(responseString, ExerciseRecentResult.class);
                listener.onSuccess(result);
            }
        });

    }
    public void logout(Context context,final OnResultListener listener){

        client.get(context, LOGOUT_URL,new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

            }
        });

    }

    public void getDayExerciseRecord(Context context, String email, String date, final OnResultListener<ExerciseDayResult> listener) {
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(EXCERCISE_REQUEST_DATE, date);


        client.get(context, EXCERCISE_DAY_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                ExerciseDayResult result = gson.fromJson(responseString, ExerciseDayResult.class);
                listener.onSuccess(result);
            }
        });

    }

    /**
     * 친구
     */
    public void getUserFriends(Context context, String email, final OnResultListener<FriendResult> listener) {

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        client.get(context, FRIEND_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                FriendResult result = gson.fromJson(responseString, FriendResult.class);
                listener.onSuccess(result);
            }
        });

    }

    public void addUserFriend(Context context, String uEmail, String fEamil, String fid,String fPhoto, final OnResultListener listener) {
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEamil);
        params.put(FRIEND_ID, fid);
        params.put(USER_IMAGE,fPhoto);


        client.post(context, FRIEND_ADD_URL, params, new TextHttpResponseHandler() {
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
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL,uEmail);
        params.put(FRIEND_EMAIL,fEamil);

        Header[] header = null;

        client.delete(context, FRIEND_REMOVE_URL, header, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

            }
        });


    }


    public void getUserFriendAddress(Context context,ArrayList<FriendAddressFragment.Contact> phoneList, final OnResultListener<FriendSearchResult> listener) {
        RequestParams params = new RequestParams();

        for(int i = 0; i < phoneList.size(); i++){
            params.add("phone",phoneList.get(i).getPhonenum());
        }

        client.post(context, FRIEND_ADDRESS_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                FriendSearchResult result = gson.fromJson(responseString, FriendSearchResult.class);
                listener.onSuccess(result);

            }
        });

    }

    public void getUserFriendDirect(Context context, String uEmail, final OnResultListener<FriendDirectSearchResult> listener) {

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, uEmail);
        client.get(context, FRIEND_DIRECT_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if(!responseString.equals("1")) {
                    FriendDirectSearchResult result = gson.fromJson(responseString, FriendDirectSearchResult.class);
                    listener.onSuccess(result);
                }

            }
        });

    }

    public void getFriendProfile(Context context,String fEmail, final OnResultListener<FriendProfileResult> listener) {
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, fEmail) ;
        client.get(context, FRIEND_PROFILE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                FriendProfileResult result = gson.fromJson(responseString, FriendProfileResult.class);
                listener.onSuccess(result);
            }
        });

    }

    /**
     * 로그인
     */
    public void saveUserInform(Context context, String id, String email, String date, String password, String phone, final OnResultListener listener) {
        RequestParams params = new RequestParams();
        params.put(JOIN_DATE, date);
        params.put(USER_EAMIL, email);
        params.put(USER_ID, id);
        params.put(USER_PASSWORD, password);
        params.put(USER_PHONE, phone);
        params.put(USER_IMAGE,"null");

        client.post(context, LOGIN_JOIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(responseString);

            }
        });
    }

    public void userAuthorization(Context context, String email, String password, final OnResultListener<LoginResult> listener) {

        RequestParams params = new RequestParams();

        params.put(USER_EAMIL, email);
        params.put(USER_PASSWORD, password);
        client.post(context, LOGIN_AUTHOR_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("1")){
                    LoginResult result = null;
                    listener.onSuccess(result);
                }
                else if (responseString.equals("2")){
                    LoginResult result = null;
                    listener.onSuccess(result);
                }
                else {
                    LoginResult result = gson.fromJson(responseString, LoginResult.class);
                    listener.onSuccess(result);
                }
            }
        });
    }

    public void sendTempPassword(Context context, String email, final OnResultListener listener) {
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);


        client.post(context, LOGIN_SEND_TEMP_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(responseString);
            }
        });
    }

    public void checkEmail(Context context, String email, final OnResultListener<String> listener) {
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);

        client.post(context, LOGIN_EXAM_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(responseString);
            }
        });


    }


    public void getFavorite(Context context, String email, final OnResultListener<FavoriteResult> listener) {
        //PARAMETER : 유저 이메일
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);

        client.get(context, NAVIGATION_GET_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d("safebike", "NetworkManager.getFavorite.onFailure : " + statusCode + "");

                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Log.d("safebike", "NetworkManager.getFavorite.onSuccess.responseString : " + responseString + "");
                FavoriteResult result = gson.fromJson(responseString, FavoriteResult.class);

                listener.onSuccess(result);

                if (headers != null) {
                    int count = headers.length;

                    for(int i = 0; i < count; i++) {
//                        Log.d("safebike", "headers : " + headers[i]);
                    }
                }
            }
        });
    }

    public void getMatchFavorite(Context context, String email, String destination, double latitude, double longitude, final OnResultListener listener) {
        //PARAMETER : 유저 이메일
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(NAVIGATION_DESTINATION, destination);
        params.put(NAVIGATION_LATITUDE, latitude);
        params.put(NAVIGATION_LONGITUDE, longitude);

        client.get(context, NAVIGATION_GET_MATCH_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d("safebike", "NetworkManager.getMatchFavorite.onFailure.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Log.d("safebike", "NetworkManager.getMatchFavorite.onSuccess.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onSuccess(responseString);
            }
        });
    }

    public void saveFavorite(Context context, String email, String destination, double latitude, double longitude, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(NAVIGATION_DESTINATION, destination);
        params.put(NAVIGATION_LATITUDE, latitude);
        params.put(NAVIGATION_LONGITUDE, longitude);

        client.post(context, NAVIGATION_ADD_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d("safebike", "NetworkManager.saveFavorite.onFailure.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Log.d("safebike", "NetworkManager.saveFavorite.onSuccess.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onSuccess(statusCode);
            }
        });
    }


    public void removeFavorite(Context context, String email, String destination, double latitude, double longitude, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(NAVIGATION_DESTINATION, destination);
        params.put(NAVIGATION_LATITUDE, latitude);
        params.put(NAVIGATION_LONGITUDE, longitude);

        Header[] header = null;
        client.delete(context, NAVIGATION_REMOVE_FAVORITE_URL, header, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d("safebike", "NetworkManager.removeFavorite.onFailure.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Log.d("safebike", "NetworkManager.removeFavorite.onSuccess.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onSuccess(statusCode);
            }
        });
    }


    public void removeAllFavorite(Context context, String email, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);

        Header[] header = null;

        client.delete(context, NAVIGATION_REMOVEALL_FAVORITE_URL, header, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d("safebike", "NetworkManager.removeAllFavorite.onFailure.statusCode : " + statusCode + " | responseString : " + responseString);

                 listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Log.d("safebike", "NetworkManager.removeAllFavorite.onSuccess.statusCode : " + statusCode + " | responseString : " + responseString);

                 listener.onSuccess(statusCode);
            }
        });
    }


    /**
     * 운동정보 서버에 저장
     * */
    public void saveExercise(Context context, String email, String date, int calorie, int speed, int distance, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,칼로리,속력,거리
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(EXCERCISE_REQUEST_DATE, date);
        params.put(NAVIGATION_CALORIE, calorie);
        params.put(NAVIGATION_SPEED, speed);
        params.put(NAVIGATION_DISTANCE, distance);

        client.post(context, NAVIGATION_SAVE_EXCERCISE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                Log.d("safebike", "NetworkManager.saveExercise.onFailure.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Log.d("safebike", "NetworkManager.saveExercise.onSuccess.statusCode : " + statusCode + " | responseString : " + responseString);

                listener.onSuccess(statusCode);
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
