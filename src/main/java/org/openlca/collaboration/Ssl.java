package org.openlca.collaboration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class Ssl {

	private static KeyStore keyStore;
	private static CertificateFactory certificateFactory;
	private static TrustManagerFactory trustManagerFactory;
	private static Path keyStorePath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
	private static String keyStorePassword = "changeit";

	static {
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
			keyStore = loadKeyStore();
			trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		} catch (Exception e) {
			certificateFactory = null;
			keyStore = null;
			trustManagerFactory = null;
		}
	}

	public static KeyStore getKeyStore() {
		return keyStore;
	}

	public static SSLContext createContext() {
		if (trustManagerFactory == null)
			return null;
		try {
			trustManagerFactory.init(keyStore);
			var context = SSLContext.getInstance("TLS");
			context.init(null, trustManagerFactory.getTrustManagers(), null);
			return context;
		} catch (Exception e) {
			return null;
		}
	}

	public static void addCertificate(String name, InputStream stream) throws CertificateException, KeyStoreException {
		var certificate = certificateFactory.generateCertificate(stream);
		addCertificate(name, certificate);
	}

	public static void addCertificate(String name, Certificate certificate) throws KeyStoreException {
		keyStore.setCertificateEntry(name, certificate);
	}

	public static void removeCertificate(String name) throws KeyStoreException {
		keyStore.deleteEntry(name);
	}

	private static KeyStore loadKeyStore() throws Exception {
		var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(Files.newInputStream(keyStorePath), keyStorePassword.toCharArray());
		return keyStore;
	}

	public static void saveKeyStore() throws Exception {
		keyStore.store(Files.newOutputStream(keyStorePath), keyStorePassword.toCharArray());
	}

}
