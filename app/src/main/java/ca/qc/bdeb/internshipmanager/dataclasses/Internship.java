package ca.qc.bdeb.internshipmanager.dataclasses;

import android.content.res.ColorStateList;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Comparator;

import ca.qc.bdeb.internshipmanager.R;

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
    private String internshipDays;
    private String startHour;
    private String endHour;
    private String startLunch;
    private String endLunch;
    private int averageVisitDuring;
    private String tutorDisponibility;
    private String comments;

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
                      Account accountStudent, Account accountTeacher, ArrayList<Visit> visitList, Priority priority,
                      String internshipDays, String startHour, String endHour, String startLunch, String endLunch, int averageVisitDuring,
                      String tutorDisponibility, String comments){
        this.idInternship = idInternship;
        this.schoolYear = schoolYear;
        this.accountTeacher = accountTeacher;
        this.accountStudent = accountStudent;
        this.enterprise = enterprise;
        this.visitList = visitList;
        this.priority = priority;
        this.internshipDays = internshipDays;
        this.startHour = startHour;
        this.endHour = endHour;
        this.startLunch = startLunch;
        this.endLunch = endLunch;
        this.averageVisitDuring = averageVisitDuring;
        this.tutorDisponibility = tutorDisponibility;
        this.comments = comments;
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

    public int getPriorityColorRessourceId(){
        switch (priority){
            case LOW:
                return R.color.green_flag;
            case MEDIUM:
                return R.color.yellow_flag;
            case HIGH:
                return R.color.red_flag;
        }

        return -1;
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

    public String getInternshipDays() {
        return internshipDays;
    }

    public void setInternshipDays(String internshipDays) {
        this.internshipDays = internshipDays;
    }

    public String getStartHour() {
        return startHour;
    }

    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    public String getStartLunch() {
        return startLunch;
    }

    public void setStartLunch(String startLunch) {
        this.startLunch = startLunch;
    }

    public String getEndLunch() {
        return endLunch;
    }

    public void setEndLunch(String endLunch) {
        this.endLunch = endLunch;
    }

    public int getAverageVisitDuring() {
        return averageVisitDuring;
    }

    public void setAverageVisitDuring(int averageVisitDuring) {
        this.averageVisitDuring = averageVisitDuring;
    }

    public String getTutorDisponibilities() {
        return tutorDisponibility;
    }

    public void setTutorDisponibility(String tutorDisponibility) {
        this.tutorDisponibility = tutorDisponibility;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setId (String id) {
        this.idInternship = id;
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
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

}
