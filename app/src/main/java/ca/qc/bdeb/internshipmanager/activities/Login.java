package ca.qc.bdeb.internshipmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    //Database local
    private Database db;
    private SQLiteDatabase sql;

    //Elements UI
    private EditText guiUser;
    private EditText guiPassword;
    private Button guiLogin;

    private JustineAPI client;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Set layout components
        setContentView(R.layout.activity_login);
        guiUser = findViewById(R.id.etUser);
        guiPassword= findViewById(R.id.etPassword);
        guiLogin= findViewById(R.id.btnLogin);

        guiLogin.setEnabled(false);

        //Set data bases
        db = Database.getInstance(getApplicationContext());
        sql = Database.getSql();

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

    /**
     * Permetre l'envoye d'une requete juste si les données envoyés par l'utilisateur ont le bon
     * format
     * @param username nom donné par l'utilisateur
     * @param password mot de passe donné par l'utilisateur
     */
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

    /**
     * Verifier si le nom d'utilisateur est bien un courriel
     * @param username la valeur évaluée
     * @return si la valeur donné est un courriel
     */
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

    /**
     * Verifier si le mot de passe n'est pas vide
     * @param password la valeur valeur évaluée
     * @return si la valeur n'est pas vide
     */
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    /**
     * Chaine de traitments faites pour valider la connection de l'utilisateur
     * @param view
     */
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
                       JSONObject teacher = new JSONObject(response.body().string());
                       //Sauvegarder informations de quel enseignant est connecté
                       ConnectionValidation.authToken = teacher.getString("access_token");
                       ConnectionValidation.authId = teacher.getString("id");
                       Log.d("TokenTag", "Token: " + ConnectionValidation.authToken +
                               " Id: " + ConnectionValidation.authId );

                       Toast.makeText(getApplicationContext(), "Connexion réussie!!",
                               Toast.LENGTH_LONG).show();

                       // Verifier à la BD Local si la compte existe déjà pour determiner comment
                       // la traiter.
                       Boolean accountExists = ! (db.queryForAccountByLocalId(ConnectionValidation
                               .authId) == null);
                       manageAccountTeacherSLQ(teacher, accountExists);

                       //Envoyer utilisateur vers activité principal de l'application.
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

    /**
     * Sauvegarder dans la BD local les données reçus comme reponse de la requete faite au moment
     * de la connexion. Ces données caractérisent une compte d'enseignant.
     * @param teacher c'est l'objet qui contient les informations de l'enseignant
     * @param exists Condition pour definir si il faut ajouter l'enseignant (false) ou s'il faut
     * juste actualiser ses informations (true)
     * @throws JSONException
     */
    private void manageAccountTeacherSLQ (JSONObject teacher, boolean exists) throws JSONException {
        String id = teacher.getString("id");
        String createdAt = teacher.getString("created_at");
        String email = teacher.getString("email");
        boolean isActive = teacher.getBoolean("est_actif");
        String password = null;
        String lastName = teacher.getString("nom");
        String firstName = teacher.getString("prenom");
        String updatedAt = teacher.getString("updated_at");
        int accountType = (Type.valueOf(teacher.getString("type_compte"))).ordinal();

        if(exists){
            Account teacherAccount = new Account(id, createdAt, null, email, isActive,
                    "projet", lastName,firstName,null, updatedAt, accountType);

            db.updateAccount(teacherAccount);
        } else{
            db.insertAccount(sql, id, createdAt, null, email, isActive,
                    password, lastName, firstName, null,
                    updatedAt, accountType);
        }
    }
}