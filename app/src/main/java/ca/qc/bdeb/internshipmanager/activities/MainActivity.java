package ca.qc.bdeb.internshipmanager.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ca.qc.bdeb.internshipmanager.ConnectionValidation;
import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.fragments.CalendarFragment;
import ca.qc.bdeb.internshipmanager.fragments.InfoFragment;
import ca.qc.bdeb.internshipmanager.fragments.ListInternshipFragment;
import ca.qc.bdeb.internshipmanager.fragments.MapsFragment;
import ca.qc.bdeb.internshipmanager.fragments.SettingsFragment;
import ca.qc.bdeb.internshipmanager.reseau.JustineAPI;
import ca.qc.bdeb.internshipmanager.reseau.JustineAPIClient;
import ca.qc.bdeb.internshipmanager.systems.Database;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

enum Type{
    ADMIN,
    PROFESSEUR,
    ETUDIANT
}
public class MainActivity extends AppCompatActivity {

    private JustineAPI client;
    private Database db;
    private SQLiteDatabase sql;
    private ArrayList<Internship> internships;

    public static final String INTERNSHIP_ID_TO_MODIFY_KEY = "TO_MODIFY";

    // Demander la permission d'activer la localisation
    public static final int PERMISSION_MAP_CODE = 1;
    private boolean isLocationEnabled = false;
    private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Database.getInstance(getApplicationContext());
        sql = Database.getSql();
        connexionAPI();

        MaterialToolbar toolbar = findViewById(R.id.topAppbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        //demande la permission de prendre votre localisation
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions,
                PERMISSION_MAP_CODE);

        // On permet que le click ouvre et ferme le menu a cote
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //On set le ListInternshipFragment comme défaut et on séléctionne le premier
        // élément du drawer

        if (ConnectionValidation.authToken.isEmpty()) {
            connecter();
        } else {
            internships = db.getAllInternships();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                    new ListInternshipFragment(internships)).commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        }

        // On set le comportment des clicks sur le menu
        navigationView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                item.setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                if (ConnectionValidation.authToken.isEmpty()) {
                    connecter();
                } else {
                    switch (id) {
                        case R.id.nav_internship_list:
                            replaceFragment(new ListInternshipFragment(internships));
                            break;
                        case R.id.nav_map:
                            replaceFragment(new MapsFragment(isLocationEnabled));
                            break;
                        case R.id.nav_calendar:
                            replaceFragment(new CalendarFragment());
                            break;
                        case R.id.nav_settings:
                            replaceFragment(new SettingsFragment());
                            break;
                        case R.id.nav_logout:
                            Intent intent = new Intent(MainActivity.this, Login.class);
                            startActivity(intent);
                            finish();
                            break;
                        case R.id.nav_info:
                            replaceFragment(new InfoFragment());
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });
    }

    private void connexionAPI() {
        client = JustineAPIClient.getRetrofit().create(JustineAPI.class);
        getStudents();
        getEntreprises();
        getStages();
    }

    public void getStages() {
        client.getStages(ConnectionValidation.authToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        JSONArray internships = new JSONArray(response.body().string());
                        for (int i = 0; i < internships.length(); i++) {
                            JSONObject internship = (JSONObject) internships.get(i);
                            String id = internship.get("id").toString();
                            String createdAt = internship.get("createdAt").toString();
                            String updatedAt = internship.get("updatedAt").toString();
                            String anneeScolaire = internship.get("anneeScolaire").toString();
                            JSONObject etudiant = (JSONObject) internship.get("etudiant");
                            JSONObject professeur = (JSONObject) internship.get("professeur");
                            JSONObject entreprise = (JSONObject) internship.get("entreprise");
                            String priorite = internship.get("priorite").toString();
                            String commentaire = internship.get("commentaire").toString();
                            String heureDebut = internship.get("heureDebut").toString();
                            String heureFin = internship.get("heureFin").toString();
                            String heureDebutPause = internship.get("heureDebutPause").toString();
                            String heureFinPause = internship.get("heureFinPause").toString();

                            if (db.queryForAccountByAPIId(etudiant.get("id").toString()) == null){
                                addAccountsSQL(etudiant);
                            }
                            if (db.queryForAccountByAPIId(professeur.get("id").toString()) == null){
                                String id_prof = professeur.get("id").toString();
                                String created_at = professeur.get("createdAt").toString();
                                String email = professeur.get("email").toString();
                                boolean isActive = professeur.getBoolean("estActif");
                                String password = "secret";
                                String lastName = professeur.get("nom").toString();
                                String firstName = professeur.get("prenom").toString();
                                String updated_at = professeur.get("updatedAt").toString();
                                int accountType = (Type.valueOf(professeur.get("typeCompte")
                                        .toString())).ordinal();

                                db.insertAccount(sql, id_prof, created_at, null, email, isActive,
                                        password, lastName, firstName, null,
                                        updated_at, accountType);
                                Log.d("TAG", "onResponse: Prof added");
                            }
                            if(db.queryForEntrepriseById(entreprise.get("id").toString()) == null){
                                addEnterpriseSQL(entreprise);
                            }

                            db.insertInternship(id, anneeScolaire, entreprise.get("id").toString(),
                                    etudiant.get("id").toString(), professeur.get("id").toString(),
                                    Internship.Priority.LOW, "intershipDays", heureDebut,
                                    heureFin, heureDebutPause,heureFinPause,30,
                                    null,commentaire);
                        }

//                        if (stages.length() == 0) {
//                            ajouterStages();
//                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

    private void getEntreprises() {
        client.getEntreprises(ConnectionValidation.authToken)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.code() == 200) {
                                JSONArray entreprises = new JSONArray(response.body().string());
                                for (int i = 0; i < entreprises.length(); i++) {
                                    JSONObject entreprise = (JSONObject) entreprises.get(i);
                                    addEnterpriseSQL(entreprise);
                                }
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("TAG", t.toString());
                    }
                });
    }

    private void addEnterpriseSQL(JSONObject entreprise) throws JSONException {
        String id = entreprise.get("id").toString();
        String name = entreprise.get("nom").toString();
        String address = entreprise.get("adresse").toString();

        db.insertEnterprise(id, name, address, "Montreal",
                "Quebec", "postalCode" );
    }

    private void getStudents() {
        client.getComptesEleves(ConnectionValidation.authToken)
                .enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        JSONArray eleves = new JSONArray(response.body().string());
                        for (int i = 0; i < eleves.length(); i++) {
                            JSONObject intern = (JSONObject) eleves.get(i);
                            addAccountsSQL(intern);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void addAccountsSQL(JSONObject intern) throws JSONException {
        String id = intern.get("id").toString();
        String createdAt = intern.get("created_at").toString();
        String email = intern.get("email").toString();
        boolean isActive = intern.getBoolean("est_actif");
        String password = "secret";
        String lastName = intern.get("nom").toString();
        String firstName = intern.get("prenom").toString();
        String updatedAt = intern.get("updated_at").toString();
        int accountType = (Type.valueOf(intern.get("type_compte")
                .toString())).ordinal();

        db.insertAccount(sql, id, createdAt, null, email, isActive,
                password, lastName, firstName, null,
                updatedAt, accountType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_MAP_CODE:
                //On check si l'utilisateur a permis l'utilisation de sa localisation par
                // l'application ou pas
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    isLocationEnabled = true;
                }else{
                    Toast.makeText(getApplicationContext(), "Permission Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void connecter() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}