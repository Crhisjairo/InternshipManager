package ca.qc.bdeb.internshipmanager.dataclasses;

/**
 * Classe qui permet de créer, modifier ou supprimer une entreprise.
 * Un entreprise est le lieu où un stage se déroule.
 * */
public class Enterprise {
    private String enterpriseId;
    private String name;
    private String address;
    private String town;
    private String province;
    private String postalCode;

    /**
     * Crée une nouvelle entreprise.
     * Le id de l'entreprise est recupéré depuis la BD ou elle est géneré aléatoirement avant d'être sauvegardé.
     *
     * @param enterpriseId
     * @param name
     * @param address
     * @param town
     * @param province
     * @param postalCode
     */
    public Enterprise(String enterpriseId, String name, String address, String town, String province, String postalCode) {
        this.enterpriseId = enterpriseId;
        this.name = name;
        this.address = address;
        this.town = town;
        this.province = province;
        this.postalCode = postalCode;
    }

    /**
     * Recupère l'id unique de l'entreprise.
     *
     * @return Id unique associé à l'entreprise.
     */
    public String getEnterpriseId() {
        return enterpriseId;
    }

    /**
     * Recupère l'adresse de l'entreprise ou un stage va se déroulé.
     *
     * @return Adresse de l'entreprise.
     */
    public String getAddress() {
        return address;
    }


    /**
     * Recupère le nom de l'entreprise.
     *
     * @return Nom de l'entreprise.
     */

    public String getName() {
        return name;
    }

    /**
     * Recupère le code postal où l'entreprise se situe.
     *
     * @return Code postal de l'entreprise.
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Recupère la province où l'entreprise se situe.
     *
     * @return Privince où l'entreprise se situe.
     */
    public String getProvince() {
        return province;
    }

    /**
     * Recupère la ville où l'entreprise se situe.
     *
     * @return Ville où l'entreprise se situe.
     */
    public String getTown() {
        return town;
    }


    public String getAddressFull() {
        return address + " " + postalCode + " " + town + " " + province + " Canada";
    }

}