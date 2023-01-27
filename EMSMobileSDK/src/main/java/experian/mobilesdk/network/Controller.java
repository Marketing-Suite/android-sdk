package experian.mobilesdk.network;

import static java.util.Objects.isNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tickaroo.tikxml.TikXml;
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory;

import java.util.concurrent.TimeUnit;

import experian.mobilesdk.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Controller {
  // API Date Format
  public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  public static final String TIMEZONE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
  private static OkHttpClient mHttpClient;
  private final String TAG = "RestController";

  public Controller() {
    initialize();
  }

  protected <T> T createRetrofitInterface(String endpoint, Class<T> classInterface) {
    return getRestAdapter(endpoint).create(classInterface);
  }

  public void initialize() {
    if (isNull(mHttpClient)) {
      mHttpClient = createHttpClient();
    }
  }

  protected OkHttpClient createHttpClient() {
    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder()
            .connectTimeout(getTimeoutConnectionInSeconds(), TimeUnit.SECONDS)
            .readTimeout(getTimeoutSocketInSeconds(), TimeUnit.SECONDS)
            .addInterceptor(new HeaderRequestInterceptor("emsmobile-sdk"));

    if (BuildConfig.DEBUG) {
      clientBuilder.addInterceptor(getLoggingInterceptor());
    }

    return clientBuilder.build();
  }

  protected int getTimeoutConnectionInSeconds() {
    return 60;
  }

  protected int getTimeoutSocketInSeconds() {
    return 60;
  }

  protected Gson getGsonConfiguration() {
    return new GsonBuilder().setLenient().create();
  }

  protected TikXml getTikXmlConfiguration() {
    return new TikXml.Builder().exceptionOnUnreadXml(false).build();
  }

  protected Retrofit getRestAdapter(String endpoint) {
    Retrofit.Builder builder = new Retrofit.Builder();
    builder.addConverterFactory(
        new JsonAndXmlConverters.QualifiedTypeConverterFactory(
            GsonConverterFactory.create(getGsonConfiguration()),
            TikXmlConverterFactory.create(getTikXmlConfiguration())));
    builder.baseUrl(endpoint);
    if (mHttpClient != null) {
      builder.client(mHttpClient);
    }
    return builder.build();
  }

  protected HttpLoggingInterceptor getLoggingInterceptor() {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    return logging;
  }
}
