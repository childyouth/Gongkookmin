package com.example.gongkookmin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class LoginActivity extends AppCompatActivity {

    Button btn_login;
    EditText input_id;
    EditText input_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        input_id = findViewById(R.id.inputID_EditText);
        input_pw = findViewById(R.id.inputPW_EditText);
        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = input_id.getText().toString();
                String pw = input_pw.getText().toString();
                JsonMaker json = new JsonMaker();
                json.putData("email",id);
                json.putData("password",pw);

                BackgroundTask task = new BackgroundTask();
                task.execute(getResources().getString(R.string.server_address)+"rest-auth/login/"
                        ,HttpRequestHelper.POST,json.toString());
            }
        });

        Button btn_go_register = findViewById(R.id.btn_go_register);

        btn_go_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    class BackgroundTask extends CommunicationTask {
        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
            JSONObject jsonObject;
            jsonObject = httpRequestHelper.getDataByJSONObject();   // 서버에게 받은 json
            if(jsonObject == null) {
                Toast.makeText(LoginActivity.this, "서버와의 연결에 문제가 있습니다", Toast.LENGTH_SHORT).show();
                return;
            }
            if(values[0]){
                String token;
                try {
                    token = jsonObject.getString("token");
                    JSONObject user = jsonObject.getJSONObject("user");
                    String username = user.getString("username");
                    String email = user.getString("email");
                    TokenHelper tokenHelper = new TokenHelper(getSharedPreferences(TokenHelper.PREF_NAME,MODE_PRIVATE));
                    if(tokenHelper.setToken(token) && tokenHelper.setEmail(email) && tokenHelper.setUserName(username)) {   // token 이 성공적으로 저장된다면
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        tokenHelper.clearToken();
                        Toast.makeText(LoginActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "죄송합니다. 이메일로 연락해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else{
                try{
                    Iterator<String> iter = jsonObject.keys();
                    while(iter.hasNext()){
                        String key = iter.next();
                        JSONArray value = jsonObject.getJSONArray(key);
                        switch(key){
                            case "non_field_errors":
                                Toast.makeText(LoginActivity.this, value.getString(0), Toast.LENGTH_SHORT).show();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "죄송합니다. 이메일로 연락해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
