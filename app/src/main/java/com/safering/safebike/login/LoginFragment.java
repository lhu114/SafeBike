package com.safering.safebike.login;



import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.safering.safebike.MainActivity;
import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.property.PropertyManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    TextView textLoginMain ;
    TextView textWarning;
    Button btnLogin;
    Button btnSignUp;
    Button btnFacebook;
    LoginButton facebookLogin;
    CallbackManager callbackManager;
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FacebookSdk.sdkInitialize(getContext());
        printKeyHash();
        Log.i("LoginFratOncreateView", "null");


        AccessToken.setCurrentAccessToken(null);

        if(AccessToken.getCurrentAccessToken() == null) {
            Log.i("accesToken:getUser", "null");
        }else{
            Log.i("accesToken:getUser", "not null");

        }

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        textLoginMain = (TextView)view.findViewById(R.id.text_login_main);
        textWarning = (TextView)view.findViewById(R.id.text_login_warning);
        btnLogin = (Button)view.findViewById(R.id.btn_login);
        btnSignUp = (Button)view.findViewById(R.id.btn_login_sign);
        btnFacebook = (Button)view.findViewById(R.id.btn_login_facebook);
        callbackManager = CallbackManager.Factory.create();
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin.callOnClick();

            }
        });

        facebookLogin = (LoginButton) view.findViewById(R.id.login_button);
        facebookLogin.setReadPermissions("user_friends");
        facebookLogin.setFragment(this);

       // facebookLogin.setLoginBehavior();
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.i("facebooklog", "suc");
                getUserInform();
                Toast.makeText(getContext(), "succ user id : " + loginResult.getAccessToken().getUserId(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancel() {
                // App code
                Log.i("facebooklog", "onCancel");

                Toast.makeText(getContext(), "cancel user id : ", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.i("facebooklog", "onError");

                Toast.makeText(getContext(), "fail user id : " + exception.toString(), Toast.LENGTH_LONG).show();

            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginInputFragment loginInputFragment = new LoginInputFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container, loginInputFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpFragment signUpFragment = new SignUpFragment();
                FragmentTransaction ft = ((LoginActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.login_container,signUpFragment);
                ft.addToBackStack(null);

                ft.commit();

            }
        });
        setFont();

        return view;
    }

    public void setFont(){
        textLoginMain.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.BMJUA));
        textWarning.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS));
        btnLogin.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));
        btnSignUp.setTypeface(FontManager.getInstance().getTypeface(getContext(),FontManager.NOTOSANS_M));
    }
    private void printKeyHash(){

        // Add code to print out the key hash
        try {
            PackageInfo info = getContext().getPackageManager().getPackageInfo(
                    "com.safering.safebike",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("KeyHashNameNotFound:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.d("NotSuchAlgorithmh:", e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    public void getUserInform(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null) {

            Log.i("accesToken:getUser", "null");
        }else{
            Log.i("accesToken:getUser", "not null");

        }
        String graphPath = "me/";
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture.type(square)");

        GraphRequest request = new GraphRequest(accessToken, graphPath,parameters, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.i("response", response.toString());

                String email = response.getJSONObject().optString("email");
                String id = response.getJSONObject().optString("name");
                try {
                    String userImage = response.getJSONObject().getJSONObject("picture").getJSONObject("data").optString("url");
                    PropertyManager.getInstance().setUserImagePath(userImage);

                }catch(Exception e){

                }finally {

                    PropertyManager.getInstance().setUserEmail(email);
                    PropertyManager.getInstance().setUserId(id);
                    PropertyManager.getInstance().setFacebookUser(1);
                    Intent intent = new Intent((LoginActivity) getActivity(), MainActivity.class);
                    startActivity(intent);
                    ((LoginActivity) getActivity()).finish();

                }

            }
        });
        request.executeAsync();
    }
}
