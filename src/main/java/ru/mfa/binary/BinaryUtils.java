package ru.mfa.binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BinaryUtils {

    private BinaryUtils() {}

    public static byte[] writeU8(int value) {
        return new byte[]{(byte) (value & 0xFF)};
    }

    public static byte[] writeU16BE(int value) {
        ByteBuffer buf = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
        buf.putShort((short) (value & 0xFFFF));
        return buf.array();
    }

    public static byte[] writeU32BE(long value) {
        ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buf.putInt((int) (value & 0xFFFFFFFFL));
        return buf.array();
    }

    public static byte[] writeU64BE(long value) {
        ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buf.putLong(value);
        return buf.array();
    }

    public static byte[] writeI64BE(long value) {
        ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buf.putLong(value);
        return buf.array();
    }

    public static byte[] writeString(String s) {
        byte[] strBytes = s.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4 + strBytes.length).order(ByteOrder.BIG_ENDIAN);
        buf.putInt(strBytes.length);
        buf.put(strBytes);
        return buf.array();
    }

    public static byte[] writeBytes(byte[] data) {
        ByteBuffer buf = ByteBuffer.allocate(4 + data.length).order(ByteOrder.BIG_ENDIAN);
        buf.putInt(data.length);
        buf.put(data);
        return buf.array();
    }

    public static byte[] writeUUID(UUID uuid) {
        ByteBuffer buf = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        return buf.array();
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
