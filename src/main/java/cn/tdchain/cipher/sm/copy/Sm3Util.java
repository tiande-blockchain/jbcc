///*
// * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
// * All Rights Reserved.
// */
//package cn.tdchain.cipher.sm.copy;
//
//import cn.tdchain.cipher.utils.ByteUtils;
//
///**
// * SM3 Utility.
// *
// * @version 2.0
// * @author Xiaoming 2017-05-03
// */
//public class Sm3Util {
//
//    public static final byte[] IV = { 0x73, (byte) 0x80, 0x16, 0x6f, 0x49, 0x14,
//        (byte) 0xb2, (byte) 0xb9, 0x17, 0x24, 0x42, (byte) 0xd7, (byte) 0xda,
//        (byte) 0x8a, 0x06, 0x00, (byte) 0xa9, 0x6f, 0x30, (byte) 0xbc,
//        (byte) 0x16, 0x31, 0x38, (byte) 0xaa, (byte) 0xe3, (byte) 0x8d,
//        (byte) 0xee, 0x4d, (byte) 0xb0, (byte) 0xfb, 0x0e, 0x4e };
//
//    private static int[] tj = new int[64];
//
//    private Sm3Util() {
//    }
//
//    /**
//     * Padding.
//     * 
//     * @param in byte[]
//     * @param bLen int
//     * @return byte[]
//     */
//    public static byte[] padding(byte[] in, int bLen) {
//        int k = 448 - (8 * in.length + 1) % 512;
//        if (k < 0) {
//            k = 960 - (8 * in.length + 1) % 512;
//        }
//        k += 1;
//        byte[] padd = new byte[k / 8];
//        padd[0] = (byte) 0x80;
//        long n = in.length * 8 + bLen * 512;
//        byte[] out = new byte[in.length + k / 8 + 64 / 8];
//        int pos = 0;
//        System.arraycopy(in, 0, out, 0, in.length);
//        pos += in.length;
//        System.arraycopy(padd, 0, out, pos, padd.length);
//        pos += padd.length;
//        byte[] tmp = back(ByteUtils.longToBytes(n));
//        System.arraycopy(tmp, 0, out, pos, tmp.length);
//        return out;
//    }
//
//    /**
//     * CF convert.
//     * 
//     * @param origV byte[]
//     * @param origB byte[]
//     * @return byte[]
//     */
//    public static byte[] cf(byte[] origV, byte[] origB) {
//        int[] v = convert(origV);
//        int[] b = convert(origB);
//        return convert(cf(v, b));
//    }
//
//    private static int p0(int x) {
//        int y = rotateLeft(x, 9);
//        y = bitCycleLeft(x, 9);
//        int z = rotateLeft(x, 17);
//        z = bitCycleLeft(x, 17);
//        int t = x ^ y ^ z;
//        return t;
//    }
//
//    private static int p1(int x) {
//        int t = x ^ bitCycleLeft(x, 15) ^ bitCycleLeft(x, 23);
//        return t;
//    }
//
//    private static int[] cf(int[] arrayV, int[] arrayB) {
//        int a = arrayV[0];
//        int b = arrayV[1];
//        int c = arrayV[2];
//        int d = arrayV[3];
//        int e = arrayV[4];
//        int f = arrayV[5];
//        int g = arrayV[6];
//        int h = arrayV[7];
//
//        int[][] arr = expand(arrayB);
//        int[] w = arr[0];
//        int[] w1 = arr[1];
//
//        for (int j = 0; j < 64; j++) {
//            int ss1 = (bitCycleLeft(a, 12) + e + bitCycleLeft(tj[j], j));
//            ss1 = bitCycleLeft(ss1, 7);
//            int ss2 = ss1 ^ bitCycleLeft(a, 12);
//            int tt1 = ffj(a, b, c, j) + d + ss2 + w1[j];
//            int tt2 = ggj(e, f, g, j) + h + ss1 + w[j];
//            d = c;
//            c = bitCycleLeft(b, 9);
//            b = a;
//            a = tt1;
//            h = g;
//            g = bitCycleLeft(f, 19);
//            f = e;
//            e = p0(tt2);
//
//        }
//
//        int[] out = new int[8];
//        out[0] = a ^ arrayV[0];
//        out[1] = b ^ arrayV[1];
//        out[2] = c ^ arrayV[2];
//        out[3] = d ^ arrayV[3];
//        out[4] = e ^ arrayV[4];
//        out[5] = f ^ arrayV[5];
//        out[6] = g ^ arrayV[6];
//        out[7] = h ^ arrayV[7];
//
//        return out;
//    }
//
//    private static int[][] expand(int[] values) {
//        int[] w = new int[68];
//        int[] w1 = new int[64];
//        for (int i = 0; i < values.length; i++) {
//            w[i] = values[i];
//        }
//
//        for (int i = 16; i < 68; i++) {
//            w[i] = p1(w[i - 16] ^ w[i - 9] ^ bitCycleLeft(w[i - 3], 15))
//                    ^ bitCycleLeft(w[i - 13], 7) ^ w[i - 6];
//        }
//
//        for (int i = 0; i < 64; i++) {
//            w1[i] = w[i] ^ w[i + 4];
//        }
//
//        int[][] arr = new int[][] { w, w1 };
//        return arr;
//    }
//
//    private static int rotateLeft(int x, int n) {
//        return (x << n) | (x >> (32 - n));
//    }
//
//    private static int bitCycleLeft(int n, int bitLen) {
//        bitLen %= 32;
//        byte[] tmp = bigEndianIntToByte(n);
//        int byteLen = bitLen / 8;
//        int len = bitLen % 8;
//        if (byteLen > 0) {
//            tmp = byteCycleLeft(tmp, byteLen);
//        }
//
//        if (len > 0) {
//            tmp = bitSmall8CycleLeft(tmp, len);
//        }
//
//        return bigEndianByteToInt(tmp);
//    }
//
//    private static byte[] bitSmall8CycleLeft(byte[] in, int len) {
//        byte[] tmp = new byte[in.length];
//        for (int i = 0; i < tmp.length; i++) {
//            int t1 = (byte) ((in[i] & 0x000000ff) << len);
//            int t2 = (byte) ((in[(i + 1) % tmp.length] & 0x000000ff) >> (8
//                    - len));
//            int t3 = (byte) (t1 | t2);
//            tmp[i] = (byte) t3;
//        }
//        return tmp;
//    }
//
//    private static byte[] byteCycleLeft(byte[] in, int byteLen) {
//        byte[] tmp = new byte[in.length];
//        System.arraycopy(in, byteLen, tmp, 0, in.length - byteLen);
//        System.arraycopy(in, 0, tmp, in.length - byteLen, byteLen);
//        return tmp;
//    }
//
//    private static int[] convert(byte[] arr) {
//        int[] out = new int[arr.length / 4];
//        byte[] tmp = new byte[4];
//        for (int i = 0; i < arr.length; i += 4) {
//            System.arraycopy(arr, i, tmp, 0, 4);
//            out[i / 4] = bigEndianByteToInt(tmp);
//        }
//        return out;
//    }
//
//    private static byte[] convert(int[] arr) {
//        byte[] out = new byte[arr.length * 4];
//        byte[] tmp = null;
//        for (int i = 0; i < arr.length; i++) {
//            tmp = bigEndianIntToByte(arr[i]);
//            System.arraycopy(tmp, 0, out, i * 4, 4);
//        }
//        return out;
//    }
//
//    private static int bigEndianByteToInt(byte[] bytes) {
//        return ByteUtils.byteToInt(back(bytes));
//    }
//
//    private static byte[] bigEndianIntToByte(int num) {
//        return back(ByteUtils.intToBytes(num));
//    }
//
//    private static byte[] back(byte[] in) {
//        byte[] out = new byte[in.length];
//        for (int i = 0; i < out.length; i++) {
//            out[i] = in[out.length - i - 1];
//        }
//        return out;
//    }
//
//    private static int ffj(int intX, int intY, int intZ, int intJ) {
//        if (intJ >= 0 && intJ <= 15) {
//            return ff1j(intX, intY, intZ);
//        } else {
//            return ff2j(intX, intY, intZ);
//        }
//    }
//
//    private static int ggj(int intX, int intY, int intZ, int intJ) {
//        if (intJ >= 0 && intJ <= 15) {
//            return gg1j(intX, intY, intZ);
//        } else {
//            return gg2j(intX, intY, intZ);
//        }
//    }
//
//    private static int ff1j(int intX, int intY, int intZ) {
//        return intX ^ intY ^ intZ;
//    }
//
//    private static int ff2j(int intX, int intY, int intZ) {
//        return ((intX & intY) | (intX & intZ) | (intY & intZ));
//    }
//
//    private static int gg1j(int intX, int intY, int intZ) {
//        return intX ^ intY ^ intZ;
//    }
//
//    private static int gg2j(int intX, int intY, int intZ) {
//        return (intX & intY) | (~intX & intZ);
//    }
//
//}
