package ca.qc.bdeb.internshipmanager.systems;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;
import android.util.Log;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.dataclasses.Visit;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Classe qui va permettre d'implémenter toutes les tables utiles au projet.
 * Elle permet de faire les différentes requêtes à la BD.
 */
public class Database extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "InternshipSystem.db";

    private static Database instance = null;
    private final Context context;

    private static SQLiteDatabase db;

    private Account currentTeacherAccount;
    private ArrayList<Internship> internshipList;
    private ArrayList<Account> studentsAccountList;
    private ArrayList<Enterprise> enterprisesList;

    //region SQL Queries for firstInsert
    /**
     * Requête pour crée la table de comptes.
     */
    private static final String CREATION_TABLE_ACCOUNTS = "CREATE TABLE "
            + AccountTable.TABLE_NAME
            + " (" + AccountTable._ID + " VARCHAR2(255) PRIMARY KEY,"
            + AccountTable.FIRST_NAME + " VARCHAR2(255),"
            + AccountTable.LAST_NAME + " VARCHAR2(255),"
            + AccountTable.EMAIL + " VARCHAR2(255),"
            + AccountTable.PASSWORD + " VARCHAR2(255),"
            + AccountTable.PROFILE + " BLOB,"
            + AccountTable.CREATED_AT + " BLOB,"
            + AccountTable.DELETED_AT + " BLOB,"
            + AccountTable.UPDATED_AT + " BLOB,"
            + AccountTable.IS_ACTIVE + " INT(1),"
            + AccountTable.ACCOUNT_TYPE + " INT(11))";

    /**
     * Requête pour crée la table des enterprises.
     */
    private static final String CREATION_TABLE_ENTERPRISE = "CREATE TABLE "
            + EnterpriseTable.TABLE_NAME
            + " (" + EnterpriseTable._ID + " VARCHAR2(255) PRIMARY KEY,"
            + EnterpriseTable.ENTERPRISE_NAME + " VARCHAR2(255),"
            + EnterpriseTable.ENTERPRISE_ADDRESS + " VARCHAR2(255),"
            + EnterpriseTable.TOWN + " VARCHAR2(255),"
            + EnterpriseTable.PROVINCE + " VARCHAR2(255),"
            + EnterpriseTable.POSTAL_CODE + " VARCHAR2(7))";

    /**
     * Requête pour crée la table de visits.
     */
    private static final String CREATION_TABLE_VISIT = "CREATE TABLE "
            + VisitTable.TABLE_NAME
            + " (" + VisitTable._ID + " VARCHAR2(255) PRIMARY KEY,"
            + VisitTable.INTERNSHIP_ID + " VARCHAR2(255),"
            + VisitTable.DATE + " DATE,"
            + VisitTable.START_HOUR + " DATETIME,"
            + VisitTable.DURING + " VARCHAR2(255))";

    /**
     * Requête pour crée la table des stages.
     */
    private static final String CREATION_TABLE_INTERNSHIP = "CREATE TABLE "
            + InternshipTable.TABLE_NAME
            + " (" + InternshipTable._ID + " VARCHAR2(255) PRIMARY KEY,"
            + InternshipTable.SCHOOL_YEAR + " VARCHAR2(255),"
            + InternshipTable.ENTERPRISE_ID + " VARCHAR2(255),"
            + InternshipTable.STUDENT_ID + " INT(20),"
            + InternshipTable.PROFESSOR_ID + " INT(20)," +
            InternshipTable.PRIORITY + " VARCHAR2(255)," +
            InternshipTable.INTERNSHIP_DAYS + " VARCHAR2(255)," +
            InternshipTable.START_HOUR + " DATETIME," +
            InternshipTable.END_HOUR + " DATETIME," +
            InternshipTable.START_LUNCH+ " DATETIME," +
            InternshipTable.END_LUNCH + " DATETIME," +
            InternshipTable.AVERAGE_VISIT_DURING + " INT(20)," +
            InternshipTable.TUTOR_DISPONIBILITY + " VARCHAR2(255)," +
            InternshipTable.COMMENTS + " VARCHAR2(255))";

    //endregion

    /**
     * Créer l'instance de SQLlite
     */
    private Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        this.db = this.getWritableDatabase();

        internshipList = queryForAllInternships();
//        enterprisesList = queryForAllEnterprises();
//        studentsAccountList = queryForAllStudentsAccount();
    }

    public static  SQLiteDatabase getSql(){
        return db;
    }

    /**
     * Permetre de recuperer la Base de donnees
     *
     * @return
     */
    public static Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context);
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATION_TABLE_ACCOUNTS);
        sqLiteDatabase.execSQL(CREATION_TABLE_VISIT);
        sqLiteDatabase.execSQL(CREATION_TABLE_ENTERPRISE);
        sqLiteDatabase.execSQL(CREATION_TABLE_INTERNSHIP);

        this.db = sqLiteDatabase;

        /*
        internshipList = queryForAllInternships();
        enterprisesList = queryForAllEnterprises();
        studentsAccountList = queryForAllStudentsAccount();
        logInTeacher();*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    //region SQL Tables names
    /**
     * Classe qui permet de définir la table entreprise
     */
    public static class EnterpriseTable implements BaseColumns {
        public static final String TABLE_NAME = "enterprises";
        public static final String ENTERPRISE_NAME = "enterprise_name";
        public static final String ENTERPRISE_ADDRESS = "address";
        public static final String TOWN = "town";
        public static final String PROVINCE = "province";
        public static final String POSTAL_CODE = "postal_code";
    }

    /**
     * Classe qui permet de définir la table etudiant
     */
    public static class AccountTable implements BaseColumns {
        public static final String TABLE_NAME = "accounts";
        public static final String CREATED_AT = "creation_date";
        public static final String DELETED_AT = "delete_date";
        public static final String EMAIL = "email";
        public static final String IS_ACTIVE = "is_active";
        public static final String PASSWORD = "password";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String PROFILE = "profile";
        public static final String UPDATED_AT = "update_date";
        public static final String ACCOUNT_TYPE = "account_type";
    }

    /**
     * Classe qui va permettre d'implémenter la table visite
     */
    public static class InternshipTable implements BaseColumns {
        public static final String TABLE_NAME = "internship";
        public static final String SCHOOL_YEAR = "year_school";
        public static final String ENTERPRISE_ID = "enterprise_id";
        public static final String STUDENT_ID = "student_id";
        public static final String PROFESSOR_ID = "professor_id";
        public static final String PRIORITY = "priority";
        public static final String INTERNSHIP_DAYS = "internship_days";
        public static final String START_HOUR = "start_hour";
        public static final String END_HOUR = "end_hour";
        public static final String START_LUNCH = "start_lunch";
        public static final String END_LUNCH = "end_lunch";
        public static final String AVERAGE_VISIT_DURING = "average_visit_during";
        public static final String TUTOR_DISPONIBILITY = "tutor_disponibility";
        public static final String COMMENTS = "comments";
    }

    /**
     * Classe qui va permettre d'implémenter la table stage
     */
    public static class VisitTable implements BaseColumns {
        public static final String TABLE_NAME = "visits";
        public static final String INTERNSHIP_ID = "stage_id";
        public static final String DATE = "date";
        public static final String START_HOUR = "start_hour";
        public static final String DURING = "during";
    }
    //endregion

    /**
     * Methode qui va permettre d'insérer un nouvel étudiant
     */
    public void insertAccount(SQLiteDatabase db, String id, String createdAt, String deletedAt, String email, boolean isActive,
                              String password, String lastName, String firstName, Bitmap photo, String updatedAt,
                              int accountType) {

        ContentValues values = new ContentValues();
        values.put(AccountTable._ID, id);
        values.put(AccountTable.CREATED_AT, createdAt);
        values.put(AccountTable.DELETED_AT, deletedAt);
        values.put(AccountTable.EMAIL, email);
        values.put(AccountTable.PASSWORD, password);
        values.put(AccountTable.LAST_NAME, lastName);
        values.put(AccountTable.FIRST_NAME, firstName);

        //Pour la photo
        byte[] img;

        if(photo != null){
            ByteArrayOutputStream photoInBytes = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, photoInBytes);
            img = photoInBytes.toByteArray();

        }else{
            //On donne une image par défaut
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile);
            ByteArrayOutputStream photoInBytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, photoInBytes);

            img = photoInBytes.toByteArray();
        }
        //On l'ajoute
        values.put(AccountTable.PROFILE, img);


        values.put(AccountTable.UPDATED_AT, updatedAt);
        values.put(AccountTable.ACCOUNT_TYPE, accountType);

        values.put(AccountTable.IS_ACTIVE, isActive ? 1 : 0);

        db.insert(AccountTable.TABLE_NAME, null, values);
    }

    /**
     * Methode qui va permettre d'insérer une nouvelle entreprise
     */
    public void insertEnterprise(String id, String name, String address, String town,
                                   String province, String postalCode) {

        ContentValues values = new ContentValues();
        values.put(EnterpriseTable._ID, id);
        values.put(EnterpriseTable.ENTERPRISE_NAME, name);
        values.put(EnterpriseTable.ENTERPRISE_ADDRESS, address);
        values.put(EnterpriseTable.PROVINCE, province);
        values.put(EnterpriseTable.POSTAL_CODE, postalCode);
        values.put(EnterpriseTable.TOWN, town);

        db.insert(EnterpriseTable.TABLE_NAME, null, values);
    }

    /**
     * Methode qui va permettre d'insérer une nouvelle visite
     */
    public void insertVisit(String internshipId ,String date, String startHour, String during) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(VisitTable._ID, UUID.randomUUID().toString());
        values.put(VisitTable.INTERNSHIP_ID, internshipId);
        values.put(VisitTable.DATE, date);
        values.put(VisitTable.START_HOUR, startHour);
        values.put(VisitTable.DURING, during);

        db.insert(VisitTable.TABLE_NAME, null, values);
    }

    /**
     * Insert un nouveau stage dans la BD.
     * @param schoolYear Année scolaire.
     * @param idEntreprise Id de l'entreprise lié au stage.
     * @param idStudentAccount Id du compte de l'étudiant lié au stage.
     * @param idTeacherAccount id du compte du professeur lié au stage.
     * @param priority Priorité du nouveau stage.
     */
    public void insertInternship(String id, String schoolYear, String idEntreprise,
                                 String idStudentAccount, String idTeacherAccount,
                                 Internship.Priority priority, String internshipDays ,String startHour, String endHour,
                                 String startLunch, String endLunch, int averageVisitDuring,
                                 String tutorDisponibility, String comments) {

        ContentValues values = new ContentValues(); //UUID.randomUUID().toString()
        values.put(InternshipTable._ID, id);
        values.put(InternshipTable.SCHOOL_YEAR, schoolYear);
        values.put(InternshipTable.ENTERPRISE_ID, idEntreprise);
        values.put(InternshipTable.STUDENT_ID, idStudentAccount);
        values.put(InternshipTable.PROFESSOR_ID, idTeacherAccount);
        values.put(InternshipTable.PRIORITY, priority.toString());
        values.put(InternshipTable.INTERNSHIP_DAYS, internshipDays);
        values.put(InternshipTable.START_HOUR, startHour);
        values.put(InternshipTable.END_HOUR, endHour);
        values.put(InternshipTable.START_LUNCH, startLunch);
        values.put(InternshipTable.END_LUNCH, endLunch);
        values.put(InternshipTable.AVERAGE_VISIT_DURING, averageVisitDuring);
        values.put(InternshipTable.TUTOR_DISPONIBILITY, tutorDisponibility);
        values.put(InternshipTable.COMMENTS, comments);

        //On ajoute pas des visites lorsqu'on crée un internship

        db.insert(InternshipTable.TABLE_NAME, null, values);
        internshipList = queryForAllInternships();
    }

    private void insertVisitsByInternshipId(String internshipId, ArrayList<Visit> visits){
        //TODO On insert les différents visits passé en paramètre avec son internshipId
    }

    private ArrayList<Internship> queryForAllInternships(){
        //SQLiteDatabase db = this.getReadableDatabase();

        // les colonnes à retourner par la requete:
        String[] columns = {
                InternshipTable._ID,
                InternshipTable.SCHOOL_YEAR,
                InternshipTable.ENTERPRISE_ID,
                InternshipTable.STUDENT_ID,
                InternshipTable.PROFESSOR_ID,
                InternshipTable.PRIORITY,
                InternshipTable.INTERNSHIP_DAYS,
                InternshipTable.START_HOUR,
                InternshipTable.END_HOUR,
                InternshipTable.START_LUNCH,
                InternshipTable.END_LUNCH,
                InternshipTable.AVERAGE_VISIT_DURING,
                InternshipTable.TUTOR_DISPONIBILITY,
                InternshipTable.COMMENTS
        };

        String selection = null;
        String[] selectionArgs = null;

        Cursor cursor = db.query(InternshipTable.TABLE_NAME, columns, selection, selectionArgs,
                null, null, null, null);

        ArrayList<Internship> internships = new ArrayList<>(cursor.getCount());
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                String idInternship = cursor.getString(0);
                String anneeScolaire = cursor.getString(1);
                //id pour créer les autres objets
                String idEntreprise = cursor.getString(2);
                String idStudentAccount = cursor.getString(3);
                String idTeacherAccount = cursor.getString(4);
                //données de le stage
                Internship.Priority priority;
                String internshipDays = cursor.getString(6);
                String starthour = cursor.getString(7);
                String endHour = cursor.getString(8);
                String startLunch = cursor.getString(9);
                String endLunch = cursor.getString(10);
                int averageVisitDuring = cursor.getInt(11);
                String tutorDisponibility = cursor.getString(12);
                String comments = cursor.getString(13);


                try {
                    priority = Internship.Priority.valueOf(cursor.getString(5));
                } catch (Exception e) {
                    Log.d("Info", "Erreur lors de la création de la priority. " +
                            "Priority set to LOW: " + e);
                    priority = Internship.Priority.LOW;
                }

                //On demande à la BD l'account du prof
                Account studentAccount = queryForAccountByLocalId(idStudentAccount);
                Account teacherAccount = queryForAccountByLocalId(idTeacherAccount);

                //On demande à la BD l'entreprise
                Enterprise entreprise = queryForEntrepriseById(idEntreprise);

                //On demande la liste de visit
                ArrayList<Visit> visitList = queryForVisitsByInternshipId(idInternship);

                Internship internship = new Internship(idInternship, anneeScolaire, entreprise,
                        studentAccount, teacherAccount, visitList, priority, internshipDays,starthour,
                        endHour, startLunch, endLunch, averageVisitDuring, tutorDisponibility,
                        comments);

                internships.add(internship);

            } while (cursor.moveToNext());
            cursor.close();
        }

        return internships;
    }

    /**
     * Fait une requête pour recupérer touts les stages disponibles dans la BD.
     *
     * @return Un ArrayList avec des Internships.
     */
    public ArrayList<Internship> getAllInternships() {
        return internshipList;
    }

    /**
     * Fait une requête pour recupérer tous les comptes du type spécifié.
     *
     * @param type Type du compte qu'on veut récupèrer. 0 admin  1 prof  2 étudiants.
     * @return Un ArrayList avec tous les comptes du type spécifié.
     */
    private ArrayList<Account> queryForAllAccountsByType(int type) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Account> studentAccounts = new ArrayList<>();


        String query = "SELECT * FROM " + AccountTable.TABLE_NAME + " WHERE " + AccountTable.ACCOUNT_TYPE + " = ?";
        String[] args = new String[]{Integer.toString(type)};

        Cursor cursor = db.rawQuery(query, args);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        } else{
            return null;
        }

        do {
            //On recupère l'image
            byte[] imgByte = cursor.getBlob(5);
            Bitmap photo = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

            Account account = new Account(
                    cursor.getString(0), cursor.getString(6),
                    cursor.getString(7), cursor.getString(3),
                    cursor.getInt(9) > 0, cursor.getString(4),
                    cursor.getString(2), cursor.getString(1),
                    photo, cursor.getString(8),
                    cursor.getInt(10));

            studentAccounts.add(account);


        } while (cursor.moveToNext());

        cursor.close();

        return studentAccounts;
    }

    public Account getTeacherByName(String email){
        SQLiteDatabase db = this.getReadableDatabase();
        Account teacher;


        String query = "SELECT * FROM " + AccountTable.TABLE_NAME + " WHERE " + AccountTable.ACCOUNT_TYPE + " = ? AND " + AccountTable.EMAIL + " = ?";
        String[] args = new String[]{"1", email};

        Cursor cursor = db.rawQuery(query, args);
        if (cursor.getCount() != 1) {
            return null;
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }
        byte[] imgByte = cursor.getBlob(5);
        Bitmap photo = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

        teacher =new Account(
                cursor.getString(0), cursor.getString(6),
                cursor.getString(7), cursor.getString(3),
                cursor.getInt(9) > 0, cursor.getString(4),
                cursor.getString(2), cursor.getString(1),
                photo, cursor.getString(8),
                cursor.getInt(10));

        cursor.close();
        return teacher;
    }

    public ArrayList<Account> getStudentsAccount(){
        return studentsAccountList;
    }

    private ArrayList<Account> queryForAllStudentsAccount(){
        return queryForAllAccountsByType(2);
    }


    /**
     * Fait une requête pour recupérer un compte selon son ID.
     *
     * @param id id du compte à recupérer.
     * @return Le compte si elle existe, sinon il return null.
     */
    public Account queryForAccountByLocalId(String id) {
        //SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + AccountTable.TABLE_NAME + " WHERE _id = ?";
        String[] args = new String[]{id};

        Cursor cursor = db.rawQuery(query, args);

        if (cursor.getCount() <= 0) {
            return null;
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }

        //On recupère l'image
        byte[] imgByte = cursor.getBlob(5);
        Bitmap photo = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

        Account account = new Account(
                cursor.getString(0), cursor.getString(6),
                cursor.getString(7), cursor.getString(3),
                cursor.getInt(9) > 0, cursor.getString(4),
                cursor.getString(2), cursor.getString(1),
                photo, cursor.getString(8),
                cursor.getInt(10));

        cursor.close();

        return account;
    }

    private Internship queryForInternshipById(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + InternshipTable.TABLE_NAME + " WHERE _id = ?";
        String[] args = new String[]{id};

        Cursor cursor = db.rawQuery(query, args);

        if (cursor.getCount() <= 0) {
            return null;
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }

        //On recupère les données.
        String idInternship = cursor.getString(0);
        String anneeScolaire = cursor.getString(1);
        //id pour créer les autres objets
        String idEntreprise = cursor.getString(2);
        String idStudentAccount = cursor.getString(3);
        String idTeacherAccount = cursor.getString(4);
        //Les données de le stage
        Internship.Priority priority;
        String internshipDays = cursor.getString(6);
        String starthour = cursor.getString(7);
        String endHour = cursor.getString(8);
        String startLunch = cursor.getString(9);
        String endLunch = cursor.getString(10);
        int averageVisitDuring = cursor.getInt(11);
        String tutorDisponibility = cursor.getString(12);
        String comments = cursor.getString(13);

        try {
            priority = Internship.Priority.valueOf(cursor.getString(5));
        } catch (Exception e) {
            Log.d("Info", "Erreur lors de la création de la priority. " +
                    "Priority set to LOW: " + e);
            priority = Internship.Priority.LOW;
        }

        //On demande à la BD l'account du prof
        Account studentAccount = queryForAccountByLocalId(idStudentAccount);
        Account teacherAccount = queryForAccountByLocalId(idTeacherAccount);

        //On demande à la BD l'entreprise
        Enterprise entreprise = getEntrepriseById(idEntreprise);

        //On demande la liste de visit
        ArrayList<Visit> visitList = queryForVisitsByInternshipId(idInternship);

        Internship internship = new Internship(idInternship, anneeScolaire, entreprise,
                studentAccount, teacherAccount, visitList, priority, internshipDays, starthour, endHour, startLunch,
                endLunch, averageVisitDuring, tutorDisponibility, comments);


        cursor.close();

        return internship;
    }

    /**
     * Récupère un stage selon son id.
     * @param id Id du stage à chercher.
     * @return Stage trouvé.
     */
    public Internship getInternshipById(String id){
        for (Internship internship : internshipList) {
            if (internship.getIdInternship().equals(id)) {
                return internship;
            }
        }

        return null;
    }

    private ArrayList<Enterprise> queryForAllEnterprises(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Enterprise> enterprises = new ArrayList<>();

        String query = "SELECT * FROM " + EnterpriseTable.TABLE_NAME;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        do {
            Enterprise enterprise = new Enterprise(
                    cursor.getString(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), cursor.getString(5));

            enterprises.add(enterprise);


        } while (cursor.moveToNext());

        cursor.close();

        return enterprises;
    }

    public Enterprise queryForEntrepriseById(String id){
        //SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + EnterpriseTable.TABLE_NAME + " WHERE _id = ?";
        String[] args = new String[]{id};

        Cursor cursor = db.rawQuery(query, args);

        if (cursor.getCount() <= 0) {
            return null;
        }

        if (cursor != null)
            cursor.moveToFirst();

        Enterprise enterprise = new Enterprise(
                cursor.getString(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5));

        cursor.close();

        return enterprise;
    }

    /**
     * Fait une requête pour recupérer une enterprise selon son ID.
     *
     * @param id id de l'entreprise désirée.
     * @return L'enterprise si elle existe, sinon il return null.
     */
    public Enterprise getEntrepriseById(String id) {
        for (Enterprise enterprise : enterprisesList) {
            if (enterprise.getEnterpriseId().equals(id)) {
                return enterprise;
            }
        }

        return null;
    }

    /**
     * Fait une requête pour recupérer touts les enterprises disponibles dans la BD.
     * @return Un ArrayList avec tous les enterprises.
     */
    public ArrayList<Enterprise> getEntreprises() {
        return enterprisesList;
    }

    /**
     * Récupère le compte actuelle de type professeur.
     * Le compte du professeur a été chargé lors du initialisation du système (pas de requête à BD).
     * @return Un compte de type professeur
     */
    public Account getCurrentTeacherAccount(){
        return currentTeacherAccount;
    }

    /**
     * @param internshipId
     * @return
     */
    private ArrayList<Visit> queryForVisitsByInternshipId(String internshipId) {
        //SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Visit> visitsList = new ArrayList<>();

        String query = "SELECT * FROM " + VisitTable.TABLE_NAME + " WHERE " + VisitTable.INTERNSHIP_ID + " = ?";
        String[] args = new String[]{internshipId};

        Cursor cursorVisit = db.rawQuery(query, args);

        if (cursorVisit != null && cursorVisit.getCount() > 0) {
            cursorVisit.moveToFirst();
        } else { //S'il y n'y a pas de visites
            return new ArrayList<>();
        }

        do {
            Visit visit = new Visit(cursorVisit.getString(0), cursorVisit.getString(1),
                    cursorVisit.getString(2), cursorVisit.getString(3), cursorVisit.getString(4));

            visitsList.add(visit);
        } while (cursorVisit.moveToNext());

        cursorVisit.close();

        return visitsList;
    }

    /**
     * @param internshipId
     * @return
     */
    public Internship queryForInternshipId(String internshipId) {

        String query = "SELECT * FROM " + InternshipTable.TABLE_NAME + " WHERE " + InternshipTable._ID + " = ?";
        String[] args = new String[]{internshipId};

        Cursor cursor = db.rawQuery(query, args);

        if (cursor.getCount() <= 0) {
            return null;
        }

        if (cursor != null)
            cursor.moveToFirst();

        ArrayList<Visit> visitList = queryForVisitsByInternshipId(internshipId);
        Enterprise enterprise = queryForEntrepriseById(cursor.getString(2));
        Account student = queryForAccountByLocalId(cursor.getString(3));
        Account teacher = queryForAccountByLocalId(cursor.getString(4));

        Internship.Priority priorite = null;
        String priorite_str = cursor.getString(5);
        if(priorite_str.equals("LOW")){
            priorite = Internship.Priority.LOW;
        } else if(priorite_str.equals("MEDIUM")){
            priorite = Internship.Priority.MEDIUM;
        } else{
            priorite = Internship.Priority.HIGH;
        }

        Internship internship = new Internship(cursor.getString(0),
                cursor.getString(1), enterprise, student, teacher,
                visitList, priorite,cursor.getString(6),
                cursor.getString(7),cursor.getString(8),
                cursor.getString(9),cursor.getString(10),
                cursor.getInt(11),cursor.getString(12),
                cursor.getString(13));

        cursor.close();

        return internship;

    }

    /**
     * Fait une requête pour mettre à jour le stage selon l'id passé en paramètre.
     * Le stage sera modifié dans la BD grâce à son id unique
     */
    public void updateInternship(Internship internship) {
        ContentValues values = new ContentValues();

        //Le id reste le même
        values.put(InternshipTable.SCHOOL_YEAR, internship.getSchoolYear());
        values.put(InternshipTable.ENTERPRISE_ID, internship.getEnterprise().getEnterpriseId());
        values.put(InternshipTable.STUDENT_ID, internship.getStudentAccount().getAccountId());
        values.put(InternshipTable.PROFESSOR_ID, internship.getTeacherAccount().getAccountId());
        values.put(InternshipTable.PRIORITY, internship.getPriority().toString());
        values.put(InternshipTable.INTERNSHIP_DAYS, internship.getInternshipDays());
        values.put(InternshipTable.START_HOUR, internship.getStartHour());
        values.put(InternshipTable.END_HOUR, internship.getEndHour());
        values.put(InternshipTable.START_LUNCH, internship.getStartLunch());
        values.put(InternshipTable.END_LUNCH, internship.getEndLunch());
        values.put(InternshipTable.AVERAGE_VISIT_DURING, internship.getAverageVisitDuring());
        values.put(InternshipTable.TUTOR_DISPONIBILITY, internship.getTutorDisponibilities());
        values.put(InternshipTable.COMMENTS, internship.getComments());


        String whereClause = InternshipTable._ID + " = " + "\"" + internship.getIdInternship() + "\"";

        db.update(InternshipTable.TABLE_NAME, values, whereClause, null);
        internshipList = queryForAllInternships();
    }

    /**
     * Fait une requête pour mettre à jour le stage selon l'id passé en paramètre.
     * Le stage sera modifié dans la BD grâce à son id unique
     */
    public void updateInternship (String internshipId, String schoolYear, String idEntreprise,
                                 String idStudent, String idTeacher, Internship.Priority priority,
                                 String startHour, String endHour, String StartLunch, String endLunch) {
        ContentValues values = new ContentValues();

        values.put(InternshipTable.SCHOOL_YEAR, schoolYear);
        values.put(InternshipTable.ENTERPRISE_ID, idEntreprise);
        values.put(InternshipTable.STUDENT_ID, idStudent);
        values.put(InternshipTable.PROFESSOR_ID, idTeacher);
        values.put(InternshipTable.PRIORITY, priority.toString());
        values.put(InternshipTable.START_HOUR, startHour);
        values.put(InternshipTable.END_HOUR, endHour);
        values.put(InternshipTable.START_LUNCH, StartLunch);
        values.put(InternshipTable.END_LUNCH, endLunch);

        String whereClause = InternshipTable._ID + " = " + "\"" + internshipId + "\"";

        db.update(InternshipTable.TABLE_NAME, values, whereClause, null);
        internshipList = queryForAllInternships();
    }

    private void updateVisitsByInternshipId(String internshipId, ArrayList<Visit> visits){
        //SELECT * visits FROM visit WHERE internship_id = internshipId;
        //TODO Pour tous les entrées, il faut UPDATE les nouvelles visits
    }

    public void updateAccount(Account account) {
        ContentValues values = new ContentValues();
        //Le id reste le même
        values.put(AccountTable.CREATED_AT, account.getCreationDate());
        values.put(AccountTable.DELETED_AT, account.getDeletionDate());
        values.put(AccountTable.EMAIL, account.getEmail());
        values.put(AccountTable.IS_ACTIVE, account.getActive());
        values.put(AccountTable.PASSWORD, account.getPassword());
        values.put(AccountTable.FIRST_NAME, account.getFirstName());
        values.put(AccountTable.LAST_NAME, account.getLastName());

        //On définit la photo
        if(! (account.getPhoto() == null)){
            ByteArrayOutputStream photoInBytes = new ByteArrayOutputStream();
            account.getPhoto().compress(Bitmap.CompressFormat.JPEG, 100, photoInBytes);
            byte[] img = photoInBytes.toByteArray();

            values.put(AccountTable.PROFILE, img);
        }

        values.put(AccountTable.UPDATED_AT, account.getUpdatedDate());
        values.put(AccountTable.ACCOUNT_TYPE, account.getAccountType());

        String whereClause = AccountTable._ID + " = " + "\"" + account.getAccountId() + "\"";

        db.update(AccountTable.TABLE_NAME, values, whereClause, null);
        studentsAccountList = queryForAllStudentsAccount();
    }

    /**
     * Supprime le stage selon id donnée.
     *
     * @param id Id du stage à supprimer.
     */
    public void deleteInternship(String id) {
        String whereClause = InternshipTable._ID + " = " + "\"" + id + "\"";

        db.delete(InternshipTable.TABLE_NAME, whereClause, null);
        internshipList = queryForAllInternships();
    }

    public void updateEntreprise(Enterprise enterprise) {
        ContentValues values = new ContentValues();
//        Le id reste le même
        values.put(EnterpriseTable.ENTERPRISE_NAME, enterprise.getName());
        values.put(EnterpriseTable.ENTERPRISE_ADDRESS, enterprise.getAddress());
        values.put(EnterpriseTable.TOWN, enterprise.getTown());
        values.put(EnterpriseTable.PROVINCE, enterprise.getProvince());
        values.put(EnterpriseTable.POSTAL_CODE, enterprise.getPostalCode());

        String whereClause = EnterpriseTable._ID + " = " + "\"" + enterprise.getEnterpriseId() + "\"";

        db.update(EnterpriseTable.TABLE_NAME, values, whereClause, null);
        enterprisesList = queryForAllEnterprises();
    }

}
