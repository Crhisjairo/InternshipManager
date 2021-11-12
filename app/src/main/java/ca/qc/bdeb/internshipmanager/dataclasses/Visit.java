package ca.qc.bdeb.internshipmanager.dataclasses;

import java.util.UUID;


/**
 * Classe qui permet de créer, modifier ou supprimer une visite.
 * Un stage peut posséder plusieurs visites. Les visites possèdent une date, une heure de début
 * et une heure de fin.
 * */
public class Visit {
    private String stageId;
    private String visitDate;
    private String startTime;
    private int during;

    /**
     * Crée une nouvelle visite.
     * Le id de la visite est la même du stage auquelle elle est associé.
     * @param stageId Id du stage auquelle la visite est associé.
     * @param visitDate Date de la visite.
     * @param startTime Heure du début de la visite.
     * @param during Heure de fin de la visite.
     */
    public Visit(String stageId, String visitDate, String startTime, int during)
    {
        this.stageId = stageId;
        this.visitDate = visitDate;
        this.startTime = startTime;
        this.during = during;
    }

    /**
     * Recupère le during de la visite.
     * @return During de la visite.
     */
    public int getDuring() {
        return during;
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
    public String getStartTime() {
        return startTime;
    }

    /**
     * Recupère l'heure de fin de la visite.
     * @return Heure de fin de la visite.
     */
    public String getVisit_id() {
        return stageId;
    }
}
