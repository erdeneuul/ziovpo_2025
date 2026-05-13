package ru.mfa.signature;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Service
public class SignatureKeyStoreService {

    private final SignatureProperties props;

    private volatile PrivateKey cachedPrivateKey;

    public SignatureKeyStoreService(SignatureProperties props) {
        this.props = props;
    }

    public PrivateKey getPrivateKey() {
        if (cachedPrivateKey == null) {
            synchronized (this) {
                if (cachedPrivateKey == null) {
                    cachedPrivateKey = loadPrivateKey();
                }
            }
        }
        return cachedPrivateKey;
    }

    public PublicKey getPublicKey() {
        KeyStore ks = loadKeyStore();
        try {
            Certificate cert = ks.getCertificate(props.getKeyAlias());
            if (cert == null) {
                throw new IllegalStateException(
                        "No certificate found for alias '" + props.getKeyAlias() + "'");
            }
            return cert.getPublicKey();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key: " + e.getMessage(), e);
        }
    }

    private PrivateKey loadPrivateKey() {
        KeyStore ks = loadKeyStore();
        try {
            String keyPass = (props.getKeyPassword() != null && !props.getKeyPassword().isEmpty())
                    ? props.getKeyPassword()
                    : props.getKeyStorePassword();

            KeyStore.ProtectionParameter protection =
                    new KeyStore.PasswordProtection(keyPass.toCharArray());
            KeyStore.PrivateKeyEntry entry =
                    (KeyStore.PrivateKeyEntry) ks.getEntry(props.getKeyAlias(), protection);

            if (entry == null) {
                throw new IllegalStateException(
                        "No private key found for alias '" + props.getKeyAlias() + "'");
            }
            return entry.getPrivateKey();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key: " + e.getMessage(), e);
        }
    }

    private KeyStore loadKeyStore() {
        String path = props.getKeyStorePath();
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Keystore not found on classpath: " + path);
            }
            KeyStore ks = KeyStore.getInstance(props.getKeyStoreType());
            ks.load(is, props.getKeyStorePassword().toCharArray());
            return ks;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load keystore: " + e.getMessage(), e);
        }
    }
}
