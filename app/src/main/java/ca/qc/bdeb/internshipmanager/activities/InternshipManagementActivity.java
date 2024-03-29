package ca.qc.bdeb.internshipmanager.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import ca.qc.bdeb.internshipmanager.ConnectionValidation;
import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.customviews.DropdownEnterprisesAdapter;
import ca.qc.bdeb.internshipmanager.customviews.DropdownStudentsAdapter;
import ca.qc.bdeb.internshipmanager.customviews.FlagSelector;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.reseau.JustineAPI;
import ca.qc.bdeb.internshipmanager.reseau.JustineAPIClient;
import ca.qc.bdeb.internshipmanager.systems.Database;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InternshipManagementActivity extends AppCompatActivity {

    private Database db;
    private JustineAPI client;

    public static final int INTENT_PHOTO_RESULT = 1;
    public static final int PERMISSION_CAMERA_CODE = 1;

    private ArrayList<TextInputLayout> inputFields;

    private CircleImageView ivImageEtudiant;
    private FloatingActionButton btnEditPhoto;

    private FlagSelector ib_new_flagSelector;

    private TextInputLayout til_list_etudiants, til_list_enterprises;

    private EditText et_adresse_stage,
            et_ville_stage, et_code_postal_stage, et_province_stage, et_list_annee_scolaire;

    private AutoCompleteTextView actv_list_etudiants, actv_list_enterprises;

    private Button btn_add_internship, btnModifyStartInternTime, btnModifyEndInternTime,
            btnModifyStartLunchTime, btnModifyEndLunchTime;

    private CheckBox cbWedInternDay, cbThuInternDay, cbFriInternDay, cbWedAMTutorAvail,
            cbThuAMTutorAvail, cbFriAMTutorAvail, cbWedPMTutorAvail,
            cbThuPMTutorAvail, cbFriPMTutorAvail;

    private RadioGroup rgAverageDuration;

    private EditText etComments;

    private String startHour, endHour, startLunch, endLunch;
    private int avgVisitDuring;

    private ArrayList<Account> list_etudiants;
    private ArrayList<Enterprise> list_enterprises;

    private DropdownStudentsAdapter arrayAdapter_etudiants;
    private DropdownEnterprisesAdapter arrayAdapter_enterprises;

    private Account selectedStudent;
    private Enterprise selectedEnterprise;

    private Internship internshipToModify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internship_management);

        db = Database.getInstance(getApplicationContext());
        client = JustineAPIClient.getRetrofit().create(JustineAPI.class);

        initViews();
        setViewsData();

        String internshipIdToModify = getIntent().getStringExtra(MainActivity.INTERNSHIP_ID_TO_MODIFY_KEY);
        //On check s'il s'agit d'une modification d'un internship
        if(internshipIdToModify != null && !internshipIdToModify.equals("")){
            setInternshipToModifyData(internshipIdToModify);
        }

    }

    private void initViews() {
        //On définit la toolbar comme back arrow
        MaterialToolbar toolbar = findViewById(R.id.topAppbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Photo de profil
        ivImageEtudiant = (CircleImageView) findViewById(R.id.ivImageEtudiant);
        btnEditPhoto = (FloatingActionButton) findViewById(R.id.btnEditPhoto);

        ib_new_flagSelector = (FlagSelector) findViewById(R.id.ib_new_flagSelector); //Pour la priorité

        //Références des listes dropdown
        //LIST ÉTUDIANTS
        til_list_etudiants = (TextInputLayout) findViewById(R.id.til_list_etudiants);
        actv_list_etudiants = (AutoCompleteTextView) findViewById(R.id.actv_list_etudiants);
        actv_list_etudiants.setThreshold(1);
        actv_list_etudiants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedStudent = (Account) adapterView.getItemAtPosition(i);

                //On définit l'image du compte
                ivImageEtudiant.setImageBitmap(selectedStudent.getPhoto());

                Log.d("INFO", selectedStudent.toString() + "");
            }
        });

        //LIST ENTERPRISES
        til_list_enterprises = (TextInputLayout) findViewById(R.id.til_list_nom_enterprises);
        actv_list_enterprises = (AutoCompleteTextView) findViewById(R.id.actv_list_nom_enterprises);
        actv_list_enterprises.setThreshold(1);
        actv_list_enterprises.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEnterprise = (Enterprise) adapterView.getItemAtPosition(i);
                //On set les informations de l'entreprise dans les fields pas modifiables
                setEntrepriseViewsData(selectedEnterprise);

                Log.d("Info", selectedEnterprise.toString() + "");
            }
        });

        //LIST ANNÉE SCOLAIRE
        et_list_annee_scolaire = (EditText) findViewById(R.id.et_annee_scolaire);

        //Références des autres fields
        et_adresse_stage = (EditText) findViewById(R.id.et_adresse_stage);
        et_ville_stage = (EditText) findViewById(R.id.et_ville_stage);
        et_code_postal_stage = (EditText) findViewById(R.id.et_code_postal_stage);
        et_province_stage = (EditText) findViewById(R.id.et_province_stage);

        btn_add_internship = (Button) findViewById(R.id.btn_add_internship);

        //Références des views contenant informations des stages

        //Journées de stage
        cbWedInternDay = (CheckBox) findViewById(R.id.cbWedInternDay);
        cbThuInternDay = (CheckBox) findViewById(R.id.cbThuInternDay);
        cbFriInternDay = (CheckBox) findViewById(R.id.cbFriInternDay);



        //Stage schedule
        btnModifyStartInternTime = (Button) findViewById(R.id.btnModifyStartInternTime);
        btnModifyEndInternTime = (Button) findViewById(R.id.btnModifyEndInternTime);
        //Diner schedule
        btnModifyStartLunchTime = (Button) findViewById(R.id.btnModifyStartLunchTime);
        btnModifyEndLunchTime = (Button) findViewById(R.id.btnModifyEndLunchTime);

        setTimeButtonsListeners();

        //Durée de la visite
        rgAverageDuration = (RadioGroup) findViewById(R.id.rgAverageDuration);
        rgAverageDuration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch(i) {
                    case R.id.rb30Minutes:
                        avgVisitDuring = 30;
                        break;
                    case R.id.rb45Minutes:
                        avgVisitDuring = 45;
                        break;
                    case R.id.rb1Hour:
                        avgVisitDuring = 60;
                        break;
                }
            }
        });

        //Disponibilité des tuteurs pour le stage
        cbWedAMTutorAvail = (CheckBox) findViewById(R.id.cbWedAMTutorAvail);
        cbThuAMTutorAvail = (CheckBox) findViewById(R.id.cbThuAMTutorAvail);
        cbFriAMTutorAvail = (CheckBox) findViewById(R.id.cbFriAMTutorAvail);

        cbWedPMTutorAvail = (CheckBox) findViewById(R.id.cbWedPMTutorAvail);
        cbThuPMTutorAvail = (CheckBox) findViewById(R.id.cbThuPMTutorAvail);
        cbFriPMTutorAvail = (CheckBox) findViewById(R.id.cbFriPMTutorAvail);

        etComments = (EditText) findViewById(R.id.etComments);

        //On ajoute les text fields pour vérifier s'ils sont vides avant l'ajout du stage.
        inputFields = new ArrayList<>();

        inputFields.add(til_list_etudiants);
        inputFields.add(til_list_enterprises);
    }

    private void setTimeButtonsListeners() {
        //Heure de début du stage
        btnModifyStartInternTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newHour = materialTimePicker.getHour();
                        int newMinute = materialTimePicker.getMinute();
                        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
                        Calendar startTimeCal = Calendar.getInstance();

                        startTimeCal.set(Calendar.HOUR_OF_DAY, newHour);
                        startTimeCal.set(Calendar.MINUTE, newMinute);

                        startHour = formater.format(startTimeCal.getTime());

                        String startHourText = newHour + ":" + newMinute;
                        //on set le text du boutton
                        btnModifyStartInternTime.setText(startHourText);
                    }
                });

                materialTimePicker.show(getSupportFragmentManager(), "ABC");
            }
        });

        //Heure de fin du stage
        btnModifyEndInternTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newHour = materialTimePicker.getHour();
                        int newMinute = materialTimePicker.getMinute();
                        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
                        Calendar endTimeCal = Calendar.getInstance();

                        endTimeCal.set(Calendar.HOUR_OF_DAY, newHour);
                        endTimeCal.set(Calendar.MINUTE, newMinute);

                        endHour = formater.format(endTimeCal.getTime());

                        String endHourText = newHour + ":" + newMinute;
                        btnModifyEndInternTime.setText(endHourText);
                    }
                });

                materialTimePicker.show(getSupportFragmentManager(), "ABC");
            }
        });

        //Heure de début du diner
        btnModifyStartLunchTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newHour = materialTimePicker.getHour();
                        int newMinute = materialTimePicker.getMinute();
                        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
                        Calendar startLunchCal = Calendar.getInstance();

                        startLunchCal.set(Calendar.HOUR_OF_DAY, newHour);
                        startLunchCal.set(Calendar.MINUTE, newMinute);

                        startLunch = formater.format(startLunchCal.getTime());

                        String startLunchText = newHour + ":" + newMinute;
                        btnModifyStartLunchTime.setText(startLunchText);
                    }
                });

                materialTimePicker.show(getSupportFragmentManager(), "ABC");
            }
        });

        //Heure de fin du diner
        btnModifyEndLunchTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int newHour = materialTimePicker.getHour();
                        int newMinute = materialTimePicker.getMinute();
                        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
                        Calendar endLunchCal = Calendar.getInstance();

                        endLunchCal.set(Calendar.HOUR_OF_DAY, newHour);
                        endLunchCal.set(Calendar.MINUTE, newMinute);

                        endLunch = formater.format(endLunchCal.getTime());

                        String endLunchText = newHour + ":" + newMinute;
                        btnModifyEndLunchTime.setText(endLunchText);
                    }
                });

                materialTimePicker.show(getSupportFragmentManager(), "ABC");
            }
        });
    }

    /**
     * Initialise et définit les données des views dans l'activité.
     * Les données des différents dropdowns sont définis icitte.
     */
    private void setViewsData() {
        //On set la liste des étudiants
        list_etudiants = new ArrayList<>();

        for (Account account : db.getStudentsAccount()) {
            list_etudiants.add(account);

        }

        arrayAdapter_etudiants = new DropdownStudentsAdapter(getApplicationContext(), list_etudiants);
        actv_list_etudiants.setAdapter(arrayAdapter_etudiants);

        //On set la liste des entreprises
        list_enterprises = new ArrayList<>();

        for (Enterprise enterprise : db.getEntreprises()) {
            list_enterprises.add(enterprise);

        }

        arrayAdapter_enterprises = new DropdownEnterprisesAdapter(getApplicationContext(), list_enterprises);
        actv_list_enterprises.setAdapter(arrayAdapter_enterprises);


        //On set la liste des années
        //TODO peut-être il faudra changer ça
        et_list_annee_scolaire.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

    }

    /**
     * On définit les données du l'internship à modifier.
     */
    private void setInternshipToModifyData(String idIntenship) {
        internshipToModify = db.getInternshipById(idIntenship);
        //Log.d("Icitte", internshipToModify.toString());
        //On désactive la liste des étudiants et entreprises
        til_list_enterprises.setEnabled(false);
        til_list_etudiants.setEnabled(false);

        //On définit le titre de la page
        getSupportActionBar().setTitle(R.string.title_modify_internship);

        //On définit l'image du compte
        ivImageEtudiant.setImageBitmap(internshipToModify.getStudentAccount().getPhoto());

        //On définit l'année scolaire
        et_list_annee_scolaire.setText(internshipToModify.getSchoolYear());

        //On définit le nom de l'entreprise et l'entreprise selected
        actv_list_enterprises.setText(internshipToModify.getEnterprise().getName());
        selectedEnterprise = internshipToModify.getEnterprise();
        setEntrepriseViewsData(selectedEnterprise);

        //On définit le nom de l'étudiant et l'étudiant sélected.
        actv_list_etudiants.setText(internshipToModify.getStudentAccount().getFullName(), false);
        selectedStudent = internshipToModify.getStudentAccount();

        //On définit la priorité
        ib_new_flagSelector.setPriority(internshipToModify.getPriority());

        //On définit les informations du stage
        setInternshipDaysChecked(internshipToModify.getInternshipDays());
        //les boutons de temps
        startHour = internshipToModify.getStartHour();
        btnModifyStartInternTime.setText(startHour);
        endHour = internshipToModify.getEndHour();
        btnModifyEndInternTime.setText(endHour);

        startLunch = internshipToModify.getStartLunch();
        btnModifyStartLunchTime.setText(startLunch);
        endLunch = internshipToModify.getEndLunch();
        btnModifyEndLunchTime.setText(endLunch);

        //Durée moyenne des visites
        switch(internshipToModify.getAverageVisitDuring()){
            case 30:
                rgAverageDuration.check(R.id.rb30Minutes);
                break;
            case 45:
                rgAverageDuration.check(R.id.rb45Minutes);
                break;
            case 60:
                rgAverageDuration.check(R.id.rb1Hour);
                break;
        }

        setTutorDisponibilitiesChecked(internshipToModify.getTutorDisponibilities());

        etComments.setText(internshipToModify.getComments());
        //On définit le text du boutton
        btn_add_internship.setText(getString(R.string.modify_internship_text));
    }


    private void setEntrepriseViewsData(Enterprise enterprise){
        et_adresse_stage.setText(enterprise.getAddress());
        et_ville_stage.setText(enterprise.getTown());
        et_code_postal_stage.setText(enterprise.getPostalCode());
        et_province_stage.setText(enterprise.getProvince());
    }

    public void onClickModifyPhoto(View view){
        //On demande la permission pour la camd'abord
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_CAMERA_CODE);
    }

    /**
     * Ajoute un nouveau stage en fonction des informations founies.
     * @param view View qui fait appelle à la méthode.
     */
    public void onClickAjouterStage(View view){
        boolean areFieldsFull = true;

        //On vérifie que les fields ne soient pas vides.
        for (TextInputLayout textField : inputFields) {
            if(textField.getEditText().getText().toString().equals("") || textField.getEditText().getText().toString().equals(null)){
                textField.setErrorEnabled(true);
                textField.setError("Vide!");
                areFieldsFull = false;
            } else {
                textField.setErrorEnabled(false);
            }
        }

        if(areFieldsFull){

            //On check s'il faut modifier un internship ou crée un nouveau.
            if(!getIntent().getStringExtra(MainActivity.INTERNSHIP_ID_TO_MODIFY_KEY).equals("")){
                modifyInternship();
            } else{
                createNewInternship();
            }
        }
    }

    private void modifyInternship() {
        if(internshipToModify == null){
            Log.d("Info", "Érreur pour modifier l'internship, Il est null");
            return;
        }

        //Photo du internship
        Bitmap photo = ((BitmapDrawable) ivImageEtudiant.getDrawable()).getBitmap();
        internshipToModify.getStudentAccount().setPhoto(photo);
        internshipToModify.setSchoolYear(et_list_annee_scolaire.getText().toString());
        internshipToModify.setEnterprise(selectedEnterprise);
        internshipToModify.setAccountStudent(selectedStudent);
        internshipToModify.setPriority(ib_new_flagSelector.getPriority());
        //Le même prof reste pour le même internship
        internshipToModify.setInternshipDays(getInternshipDaysChecked());
        internshipToModify.setStartHour(this.startHour);
        internshipToModify.setEndHour(this.endHour);
        internshipToModify.setStartLunch(this.startLunch);
        internshipToModify.setEndLunch(this.endLunch);
        internshipToModify.setAverageVisitDuring(this.avgVisitDuring);
        internshipToModify.setTutorDisponibility(getTutorDisponibilitiesChecked());
        internshipToModify.setComments(etComments.getText().toString());
//        internshipToModify.setId(id);

        //On modifie le compte étudiant avec sa photo
        db.updateAccount(internshipToModify.getStudentAccount());

        //On le modifie dans la bd local
        db.updateInternship(internshipToModify);

        //on le modifie dans la bd à distance
        HashMap<String, Object> requete = new HashMap<>();
        requete.put("id", internshipToModify.getIdInternship());
        requete.put("annee", internshipToModify.getSchoolYear()); //anneeScolaire
        requete.put("commentaire", internshipToModify.getComments());
        requete.put("id_entreprise", internshipToModify.getEnterprise().getEnterpriseId());
        requete.put("id_etudiant", internshipToModify.getStudentAccount().getAccountId());
        requete.put("id_professeur", internshipToModify.getTeacherAccount().getAccountId());
        requete.put("heureDebut", internshipToModify.getStartHour());
        requete.put("heureFin", internshipToModify.getEndHour());
        requete.put("heureDebutPause", internshipToModify.getStartLunch());
        requete.put("heureFinPause", internshipToModify.getEndLunch());

        if(internshipToModify.getPriority() == Internship.Priority.HIGH){
            requete.put("priorite", "HAUTE");
        } else if(internshipToModify.getPriority() == Internship.Priority.MEDIUM){
            requete.put("priorite", "MOYENNE");
        } else {
            requete.put("priorite", "BASSE");
        }

        client.ajouterStage(ConnectionValidation.authToken, requete).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("justine_tag", response.toString());
                try {
                    if (response.code() == 200) {
                        JSONObject stage = new JSONObject(response.body().string());
                        Log.d("MODIFY STAGE", "MODIFY STAGE : SUCCESS \n>>" + stage);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

        returnToPreviousActivity();
    }


    /**
     * Recupère les données dans les field correspondants pour créer un internship.
     */
    private void createNewInternship() {

        Bitmap studentPhoto = ((BitmapDrawable) ivImageEtudiant.getDrawable()).getBitmap();
        String anneeScolaire = et_list_annee_scolaire.getText().toString();
        Enterprise entreprise = selectedEnterprise;
        Account student = selectedStudent;
        Account teacher = db.queryForAccountByLocalId(ConnectionValidation.authId);

        //On modifie le compte étudiant avec sa nouvelle photo
        student.setPhoto(studentPhoto);
        db.updateAccount(student);

        //définir les données pour le stage
        String internshipDays = getInternshipDaysChecked();
        String startHour = this.startHour;
        String endHour = this.endHour;
        String startLunch = this.startLunch;
        String endLunch = this.endLunch;
        int averageVisitDuring = avgVisitDuring;
        String tutorDisponibility = getTutorDisponibilitiesChecked();
        String comments = etComments.getText().toString();
        String id = UUID.randomUUID().toString();
        Internship.Priority priority = ib_new_flagSelector.getPriority();

        //On l'ajoute à la BD
        db.insertInternship(id, anneeScolaire, entreprise.getEnterpriseId(), student.getAccountId(),
                teacher.getAccountId(), priority, internshipDays, startHour,
                endHour, startLunch, endLunch, averageVisitDuring, tutorDisponibility, comments);

        //on le modifie dans la bd à distance

        HashMap<String, Object> requete = new HashMap<>();
        requete.put("id", id);
        requete.put("annee", anneeScolaire); //anneeScolaire
        requete.put("commentaire", comments);
        requete.put("id_entreprise", entreprise.getEnterpriseId());
        requete.put("id_etudiant", student.getAccountId());
        requete.put("id_professeur", teacher.getAccountId());
        requete.put("heureDebut", startHour);
        requete.put("heureFin", endHour);
        requete.put("heureDebutPause", startLunch);
        requete.put("heureFinPause", endLunch);

        if(priority == Internship.Priority.HIGH){
            requete.put("priorite", "HAUTE");
        } else if(priority == Internship.Priority.MEDIUM){
            requete.put("priorite", "MOYENNE");
        } else {
            requete.put("priorite", "BASSE");
        }

        client.ajouterStage(ConnectionValidation.authToken, requete).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("justine_tag", response.toString());
                try {
                    if (response.code() == 200) {
                        JSONObject stage = new JSONObject(response.body().string());
                        Log.d("MODIFY STAGE", "MODIFY STAGE : SUCCESS \n>>" + stage);
                    }else{
                        Log.d("MODIFY STAGE", "MODIFY STAGE : FAIL \n>>" + response.raw());
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

        //On retourne à l'activité précedente après avoir insérer le nouveau étudiant
        returnToPreviousActivity();
    }

    private String getTutorDisponibilitiesChecked() {
        String tutorDisponibility = "";

        if(cbWedAMTutorAvail.isChecked()){
            tutorDisponibility += "wednesdayAM|";
        }

        if(cbWedPMTutorAvail.isChecked()){
            tutorDisponibility += "wednesdayPM|";
        }

        if(cbThuAMTutorAvail.isChecked()){
            tutorDisponibility += "thursdayAM|";
        }

        if(cbThuPMTutorAvail.isChecked()){
            tutorDisponibility += "thursdayPM|";
        }

        if(cbFriAMTutorAvail.isChecked()){
            tutorDisponibility += "fridayAM|";
        }

        if(cbFriPMTutorAvail.isChecked()){
            tutorDisponibility += "fridayPM|";
        }

        return tutorDisponibility;
    }

    private void setTutorDisponibilitiesChecked(String tutorDisponibilitiesDays){
        String[] days = tutorDisponibilitiesDays.split("\\|");

        for (String day : days) {
            switch (day){
                case "wednesdayAM":
                    cbWedAMTutorAvail.setChecked(true);
                    break;
                case "wednesdayPM":
                    cbWedPMTutorAvail.setChecked(true);
                    break;
                case "thursdayAM":
                    cbThuAMTutorAvail.setChecked(true);
                    break;
                case "thursdayPM":
                    cbThuPMTutorAvail.setChecked(true);
                    break;
                case "fridayAM":
                    cbFriAMTutorAvail.setChecked(true);
                    break;
                case "fridayPM":
                    cbFriPMTutorAvail.setChecked(true);
                    break;
            }
        }
    }

    private String getInternshipDaysChecked(){
        String internshipDays = "";

        //TODO On peut optimiser ça par assignant un onCheckListener à chaque checkboxe et remplir un string en fonction de ça
        if(cbWedInternDay.isChecked()){
            internshipDays += "wednesday|";
        }

        if(cbThuInternDay.isChecked()){
            internshipDays += "thursday|";
        }

        if(cbFriInternDay.isChecked()){
            internshipDays += "friday";
        }

        return internshipDays;
    }

    private void setInternshipDaysChecked(String internshipDays){
        String[] days = internshipDays.split("\\|");
        Log.d("Icitte", days[0] + "");

        for (String day : days) {
            switch (day){
                case "wednesday":
                    cbWedInternDay.setChecked(true);
                    break;
                case "thursday":
                    cbThuInternDay.setChecked(true);
                    break;
                case "friday":
                    cbFriInternDay.setChecked(true);
                    break;
            }
        }
    }

    /**
     * Permet de retourner à l'activité précédente
     */
    private void returnToPreviousActivity() {
        Intent intentMessage = new Intent();

        setResult(RESULT_OK, intentMessage);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_CAMERA_CODE:
                //On check si on a la permission ou pas
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //On lance l'intent pour recupérer la photo
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, INTENT_PHOTO_RESULT);

                }else{
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == INTENT_PHOTO_RESULT){

            if(data == null){
                return;
            }

            //Recupère la photo
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            //On le définit dans l'ImageView
            ivImageEtudiant.setImageBitmap(photo);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



}