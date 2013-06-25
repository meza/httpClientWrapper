package hu.meza.tools;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;

public class HttpCall {

	private final HttpResponse response;
	private final HttpRequest request;
	private String body;

	public HttpCall(HttpResponse response, HttpRequest request) {

		this.response = response;
		this.request = request;

		try {
			body = new BasicResponseHandler().handleResponse(response);
		} catch (IOException e) {
			body = "";
		}
	}

	public HttpResponse response() {
		return response;
	}

	public HttpRequest request() {
		return request;
	}

	public String body() {
		return body;
	}

}
