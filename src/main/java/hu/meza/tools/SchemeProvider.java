package hu.meza.tools;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SchemeProvider {

	private static final int PORT = 443;

	public Scheme trustAllScheme() {
		try {
			SSLSocketFactory sslsf = new SSLSocketFactory(new TrustStrategy() {

				public boolean isTrusted(
						final X509Certificate[] chain, String authType
				) throws CertificateException {
					// Oh, I am easy...
					return true;
				}

			});
			return new Scheme("https", PORT, sslsf);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
