package experian.mobilesdk;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({ "org.powermock.*", "org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest(RemoteMessage.class)
@Config(sdk=21)
public class MessagingServiceTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private MessagingService messagingService;

    private RemoteMessage remoteMessage;

    private Context context;

    @Before
    public void setup() {
        remoteMessage = PowerMockito.mock(RemoteMessage.class);
        messagingService = PowerMockito.spy(new MessagingService());
        Map<String, String> testMap = new HashMap<>();
        testMap.put("TEST", "TEST");
        PowerMockito.when(remoteMessage.getData()).thenReturn(testMap);

        context = PowerMockito.spy(RuntimeEnvironment.application);
        PowerMockito.doReturn(context).when(messagingService).getApplicationContext();
    }

    @Test
    public void testOnMessageReceived_intentBroadcast() {
        PowerMockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object object = invocation.getArguments()[0];
                if (object instanceof Intent && (
                        ((Intent) object).getAction().equals(context.getPackageName() +
                                EMSIntents.EMS_PUSH_RECEIVED) ||
                                ((Intent) object).getAction().equals(context.getPackageName() +
                                        EMSIntents.EMS_SHOW_NOTIFICATION)
                )) {
                    Assert.assertEquals(remoteMessage, ((Intent) object).getParcelableExtra("data"));
                    Assert.assertEquals(context.getPackageName(), ((Intent) object).getPackage());
                } else {
                    Assert.fail();
                }
                return null;
            }
        }).when(messagingService).sendBroadcast(Mockito.any());
        messagingService.onMessageReceived(remoteMessage);
    }
}
