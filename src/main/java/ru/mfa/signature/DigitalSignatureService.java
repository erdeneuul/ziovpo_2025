package ru.mfa.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.security.Signature;
import java.util.Base64;

@Service
public class DigitalSignatureService {

    private static final String ALGORITHM = "SHA256withRSA";

    private final SignatureKeyStoreService keyStoreService;
    private final ObjectMapper objectMapper;

    public DigitalSignatureService(SignatureKeyStoreService keyStoreService, ObjectMapper objectMapper) {
        this.keyStoreService = keyStoreService;
        this.objectMapper = objectMapper;
    }

    public String sign(Object payload) throws Exception {
        byte[] canonical = canonicalize(payload);

        Signature sig = Signature.getInstance(ALGORITHM);
        sig.initSign(keyStoreService.getPrivateKey());
        sig.update(canonical);

        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public byte[] signBytes(byte[] data) throws Exception {
        Signature sig = Signature.getInstance(ALGORITHM);
        sig.initSign(keyStoreService.getPrivateKey());
        sig.update(data);
        return sig.sign();
    }

    public boolean verify(Object payload, String signatureBase64) throws Exception {
        byte[] canonical = canonicalize(payload);

        Signature sig = Signature.getInstance(ALGORITHM);
        sig.initVerify(keyStoreService.getPublicKey());
        sig.update(canonical);

        return sig.verify(Base64.getDecoder().decode(signatureBase64));
    }

    private byte[] canonicalize(Object payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        return new JsonCanonicalizer(json).getEncodedUTF8();
    }
}
