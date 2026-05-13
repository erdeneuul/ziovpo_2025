package ru.mfa.binary;

import org.springframework.stereotype.Component;
import ru.mfa.model.MalwareSignature;
import ru.mfa.model.SignatureStatus;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataBinaryBuilder {

    private static final byte[] MAGIC = "DB-Erdeneuul".getBytes(StandardCharsets.UTF_8);

    public DataBuildResult build(List<MalwareSignature> signatures) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<ManifestEntry> entries = new ArrayList<>();

        baos.writeBytes(MAGIC);
        baos.writeBytes(BinaryUtils.writeU16BE(1));
        baos.writeBytes(BinaryUtils.writeU32BE(signatures.size()));

        for (MalwareSignature sig : signatures) {
            long offset = baos.size();

            baos.writeBytes(BinaryUtils.writeString(sig.getThreatName()));
            baos.writeBytes(BinaryUtils.writeBytes(BinaryUtils.hexToBytes(sig.getFirstBytesHex())));
            baos.writeBytes(BinaryUtils.writeBytes(BinaryUtils.hexToBytes(sig.getRemainderHashHex())));
            baos.writeBytes(BinaryUtils.writeI64BE(sig.getRemainderLength()));
            baos.writeBytes(BinaryUtils.writeString(sig.getFileType()));
            baos.writeBytes(BinaryUtils.writeI64BE(sig.getOffsetStart()));
            baos.writeBytes(BinaryUtils.writeI64BE(sig.getOffsetEnd()));

            long length = baos.size() - offset;

            ManifestEntry entry = new ManifestEntry();
            entry.setId(sig.getId());
            entry.setStatusCode(sig.getStatus() == SignatureStatus.ACTUAL ? 1 : 2);
            entry.setUpdatedAtEpochMillis(sig.getUpdatedAt().toEpochMilli());
            entry.setDataOffset(offset);
            entry.setDataLength(length);
            entry.setRecordSignatureBytes(new byte[0]);
            entries.add(entry);
        }

        return new DataBuildResult(baos.toByteArray(), entries);
    }
}
