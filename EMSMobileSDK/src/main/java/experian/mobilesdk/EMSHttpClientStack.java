package experian.mobilesdk;

import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import java.net.URI;
import java.util.Map;
import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;

/**
 * Created by lev on 12/14/17.
 * The whole purpose for this class extension, is so that we can continue
 * usage of the Volley library, and perform a DELETE with a body.  Volley
 * doesn't support DELETE's with a body by default.
 */

public class EMSHttpClientStack extends HttpClientStack {

    private final static String HEADER_CONTENT_TYPE = "Content-Type";

    public EMSHttpClientStack(HttpClient client) {
        super(client);
    }

    /**
     * Performs an HTTP request with the given parameters.
     *
     * @param request           the request to perform
     * @param additionalHeaders additional headers to be sent together with Request.getHeaders()
     * @return the HTTP response
     * @throws IOException
     * @throws AuthFailureError
     */
    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
        addHeaders(httpRequest, additionalHeaders);
        addHeaders(httpRequest, request.getHeaders());
        onPrepareRequest(httpRequest);
        HttpParams httpParams = httpRequest.getParams();
        int timeoutMs = request.getTimeoutMs();
        // TODO: Reevaluate this connection timeout based on more wide-scale
        // data collection and possibly different for wifi vs. 3G.
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        return mClient.execute(httpRequest);
    }

    /**
     * Sets the headers to the HTTP Request
     *
     * @param httpRequest httprequest
     * @param headers     header that wants to be added to the httprequest
     */
    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     *
     * @param request           the request to perform
     * @param additionalHeaders additional headers to be sent together with Request.getHeaders()
     * @return {@link HttpUriRequest}
     * @throws AuthFailureError
     */
    static HttpUriRequest createHttpRequest(Request<?> request, Map<String, String> additionalHeaders) throws AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST: {
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    HttpPost postRequest = new HttpPost(request.getUrl());
                    postRequest.addHeader(HEADER_CONTENT_TYPE, request.getPostBodyContentType());
                    HttpEntity entity;
                    entity = new ByteArrayEntity(postBody);
                    postRequest.setEntity(entity);
                    return postRequest;
                } else {
                    return new HttpGet(request.getUrl());
                }
            }
            case Request.Method.GET:
                return new HttpGet(request.getUrl());
            case Request.Method.DELETE:
                EMSHttpDelete deleteRequest = new EMSHttpDelete(request.getUrl());
                deleteRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(deleteRequest, request);
                return deleteRequest;
            case Request.Method.POST: {
                HttpPost postRequest = new HttpPost(request.getUrl());
                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            case Request.Method.PUT: {
                HttpPut putRequest = new HttpPut(request.getUrl());
                putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(putRequest, request);
                return putRequest;
            }
            case Request.Method.HEAD:
                return new HttpHead(request.getUrl());
            case Request.Method.OPTIONS:
                return new HttpOptions(request.getUrl());
            case Request.Method.TRACE:
                return new HttpTrace(request.getUrl());
            case Request.Method.PATCH: {
                HttpPatch patchRequest = new HttpPatch(request.getUrl());
                patchRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(patchRequest, request);
                return patchRequest;
            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    /**
     * Set HTTP Entity to  body if body is not empty
     *
     * @param httpRequest {@link HttpEntityEnclosingRequestBase}
     * @param request     {@link Request}
     * @throws AuthFailureError
     */
    private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequestBase httpRequest,
                                                Request<?> request) throws AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }

    /**
     * Class which extends HttpPost
     */
    private static class EMSHttpDelete extends HttpPost {
        public static final String METHOD_NAME = "DELETE";

        public EMSHttpDelete() {
            super();
        }

        public EMSHttpDelete(URI uri) {
            super(uri);
        }

        /**
         * Constructor for {@link EMSHttpDelete}
         *
         * @param uri in String format
         */
        public EMSHttpDelete(String uri) {
            super(uri);
        }

        /**
         * String which returns value of METHOD_NAME
         *
         * @return METHOD_NAME
         */
        public String getMethod() {
            return METHOD_NAME;
        }
    }
}
