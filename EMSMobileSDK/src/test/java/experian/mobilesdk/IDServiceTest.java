package experian.mobilesdk;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({
  "org.powermock.*",
  "org.mockito.*",
  "org.robolectric.*",
  "android.*",
  "jdk.internal.reflect.*"
})
@PrepareForTest(FirebaseInstanceId.class)
@Config(sdk = 21)
public class IDServiceTest {

  private final String SAMPLE_TOKEN = "123456789";
  private final String APP_ID = "0d941093-9f85-41d3-a92b-0cf205183a51";
  private final int CUSTOMER_ID = 100;
  private final Region REGION = Region.NORTH_AMERICA;
  @Rule public PowerMockRule rule = new PowerMockRule();
  IDService idService;
  Context context;

  private FirebaseInstanceId firebaseInstanceId;

  @Before
  public void setup() {
    context = PowerMockito.spy(RuntimeEnvironment.application);
    FirebaseApp.initializeApp(context);

    idService = PowerMockito.spy(new IDService());
    PowerMockito.doReturn(context).when(idService).getApplicationContext();

    PowerMockito.mockStatic(FirebaseInstanceId.class);
    firebaseInstanceId = PowerMockito.mock(FirebaseInstanceId.class);
    PowerMockito.when(firebaseInstanceId.getInstance()).thenReturn(firebaseInstanceId);
  }

  @Test
  public void testOnTokenRefresh_setToken() {
    PowerMockito.doReturn(SAMPLE_TOKEN).when(firebaseInstanceId).getToken();
    EMSMobileSDK.Default().init(context, APP_ID, CUSTOMER_ID, REGION);
    idService.onTokenRefresh();
    Assert.assertEquals(SAMPLE_TOKEN, EMSMobileSDK.Default().getToken());
  }

  @Test
  public void testOnTokenRefresh_notInitialized() {
    PowerMockito.doReturn(null).when(firebaseInstanceId).getToken();
    idService.onTokenRefresh();
    Assert.assertEquals("", EMSMobileSDK.Default().getToken());
  }
}
