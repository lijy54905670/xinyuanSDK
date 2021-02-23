package com.xinyuan.ms.Utils;

public final class HexUtils {
    private static final int[] DEC = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15};
    private static final byte[] HEX = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    private static final char[] hex = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};

    public HexUtils() {
    }

    /**
     * 字符获取十进制
     * @param index
     * @return
     */
    public static int getDec(int index) {
        try {
            return DEC[index - 48];
        } catch (ArrayIndexOutOfBoundsException var2) {
            return -1;
        }
    }

    public static byte getHex(int index) {
        return HEX[index];
    }

    public static String toHexString(byte[] bytes) {
        if(null == bytes) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder(bytes.length << 1);

            for(int i = 0; i < bytes.length; ++i) {
                sb.append(hex[(bytes[i] & 240) >> 4]).append(hex[bytes[i] & 15]);
            }

            return sb.toString();
        }
    }

    public static byte[] fromHexString(String input) {
        if(input == null) {
            return null;
        } else if((input.length() & 1) == 1) {
            // 肯定是偶数位，奇数位转换失败：一个byte对应两个字符
            throw new IllegalArgumentException("hexUtils.fromHex.oddDigits");
        } else {
            char[] inputChars = input.toCharArray();
            byte[] result = new byte[input.length() >> 1];

            for(int i = 0; i < result.length; ++i) {
                int upperNibble = getDec(inputChars[2 * i]);
                int lowerNibble = getDec(inputChars[2 * i + 1]);
                if(upperNibble < 0 || lowerNibble < 0) {
                    // 字符不存在
                    throw new IllegalArgumentException("hexUtils.fromHex.nonHex");
                }

                result[i] = (byte)((upperNibble << 4) + lowerNibble);
            }

            return result;
        }
    }


    public static byte[] toBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytes2Hex(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}