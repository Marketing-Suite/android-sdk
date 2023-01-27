package experian.mobilesdk

import experian.mobilesdk.model.DeviceToken
import experian.mobilesdk.network.MessagingAPI
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.configuration.MockResponseFileReader
import org.mockito.junit.MockitoJUnitRunner
import javax.net.ssl.HttpsURLConnection

@RunWith(MockitoJUnitRunner::class)
class EMSMobileSDKTest {

    private lateinit var messagingAPI: MessagingAPI
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        messagingAPI =
            MessagingAPI("https://xts.eccmp.com/xts/registration/cust/394/application/ac1e5ffb-32aa-4881-a795-25a155905b5b/")
    }

    @Test
    fun readSampleSuccessJSONFile() {
        val reader = MockResponseFileReader("register_token_success.json")
        assertNotNull(reader.content)
    }

    @Test
    fun testRegisterToken_Success() {
        val response = MockResponse()
            .setResponseCode(HttpsURLConnection.HTTP_CREATED)
            .setBody(MockResponseFileReader("register_token_success.json").content)
        mockWebServer.enqueue(response)
        var actualResponse = messagingAPI.registerToken(DeviceToken("test-token")).execute()
        assertEquals(
            response.toString().contains("201"),
            actualResponse.code().toString().contains("201")
        )
        assertNotNull(actualResponse.body().pushRegistrationId)
        assertEquals(actualResponse.body().deviceToken, "test-token")
    }

    @Test
    fun testDeactivateToken_Success() {
        val response = MockResponse()
            .setResponseCode(HttpsURLConnection.HTTP_CREATED)
            .setBody(MockResponseFileReader("register_token_success.json").content)
        mockWebServer.enqueue(response)
        var actualResponse = messagingAPI.deactivateToken(DeviceToken("test-token")).execute()
        assertEquals(
            response.toString().contains("201"),
            actualResponse.code().toString().contains("201")
        )
        assert(actualResponse.body() == "true")
    }

    @Test
    fun testUpdateToken_Success() {
        val response = MockResponse()
            .setResponseCode(HttpsURLConnection.HTTP_CREATED)
        mockWebServer.enqueue(response)
        var actualResponse = messagingAPI.updateToken(
            "49b194dd-ce21-446b-a7c2-956c6a2a83ea",
            DeviceToken("test-token-2")
        ).execute()
        assertEquals(
            response.toString().contains("201"),
            actualResponse.code().toString().contains("201")
        )
        assert(actualResponse.body().pushRegistrationId == "49b194dd-ce21-446b-a7c2-956c6a2a83ea")
        assert(actualResponse.body().deviceToken == "test-token-2")
    }

    @Test
    fun testTrackEmsOpen_Success() {
        val response = MockResponse()
            .setResponseCode(HttpsURLConnection.HTTP_OK)
        mockWebServer.enqueue(response)
        var actualResponse = messagingAPI.trackEmsOpen("https://catfact.ninja/fact").execute()
        assertEquals(
            response.toString().contains("200"),
            actualResponse.code().toString().contains("200")
        )
        assert(actualResponse.body() != null)
    }

    @Test
    fun testPostAPI_Success() {
        val response = MockResponse()
            .setResponseCode(HttpsURLConnection.HTTP_OK)
        mockWebServer.enqueue(response)
        val map = mapOf(
            "s_first_name" to "SDK",
            "s_last_name" to "Sample",
            "s_email_address" to "test@test.com",
            "s_password" to "123456789",
            "s_phone_number" to "9173827374",
            "s_push_registration_id" to "49b194dd-ce21-446b-a7c2-956c6a2a83ea",
            "s_logged_in" to "N",
            "cr" to 394,
            "fm" to 3522
        )
        var actualResponse =
            messagingAPI.postApi("https://ats.eccmp.com/ats/post.aspx", map).execute()
        assertEquals(
            response.toString().contains("200"),
            actualResponse.code().toString().contains("200")
        )
    }

    @Test
    fun testOpenDeepLink_Success() {
        val response = MockResponse()
            .setResponseCode(HttpsURLConnection.HTTP_OK)
        mockWebServer.enqueue(response)

        var actualResponse = messagingAPI.openDeepLink("https://www.google.com").execute()
        assertEquals(
            response.toString().contains("200"),
            actualResponse.code().toString().contains("200")
        )
    }
}