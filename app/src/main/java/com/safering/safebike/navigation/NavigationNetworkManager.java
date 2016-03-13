package com.safering.safebike.navigation;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private static final String KEY_TMAP_HEADERS_ACCEPT = "Accept";
    private static final String KEY_TMAP_HEADERS_APPKEY = "appKey";

    private static final String VALUE_TMAP_HEADERS_ACCEPT = "application/json";
    private static final String VALUE_TMAP_HEADERS_APPKEY = "fae4be30-90e4-3c96-b227-0086b07ae5e1";

    AsyncHttpClient client;
    Gson poiGson, rvsGeoGson, bicycleRouteGson, tmapResponseInfoGson;
    Gson routeGson;
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

        poiGson = new Gson();
        bicycleRouteGson = new GsonBuilder().registerTypeAdapter(Geometry.class, new GeometryDeserializer()).create();
        tmapResponseInfoGson = new Gson();
    }

    public static synchronized NavigationNetworkManager getInstance() {
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
    private static final String KEY_POI_COUNT = "count";
    private static final String KEY_POI_SEARCH_KEYWORD = "searchKeyword";
    private static final String KEY_POI_RESCOORDTYPE = "resCoordType";

    private static final int VALUE_POI_VERSION = 1;
    private static final String VALUE_POI_COUNT = "10";
    private static final String VALUE_POI_RESCOORDTYPE = "WGS84GEO";

    public void searchPOI(Context context, String keyword, final OnResultListener<SearchPOIInfo> listener) {
//        Log.d("safebike", "NavigationNetworkManager.searchPOI");
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(KEY_TMAP_HEADERS_ACCEPT, VALUE_TMAP_HEADERS_ACCEPT);
        headers[1] = new BasicHeader(KEY_TMAP_HEADERS_APPKEY, VALUE_TMAP_HEADERS_APPKEY);

        RequestParams params = new RequestParams();
        params.put(KEY_POI_VERSION, VALUE_POI_VERSION);
        params.put(KEY_POI_COUNT, VALUE_POI_COUNT);
        params.put(KEY_POI_SEARCH_KEYWORD, keyword);
        params.put(KEY_POI_RESCOORDTYPE, VALUE_POI_RESCOORDTYPE);

//        Log.d("safebike", "keyword : " + keyword);
        if (headers != null) {
            int count = headers.length;

            for(int i = 0; i < count; i++) {
//                Log.d("safebike", "headers : " + headers[i]);
            }
        }

//        Log.d("safebike", "----------------------------------------------------------------------------------------------------------------------------");
        client.get(context, SEARCH_POI_URL, headers, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                /*
                 * fail에 따른 statusCode 처리
                 */
                listener.onFail(statusCode);

                String code = Integer.toString(statusCode);
//                Log.d("safebike", "code : " + code + " / responseString : " + responseString);

                if (headers != null) {
                    int count = headers.length;

                    for(int i = 0; i < count; i++) {
//                        Log.d("safebike", "headers : " + headers[i]);
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                /*
                 * success에 따른 statusCode 처리
                 */
                SearchPOIInfoResult result = poiGson.fromJson(responseString, SearchPOIInfoResult.class);

                if (result != null) {
                    listener.onSuccess(result.searchPoiInfo);
                }

                String code = Integer.toString(statusCode);
//                Log.d("safebike", "code : " + code + " / responseString : " + responseString);
//                Log.d("safebike", "headers : " + headers);
            }
        });
    }

    public static final String SEARCH_REVERSEGEOCODING_URL = "https://apis.skplanetx.com/tmap/geo/reversegeocoding";

    private static final String KEY_REVERSEGEOCODING_VERSION = "version";
    private static final String KEY_REVERSEGEOCODING_LATITUDE = "lat";
    private static final String KEY_REVERSEGEOCODING_LONGITUDE = "lon";
    private static final String KEY_REVERSEGEOCODING_COORDTYPE = "coordType";
    private static final String KEY_REVERSEGEOCODING_ADDRESSTYPE = "addressType";

    private static final int VALUE_REVERSEGEOCODING_VERSION = 1;
    private static final String VALUE_REVERSEGEOCODING_COORDTYPE = "WGS84GEO";
    private static final String VALUE_REVERSEGEOCODING_ADDRESSTYPE = "A02";

    public void searchReverseGeo(Context context, LatLng latLng, final OnResultListener<AddressInfo> listener) {
//        Log.d("safebike", "NavigationNetworkManager.searchReverseGeo");
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(KEY_TMAP_HEADERS_ACCEPT, VALUE_TMAP_HEADERS_ACCEPT);
        headers[1] = new BasicHeader(KEY_TMAP_HEADERS_APPKEY, VALUE_TMAP_HEADERS_APPKEY);

        final String latitude = Double.toString(latLng.latitude);
        final String longitude = Double.toString(latLng.longitude);

        RequestParams params = new RequestParams();
        params.put(KEY_REVERSEGEOCODING_VERSION, VALUE_REVERSEGEOCODING_VERSION);
        params.put(KEY_REVERSEGEOCODING_LATITUDE, latitude);
        params.put(KEY_REVERSEGEOCODING_LONGITUDE, longitude);
        params.put(KEY_REVERSEGEOCODING_COORDTYPE, VALUE_REVERSEGEOCODING_COORDTYPE);
        params.put(KEY_REVERSEGEOCODING_ADDRESSTYPE, VALUE_REVERSEGEOCODING_ADDRESSTYPE);

//        Log.d("safebike", "lntLng : " + latitude + ", " + longitude);

        int count = headers.length;
        for(int i = 0; i < count; i++) {
//            Log.d("safebike", "headers : " + headers[i]);
        }

//        Log.d("safebike", "----------------------------------------------------------------------------------------------------------------------------");
        client.get(context, SEARCH_REVERSEGEOCODING_URL, headers, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                /*
                 * fail에 따른 statusCode 처리
                 */
                listener.onFail(statusCode);

                String code = Integer.toString(statusCode);
//                Log.d("safebike", "code : " + code + " / responseString : " + responseString);

                if (headers != null) {
                    int count = headers.length;

                    for(int i = 0; i < count; i++) {
//                        Log.d("safebike", "headers : " + headers[i]);
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                /*
                 * success에 따른 statusCode 처리
                 */

                String code = Integer.toString(statusCode);
//                Log.d("safebike", "code : " + code + " / responseString : " + responseString);
//                Log.d("safebike", "headers : " + headers);


                AddressInfoResult result = poiGson.fromJson(responseString, AddressInfoResult.class);
                listener.onSuccess(result.addressInfo);
            }
        });
    }

    public static final String BICYCLE_ROUTE_UTL = "https://apis.skplanetx.com/tmap/routes/bicycle?version=1";

    private static final String KEY_BICYCLE_ROUTE_STARTX = "startX";
    private static final String KEY_BICYCLE_ROUTE_STARTY = "startY";
    private static final String KEY_BICYCLE_ROUTE_ENDX = "endX";
    private static final String KEY_BICYCLE_ROUTE_ENDY = "endY";
    private static final String KEY_BICYCLE_ROUTE_REQCOORDTYPE = "reqCoordType";
    private static final String KEY_BICYCLE_ROUTE_SEARCHOPTION = "searchOption";
    private static final String KEY_BICYCLE_ROUTE_RESCOORDTYPE = "resCoordType";

    private static final String VALUE_BICYCLE_ROUTE_REQCOORDTYPE = "WGS84GEO";
    private static final String VALUE_BICYCLE_ROUTE_RESCOORDTYPE = "WGS84GEO";

    private static final String BICYCLE_ROUTE_GEOMETRY_TYPE_POINT = "Point";

    private static final String POINTTYPE_ST = "ST";
    private static final String POINTTYPE_CI = "CI";

    public void findRoute(Context context, double startX, double startY, double endX, double endY, int searchOption, final OnResultListener<BicycleRouteInfo> listener) {
//        Log.d("safebike", "NavigationNetworkManager.findRoute");
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(KEY_TMAP_HEADERS_ACCEPT, VALUE_TMAP_HEADERS_ACCEPT);
        headers[1] = new BasicHeader(KEY_TMAP_HEADERS_APPKEY, VALUE_TMAP_HEADERS_APPKEY);

        RequestParams params = new RequestParams();
        params.put(KEY_BICYCLE_ROUTE_STARTX, startX);
        params.put(KEY_BICYCLE_ROUTE_STARTY, startY);
        params.put(KEY_BICYCLE_ROUTE_ENDX, endX);
        params.put(KEY_BICYCLE_ROUTE_ENDY, endY);
        params.put(KEY_BICYCLE_ROUTE_REQCOORDTYPE, VALUE_BICYCLE_ROUTE_REQCOORDTYPE);
        params.put(KEY_BICYCLE_ROUTE_SEARCHOPTION, searchOption);
        params.put(KEY_BICYCLE_ROUTE_RESCOORDTYPE, VALUE_BICYCLE_ROUTE_RESCOORDTYPE);

//        Log.d("safebike", "----------------------------------------------------------------------------------------------------------------------------");

        client.post(context, BICYCLE_ROUTE_UTL, headers, params, null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);

//                Log.d("safebike", "onFailure");

                String code = Integer.toString(statusCode);
//                Log.d("safebike", "code : " + code + " / responseString : " + responseString);

                if (headers != null) {
                    int count = headers.length;

                    for(int i = 0; i < count; i++) {
//                        Log.d("safebike", "headers : " + headers[i]);
                    }
                }

                if (statusCode == 400) {
                    TmapResponseInfoResult info = tmapResponseInfoGson.fromJson(responseString, TmapResponseInfoResult.class);

                    listener.onFail(Integer.parseInt(info.error.code));
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                Log.d("safebike", "onSuccess");

                String code = Integer.toString(statusCode);
//                Log.d("safebike", "code : " + code + " / responseString : " + responseString);

                if (headers != null) {
                    int count = headers.length;

                    for(int i = 0; i < count; i++) {
//                        Log.d("safebike", "headers : " + headers[i]);
                    }
                }

                BicycleRouteInfo info = bicycleRouteGson.fromJson(responseString, BicycleRouteInfo.class);

                for (int i = 0; i < info.features.size(); i++) {
                    BicycleFeature feature = info.features.get(i);

                    if ((feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_ST))) {
                        info.features.remove(i);
                    } else if ((feature.geometry.type.equals(BICYCLE_ROUTE_GEOMETRY_TYPE_POINT) && feature.properties.pointType.equals(POINTTYPE_CI))) {
                        info.features.remove(i);
                    }
                }

                listener.onSuccess(info);
            }
        });

    }
}
