package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ByteArrayTest {

    @Test
    public void toByteArrayFromCharArrayTest() throws Exception {

        char[] chars = "0123456789".toCharArray();

        ByteArray byteArray = new ByteArray.CharSeqByteArray(chars, Charset.forName("UTF-8"));
        byte[] bytes = byteArray.getBytes();

        assertTrue(Arrays.equals(new String(chars).getBytes("UTF-8"), bytes));
    }

    @Test
    public void toByteArrayFromCharArrayNonAsciiTest() throws Exception {

        char[] chars = "Φούμπαρ".toCharArray();

        ByteArray byteArray = new ByteArray.CharSeqByteArray(chars, Charset.forName("UTF-8"));
        byte[] bytes = byteArray.getBytes();

        assertTrue(Arrays.equals(new String(chars).getBytes("UTF-8"), bytes));
    }

    @Test
    public void toByteArrayFromCharArrayNonAsciiNonDefaultEncodingTest() throws Exception {

        char[] chars = "Φούμπαρ".toCharArray();

        ByteArray byteArray1 = new ByteArray.CharSeqByteArray(chars, Charset.forName("UTF-8"));
        byte[] bytes1 = byteArray1.getBytes();

        ByteArray byteArray2 = new ByteArray.CharSeqByteArray(chars, Charset.forName("UTF-8")).encoding("ISO8859_7");
        byte[] bytes2 = byteArray2.getBytes();

        assertFalse(Arrays.equals(bytes1, bytes2));
        assertTrue(Arrays.equals(new String(chars).getBytes("ISO8859_7"), bytes2));
    }

    @Test
    public void toByteArrayFromStringTest() throws Exception {

        String str = "0123456789";

        ByteArray byteArray = new ByteArray.CharSeqByteArray(str, Charset.forName("UTF-8"));
        byte[] bytes = byteArray.getBytes();

        assertTrue(Arrays.equals(str.getBytes("UTF-8"), bytes));
    }

    @Test
    public void toByteArrayFromStringNonAsciiTest() throws Exception {

        String str = "Φούμπαρ";

        ByteArray byteArray = new ByteArray.CharSeqByteArray(str, Charset.forName("UTF-8"));
        byte[] bytes = byteArray.getBytes();

        assertTrue(Arrays.equals(str.getBytes("UTF-8"), bytes));
    }

    @Test
    public void toByteArrayFromStringNonAsciiNonDefaultEncodingTest() throws Exception {

        String str = "Φούμπαρ";

        ByteArray byteArray1 = new ByteArray.CharSeqByteArray(str, Charset.forName("UTF-8"));
        byte[] bytes1 = byteArray1.getBytes();

        ByteArray byteArray2 = new ByteArray.CharSeqByteArray(str, Charset.forName("UTF-8")).encoding("ISO8859_7");
        byte[] bytes2 = byteArray2.getBytes();

        assertFalse(Arrays.equals(bytes1, bytes2));
        assertTrue(Arrays.equals(str.getBytes("ISO8859_7"), bytes2));
    }

    @Test
    public void toByteArrayFromInputStreamTest() throws Exception {
        int bufferSize = 64;

        {
            // zero bytes
            byte[] bytes = new byte[0];

            assertTrue(bytes.length == 0);

            ByteArray byteArray = new ByteArray(new ByteArrayInputStream(bytes), bufferSize);
            byte[] bytes2 = byteArray.getBytes();

            assertTrue(bytes2.length == 0);
        }

        {
            // less than 64 bytes
            byte[] bytes = "0123456789".getBytes();

            assertTrue(bytes.length < 64);

            ByteArray byteArray = new ByteArray(new ByteArrayInputStream(bytes), bufferSize);
            byte[] bytes2 = byteArray.getBytes();

            assertTrue(Arrays.equals(bytes, bytes2));
        }

        {
            // exactly 64 bytes
            byte[] bytes = "0123456789 0123456789 0123456789 0123456789 0123456789 012345678".getBytes();

            assertTrue(bytes.length == 64);

            ByteArray byteArray = new ByteArray(new ByteArrayInputStream(bytes), bufferSize);
            byte[] bytes2 = byteArray.getBytes();

            assertTrue(Arrays.equals(bytes, bytes2));
        }

        {
            // less than 128 bytes
            byte[] bytes = "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 01234".getBytes();

            assertTrue(bytes.length > 64 && bytes.length < 128);

            ByteArray byteArray = new ByteArray(new ByteArrayInputStream(bytes), bufferSize);
            byte[] bytes2 = byteArray.getBytes();

            assertTrue(Arrays.equals(bytes, bytes2));
        }

        {
            // exactly 128 bytes
            byte[] bytes = "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456".getBytes();

            assertTrue(bytes.length == 128);

            ByteArray byteArray = new ByteArray(new ByteArrayInputStream(bytes), bufferSize);
            byte[] bytes2 = byteArray.getBytes();

            assertTrue(Arrays.equals(bytes, bytes2));
        }

        {
            // more than 128 bytes
            byte[] bytes = "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789".getBytes();

            assertTrue(bytes.length > 128);

            ByteArray byteArray = new ByteArray(new ByteArrayInputStream(bytes), bufferSize);
            byte[] bytes2 = byteArray.getBytes();

            assertTrue(Arrays.equals(bytes, bytes2));
        }
    }

    @Test
    public void toByteArrayFromReaderTest() throws Exception {
        char[] chars = "value".toCharArray();

        ByteArray byteArray = new ByteArray.CharSeqByteArray(new CharArrayReader(chars), 64, Charset.forName("UTF-8"));
        byte[] bytes = byteArray.getBytes();

        assertTrue(Arrays.equals(new String(chars).getBytes("UTF-8"), bytes));
    }

    @Test
    public void toByteArrayFromReaderNonAsciiTest() throws Exception {
        char[] chars = "Φούμπαρ".toCharArray();

        ByteArray byteArray = new ByteArray.CharSeqByteArray(new CharArrayReader(chars), 64, Charset.forName("UTF-8"));
        byte[] bytes = byteArray.getBytes();

        assertTrue(Arrays.equals(new String(chars).getBytes("UTF-8"), bytes));
    }

    @Test
    public void toByteArrayFromReaderNonAsciiNonDefaultEncodingTest() throws Exception {
        char[] chars = "Φούμπαρ".toCharArray();

        ByteArray byteArray1 = new ByteArray.CharSeqByteArray(new CharArrayReader(chars), 64, Charset.forName("UTF-8"));
        byte[] bytes1 = byteArray1.getBytes();

        ByteArray byteArray2 = new ByteArray.CharSeqByteArray(new CharArrayReader(chars), 64, Charset.forName("UTF-8")).encoding("ISO8859_7");
        byte[] bytes2 = byteArray2.getBytes();

        assertFalse(Arrays.equals(bytes1, bytes2));
        assertTrue(Arrays.equals(new String(chars).getBytes("ISO8859_7"), bytes2));
    }

    @Test
    public void toByteArrayFromCharArrayNormalizedTest() throws Exception {
        char[] chars1 = "\u00C1".toCharArray();
        char[] chars2 = "\u0041\u0301".toCharArray();

        {
            ByteArray byteArray1 = new ByteArray.CharSeqByteArray(chars1, Charset.forName("UTF-8"));
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = new ByteArray.CharSeqByteArray(chars2, Charset.forName("UTF-8"));
            byte[] bytes2 = byteArray2.getBytes();

            assertFalse(Arrays.equals(bytes1, bytes2));
        }

        {
            ByteArray byteArray1 = new ByteArray.CharSeqByteArray(chars1, Charset.forName("UTF-8")).normalize();
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = new ByteArray.CharSeqByteArray(chars2, Charset.forName("UTF-8")).normalize();
            byte[] bytes2 = byteArray2.getBytes();

            assertTrue(Arrays.equals(bytes1, bytes2));
        }
    }

    @Test
    public void toByteArrayFromStringNormalizedTest() throws Exception {
        String str1 = "\u00C1";
        String str2 = "\u0041\u0301";

        {
            ByteArray byteArray1 = new ByteArray.CharSeqByteArray(str1, Charset.forName("UTF-8"));
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = new ByteArray.CharSeqByteArray(str2, Charset.forName("UTF-8"));
            byte[] bytes2 = byteArray2.getBytes();

            assertFalse(Arrays.equals(bytes1, bytes2));
        }

        {
            ByteArray byteArray1 = new ByteArray.CharSeqByteArray(str1, Charset.forName("UTF-8")).normalize();
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = new ByteArray.CharSeqByteArray(str2, Charset.forName("UTF-8")).normalize();
            byte[] bytes2 = byteArray2.getBytes();

            assertTrue(Arrays.equals(bytes1, bytes2));
        }
    }

    @Test
    public void toByteArrayFromReaderNormalizedTest() throws Exception {
        char[] chars1 = "\u00C1".toCharArray();
        char[] chars2 = "\u0041\u0301".toCharArray();

        {
            ByteArray byteArray1 = new ByteArray.CharSeqByteArray(new CharArrayReader(chars1), 64, Charset.forName("UTF-8"));
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = new ByteArray.CharSeqByteArray(new CharArrayReader(chars2), 64, Charset.forName("UTF-8"));
            byte[] bytes2 = byteArray2.getBytes();

            assertFalse(Arrays.equals(bytes1, bytes2));
        }

        {
            ByteArray byteArray1 = new ByteArray.CharSeqByteArray(new CharArrayReader(chars1), 64, Charset.forName("UTF-8")).normalize();
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = new ByteArray.CharSeqByteArray(new CharArrayReader(chars2), 64, Charset.forName("UTF-8")).normalize();
            byte[] bytes2 = byteArray2.getBytes();

            assertTrue(Arrays.equals(bytes1, bytes2));
        }
    }

    @Test
    public void byteArrayClearSourceTest() throws Exception {
        byte[] referenceBytes = "12345".getBytes();
        {
            byte[] source = Arrays.copyOf(referenceBytes, referenceBytes.length);
            byte[] copy;

            try (ByteArray byteArray = new ByteArray.ClearableSourceByteArray(source)) {
                copy = byteArray.getBytes();

                assertTrue(Arrays.equals(source, copy));
            }

            byte[] zeros = new byte[copy.length];
            assertTrue(Arrays.equals(zeros, copy));
            assertTrue(Arrays.equals(referenceBytes, source));
        }

        {
            byte[] source = Arrays.copyOf(referenceBytes, referenceBytes.length);
            byte[] copy;

            try (ByteArray byteArray = new ByteArray.ClearableSourceByteArray(source).clearSource()) {
                copy = byteArray.getBytes();

                assertTrue(Arrays.equals(source, copy));
            }

            byte[] zeros = new byte[copy.length];
            assertTrue(Arrays.equals(zeros, copy));
            assertTrue(Arrays.equals(zeros, source));
        }

        String referenceString = "Φούμπαρ";

        {
            char[] source = referenceString.toCharArray();
            byte[] copy;

            try(ByteArray byteArray = new ByteArray.ClearableSourceCharSeqByteArray(source, Jargon2.DEFAULT_ENCODING).encoding("ISO8859_7")) {
                copy = byteArray.getBytes();

                assertTrue(Arrays.equals(new String(source).getBytes("ISO8859_7"), copy));
            }

            byte[] zeros = new byte[copy.length];
            assertTrue(Arrays.equals(zeros, copy));
            assertTrue(Arrays.equals(referenceString.toCharArray(), source));
        }

        {
            char[] source = referenceString.toCharArray();
            byte[] copy;

            try(ByteArray byteArray = new ByteArray.ClearableSourceCharSeqByteArray(source, Jargon2.DEFAULT_ENCODING).encoding("ISO8859_7").clearSource()) {
                copy = byteArray.getBytes();

                assertTrue(Arrays.equals(new String(source).getBytes("ISO8859_7"), copy));
            }

            byte[] zeroBytes = new byte[copy.length];
            assertTrue(Arrays.equals(zeroBytes, copy));
            char[] zeroChars = new char[copy.length];
            assertTrue(Arrays.equals(zeroChars, source));
        }
    }

    @Test
    public void finalizableByteArrayTest() throws Exception {

        {
            byte[] bytes = new byte[] { 0x01 };
            // non-finalizable: copyBytes stays as-is after GC
            byte[] copyBytes = new ByteArray(new ByteArrayInputStream(bytes), bytes.length).getBytes();
            assertTrue(Arrays.equals(bytes, copyBytes));
            System.gc();
            Thread.sleep(10);
            System.runFinalization();
            Thread.sleep(10);
            assertTrue(Arrays.equals(bytes, copyBytes));
        }

        {
            byte[] bytes = new byte[] { 0x01 };
            // finalizable: copyBytes are wiped out after GC
            byte[] copyBytes = new ByteArray(new ByteArrayInputStream(bytes), bytes.length).finalizable().getBytes();
            assertTrue(Arrays.equals(bytes, copyBytes));
            System.gc();
            Thread.sleep(10);
            System.runFinalization();
            Thread.sleep(10);
            assertFalse(Arrays.equals(bytes, copyBytes));
            assertEquals(0x00, copyBytes[0]);
        }

        {
            byte[] bytes = new byte[] { 0x01 };
            // non-finalizable: copyBytes and bytes stays as-is after GC
            byte[] copyBytes = new ByteArray.ClearableSourceByteArray(bytes).clearSource().getBytes();
            assertTrue(Arrays.equals(bytes, copyBytes));
            System.gc();
            Thread.sleep(10);
            System.runFinalization();
            Thread.sleep(10);
            assertTrue(Arrays.equals(bytes, copyBytes));
        }

        {
            byte[] bytes = new byte[] { 0x01 };
            // finalizable: copyBytes and bytes are wiped out after GC
            byte[] copyBytes = new ByteArray.ClearableSourceByteArray(bytes).clearSource().finalizable().getBytes();
            assertTrue(Arrays.equals(bytes, copyBytes));
            System.gc();
            Thread.sleep(10);
            System.runFinalization();
            Thread.sleep(10);
            assertEquals(0x00, bytes[0]);
            assertEquals(0x00, copyBytes[0]);
        }

        {
            char c = 'a';
            byte b = (byte) c;
            char[] chars = new char[] { c };
            // non-finalizable: copyBytes and chars stays as-is after GC
            byte[] copyBytes = new ByteArray.ClearableSourceCharSeqByteArray(chars, Charset.forName("UTF-8")).clearSource().getBytes();
            assertEquals(c, chars[0]);
            assertEquals(b, copyBytes[0]);
            System.gc();
            Thread.sleep(10);
            System.runFinalization();
            Thread.sleep(10);
            assertEquals(c, chars[0]);
            assertEquals(b, copyBytes[0]);
        }

        {
            char c = 'a';
            byte b = (byte) c;
            char[] chars = new char[] { c };
            // finalizable: copyBytes and chars are wiped out after GC
            byte[] copyBytes = new ByteArray.ClearableSourceCharSeqByteArray(chars, Charset.forName("UTF-8")).clearSource().finalizable().getBytes();
            assertEquals(c, chars[0]);
            assertEquals(b, copyBytes[0]);
            System.gc();
            Thread.sleep(10);
            System.runFinalization();
            Thread.sleep(10);
            assertEquals(0, chars[0]);
            assertEquals(0x00, copyBytes[0]);
        }
    }
}