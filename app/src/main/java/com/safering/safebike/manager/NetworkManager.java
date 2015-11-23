package com.safering.safebike.manager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.http.Headers;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.utils.L;
import com.safering.safebike.R;
import com.safering.safebike.exercisereport.ExcerciseResult;
import com.safering.safebike.exercisereport.ExerciseDayResult;
import com.safering.safebike.friend.FriendAddressFragment;
import com.safering.safebike.friend.FriendDirectSearchResult;
import com.safering.safebike.friend.FriendProfileResult;
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
import java.net.URI;
import java.net.URISyntaxException;
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
    private static final String ACCOUNT_PROFILE_URL = "http://52.69.133.212:3000/user/photoedit";//서버 URL
    private static final String ACCOUNT_PROFILE_NOFILE_URL = "http://52.69.133.212:3000/user/edit";//서버 URL

    private static final String ACCOUNT_IMAGE_URL = "http:...";//서버 URL
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
    private static final String EXCERCISE_URL = "http://52.69.133.212:3000/workout/list";//서버 URL
    private static final String EXCERCISE_DAY_URL = "http://52.69.133.212:3000/workout/";//서버 URL
    private static final String EXERCISE_REQUEST_TYPE = "EXERCISE_TYPE";
    private static final String EXCERCISE_REQUEST_DATE = "date";
    private static final String EXCERCISE_REQUEST_NUMBER = "REQUEST_NUMBER";

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
    private static final String FRIEND_PHONE_LIST = "FRIEND_PHONE_LIST";
    private static final String FRIEND_ID = "name";

    /*
    * 로그인
    * */
    private static final String LOGIN_JOIN_URL = "http://52.69.133.212:3000/user/add";//서버 URL
    private static final String LOGIN_SEND_TEMP_URL = "http:...";//서버 URL
    private static final String LOGIN_AUTHOR_URL = "http://52.69.133.212:3000/user/login";//서버 URL
    private static final String LOGIN_EXAM_URL = "http://52.69.133.212:3000/user/email";//서버 URL


    /**
     * 네비게이션
     */
    private static final String NAVIGATION_FAVORITE_URL = "http://52.69.133.212:3000";
    private static final String FAVORITE_NAME = "favoritesname";
    private static final String NAVIGATION_LATITUDE = "favoriteslatitude";
    private static final String NAVIGATION_LONGITUDE = "favoriteslongtude";


    private static final String NAVIGATION_SEND_EXCERCISE_URL = "https://apis.skplanetx.com/tmap/pois";
    private static final String NAVIGATION_SEARCH_POI_URL = "https://apis.skplanetx.com/tmap/pois";
    private static final String NAVIGATION_ADD_FAVORITE_URL = "http://52.69.133.212:3000/favorites/add";
    private static final String NAVIGATION_REMOVE_FAVORITE_URL = "http://52.69.133.212:3000/favorites/delete";
    private static final String NAVIGATION_REMOVEALL_FAVORITE_URL = "http://52.69.133.212:3000/favorites/alldelete";

    private static final String NAVIGATION_GET_FAVORITE_URL = "http://52.69.133.212:3000/favorites";
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
    private static final String NAVIGATION_DISTANCE = "load";

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
        boolean hasFile = true;
        RequestParams params = new RequestParams();

        try {

            params.put(USER_IMAGE, file);
        } catch (FileNotFoundException e) {
            hasFile = false;

        } finally {
            if(hasFile){
                params.put(USER_EAMIL, "lowgiant@gmail.com");
                params.put(USER_ID, id);
                params.put(USER_PASSWORD, password);



                client.post(context, ACCOUNT_PROFILE_URL, params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.i("ProfileEditFail", statusCode + "");
                        //다이얼로그 띄우기

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Log.i("ProfileEditSuccess", statusCode + "");
                        //listener.onSuccess(1);

                    }
                });
            }else{

                params.put(USER_EAMIL, "lowgiant@gmail.com");
                params.put(USER_ID, id);
                params.put(USER_PASSWORD, password);

                client.post(context, ACCOUNT_PROFILE_NOFILE_URL, params, new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.i("ProfileEditNoFileFail", statusCode + "");

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Log.i("ProfileEditNoFileSucc", responseString + "");
                        //201

                    }
                });
            }

        }




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
   /* public void getFavoiteList(Context context,String email,final OnResultListener<FavoriteResult> listener){
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL,email);
        client.get(context, NAVIGATION_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("favoriteListfAIL",statusCode + "");

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("favoriteListSuccess",statusCode + "");

            }
        });

    }*/

  /*  public void addFavorite(Context context,String email,String favoritename,float lat,float lon,final OnResultListener listener){
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL,email);
        params.put(FAVORITE_NAME,favoritename);
        params.put(NAVIGATION_LATITUDE,lat);
        params.put(NAVIGATION_LONGITUDE,lon);

        client.post(context, NAVIGATION_ADD_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("favorite add fail",statusCode + "");

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("favorite add success",statusCode + "");

            }
        });

    }*/
    public void getExerciseRecord(Context context, String email, String date, final OnResultListener<ExcerciseResult> listener) {
        RequestParams params = new RequestParams();
        //PARAMETER : 유저 이메일,종류,시작날짜,개수
        //결과값 : JSON(종류에 대한 값들)
       /*params.put(USER_EAMIL,email);
        params.put(EXCERCISE_REQUEST_DATE,date);
        */
        params.put(USER_EAMIL, "lowgiant@gmail.com");
        params.put(EXCERCISE_REQUEST_DATE, "2015-11-03");


        client.get(context, EXCERCISE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("exercise",responseString);
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
        params.put("uemail", "lowgiant@gmai.com");
        params.put("date", "2015-11-03");


        client.get(context, EXCERCISE_DAY_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("click result", responseString);
                ExerciseDayResult result = gson.fromJson(responseString, ExerciseDayResult.class);
                Log.i("click size : ",result.workout.size() + "");
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
                Log.i("friendlist", responseString);
                FriendResult result = gson.fromJson(responseString, FriendResult.class);
                listener.onSuccess(result);
            }
        });

    }

    public void addUserFriend(Context context, String uEmail, String fEamil, String fid, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,친구 이메일, 친구 아이디
        //결과값 : INT

        RequestParams params = new RequestParams();
        /*
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEamil);
        params.put(FRIEND_ID, fid);
        */
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEamil);
        params.put(FRIEND_ID, fid);


        client.post(context, FRIEND_ADD_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("friendaddfail", statusCode + "");


                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("friendaddsuccess", responseString);

                listener.onSuccess(ON_SUCCESS);
            }
        });


    }

    public void removeUserFriend(Context context, String uEmail, String fEamil, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,친구 이메일
        //결과값 : INT

        RequestParams params = new RequestParams();
        /*params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEamil);
*/
        params.put(USER_EAMIL, "lowgiant@gmail.com");
        params.put(FRIEND_EMAIL, "newreview@naver.com");

/*
        client.get(context, FRIEND_REMOVE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);l
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                listener.onSuccess(ON_SUCCESS);
            }
        });*/
        Header[] header = null;

        client.delete(context, FRIEND_REMOVE_URL, header, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("removefirndfail", statusCode + "");

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("removefirnd", responseString);
            }
        });


    }


    public void getUserFriendAddress(Context context,ArrayList<FriendAddressFragment.Contact> phoneList, final OnResultListener<FriendSearchResult> listener) {
        //PARAMETER : 유저 이메일,전화번호리스트(Array)
        //결과값 : JSON(친구아이디,이메일,사진)
        RequestParams params = new RequestParams();
        /*params.add("phone", "01023232321");
        params.add("phone", "01023212333");
        params.add("phone", "01041110256");
        */
        for(int i = 0; i < phoneList.size(); i++){
            params.add("phone",phoneList.get(i).getPhonenum());
        }

        client.post(context, FRIEND_ADDRESS_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //Log.i("getUserFrendFail", statusCode + "");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                Log.i("searchResultSuccess",responseString);
                FriendSearchResult result = gson.fromJson(responseString, FriendSearchResult.class);
                listener.onSuccess(result);

            }
        });

    }

    public void getUserFriendDirect(Context context, String uEmail, final OnResultListener<FriendDirectSearchResult> listener) {
        //PARAMETER : 유저 이메일,입력이메일값
        //결과값 : JSON(친구아이디,이메일,사진)
        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, "lowgiant@gmail.com");
      //  params.put(FRIEND_EMAIL, iEmail);
        client.get(context, FRIEND_DIRECT_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("direct suc", responseString);
               // listener.onSuccess(ON_SUCCESS);
                FriendDirectSearchResult result = gson.fromJson(responseString, FriendDirectSearchResult.class);
                listener.onSuccess(result);

            }
        });

    }

    public void getFriendProfile(Context context, String uEmail, String fEmail, final OnResultListener<FriendProfileResult> listener) {
        //PARAMETER : 유저 이메일,친구이메일,구분값(친구추가삭제 메소드랑 구분)
        //결과값 : JSON(친구아이디,이메일,가입일,활동량(칼로리,속력,거리))
        RequestParams params = new RequestParams();
/*
        params.put(USER_EAMIL, uEmail);
        params.put(FRIEND_EMAIL, fEmail);

*/
        params.put(USER_EAMIL, "lowgiant@gmail.com");


        client.get(context, FRIEND_PROFILE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("priflefail", statusCode + "");
                listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("friendprofile", responseString);
                FriendProfileResult result = gson.fromJson(responseString, FriendProfileResult.class);
                listener.onSuccess(result);
                Log.i("friend name", result.friendprofile.name);
                Log.i("friend email", result.friendprofile.email);
                Log.i("friend join", result.friendprofile.join);
                Log.i("friend calorie", result.friendprofile.calorie + "");
                Log.i("friend speed", result.friendprofile.speed + "");
                Log.i("friend road", result.friendprofile.road + "");


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

        params.put(JOIN_DATE, date);
        params.put(USER_EAMIL, email);
        params.put(USER_ID, id);
        params.put(USER_PASSWORD, password);
        params.put(USER_PHONE, phone);
        params.put(USER_IMAGE,"null");

        client.post(context, LOGIN_JOIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("ConfirmSignFail", statusCode + "");

                listener.onFail(statusCode);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("ConfirmSignSuccess", responseString);
                listener.onSuccess(responseString);

            }
        });
    }

    public void userAuthorization(Context context, String email, String password, final OnResultListener<LoginResult> listener) {
        //PARAMETER : 이메일,비밀번호
        //결과값 : 이메일,비밀번호,패스워드,가입일

        RequestParams params = new RequestParams();
        Log.i("inputid",email);
        Log.i("inputpass",password);
        params.put(USER_EAMIL, email);
        params.put(USER_PASSWORD, password);

        //params.put(USER_EAMIL, "lowgiant@gmail.com");
        //params.put(USER_PASSWORD, "1234");

        client.post(context, LOGIN_AUTHOR_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("LoginInputFragmentFail", "fail");
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("LoginInputFragmentSucc", responseString);
                if (responseString.equals("1")){
                    Log.i("LoginInputNoId", responseString);
                    LoginResult result = null;
                    listener.onSuccess(result);

                    //listener.onSuccess();
                    //return;

                }
                else if (responseString.equals("2")){
                    Log.i("LoginInputNoPass", responseString);
                    LoginResult result = null;
                    listener.onSuccess(result);


                }
                else {


                    LoginResult result = gson.fromJson(responseString, LoginResult.class);
                    listener.onSuccess(result);
                    //
                    // "userlogin":{"id":"ㅂㅎ","join":"345463@gmail.com"}
                }
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

    public void checkEmail(Context context, String email, final OnResultListener<String> listener) {
        //PARAMETER : 유저이메일
        //결과값 INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);

        client.post(context, LOGIN_EXAM_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                //listener.onSuccess(ON_SUCCESS);
                //if(responseString.equals("1"))
                Log.i("suc",responseString);

                listener.onSuccess(responseString);
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

    public void getFavorite(Context context, String email, final OnResultListener<FavoriteResult> listener) {
        //PARAMETER : 유저 이메일
        //결과값 : INT

        RequestParams params = new RequestParams();
        //params.put(USER_EAMIL, email);
        params.put(USER_EAMIL, "lowgiant@gmail.com");


        client.get(context, NAVIGATION_GET_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("getFavoriteFail", statusCode + "");

                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("getFavoriteSuccess", responseString + "");
                /*FavoriteResult result = gson.fromJson(responseString,FavoriteResult.class);

                listener.onSuccess(result);
                */
            }
        });
    }

    public void saveFavorite(Context context, String email, String destication, double latitude, double longitude, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        params.put(USER_EAMIL, email);
        params.put(NAVIGATION_DESTINATION, destication);
        params.put(NAVIGATION_LATITUDE, latitude);
        params.put(NAVIGATION_LONGITUDE, longitude);
        client.post(context, NAVIGATION_ADD_FAVORITE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("saveFavoriteFail", statusCode + "");

                //listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("saveFavoriteSuccess", statusCode + " / " + responseString);

                //listener.onSuccess(ON_SUCCESS);
            }
        });
    }


    public void removeFavorite(Context context, String email, String destination, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        /*params.put(USER_EAMIL, email);
        params.put(NAVIGATION_DESTINATION, destination);
        */
        params.put(USER_EAMIL, "lowgiant@gmail.com");
        params.put(NAVIGATION_DESTINATION, "티아카데미");

        Header[] header = null;
        client.delete(context, NAVIGATION_REMOVE_FAVORITE_URL, header, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("removeFavoriteFail", statusCode + "");

                // listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("removeFavoriteSuccess", statusCode + responseString);

                //listener.onSuccess(ON_SUCCESS);
            }
        });
    }


    public void removeAllFavorite(Context context, String email, final OnResultListener listener) {
        //PARAMETER : 유저 이메일,목적지
        //결과값 : INT

        RequestParams params = new RequestParams();
        //params.put(USER_EAMIL, email);
        params.put(USER_EAMIL, "lowgiant@gmail.com");
        Header[] header = null;
        client.delete(context, NAVIGATION_REMOVEALL_FAVORITE_URL, header, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("removeAllFavoriteFail", statusCode + "");

                // listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i("removeAllFavoriteFail", statusCode + responseString);

                // listener.onSuccess(ON_SUCCESS);
            }
        });
    }


    /**
     * 운동정보 서버에 저장
     * */
    public void saveExcercise(Context context, String email, String date, double calorie, double speed, double distance, final OnResultListener listener) {
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
                Log.i("saveExcerciseFail", statusCode + "");

                //listener.onFail(ON_FAIL);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                Log.i("saveExcerciseSuccess", statusCode + " / " + responseString);
                //listener.onSuccess(ON_SUCCESS);
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
