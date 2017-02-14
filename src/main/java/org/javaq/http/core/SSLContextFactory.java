package org.javaq.http.core;


import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.InitializingBean;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static com.google.common.base.Strings.isNullOrEmpty;


public class SSLContextFactory implements InitializingBean {

    private String trustedCertsPath;
    private String trustedStorePassword;
    private String trustedHosts;
    private String privateKeyPath;
    private String privateKeyPassword;
    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;

    @Override
    public void afterPropertiesSet() throws Exception {
        createSSLContext();
        createHostnameVerifier();
    }

    private void createSSLContext() throws Exception {
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(createKeyManager(), createTrustManager(), new SecureRandom());
    }

    private KeyManager[] createKeyManager() throws Exception {
        if (isNullOrEmpty(privateKeyPath)) {
            return null;
        }
        checkPropertyValue("privateKeyPassword", privateKeyPassword);
        return doCreateKeyManager();
    }

    private KeyManager[] doCreateKeyManager() throws Exception {
        File privateKeyFile = new File(privateKeyPath);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(privateKeyFile), privateKeyPassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, privateKeyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    private TrustManager[] createTrustManager() throws Exception {
        if (isNullOrEmpty(trustedCertsPath)) {
            return null;
        }
        checkPropertyValue("trustedStorePassword", trustedStorePassword);
        return doCreateTrustManager();
    }

    private void checkPropertyValue(String propertyName, String propertyValue) {
        if (isNullOrEmpty(propertyValue)) {
            throw new RuntimeException(propertyName + " is empty , should be set value");
        }
    }

    private TrustManager[] doCreateTrustManager() throws Exception {
        KeyStore trustKeyStore = createTrustKeyStore(trustedCertsPath);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);
        return trustManagerFactory.getTrustManagers();
    }

    private KeyStore createTrustKeyStore(String filePath) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore securityStore = KeyStore.getInstance(KeyStore.getDefaultType());
        securityStore.load(new FileInputStream(getDefaultTrustedFile()), trustedStorePassword.toCharArray());
        Path path = Paths.get(filePath);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        FileInputStream fileInputStream = null;
                        try {
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            File certificateFile = file.toFile();
                            fileInputStream = new FileInputStream(certificateFile);
                            while (fileInputStream.available() > 0) {
                                Certificate cert = cf.generateCertificate(fileInputStream);
                                securityStore.setCertificateEntry(file.toString(), cert);
                            }
                            return FileVisitResult.CONTINUE;
                        } catch (CertificateException | KeyStoreException e) {
                            throw new RuntimeException("add certificate to key store exception");
                        } finally {
                            IOUtils.closeQuietly(fileInputStream);
                        }
                    }
                }
        );
        return securityStore;
    }

    private File getDefaultTrustedFile() {
        File file = new File("jssecacerts");
        if (!file.isFile()) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP
                    + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (!file.isFile()) {
                file = new File(dir, "cacerts");
            }
        }
        return file;
    }

    private void createHostnameVerifier() {
        if (isNullOrEmpty(trustedHosts)) {
            hostnameVerifier = new NoopHostnameVerifier();
            return;
        }
        doCreateHostnameVerifier();
    }

    private void doCreateHostnameVerifier() {
        hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return trustedHosts.contains(s);
            }
        };
    }

    public String getTrustedCertsPath() {
        return trustedCertsPath;
    }

    public void setTrustedCertsPath(String trustedCertsPath) {
        this.trustedCertsPath = trustedCertsPath;
    }

    public String getTrustedStorePassword() {
        return trustedStorePassword;
    }

    public void setTrustedStorePassword(String trustedStorePassword) {
        this.trustedStorePassword = trustedStorePassword;
    }

    public String getTrustedHosts() {
        return trustedHosts;
    }

    public void setTrustedHosts(String trustedHosts) {
        this.trustedHosts = trustedHosts;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }
}
