package cn.tdchain.cipher.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * String Utility.
 *
 * @version 1.0
 * @author Homer.J 2018-06-26
 */
public final class StringUtils {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private StringUtils() {
    }

    /**
     * New string with UTF_8 charset.
     * 
     * @param bytes byte[]
     * @return string
     */
    public static String newStr(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, UTF8);
    }

    /**
     * Get bytes with UTF_8 charset.
     * 
     * @param value String
     * @return bytes
     */
    public static byte[] getBytes(String value) {
        if (value == null) {
            return null;
        }
        return value.getBytes(UTF8);
    }

    /**
     * Compare two nullable string.
     * 
     * @param s1 String
     * @param s2 String
     * @return boolean
     */
    public static boolean equals(String s1, String s2) {
        if (isBlank(s1) || isBlank(s2)) {
            return false;
        } else {
            return s1.equals(s2);
        }
    }

    /**
     * <p>
     * Checks if a String is whitespace, empty ("") or null.
     * </p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param value the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     */
    public static boolean isBlank(String value) {
        return org.apache.commons.lang3.StringUtils.isBlank(value);
    }

    /**
     * <p>
     * Checks if a String is not empty (""), not null and not whitespace only.
     * </p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param value the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null and not whitespace
     */
    public static boolean isNotBlank(String value) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(value);
    }

    /**
     * Gets a substring from the specified String avoiding exceptions.
     * 
     * @param value the String to get the substring from, may be null
     * @param start the position to start from, negative means count back from the end of the String
     *            by this many characters
     * @param end the position to end at (exclusive), negative means count back from the end of the
     *            String by this many characters
     * @return substring from start position to end position, <code>null</code> if null String input
     */
    public static String subString(String value, int start, int end) {
        return org.apache.commons.lang3.StringUtils.substring(value, start,
                end);
    }

    /**
     * Get formatted string with zero suffix.
     * 
     * @param orig original value
     * @param length formatted length
     * @return formatted string
     */
    public static String zeroSuffix(String orig, int length) {
        String zeros = org.apache.commons.lang3.StringUtils.repeat("0", length);
        if (isBlank(orig)) {
            return zeros;
        }
        String expendStr = orig + zeros;
        return subString(expendStr, 0, length);
    }

    /**
     * Clear empty char from String.
     * 
     * <pre>
     * StringUtils.clearEmptyChar(" tes t") = "test"
     * </pre>
     * 
     * @param text original text
     * @return text without blank
     */
    public static String clearEmptyChar(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            String ch = String.valueOf(c);
            if (!" ".equals(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Repeat zero.
     * 
     * @param n number of zeros
     * @return zeros
     */
    public static String zeros(int n) {
        return repeat('0', n);
    }

    /**
     * Repeat.
     * 
     * @param value repeat value
     * @param n repeat times
     * @return repeat value
     */
    public static String repeat(char value, int n) {
        return new String(new char[n]).replace("\0", String.valueOf(value));
    }

}
