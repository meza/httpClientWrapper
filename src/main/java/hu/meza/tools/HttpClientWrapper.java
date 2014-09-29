package hu.meza.tools;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HttpClientWrapper {

	public static final int PORT = 443;
	public static final String DEFAULT_CHARSET = "UTF-8";
	private HttpClient client;
	private CookieStore cookieStore = new BasicCookieStore();
	private HttpContext localContext = new BasicHttpContext();
	private SchemeProvider schemeProvider = new SchemeProvider();
	private String host = "";

	public HttpClientWrapper(HttpClient client) {
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		this.client = client;
	}

	public HttpClientWrapper() {
		this(new DefaultHttpClient());
	}

	public void addCookie(Cookie cookie) {
		cookieStore.addCookie(cookie);
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void dontCareAboutSSL() {
		client.getConnectionManager().getSchemeRegistry().register(schemeProvider.trustAllScheme());
	}

	public void followRedirects() {
		client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
	}

	public void doNotFollowRedirects() {
		client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
	}

	public HttpCall getFrom(String url) {
		return getFrom(url, new Header[]{});
	}

	public HttpCall getFrom(String url, String user, String pass) {
		String authStr = String.format("%s:%s", user, pass);
		String authEncoded = Base64.encodeBase64String(authStr.getBytes());
		Header[] headers = {
			new BasicHeader("Authorization", "Basic " + authEncoded)
		};
		return getFrom(url, headers);
	}

	public HttpCall getFrom(String url, Header[] headers) {
		HttpUriRequest request = new HttpGet(constructUrl(url));
		return doRequest(request, headers);
	}

	public HttpCall postFormTo(String requestUrl, String requestBody) {
		return postFormTo(requestUrl, requestBody, new Header[]{});
	}

	public HttpCall postFormTo(
		String requestUrl, String requestBody, Header[] headers
	) {
		HttpPost request = new HttpPost(constructUrl(requestUrl));
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			request.setEntity(new StringEntity(requestBody));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return doRequest(request, headers);
	}

	public HttpCall postJsonTo(String requestUrl, String requestBody) {
		return postTo(requestUrl, requestBody, getJsonHeaders(), DEFAULT_CHARSET);
	}

	public HttpCall postJsonTo(String requestUrl, String requestBody, Header[] headers) {

		int len = getJsonHeaders().length + headers.length;
		Header[] allheaders = new Header[len];

		int index = 0;
		for (Header h : getJsonHeaders()) {
			allheaders[index++] = h;
		}
		for (Header h : headers) {
			allheaders[index++] = h;
		}

		return postTo(requestUrl, requestBody, allheaders, DEFAULT_CHARSET);
	}

	public HttpCall postJsonTo(
		String requestUrl, String requestBody, String charset
	) {
		return postTo(requestUrl, requestBody, getJsonHeaders(), charset);
	}

	public HttpCall postTo(String requestUrl, String requestBody) {
		return postTo(requestUrl, requestBody, new Header[]{}, DEFAULT_CHARSET);
	}

	public HttpCall postTo(
		String requestUrl, String requestBody, Header[] headers, String charset
	) {
		HttpPost request = new HttpPost(requestUrl);

		try {
			request.setEntity(new StringEntity(requestBody, charset));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return doRequest(request, headers);
	}

	public HttpCall deleteFromWithJson(String requestUrl, String requestBody) {
		return delete(requestUrl, requestBody, getJsonHeaders());
	}

	public HttpCall delete(
		String requestUrl, String requestBody, Header[] headers
	) {
		DeletePayload request = new DeletePayload(constructUrl(requestUrl));

		try {
			request.setEntity(new StringEntity(requestBody));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return doRequest(request, headers);
	}

	public HttpCall patch(
		String requestUrl, String requestBody, Header[] headers
	) {
		HttpPatch request = new HttpPatch(constructUrl(requestUrl));

		try {
			request.setEntity(new StringEntity(requestBody));

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return doRequest(request, headers);
	}

	private Header[] getJsonHeaders() {
		Header[] headers = {
			new BasicHeader("Content-Type", "application/json")
		};

		return headers;
	}

	private HttpCall doRequest(HttpUriRequest request, Header[] headers) {
		addHeadersToRequest(request, headers);
		try {

			HttpResponse response = client.execute(request, localContext);
			return new HttpCall(response, request);
		} catch (IOException e) {
			String msg = String.format("Could not %s %s\n%s", request.getMethod(), request.getURI(),
				e.getLocalizedMessage());
			throw new RuntimeException(msg);
		}
	}

	private void addHeadersToRequest(HttpUriRequest request, Header[] headers) {
		for (int i = 0; i < headers.length; i++) {
			Header header = headers[i];
			request.addHeader(header);
		}
	}

	private String constructUrl(String uri) {
		if (host.isEmpty()) {
			return uri;
		}
		return host + uri;
	}

}
