package ru.mfa.binary;

import java.util.UUID;

public class ManifestEntry {

    private UUID id;
    private int statusCode;
    private long updatedAtEpochMillis;
    private long dataOffset;
    private long dataLength;
    private byte[] recordSignatureBytes;

    public ManifestEntry() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public long getUpdatedAtEpochMillis() { return updatedAtEpochMillis; }
    public void setUpdatedAtEpochMillis(long updatedAtEpochMillis) { this.updatedAtEpochMillis = updatedAtEpochMillis; }

    public long getDataOffset() { return dataOffset; }
    public void setDataOffset(long dataOffset) { this.dataOffset = dataOffset; }

    public long getDataLength() { return dataLength; }
    public void setDataLength(long dataLength) { this.dataLength = dataLength; }

    public byte[] getRecordSignatureBytes() { return recordSignatureBytes; }
    public void setRecordSignatureBytes(byte[] recordSignatureBytes) { this.recordSignatureBytes = recordSignatureBytes; }
}
