package hu.meza.tools;

import org.apache.http.client.methods.HttpPost;

class DeletePayload extends HttpPost {
	public DeletePayload(String url){
		super(url);
	}
	@Override
	public String getMethod() {
		return "DELETE";
	}
}
