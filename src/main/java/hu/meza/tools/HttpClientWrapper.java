package hu.meza.tools;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HttpClientWrapper {

	private HttpClient client;
	private CookieStore cookieStore = new BasicCookieStore();
	private HttpContext localContext = new BasicHttpContext();

	public HttpClientWrapper(HttpClient client) {
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		this.client = client;
	}

	public HttpClientWrapper() {
		this(new DefaultHttpClient());
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

	public HttpCall getFrom(String url, Header[] headers) {
		HttpUriRequest request = new HttpGet(url);
		return doRequest(request, headers);
	}

	public HttpCall postFormTo(String requestUrl, String requestBody) {
		return postFormTo(requestUrl, requestBody, new Header[]{});
	}

	public HttpCall postFormTo(String requestUrl, String requestBody, Header[] headers) {
		HttpPost request = new HttpPost(requestUrl);
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			request.setEntity(new StringEntity(requestBody));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return doRequest(request, headers);
	}

	private HttpCall doRequest(HttpUriRequest request, Header[] headers) {
		addHeadersToRequest(request, headers);
		try {
			HttpResponse response = client.execute(request, localContext);
			return new HttpCall(response, request);
		} catch (IOException e) {
			String msg = String.format("Could not %s %s", request.getMethod(), request.getURI());
			throw new RuntimeException(msg);
		}
	}

	private void addHeadersToRequest(HttpUriRequest request, Header[] headers) {
		for (int i = 0; i < headers.length; i++) {
			Header header = headers[i];
			request.addHeader(header);
		}
	}

}
