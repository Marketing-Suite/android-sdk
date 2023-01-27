package experian.mobilesdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

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
@PowerMockIgnore({
  "org.powermock.*",
  "org.mockito.*",
  "org.robolectric.*",
  "android.*",
  "org.json.*",
  "jdk.internal.reflect.*"
})
@PrepareForTest({RemoteMessage.class, NotificationManager.class})
@Config(sdk = 21)
public class NotificationReceiverTest {
  private static final String TITLE = "Hello World!";
  private static final String BODY = "Lorem ipsum dolor sit amet";
  @Rule public PowerMockRule rule = new PowerMockRule();
  private NotificationReceiver notificationReceiver;
  private Context context;
  private RemoteMessage remoteMessage;
  private NotificationManager notificationManager;

  @Before
  public void setUp() {
    notificationReceiver = PowerMockito.spy(new NotificationReceiver());
    context = PowerMockito.spy(RuntimeEnvironment.application);
    remoteMessage = PowerMockito.mock(RemoteMessage.class);
    notificationManager = PowerMockito.mock(NotificationManager.class);
  }

  @Test
  public void testOnReceive_showNotificationIntent() {
    Map<String, String> map = new HashMap<>();
    map.put("title", TITLE);
    map.put("body", BODY);
    map.put("ems_open", "ems_open");
    PowerMockito.when(remoteMessage.getData()).thenReturn(map);
    PowerMockito.doReturn(notificationManager).when(context).getSystemService(Mockito.anyString());
    PowerMockito.doAnswer(
            new Answer<Object>() {
              @Override
              public Object answer(InvocationOnMock invocation) throws Throwable {
                Object notificationTag = invocation.getArguments()[0];
                Object notificationId = invocation.getArguments()[1];
                Notification notification = (Notification) invocation.getArguments()[2];

                Assert.assertEquals(NotificationReceiver.NOTIFICATION_TAG, notificationTag);
                Assert.assertEquals(NotificationReceiver.NOTIFICATION_ID, notificationId);
                Assert.assertEquals(TITLE, notification.extras.getString("android.title"));
                Assert.assertEquals(BODY, notification.extras.getString("android.text"));
                return null;
              }
            })
        .when(notificationManager)
        .notify(Mockito.anyString(), Mockito.anyInt(), Mockito.any());

    Intent intent = new Intent();
    intent.setAction(context.getPackageName() + EMSIntents.EMS_SHOW_NOTIFICATION);
    intent.putExtra("data", remoteMessage);
    notificationReceiver.onReceive(context, intent);
  }

  @Test
  public void testOnReceive_openNotificationLaunchIntentNotNull() {
    PackageManager packageManager = PowerMockito.mock(PackageManager.class);
    PowerMockito.when(context.getPackageManager()).thenReturn(packageManager);
    Intent launchIntent = new Intent();
    PowerMockito.when(packageManager.getLaunchIntentForPackage(Mockito.anyString()))
        .thenReturn(launchIntent);
    PowerMockito.doAnswer(
            new Answer<Object>() {
              @Override
              public Object answer(InvocationOnMock invocation) throws Throwable {
                Intent launchIntentWithExtras = (Intent) invocation.getArguments()[0];
                boolean isOpenFromNotification =
                    launchIntentWithExtras.getBooleanExtra("EMS_OPEN_FROM_NOTIFICATION", false);
                Assert.assertTrue(isOpenFromNotification);
                return null;
              }
            })
        .when(context)
        .startActivity(Mockito.any());

    Intent intent = new Intent();
    intent.setAction(context.getPackageName() + EMSIntents.EMS_OPEN_NOTIFICATION);
    notificationReceiver.onReceive(context, intent);
  }
}
