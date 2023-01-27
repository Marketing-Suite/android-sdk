package experian.mobilesdk.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderRequestInterceptor implements Interceptor {
  private final String userAgentString;

  public HeaderRequestInterceptor(String userAgentString) {
    this.userAgentString = userAgentString;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    request =
        request
            .newBuilder()
            .addHeader("User-Agent", userAgentString)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build();
    return chain.proceed(request);
  }
}
