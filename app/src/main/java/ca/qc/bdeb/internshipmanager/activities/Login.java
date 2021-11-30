package ca.qc.bdeb.internshipmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import ca.qc.bdeb.internshipmanager.ConnectionValidation;
import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.reseau.JustineAPI;
import ca.qc.bdeb.internshipmanager.reseau.JustineAPIClient;
import ca.qc.bdeb.internshipmanager.systems.Database;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private Database db;

    private EditText guiUser;
    private EditText guiPassword;
    private Button guiLogin;

    private JustineAPI client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        guiUser = findViewById(R.id.etUser);
        guiPassword= findViewById(R.id.etPassword);
        guiLogin= findViewById(R.id.btnLogin);

        db = Database.getInstance(getApplicationContext());
        guiLogin.setEnabled(false);

        client = JustineAPIClient.getRetrofit().create(JustineAPI.class);

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginDataChanged(guiUser.getText().toString(),
                        guiPassword.getText().toString());
            }
        };
        guiUser.addTextChangedListener(afterTextChangedListener);
        guiPassword.addTextChangedListener(afterTextChangedListener);

    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            guiUser.setError(getString(R.string.invalid_username));
        } else if (!isPasswordValid(password)) {
            guiPassword.setError(getString(R.string.invalid_password));
        } else {
            guiUser.setError(null);
            guiPassword.setError(null);
            guiLogin.setEnabled(true);
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public void connecter(View view) {

        HashMap<String, Object> loginData = new HashMap<>();
        if (!guiUser.getText().toString().isEmpty()){
            loginData.put("email", guiUser.getText().toString() );
        }
        if (!guiPassword.getText().toString().isEmpty()){
            loginData.put("mot_de_passe", guiPassword.getText().toString() );
        }

        Call<ResponseBody> call = client.connecter(loginData);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
               if(response.code() == 200 && response.body() != null) {
                   try {
                       JSONObject rep = new JSONObject(response.body().string());
                       ConnectionValidation.authToken = rep.getString("access_token");
                       ConnectionValidation.authId = rep.getString("id");
                       Log.d("Icitte", "Token: " + ConnectionValidation.authToken + " Id: " + ConnectionValidation.authId );

                       Toast.makeText(getApplicationContext(), "Connexion réussie!!", Toast.LENGTH_LONG).show();
                       Intent intent = new Intent(Login.this, MainActivity.class);
                       startActivity(intent);
                       finish();

                   } catch (JSONException e) {
                       e.printStackTrace();
                   } catch (IOException e) {
                       e.printStackTrace();
                   } finally {
                   }
               }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", t.toString());
                Toast.makeText(getApplicationContext(), "Echec de connexion!!", Toast.LENGTH_LONG).show();
            }
        });
    }
}