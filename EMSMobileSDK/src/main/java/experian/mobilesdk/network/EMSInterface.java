package experian.mobilesdk.network;

import java.util.Map;

import experian.mobilesdk.model.ApiResponse;
import experian.mobilesdk.model.DeviceToken;
import experian.mobilesdk.model.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface EMSInterface {

  @POST("token")
  @JsonAndXmlConverters.Json
  Call<TokenResponse> registerToken(@Body DeviceToken token);

  @HTTP(method = "DELETE", path = "token", hasBody = true)
  @JsonAndXmlConverters.Json
  Call<String> deactivateToken(@Body DeviceToken token);

  @PUT("registration/{prid}/token")
  @JsonAndXmlConverters.Json
  Call<TokenResponse> updateToken(@Path("prid") String prid, @Body DeviceToken token);

  @GET
  @JsonAndXmlConverters.Json
  Call<Object> trackEmsOpen(@Url String emsOpen);

  @FormUrlEncoded
  @POST
  @JsonAndXmlConverters.Xml
  Call<ApiResponse> postAPI(@Url String postUrl, @FieldMap Map<String, Object> fields);

  @GET
  @JsonAndXmlConverters.Json
  Call<String> openDeepLink(@Url String deepLinkUrl);
}
