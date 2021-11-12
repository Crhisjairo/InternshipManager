package ca.qc.bdeb.internshipmanager.dataclasses;


import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * Classe qui va permettre de créer, modifier ou supprimer un compte.
 * On a trois types de comptes: 0. Admin  1. Professeur  2. Étudiant.
 */
public class Account implements Comparable<Account>  {
    private int accountId;
    private String createdAt;
    private String deletedAt;
    private String email;
    private boolean isActive;
    private String password;
    private String lastName;
    private String firstName;
    private Bitmap photo;
    private String updatedAt;
    private int accountType;

    /**
     * Crée une nouvelle compte.
     * Le id du compte est recupéré depuis la BD ou elle est géneré aléatoirement avant d'être sauvegardé.
     * Les comptes peuvent être utilisé en fonction de son type.
     * @param accountId Id unique au compte.
     * @param createdAt Date de création du compte.
     * @param deletedAt Date de suppression du compte.
     * @param email Adresse email du compte.
     * @param isActive Si le compte est active.
     * @param password Mot de passe du compte.
     * @param lastName Prénom de l'utilisateur du compte.
     * @param firstName Nom de l'utilisateur du compte.
     * @param photo Photo du compte.
     * @param updatedAt Date de la dernière actualisation des information du compte.
     * @param accountType Type de compte, s'il s'agit d'un admin, professeur ou étudiant.
     */
    public Account(int accountId, String createdAt, String deletedAt, String email, boolean isActive,
                   String password, String lastName, String firstName, Bitmap photo, String updatedAt,
                   int accountType) {
        this.accountId = accountId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
        this.email = email;
        this.isActive = isActive;
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.photo = photo;
        this.updatedAt = updatedAt;
        this.accountType = accountType;
    }

    public void setCreationDate(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setDeleteDate(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUpdatedDate(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreationDate() {
        return createdAt;
    }

    public String getDeletionDate() {
        return deletedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getPassword() {
        return password;
    }

    public String getUpdatedDate() {
        return updatedAt;
    }

    /**
     * Recupère le prénom associé du compte.
     * @return Prénom assigné au compte.
     */
    public String getFirstName() {
        return firstName;
    }


    /**
     * Recupère le nom associé au compté.
     * @return Nom assigné au compte.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Recupère le nom complet associé au compte.
     * @return Nom complet assigné au compte.
     */
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    /**
     * Recupère l'adresse email associé au compte.
     * @return Adresse email assigné au compte.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Recupère le type du compte.
     * 0. Admin  1. Professeur  2. Étudiant.
     * @return Le type du compte.
     */
    public int getAccountType() {
        return accountType;
    }

    /**
     * TODO Recupère l'id de la ressource d'image associé au compte.
     * Plus tard, une photo de profil recupéré par la caméra sera utilisée.
     * @return Id de la ressour d'image assigné au compte.
     */
    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    /**
     * Recupère l'état d'activation du compte.
     * @return Si le compte est active.
     */
    public boolean getActive() {
        return isActive;
    }

    /**
     * Recupère l'id unique du compte.
     * @return Id unique du compte.
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Modifie le prénom associé au compte.
     * @param firstName Nouveau prénom de l'utilisateur du compte.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Modifie le nom associé associé au compte.
     * @param lastName Nouveau nom de l'utilisateur du compte.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Modifie le courriel associé au compte.
     * @param email Nouvelle adresse email de l'utilisateur du compte.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Essaye de changer le mot de passe associé au compte si le nouveau mot de passe es différent du
     * premier.
     * @param password Nouveau mot de passe.
     * @return True si le mot passe a bien été changé, false si le mot passe n'as pas été changé.
     */
    public void trySetPassword(String password) {
        this.password = password;
    }

    /**
     * Modifie l'état d'activation du compte.
     * @param active Nouvelle état d'activation du compte.
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public int compareTo(Account student) {
        return this.getFirstName().compareToIgnoreCase(student.getFirstName());
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", createdAt='" + createdAt + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", password='" + password + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", photo='" + photo + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", accountType=" + accountType +
                '}';
    }
}
