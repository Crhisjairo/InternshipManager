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
import java.util.HashMap;

import ca.qc.bdeb.internshipmanager.ConnectionValidation;
import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;
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

    public static final String INTERNSHIP_ID_TO_MODIFY_KEY = "TO_MODIFY";
    private NavigationView navigationView;

    //Persistence de données
    private JustineAPI client;
    private Database db;
    private SQLiteDatabase sql;
    private ArrayList<Internship> internships;

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

        client = JustineAPIClient.getRetrofit().create(JustineAPI.class);
        verifyAuth();
        connexionAPI();

        // Navigation Bar
        MaterialToolbar toolbar = findViewById(R.id.topAppbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

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

        // On set le comportment des clicks sur le menu
        navigationView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                item.setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);

                if (ConnectionValidation.authToken.isEmpty()) {
                    demanderConnection();
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
                            client.deconnecter(ConnectionValidation.authToken);
                            ConnectionValidation.authToken = "";
                            ConnectionValidation.authId = "";
//                            replaceFragment(new LogoutFragment());
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

    private void verifyAuth() {
        if (ConnectionValidation.authToken.isEmpty()) {
            demanderConnection();
        } else {
            //tester si connection est valide
            HashMap<String, Object> user = new HashMap<>();
            user.put("id_compte", ConnectionValidation.authId);
            client.testerConnexion(ConnectionValidation.authToken, user).enqueue(
                    new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.code() != 200) {
                                demanderConnection();
                            }
                            else{

                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            demanderConnection();
                        }
                    }
            );
        }
    }

    /**
     * Permet la récuperation des données d'une base de données externe.
     */
    private void connexionAPI() {
        getStudents();
        getEntreprises();
        getStages();
    }

    /**
     * Récupérer les comptes du type "ETUDIANT"
     */
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
                                    boolean interExists = ! (db.queryForAccountByLocalId(intern
                                            .getString("id")) == null);
                                    manageAccountStudentSLQ(intern, interExists);
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

    /**
     * Sauvegarder dans la BD local les données reçus comme reponse de la requete faite au moment
     * de la connexion. Ces données caractérisent une compte d'etudiant
     * @param intern c'est l'objet qui contient les informations de l'etudiant
     * @param internExists Condition pour definir si il faut ajouter l'etudiant (false) ou s'il faut
     * juste actualiser ses informations (true)
     * @throws JSONException
     */
    private void manageAccountStudentSLQ(JSONObject intern, boolean internExists) throws JSONException {
        String id = intern.getString("id");
        String createdAt = intern.getString("created_at");
        String email = intern.getString("email");
        boolean isActive = intern.getBoolean("est_actif");
        String password = null;
        String lastName = intern.getString("nom");
        String firstName = intern.getString("prenom");
        String updatedAt = intern.getString("updated_at");
        int accountType = (Type.valueOf(intern.getString("type_compte"))).ordinal();

        if(internExists){
            Account studentAccount = new Account(id, createdAt, null, email, isActive,
                    "projet", lastName,firstName,null, updatedAt, accountType);

            db.updateAccount(studentAccount);
        }else{
            db.insertAccount(sql, id, createdAt, null, email, isActive,
                    password, lastName, firstName, null,
                    updatedAt, accountType);
        }
    }

    /**
     * Récupérer les entreprises qui sont dans la DB à distance
     */
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
                                    boolean entrepriseExistes = ! (db.queryForEntrepriseById(entreprise
                                            .getString("id")) == null);
                                    manageEntrepriseSLQ(entreprise, entrepriseExistes);
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

    /**
     * Sauvegarder dans la BD local les données reçus comme reponse de la requete faite au moment
     * de la connexion. Ces données caractérisent une compte d'entreprise
     * @param entreprise c'est l'objet qui contient les informations de l'entreprise
     * @param entrepriseExistes Condition pour definir si il faut ajouter l'etudiant (false) ou
     * s'il faut juste actualiser ses entreprise (true)
     * @throws JSONException
     */
    private void manageEntrepriseSLQ(JSONObject entreprise, boolean entrepriseExistes) throws JSONException {
        String id = entreprise.getString("id");
        String name = entreprise.getString("nom");
        String address = entreprise.getString("adresse");
        String postalCode = entreprise.getString("codePostal");
        String province = entreprise.getString("province");
        String city  = entreprise.getString("ville");

        if(entrepriseExistes){
            Enterprise newEntreprise = new Enterprise(id, name, address, city, province, postalCode);
            db.updateEntreprise(newEntreprise);
        } else{
            db.insertEnterprise(id, name, address, city, province, postalCode );
        }
    }

    /**
     * Récupérer les comptes des stages qui doivent être liées à un étudiant,
     * une entreprise et à un professeur existents.
     */
    public void getStages() {
        client.getStages(ConnectionValidation.authToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        JSONArray internships = new JSONArray(response.body().string());

                        for (int i = 0; i < internships.length(); i++) {
                            JSONObject internship = (JSONObject) internships.get(i);
                            JSONObject professeur = internship.getJSONObject("professeur");

                            boolean goodTeacher = (professeur.getString("id")).equals(ConnectionValidation.authId);
                            boolean internshipIsActive = (internship.getString("deletedAt").equals("null"));
                            if(goodTeacher && internshipIsActive){
                                String id = internship.getString("id");
                                boolean stageExists = !(db.queryForInternshipId(id) == null);
                                manageStageSLQ(internship, stageExists);
                            }
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                //Garantir que les méthodes respectent l'ordre necessaire de récupérer tous
                // les informations avant de créer le fragment avec son adapteur.
                initListFragment();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

    /**
     * Sauvegarder dans la BD local les données reçus comme reponse de la requete faite au moment
     * de la connexion. Ces données caractérisent une compte d'internship
     * @param internship c'est l'objet qui contient les informations de l'internship
     * @param stageExists Condition pour definir si il faut ajouter l'internship (false) ou
     * s'il faut juste actualiser ses entreprise (true)
     * @throws JSONException
     */
    private void manageStageSLQ(JSONObject internship, boolean stageExists) throws JSONException {

        String id = internship.getString("id");
        String anneeScolaire = internship.getString("anneeScolaire");
        String commentaire = internship.getString("commentaire");
        String heureDebut = internship.getString("heureDebut");
        String heureFin = internship.getString("heureFin");
        String heureDebutPause = internship.getString("heureDebutPause");
        String heureFinPause = internship.getString("heureFinPause");

        String priorite_str = internship.getString("priorite");
        Log.d("TAG", "manageStageSLQ HAUTE: " + priorite_str);
        Internship.Priority priority = null;
        if(priorite_str.equals("HAUTE")){
            priority = Internship.Priority.HIGH;
        } else if(priorite_str.equals("MOYENNE")){
            priority = Internship.Priority.MEDIUM;
        } else {
            priority = Internship.Priority.LOW;
        }

        JSONObject etudiant = internship.getJSONObject("etudiant");
        JSONObject professeur = internship.getJSONObject("professeur");
        JSONObject entreprise = internship.getJSONObject("entreprise");
        //Étudiant
        if (db.queryForAccountByLocalId(etudiant.get("id").toString()) == null){
            createAccount(etudiant);
        }

        //Entreprise
        if(db.queryForEntrepriseById(entreprise.get("id").toString()) == null){
            manageEntrepriseSLQ(entreprise,false);
        }

        if(stageExists){

            db.updateInternship(id, anneeScolaire, entreprise.getString("id"),
                    etudiant.getString("id"), professeur.getString("id"),
                    priority, heureDebut, heureFin,heureDebutPause, heureFinPause );
        }else{
            db.insertInternship(id, anneeScolaire, entreprise.get("id").toString(),
                    etudiant.getString("id"), professeur.get("id").toString(),
                    priority, "monday", heureDebut, heureFin,
                    heureDebutPause,heureFinPause,30,"wednesdayAM|",
                    commentaire);
        }

    }

    /**
     * Permet de recuperer un account de la BD à distance et l'inserer dans notre BD local
     * @param account objet JSON qui contient tous les informations sur le professeur ou l'etudiant
     * @throws JSONException
     */
    private void createAccount(JSONObject account) throws JSONException {
        String id = account.getString("id");
        String createdAt = account.getString("createdAt");
        String deletedAt = account.getString("deletedAt");
        String email = account.getString("email");
        boolean isActive = account.getBoolean("estActif");
        String password = "secret";
        String lastName = account.getString("nom");
        String firstName = account.getString("prenom");
        String updatedAt = account.getString("updatedAt");
        int accountType = (Type.valueOf(account.get("typeCompte")
                .toString())).ordinal();

        db.insertAccount(sql, id, createdAt, deletedAt, email, isActive, password,
                lastName, firstName, null, updatedAt, accountType);
    }

    /**
     * Initialise le fragment qui contient la liste des stages.
     */
    private void initListFragment() {
        if (ConnectionValidation.authToken.isEmpty()) {
            demanderConnection();
        }else {
            internships = db.getInternshipFromOneTeacher(ConnectionValidation.authId);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                    new ListInternshipFragment(internships)).commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        }
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

    /**
     * Gérer les routes des pages de l'application. La méthode dirait quel fragment il
     * faut charger dès le click dans le menu lateral d'options.
     * @param fragment c'est où il faut charger le layout desiré
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Envoier utilisateur vers la page de login pour l'authentification
     */
    private void demanderConnection() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}