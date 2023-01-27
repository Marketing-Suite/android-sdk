package experian.mobilesdk.network;

import java.util.Map;

import experian.mobilesdk.model.ApiResponse;
import experian.mobilesdk.model.DeviceToken;
import experian.mobilesdk.model.TokenResponse;
import retrofit2.Call;

/** */
public class MessagingAPI extends Controller {
  private final EMSInterface emsInterface;

  /** */
  public MessagingAPI(String endpoint) {
    emsInterface = createRetrofitInterface(endpoint, EMSInterface.class);
  }

  /**
   * @param token
   */
  public Call<TokenResponse> registerToken(DeviceToken token) {
    return emsInterface.registerToken(token);
  }

  /**
   * @param prid
   * @param token
   */
  public Call<TokenResponse> updateToken(String prid, DeviceToken token) {
    return emsInterface.updateToken(prid, token);
  }

  /**
   * @param token
   */
  public Call<String> deactivateToken(DeviceToken token) {
    return emsInterface.deactivateToken(token);
  }

  /**
   * @param emsOpen
   */
  public Call<Object> trackEmsOpen(String emsOpen) {
    return emsInterface.trackEmsOpen(emsOpen);
  }

  /**
   * @param postUrl
   * @param fields
   */
  public Call<ApiResponse> postApi(String postUrl, Map<String, Object> fields) {
    return emsInterface.postAPI(postUrl, fields);
  }

  /**
   * @param deepLinkUrl
   */
  public Call<String> openDeepLink(String deepLinkUrl) {
    return emsInterface.openDeepLink(deepLinkUrl);
  }
}
