package ru.abch.acceptgoods6;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class WebserviceHTTPClient extends OkHttpClient {
    OkHttpClient client = null;
    public WebserviceHTTPClient(String url) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream clientCertificateContent = App.getInstance().getResources().openRawResource(R.raw.terminal);
            keyStore.load(clientCertificateContent, "".toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "".toCharArray());
            InputStream myTrustedCAFileContent = App.getInstance().getResources().openRawResource(R.raw.chaincert);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate myCAPublicKey = (X509Certificate) certificateFactory.generateCertificate(myTrustedCAFileContent);
            KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustedStore.load(null);
            trustedStore.setCertificateEntry(myCAPublicKey.getSubjectX500Principal().getName(), myCAPublicKey);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustedStore);
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            if (url.contains("https:")) {
                client = new Builder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .build();
            } else client = new OkHttpClient();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
    OkHttpClient getClient() {
        return client;
    }
}
