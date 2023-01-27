package experian.mobilesdk.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import experian.mobilesdk.model.DeviceToken;
import experian.mobilesdk.model.TokenResponse;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

class ControllerTest {

  @Test
  void testRetrofitInstance() {
    Retrofit instance =
        new Controller().getRestAdapter("https://xts.eccmp.com/xts/registration/cust/394/");
    assertNotNull(instance.baseUrl().url().toString());
  }

  @Test
  void testRegisterToken() throws IOException {
    MessagingAPI messagingAPI =
        new MessagingAPI(
            "https://xts.eccmp.com/xts/registration/cust/394/application/ac1e5ffb-32aa-4881-a795-25a155905b5b/");

    DeviceToken deviceToken = new DeviceToken("this_is_a_test_token");
    Response<TokenResponse> response = messagingAPI.registerToken(deviceToken).execute();
    ResponseBody errorBody = response.errorBody();

    assertNull(errorBody);

    TokenResponse responseWrapper = response.body();
    assertNotNull(responseWrapper);
    assertEquals(response.code(), 201);
  }
}
