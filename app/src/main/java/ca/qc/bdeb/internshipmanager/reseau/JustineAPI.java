package ca.qc.bdeb.internshipmanager.reseau;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface JustineAPI {

    @POST("/auth/connexion")
    @Headers({
            "Content-Type:application/json",
            "Authorization:Token"
    })
    Call<ResponseBody> connecter(@Body HashMap<String, Object> loginData);

    @POST("/auth/deconnexion")
    Call<ResponseBody> deconnecter(@Header("Authorization") String token);

    @POST("/auth/testerconnexion")
    Call<ResponseBody> testerConnexion(@Header("Authorization") String token, @Body HashMap<String, Object> userId);

    @GET("/entreprise")
    Call<ResponseBody> getEntreprises(@Header("Authorization") String token);

    @GET("/stage")
    Call<ResponseBody> getStages(@Header("Authorization") String token);

    @GET("/stage/{idStage}")
    Call<ResponseBody> getStage(@Header("Authorization") String token, @Path("idStage") String idStage);

    @GET("/compte/getcomptesetudiantsactifs")
    Call<ResponseBody> getComptesEleves(@Header("Authorization") String token);

    @POST("/stage")
    Call<ResponseBody> ajouterStage(@Header("Authorization") String token, @Body HashMap<String, Object> data);

    @DELETE("/stage/{idStage}")
    Call<ResponseBody> supprStage(@Header("Authorization") String token, @Path("idStage") String idStage);
}

