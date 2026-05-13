package ru.mfa.binary;

import org.springframework.stereotype.Component;
import ru.mfa.signature.DigitalSignatureService;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

@Component
public class ManifestBinaryBuilder {

    private static final byte[] MAGIC = "MF-Erdeneuul".getBytes(StandardCharsets.UTF_8);

    private final DigitalSignatureService digitalSignatureService;

    public ManifestBinaryBuilder(DigitalSignatureService digitalSignatureService) {
        this.digitalSignatureService = digitalSignatureService;
    }

    public byte[] build(List<ManifestEntry> entries, byte[] dataBytes,
                        int exportType, long sinceMillis) throws Exception {
        byte[] dataSha256 = MessageDigest.getInstance("SHA-256").digest(dataBytes);

        for (ManifestEntry entry : entries) {
            byte[] recordBytes = Arrays.copyOfRange(
                    dataBytes,
                    (int) entry.getDataOffset(),
                    (int) (entry.getDataOffset() + entry.getDataLength()));
            entry.setRecordSignatureBytes(digitalSignatureService.signBytes(recordBytes));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.writeBytes(MAGIC);
        baos.writeBytes(BinaryUtils.writeU16BE(1));
        baos.writeBytes(BinaryUtils.writeU8(exportType));
        baos.writeBytes(BinaryUtils.writeI64BE(System.currentTimeMillis()));
        baos.writeBytes(BinaryUtils.writeI64BE(sinceMillis));
        baos.writeBytes(BinaryUtils.writeU32BE(entries.size()));
        baos.writeBytes(dataSha256);

        for (ManifestEntry entry : entries) {
            baos.writeBytes(BinaryUtils.writeUUID(entry.getId()));
            baos.writeBytes(BinaryUtils.writeU8(entry.getStatusCode()));
            baos.writeBytes(BinaryUtils.writeI64BE(entry.getUpdatedAtEpochMillis()));
            baos.writeBytes(BinaryUtils.writeI64BE(entry.getDataOffset()));
            baos.writeBytes(BinaryUtils.writeI64BE(entry.getDataLength()));
            baos.writeBytes(BinaryUtils.writeU32BE(entry.getRecordSignatureBytes().length));
            baos.writeBytes(entry.getRecordSignatureBytes());
        }

        byte[] manifestSoFar = baos.toByteArray();
        byte[] sigBytes = digitalSignatureService.signBytes(manifestSoFar);

        baos.writeBytes(BinaryUtils.writeU32BE(sigBytes.length));
        baos.writeBytes(sigBytes);

        return baos.toByteArray();
    }
}
