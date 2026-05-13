package ru.mfa.binary;

import java.util.List;

public class DataBuildResult {

    private final byte[] dataBytes;
    private final List<ManifestEntry> entries;

    public DataBuildResult(byte[] dataBytes, List<ManifestEntry> entries) {
        this.dataBytes = dataBytes;
        this.entries = entries;
    }

    public byte[] getDataBytes() { return dataBytes; }
    public List<ManifestEntry> getEntries() { return entries; }
}
