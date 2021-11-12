package ca.qc.bdeb.internshipmanager.dataclasses;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Classe qui permet de créer, modifier ou supprimer un stage.
 * Chaque stage est associé avec un compte d'un prof, compte d'un étudiant et une entreprise.
 * Un stage possède aussi une liste de visites.
 * Un stage peut posseder un des trois priorités: LOW, MEDIUM ou HIGH.
 **/
public class Internship implements Comparable<Internship> {

    private String idInternship;
    private String schoolYear;
    private Enterprise enterprise;
    private Account accountStudent;
    private Account accountTeacher;
    private ArrayList<Visit> visitList;
    private Priority priority = Priority.LOW;

    /**
     * Permet de créer un nouveau stage. Un stage doit contenir les informations d'un compte d'un prof,
     * d'un étudiant, et une entreprise.
     * Le id du stage est recupéré depuis la BD ou elle est géneré aléatoirement avant d'être sauvegardé.
     * @param idInternship Id unique au stage.
     * @param schoolYear Année scolaire du stage.
     * @param enterprise Entreprise associé à ce stage.
     * @param accountStudent Compte de l'étudiant qui fait le stage.
     * @param accountTeacher Compte du professeur qui gère le stage.
     * @param visitList Liste de visites de ce stage.
     * @param priority Priorité du stage.
     */
    public Internship(String idInternship, String schoolYear, Enterprise enterprise,
                      Account accountStudent, Account accountTeacher, ArrayList<Visit> visitList, Priority priority){
        this.idInternship = idInternship;
        this.schoolYear = schoolYear;
        this.accountTeacher = accountTeacher;
        this.accountStudent = accountStudent;
        this.enterprise = enterprise;
        this.visitList = visitList;
        this.priority = priority;
    }

    /**
     * Recupère l'id unique du stage.
     * @return Id unique du stage.
     */
    public String getIdInternship() {
        return idInternship;
    }

    /**
     * Recupère un objet de type Account de type étudiant associé au stage.
     * @return Account Étudiant associé au stage.
     */
    public Account getStudentAccount() {
        return accountStudent;
    }

    /**
     * Recupère la priorité du stage.
     * @return La priorité assigné au stage.
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Recupère l'année scolaire associé au stage.
     * @return L'année scolaire du stage.
     */
    public String getSchoolYear(){
        return schoolYear;
    }

    /**
     * Recupère un objet de type Enterprise associé au stage.
     * @return Enterprise associé au stage.
     */
    public Enterprise getEnterprise() {
        return enterprise;
    }

    /**
     * Recupère un objet de type Account de type professeur associé au stage.
     * @return Account Professeur associé au stage.
     */
    public Account getTeacherAccount() {
        return accountTeacher;
    }

    /**
     * Modifie la priorité du stage.
     * @param priority Nouvelle priorité du stage.
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ArrayList<Visit> getVisitList() {
        return visitList;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public void setEnterprise(Enterprise enterprise) {
        this.enterprise = enterprise;
    }

    public void setAccountStudent(Account accountStudent) {
        this.accountStudent = accountStudent;
    }

    public void setAccountTeacher(Account accountTeacher) {
        this.accountTeacher = accountTeacher;
    }

    public void setVisitList(ArrayList<Visit> visitList) {
        this.visitList = visitList;
    }

    @Override
    public int compareTo(Internship intership) {
        int i = this.getPriority().compareTo(intership.getPriority());
        return -i;
    }

    @Override
    public String toString() {
        return "Internship{" +
                "idInternship='" + idInternship + '\'' +
                ", schoolYear='" + schoolYear + '\'' +
                ", enterprise=" + enterprise +
                ", accountStudent=" + accountStudent +
                ", accountTeacher=" + accountTeacher +
                ", visitList=" + visitList +
                ", priority=" + priority +
                '}';
    }

    public static final Comparator<Internship> NameComparator = new Comparator<Internship>(){
        @Override
        public int compare(Internship i1, Internship i2) {
            return i1.getStudentAccount().compareTo(i2.getStudentAccount());
        }
    };

    /**
     * Prioritées qu'un stage peu avoir.
     */
    public enum Priority{
        LOW,
        MEDIUM,
        HIGH
    }

}
