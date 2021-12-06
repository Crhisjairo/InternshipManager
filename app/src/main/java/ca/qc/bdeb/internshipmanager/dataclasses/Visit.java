package ca.qc.bdeb.internshipmanager.dataclasses;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
     * On formate les dates.
     *
     * @param internship Stage auquel appartient les visites.
     * @return Visites génerées à partir des données du stage.
     */
    public static ArrayList<Visit> createVisitsFromIntership(Internship internship){
        ArrayList<Visit> visits = new ArrayList<>();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        //On separe les jours de stages et on crée les visites en fonction
        String visitDays = internship.getInternshipDays();

        if(visitDays.isEmpty() || visitDays.equals("null")){
            return new ArrayList<>(); //Parce qu'il contient pas les journées
        }

        String[] days = visitDays.split("\\|");
        //Données pour toutes les visites
        String internshipId = internship.getIdInternship();
        String startHour = internship.getStartHour();
        String during = Integer.toString(internship.getAverageVisitDuring()); //utiliser pour calculer heureFin de la visite dans calendar

        for (String day : days) {
            Log.d("Visit:" , day + "");
            String visitId = UUID.randomUUID().toString();
            //On format la journée pour le traiter dans le calendrier après.
            Date visitDate = nextDayOfWeek(day).getTime();
            String visitDateString = formater.format(visitDate);

            visits.add(new Visit(visitId, internshipId, visitDateString, startHour, during));
        }

        //On adapte les dates pour qu'elles restent dans une marge unique dans le calendrier.
        
        return visits;
    }

    /**
     * Recupère la prochaine journée (avec la date complète) selon le jour qu'on lui passe.
     * @param day
     * @return
     */
    public static Calendar nextDayOfWeek(String day) {
        Calendar date = Calendar.getInstance();
        int dayOfWeek = 0;

        switch (day.toLowerCase()){
            case "monday":
                dayOfWeek = Calendar.MONDAY;
                break;
            case "tuesday":
                dayOfWeek = Calendar.TUESDAY;
                break;
            case "wednesday":
                dayOfWeek = Calendar.WEDNESDAY;
                break;
            case "thursday":
                dayOfWeek = Calendar.THURSDAY;
                break;
            case "friday":
                dayOfWeek = Calendar.FRIDAY;
                break;
            case "saturday":
                dayOfWeek = Calendar.SATURDAY;
                break;
            case "sunday":
                dayOfWeek = Calendar.SUNDAY;
                break;
            default:
                Log.e("Error: ", "NextDayOfWeek(), day parse");
        }

        //On calcule la prochaine journée
        int difference = dayOfWeek - date.get(Calendar.DAY_OF_WEEK);

        if (difference <= 0) {
            difference += 7;
        }

        date.add(Calendar.DAY_OF_MONTH, difference);

        return date;
    }

    /**
     * On vérifie si le stage contient déjà une visite pour la même journée.
     * @param internship Stage auquel on va vérifier ses visites.
     * @param visit Visit qu'on veut vérifier que sa journée ne se trouve pas dans le stage.
     * @return S'il exite une visite dans la même journée.
     */
    public static boolean containsVisitAtSameDay(Internship internship, Visit visit){
        ArrayList<Visit> visitsOnIntern = internship.getVisitList();

        for (Visit visitOnIntern : visitsOnIntern) {
            Date visitOnInternDate = new Date();
            Date visitToProveDate = new Date();

            try{
                visitOnInternDate = new SimpleDateFormat("yyyy-MM-dd").parse(visitOnIntern.getVisitDate());
                visitToProveDate = new SimpleDateFormat("yyyy-MM-dd").parse(visit.getVisitDate());

                if(visitOnInternDate.getDay() == visitToProveDate.getDay()){
                    return true;
                }

            }catch (Exception e){
                Log.d("Visit", "Error containsVisitAtSameDay():" + e);
            }

        }

        return false;
    }

    /**
     * Vérifie que chaque visite contient une marge de temps unique.
     * @param visits Toutes les visites à comparer.
     * @return ArrayList des visites avec une marge de temps unique pour une même journée.
     */
    public static ArrayList<Visit> adaptVisitDates(ArrayList<Visit> visits){
        SimpleDateFormat formaterHour = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat formaterDay = new SimpleDateFormat("yyyy-MM-dd");
        Date startVisitDate1 = new Date();
        Date startVisitDate2 = new Date();

        try{
            for (int i = 0; i < visits.size(); i++) {
                Visit visit1 = visits.get(i);
                startVisitDate1 = new SimpleDateFormat("HH:mm:ss").parse(visit1.getStartHour());

                Calendar endVisit1 = Calendar.getInstance();
                endVisit1.set(Calendar.HOUR_OF_DAY, startVisitDate1.getHours());
                endVisit1.set(Calendar.MINUTE, startVisitDate1.getMinutes());
                endVisit1.add(Calendar.MINUTE, Integer.parseInt(visit1.getDuring()));

                Date dayVisit1 = new SimpleDateFormat("yyyy-MM-dd").parse(visit1.getVisitDate());

                for (int j = 0; j < visits.size(); j++) {
                    Visit visit2 = visits.get(j);
                    startVisitDate2 = new SimpleDateFormat("HH:mm:ss").parse(visit2.getStartHour());

                    Calendar endVisit2 = Calendar.getInstance();
                    endVisit2.set(Calendar.HOUR_OF_DAY, startVisitDate2.getHours());
                    endVisit2.set(Calendar.MINUTE, startVisitDate2.getMinutes());
                    endVisit2.add(Calendar.MINUTE, Integer.parseInt(visit2.getDuring()));

                    Date dayVisit2 = new SimpleDateFormat("yyyy-MM-dd").parse(visit2.getVisitDate());

                    if(startVisitDate1.getHours() >= startVisitDate2.getHours() &&
                            startVisitDate1.getHours() <= endVisit2.getTime().getHours() &&
                            dayVisit1.getDay() == dayVisit2.getDay() &&
                            !visit1.getVisitId().equals(visit2.getVisitId()) ){
                        //ici on a la même heure, même journée

                        //On donne le endVisit2 au visitDate1
                        Calendar newTime = Calendar.getInstance();
                        newTime.set(Calendar.HOUR_OF_DAY, endVisit2.getTime().getHours());
                        newTime.set(Calendar.MINUTE, endVisit2.getTime().getMinutes());
                        newTime.add(Calendar.MINUTE, 30);//On ajoute 1 heure

                        visit1.setStartHour(formaterHour.format(newTime.getTime()));
                        i = 0; //Pour revérifier si la nouvelle date a des conflits encore.

                        break;
                    }

                }

            }

        }catch (Exception e){
            Log.d("Visit", "Error adaptVisitDates():" + e);
        }

        return visits;

    }
}
