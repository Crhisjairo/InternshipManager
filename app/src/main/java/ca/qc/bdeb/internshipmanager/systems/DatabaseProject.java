package ca.qc.bdeb.internshipmanager.systems;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.provider.BaseColumns;
import android.util.Log;

import ca.qc.bdeb.internshipmanager.R;
import ca.qc.bdeb.internshipmanager.dataclasses.Account;
import ca.qc.bdeb.internshipmanager.dataclasses.Enterprise;
import ca.qc.bdeb.internshipmanager.dataclasses.Internship;
import ca.qc.bdeb.internshipmanager.dataclasses.Visit;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Classe qui va permettre d'implémenter toutes les tables utiles au projet.
 * Elle permet de faire les différentes requêtes à la BD.
 */
public class DatabaseProject extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "InternshipSystem.db";
    private Context context;
    private SQLiteDatabase db;

    private static DatabaseProject instance = null;
    private static Context sysContext;

    private static Account currentTeacherAccount;


    /**
     * Requête pour crée la table de comptes.
     */
    private static final String CREATION_TABLE_ACCOUNTS = "CREATE TABLE "
            + Accounts.TABLE_NAME
            + " (" + Accounts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + Accounts.FIRST_NAME + " VARCHAR2(255),"
            + Accounts.LAST_NAME + " VARCHAR2(255),"
            + Accounts.EMAIL + " VARCHAR2(255),"
            + Accounts.PASSWORD + " VARCHAR2(255),"
            + Accounts.PROFILE + " BLOB,"
            + Accounts.CREATED_AT + " BLOB,"
            + Accounts.DELETED_AT + " BLOB,"
            + Accounts.UPDATED_AT + " BLOB,"
            + Accounts.IS_ACTIVE + " INT(1),"
            + Accounts.ACCOUNT_TYPE + " INT(11))";

    /**
     * Requête pour crée la table des enterprises.
     */
    private static final String CREATION_TABLE_ENTERPRISE = "CREATE TABLE "
            + Enterprises.TABLE_NAME
            + " (" + Enterprises._ID + " VARCHAR2(255) PRIMARY KEY,"
            + Enterprises.ENTERPRISE_NAME + " VARCHAR2(255),"
            + Enterprises.ENTERPRISE_ADDRESS + " VARCHAR2(255),"
            + Enterprises.TOWN + " VARCHAR2(255),"
            + Enterprises.PROVINCE + " VARCHAR2(255),"
            + Enterprises.POSTAL_CODE + " VARCHAR2(7))";

    /**
     * Requête pour crée la table de visits.
     */
    private static final String CREATION_TABLE_VISIT = "CREATE TABLE "
            + Visits.TABLE_NAME
            + " (" + Visits._ID + " VARCHAR2(255) PRIMARY KEY,"
            + Visits.CREATED_DATE + " BLOB,"
            + Visits.START_HOUR + " BLOB,"
            + Visits.DURING + " INT(20))";


    /**
     * Requête pour crée la table des stages.
     */
    private static final String CREATION_TABLE_INTERNSHIP = "CREATE TABLE "
            + Internships.TABLE_NAME
            + " (" + Internships._ID + " VARCHAR2(255) PRIMARY KEY,"
            + Internships.SCHOOL_YEAR + " VARCHAR2(255),"
            + Internships.ENTERPRISE_ID + " VARCHAR2(255),"
            + Internships.STUDENT_ID + " INT(20),"
            + Internships.PROFESSOR_ID + " INT(20)," +
            Internships.PRIORITY + " VARCHAR2(255))";


    /**
     * Créer l\ instance de SQLlite
     */
    private DatabaseProject() {
        super(sysContext, DB_NAME, null, DB_VERSION);

        this.db = this.getWritableDatabase();

        logInTeacher();
    }

    /**
     * Permetre de recuperer la Base de donnees
     *
     * @return
     */
    public static DatabaseProject getInstance() {
        if(sysContext == null){
            return null;
        }

        if (instance == null) {
            instance = new DatabaseProject();
        }

        return instance;
    }

    /**
     * Définit le context du système. Il faut définir le contexte avant de getInstance().
     * @param context Context de l'application.
     */
    public static void setSysContext(Context context){
        sysContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATION_TABLE_ACCOUNTS);
        sqLiteDatabase.execSQL(CREATION_TABLE_VISIT);
        sqLiteDatabase.execSQL(CREATION_TABLE_ENTERPRISE);
        sqLiteDatabase.execSQL(CREATION_TABLE_INTERNSHIP);

        this.db = sqLiteDatabase;

        firstInsert(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    /**
     * Il récupère le compte du prof qui utilise l'application
     * @deprecated Cette méthode récupère le premier compte de type prof qui a été ajouté à la BD. Il
     * faut rendre ça dinamique.
     */
    private void logInTeacher() {
        /*TODO il faut get le account du teacher en fonction de la page de Login.
          Pour le moment, on get le seul teacher qui doit exister.
         */
        currentTeacherAccount = getAccounts(1).get(0);
    }

    /**
     * Classe qui permet de définir la table entreprise
     */
    public static class Enterprises implements BaseColumns {
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
    public static class Accounts implements BaseColumns {
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
    public static class Internships implements BaseColumns {
        public static final String TABLE_NAME = "internship";
        public static final String SCHOOL_YEAR = "year_school";
        public static final String ENTERPRISE_ID = "enterprise_id";
        public static final String STUDENT_ID = "student_id";
        public static final String PROFESSOR_ID = "professor_id";
        public static final String PRIORITY = "priority";
    }

    /**
     * Classe qui va permettre d'implémenter la table stage
     */
    public static class Visits implements BaseColumns {
        public static final String TABLE_NAME = "visits";
        public static final String CREATED_DATE = "date";
        public static final String START_HOUR = "start_hour";
        public static final String DURING = "during";
    }


    /**
     * Methode qui va permettre d'insérer un nouvel étudiant
     */
    public void insertAccount(SQLiteDatabase db, String createdAt, String deletedAt, String email, boolean isActive,
                              String password, String lastName, String firstName, Bitmap photo, String updatedAt,
                              int accountType) {

        ContentValues values = new ContentValues();

        values.put(Accounts.CREATED_AT, createdAt);
        values.put(Accounts.DELETED_AT, deletedAt);
        values.put(Accounts.EMAIL, email);
        values.put(Accounts.PASSWORD, password);
        values.put(Accounts.LAST_NAME, lastName);
        values.put(Accounts.FIRST_NAME, firstName);

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
        values.put(Accounts.PROFILE, img);


        values.put(Accounts.UPDATED_AT, updatedAt);
        values.put(Accounts.ACCOUNT_TYPE, accountType);

        int active = 0;
        if (isActive)
            active = 1;
        values.put(Accounts.IS_ACTIVE, active);

        db.insert(Accounts.TABLE_NAME, null, values);

    }

    /**
     * Methode qui va permettre d'insérer une nouvelle entreprise
     */
    public String insertEnterprise(String name, String address, String town,
                                   String province, String postalCode) {

        ContentValues values = new ContentValues();
        String idEntreprise = UUID.randomUUID().toString();
        values.put(Enterprises._ID, idEntreprise);
        values.put(Enterprises.ENTERPRISE_NAME, name);
        values.put(Enterprises.ENTERPRISE_ADDRESS, address);
        values.put(Enterprises.PROVINCE, province);
        values.put(Enterprises.POSTAL_CODE, postalCode);
        values.put(Enterprises.TOWN, town);

        long id = db.insert(Enterprises.TABLE_NAME, null, values);

        if (id != 0) {
            return idEntreprise;
        }
        return "";
    }

    /**
     * Methode qui va permettre d'insérer une nouvelle visite
     */
    public void insertVisit(String idStage, String visiteDate, String startTime, int during) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Visits._ID, idStage);
        values.put(Visits.CREATED_DATE, visiteDate);
        values.put(Visits.START_HOUR, startTime);
        values.put(Visits.DURING, during);

        long id = db.insert(Visits.TABLE_NAME, null, values);
    }

    /**
     * Insert un nouveau stage dans la BD.
     * @param schoolYear Année scolaire.
     * @param idEntreprise Id de l'entreprise lié au stage.
     * @param idStudentAccount Id du compte de l'étudiant lié au stage.
     * @param idTeacherAccount id du compte du professeur lié au stage.
     * @param priority Priorité du nouveau stage.
     */
    public void insertIntership(String schoolYear, String idEntreprise,
                                int idStudentAccount, int idTeacherAccount, Internship.Priority priority) {

        ContentValues values = new ContentValues();
        values.put(Internships._ID, UUID.randomUUID().toString());
        values.put(Internships.SCHOOL_YEAR, schoolYear);
        values.put(Internships.ENTERPRISE_ID, idEntreprise);
        values.put(Internships.STUDENT_ID, idStudentAccount);
        values.put(Internships.PROFESSOR_ID, idTeacherAccount);
        values.put(Internships.PRIORITY, priority.toString());

        long id = db.insert(Internships.TABLE_NAME, null, values);
    }

    /**
     * Fait une requête pour recupérer touts les stages disponibles dans la BD.
     *
     * @return Un ArrayList avec des Internships.
     */
    public ArrayList<Internship> getAllInternships() {
        SQLiteDatabase db = this.getReadableDatabase();

        // les colonnes à retourner par la requete:
        String[] columns = {
                Internships._ID,
                Internships.SCHOOL_YEAR,
                Internships.ENTERPRISE_ID,
                Internships.STUDENT_ID,
                Internships.PROFESSOR_ID,
                Internships.PRIORITY
        };

        String selection = null;
        String[] selectionArgs = null;

        Cursor cursor = db.query(Internships.TABLE_NAME, columns, selection, selectionArgs,
                null, null, null, null);

        ArrayList<Internship> internships = new ArrayList<>(cursor.getCount());
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                String idInternship = cursor.getString(0);
                String anneeScolaire = cursor.getString(1);
                //id pour créer les autres objets
                int idStudentAccount = cursor.getInt(3);
                int idTeacherAccount = cursor.getInt(4);
                String idEntreprise = cursor.getString(2);

                Internship.Priority priority;

                try {
                    priority = Internship.Priority.valueOf(cursor.getString(5));
                } catch (Exception e) {
                    Log.d("Info", "Erreur lors de la création de la priority. " +
                            "Priority set to LOW: " + e);
                    priority = Internship.Priority.LOW;
                }


                //On demande à la BD l'account du prof
                Account studentAccount = getAccountById(idStudentAccount);
                Account teacherAccount = getAccountById(idTeacherAccount);

                //On demande à la BD l'entreprise
                Enterprise entreprise = getEntrepriseById(idEntreprise);

                //On demande la liste de visit
                ArrayList<Visit> visitList = getVisitListOneStudent(idInternship);

                Internship internship = new Internship(idInternship, anneeScolaire,
                        entreprise, studentAccount, teacherAccount, visitList, priority);

                internships.add(internship);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return internships;
    }

    public ArrayList<Account> getStudentsAccounts(){
        return getAccounts(2);
    }

    /**
     * Fait une requête pour recupérer tous les comptes du type spécifié.
     *
     * @param type Type du compte qu'on veut récupèrer. 0 admin  1 prof  2 étudiants.
     * @return Un ArrayList avec tous les comptes du type spécifié.
     */
    private ArrayList<Account> getAccounts(int type) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Account> studentAccounts = new ArrayList<>();

        String query = "SELECT * FROM " + Accounts.TABLE_NAME + " WHERE " + Accounts.ACCOUNT_TYPE + " = ?";
        String[] args = new String[]{Integer.toString(type)};

        Cursor cursor = db.rawQuery(query, args);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        do {
            //On recupère l'image
            byte[] imgByte = cursor.getBlob(5);
            Bitmap photo = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

            Account account = new Account(
                    cursor.getInt(0), cursor.getString(6),
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

    /**
     * Fait une requête pour recupérer un compte selon son ID.
     *
     * @param id id du compte à recupérer.
     * @return Le compte si elle existe, sinon il return null.
     */
    private Account getAccountById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + Accounts.TABLE_NAME + " WHERE _id = ?";
        String[] args = new String[]{Integer.toString(id)};

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
                cursor.getInt(0), cursor.getString(6),
                cursor.getString(7), cursor.getString(3),
                cursor.getInt(9) > 0, cursor.getString(4),
                cursor.getString(2), cursor.getString(1),
                photo, cursor.getString(8),
                cursor.getInt(10));

        cursor.close();

        return account;
    }

    /**
     * Récupère un stage selon son id.
     * @param id Id du stage à chercher.
     * @return Stage trouvé.
     */
    public Internship getInternshipById(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + Internships.TABLE_NAME + " WHERE _id = ?";
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
        int idStudentAccount = cursor.getInt(3);
        int idTeacherAccount = cursor.getInt(4);
        String idEntreprise = cursor.getString(2);

        Internship.Priority priority;

        try {
            priority = Internship.Priority.valueOf(cursor.getString(5));
        } catch (Exception e) {
            Log.d("Info", "Erreur lors de la création de la priority. " +
                    "Priority set to LOW: " + e);
            priority = Internship.Priority.LOW;
        }

        //On demande à la BD l'account du prof
        Account studentAccount = getAccountById(idStudentAccount);
        Account teacherAccount = getAccountById(idTeacherAccount);

        //On demande à la BD l'entreprise
        Enterprise entreprise = getEntrepriseById(idEntreprise);

        //On demande la liste de visit
        ArrayList<Visit> visitList = getVisitListOneStudent(idInternship);

        Internship internship = new Internship(idInternship, anneeScolaire,
                entreprise, studentAccount, teacherAccount, visitList, priority);

        cursor.close();

        return internship;
    }

    /**
     * Fait une requête pour recupérer touts les enterprises disponibles dans la BD.
     *
     * @return Un ArrayList avec tous les enterprises.
     */
    public ArrayList<Enterprise> getEntreprises() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Enterprise> enterprises = new ArrayList<>();

        String query = "SELECT * FROM " + Enterprises.TABLE_NAME;

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

    /**
     * Récupère le compte actuelle de type professeur.
     * Le compte du professeur a été chargé lors du initialisation du système (pas de requête à BD).
     * @return Un compte de type professeur
     */
    public Account getCurrentTeacherAccount(){
        return currentTeacherAccount;
    }

    /**
     * Fait une requête pour recupérer une enterprise selon son ID.
     *
     * @param id id de l'entreprise désirée.
     * @return L'enterprise si elle existe, sinon il return null.
     */
    public Enterprise getEntrepriseById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + Enterprises.TABLE_NAME + " WHERE _id = ?";
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
     * @param id
     * @return
     */
    public ArrayList<Visit> getVisitListOneStudent(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Visit> visitsList = new ArrayList<>();

        String query = "SELECT * FROM " + Visits.TABLE_NAME + " WHERE _id = ?";
        String[] args = new String[]{id};

        Cursor cursorVisit = db.rawQuery(query, args);

        if (cursorVisit != null) {
            cursorVisit.moveToFirst();
        } else { //S'il y n'y a pas de visites
            return new ArrayList<>();
        }

        do {
            try {

                Visit visit = new Visit(cursorVisit.getString(0), cursorVisit.getString(1),
                        cursorVisit.getString(2), cursorVisit.getInt(3));

                visitsList.add(visit);
            } catch (Exception e) {
                return new ArrayList<>();
            }

        } while (cursorVisit.moveToNext());

        cursorVisit.close();

        return visitsList;
    }

    /**
     * Fait une requête pour mettre à jour le stage selon l'id passé en paramètre.
     * Le stage sera modifié dans la BD grâce à son id unique
     */
    public void updateInternship(Internship internship) {
        ContentValues values = new ContentValues();

        //Le id reste le même
        values.put(Internships.SCHOOL_YEAR, internship.getSchoolYear());
        values.put(Internships.ENTERPRISE_ID, internship.getEnterprise().getEnterpriseId());
        values.put(Internships.STUDENT_ID, internship.getStudentAccount().getAccountId());
        values.put(Internships.PROFESSOR_ID, internship.getTeacherAccount().getAccountId());
        values.put(Internships.PRIORITY, internship.getPriority().toString());

        String whereClause = Internships._ID + " = " + "\"" + internship.getIdInternship() + "\"";

        db.update(Internships.TABLE_NAME, values, whereClause, null);
    }

    public void updateAccount(int accountIdToModify, String createdAt, String deletedAt, String email,
                              boolean active, String password, String lastName, String firstName,
                              Bitmap photo, String updatedAt, int accountType) {
        ContentValues values = new ContentValues();
        //Le id reste le même
        values.put(Accounts.CREATED_AT, createdAt);
        values.put(Accounts.DELETED_AT, deletedAt);
        values.put(Accounts.EMAIL, email);
        values.put(Accounts.IS_ACTIVE, active);
        values.put(Accounts.PASSWORD, password);
        values.put(Accounts.FIRST_NAME, firstName);
        values.put(Accounts.LAST_NAME, lastName);

        //On définit la photo
        ByteArrayOutputStream photoInBytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, photoInBytes);
        byte[] img = photoInBytes.toByteArray();

        values.put(Accounts.PROFILE, img);

        values.put(Accounts.UPDATED_AT, updatedAt);
        values.put(Accounts.ACCOUNT_TYPE, accountType);

        String whereClause = Accounts._ID + " = " + "\"" + accountIdToModify + "\"";


        db.update(Accounts.TABLE_NAME, values, whereClause, null);
    }

    /**
     * Supprime toute la BD.
     */
    /*public void deleteAllDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "";
        String[] whereArgs = {String.valueOf("")};
        db.delete(Accounts.TABLE_NAME, whereClause, whereArgs);
        db.delete(Internships.TABLE_NAME, whereClause, whereArgs);
        db.delete(Enterprises.TABLE_NAME, whereClause, whereArgs);
        db.delete(Visits.TABLE_NAME, whereClause, whereArgs);
    }*/

    /**
     * Supprime le stage selon id donnée.
     *
     * @param id Id du stage à supprimer.
     */
    public void deleteInternship(String id) {
        String whereClause = Internships._ID + " = " + "\"" + id + "\"";

        db.delete(Internships.TABLE_NAME, whereClause, null);
    }

    /**
     * Méthode utilisé lors de la première insertion des données.
     * Elle ajoute les données par défault.
     *
     * @param db Connection à la BD.
     */
    private void firstInsert(SQLiteDatabase db) {

        String[] firstName = {"Mikaël", "Thomas", "Simon", "Kevin", "Cédric", "Vanessa", "Vincent", "Mélissa", "Diego", "Geneviève"};
        String[] lastName = {"Boucher", "Caron", "Gingras", "Leblanc", "Masson", "Monette", "Picard", "Poulain", "Vargas", "Tremblay"};
        String date = (new Date()).toString();

        //Insert prof
        insertAccount(db, date, null, "prades.pierre@test.com",
                true, "mdp123", "Prades", "Pierre", null,
                date, 1);

        //Insert élèves
        for (int inserts = 0; inserts < firstName.length; inserts++) {

            String courriel = lastName[inserts].toLowerCase() + "." + firstName[inserts].toLowerCase()
                    + "@test.com";
            courriel = Normalizer.normalize(courriel, Normalizer.Form.NFD);
            courriel = courriel.replaceAll("[^\\p{ASCII}]", "");

            insertAccount(db, date, null, courriel, true,
                    "mdp123", lastName[inserts], firstName[inserts], null,
                    date, 2);
        }

        //TODO
        //Première donnée
        String jeanCoutu = insertEnterprise("Jean Coutu",
                "4885 Henri-Bourassa Blvd W #731", "Montréal",
                "Quebec", "H3L 1P3");
        String garageTremblay = insertEnterprise("Garage Tremblay",
                "10142 Boul. Saint-Laurent", "Montréal",
                "Quebec", "H3L 2N7");
        String pharmaprix = insertEnterprise("Pharmaprix",
                "3611 Rue Jarry E", "Montréal",
                "Quebec", "H1Z 2G1");
        String alimentationGenerale = insertEnterprise("Alimentation Générale",
                "1853 Chem. Rockland,", "Montréal",
                "Quebec", "H3P 2Y7");
        String autoRepair = insertEnterprise("Auto Repair",
                "8490 Rue Saint-Dominique", "Montréal",
                "Quebec", "H2P 2L5");
        String subway = insertEnterprise("Subway",
                "775 Rue Chabanel O", "Montréal",
                "Quebec", "H4N 3J7");
        String metro = insertEnterprise("Métro",
                "1331 Blvd. de la Côte-Vertu", "Montréal",
                "Quebec", "H4L 1Z1");
        String epicerieLesJardinieres = insertEnterprise("Épicerie les Jardinières",
                "10345 Ave Christophe-Colomb", "Montréal",
                "Quebec", "H2C 2V1");
        String boucherieMarien = insertEnterprise("Boucherie Marien",
                "1499-1415 Rue Jarry E", "Montréal",
                "Quebec", "");
        String iga = insertEnterprise("IGA",
                "8921 Rue Lajeunesse", "Montréal",
                "Quebec", "H2M 1S1");

        //String[] firstName = {"Mikaël", "Thomas", "Simon", "Kevin", "Cédric", "Vanessa", "Vincent", "Mélissa", "Diego", "Geneviève"};
        //String[] lastName = {"Boucher", "Caron", "Gingras", "Leblanc", "Masson", "Monette", "Picard", "Poulain", "Vargas", "Tremblay"};

        insertIntership("2021", jeanCoutu, 2,
                1, Internship.Priority.LOW);

        insertIntership("2021", garageTremblay, 3,
                1, Internship.Priority.LOW);

        insertIntership("2021", pharmaprix, 4,
                1, Internship.Priority.LOW);

        insertIntership("2021", alimentationGenerale, 5,
                1, Internship.Priority.LOW);

        insertIntership("2021", autoRepair, 6,
                1, Internship.Priority.LOW);

        insertIntership("2021", subway, 7,
                1, Internship.Priority.LOW);

        insertIntership("2021", metro, 8,
                1, Internship.Priority.LOW);

        insertIntership("2021", epicerieLesJardinieres, 9,
                1, Internship.Priority.LOW);

        insertIntership("2021", boucherieMarien, 10,
                1, Internship.Priority.LOW);

        insertIntership("2021", iga, 11,
                1, Internship.Priority.LOW);

    }

}
