package ca.qc.bdeb.internshipmanager.dataclasses;


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
    private String endHour;
    private String startLunch;
    private String endLunch;
    private String startDuring;
    private String endDuring;

    /**
     * Crée une nouvelle visite.
     * Le id de la visite est la même du stage auquelle elle est associé.
     * @param visitId Id du stage auquelle la visite est associé.
     * @param visitDate Date de la visite.
     * @param startHour Heure du début de la visite.
     */
    public Visit(String visitId, String internshipId, String visitDate, String startHour, String endHour, String startLunch, String endLunch, String startDuring, String endDuring) {
        this.visitId = visitId;
        this.internshipId = internshipId;
        this.visitDate = visitDate;
        this.startHour = startHour;
        this.endHour = endHour;
        this.startLunch = startLunch;
        this.endLunch = endLunch;
        this.startDuring = startDuring;
        this.endDuring = endDuring;
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

    public String getStartDuring() {
        return startDuring;
    }

    public void setStartDuring(String startDuring) {
        this.startDuring = startDuring;
    }

    public String getEndDuring() {
        return endDuring;
    }

    public void setEndDuring(String endDuring) {
        this.endDuring = endDuring;
    }
}
