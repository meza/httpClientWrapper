package hu.meza.tools;

import org.junit.Test;

public class HttpClientWrapperTest {

	@Test
	public void x() {
		HttpClientWrapper c = new HttpClientWrapper();
		c.getFrom("http://www.google.com");

	}

}
