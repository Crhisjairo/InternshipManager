package ca.qc.bdeb.internshipmanager.dataclasses;


import java.util.ArrayList;
import java.util.UUID;

/**
 * Classe qui permet de créer, modifier ou supprimer une visite.
 * Un stage peut posséder plusieurs visites. Les visites possèdent une date, une heure de début
 * et une heure de fin.
 * */
public class Visit {
    private String visitId;
    private String internshipId;
    private String visitDate;
    private String startHour;
    private String during;

    /*
    public static final String TABLE_NAME = "visits";
        public static final String INTERNSHIP_ID = "stage_id";
        public static final String DATE = "date";
        public static final String START_HOUR = "start_hour";
        public static final String DURING = "during";
     */

    /**
     * Crée une nouvelle visite.
     * Le id de la visite est la même du stage auquelle elle est associé.
     * @param visitId Id du stage auquelle la visite est associé.
     * @param visitDate Date de la visite.
     * @param startHour Heure du début de la visite.
     */
    public Visit(String visitId, String internshipId, String visitDate, String startHour, String during) {
        this.visitId = visitId;
        this.internshipId = internshipId;
        this.visitDate = visitDate;
        this.startHour = startHour;
        this.during = during;
    }


    public String getInternshipId() {
        return internshipId;
    }

    public void setInternshipId(String internshipId) {
        this.internshipId = internshipId;
    }

    /**
     * Recupère la date de la visite.
     * @return Date de la visite.
     */
    public String getVisitDate() {
        return visitDate;
    }

    /**
     * Recupère l'heure du début de la visite.
     * @return heure de début de la visite.
     */
    public String getStartHour() {
        return startHour;
    }

    /**
     * Recupère l'heure de fin de la visite.
     * @return Heure de fin de la visite.
     */
    public String getVisit_id() {
        return visitId;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }



    public String getDuring() {
        return during;
    }

    public void setDuring(String during) {
        this.during = during;
    }

    /**
     * Crée une visite en base des données founis dans un stage.
     * On formate les dates et on s'assure que toutes les dates peuvent s'afficher dans le calendrier.
     *
     * @param internship Stage auquel appartient les visites.
     * @return Visites génerées à partir des données du stage.
     */
    public static ArrayList<Visit> createVisitsFromIntership(Internship internship){
        ArrayList<Visit> visits = new ArrayList<>();
        //On separe les jours de stages et on crée les visites en fonction
        String visitDays = internship.getInternshipDays();
        String[] days = visitDays.split("\\|");
        //Données pour toutes les visites
        String internshipId = internship.getIdInternship();
        String startHour = internship.getStartHour();
        String during = Integer.toString(internship.getAverageVisitDuring()); //utiliser pour calculer heureFin dans calendar

        for (String day : days) {
            String visitId = UUID.randomUUID().toString();
            String visitDate = day;

            visits.add(new Visit(visitId, internshipId, visitDate, startHour, during));
        }

        //On adapte les dates pour qu'elles restent dans une marge unique dans le calendrier.
        
        return visits;
    }

    /**
     * Vérifie que chaque visite contient une marge de temps unique.
     * @param visits
     * @return
     */
    public static ArrayList<Visit> adaptVisitDates(ArrayList<Visit> visits){
        return null;
    }
}
