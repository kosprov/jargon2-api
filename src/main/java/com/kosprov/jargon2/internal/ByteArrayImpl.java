package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2Exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.text.Normalizer;
import java.util.Arrays;

import static com.kosprov.jargon2.api.Jargon2.DEFAULT_NORMALIZED_FORM;
import static com.kosprov.jargon2.api.Jargon2.Normalization;

public class ByteArrayImpl implements Jargon2.ByteArray {

    private boolean cleared = false;
    Data data;

    private ByteArrayImpl(Data data) {
        this.data = data;
    }

    public ByteArrayImpl(InputStream value, int bufferSize) {
        this(new InputStreamConsumer(value, bufferSize));
    }

    @Override
    public byte[] getBytes() {
        return data.toByteArray();
    }

    @Override
    public void close() {
        clear();
    }

    @Override
    public void clear() {
        if (!cleared) {
            data.wipe();
            cleared = true;
        }
    }

    private class FinalizationTrigger {
        @Override
        protected void finalize() {
            clear();
        }
    }

    private volatile FinalizationTrigger finalizationTrigger;

    @Override
    public ByteArrayImpl finalizable() {
        // Instantiate the finalization trigger only once for this object
        if (finalizationTrigger == null) {
            synchronized (this) {
                if (finalizationTrigger == null) {
                    finalizationTrigger = new FinalizationTrigger();
                }
            }
        }
        return this;
    }

    static byte[] encode(char[] value, Charset encoding) {
        CharsetEncoder encoder = encoding.newEncoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        byte[] bytes = new byte[(int) encoder.maxBytesPerChar() * value.length];
        encoder.reset();
        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = CharBuffer.wrap(value);
        try {
            CoderResult result = encoder.encode(charBuffer, bytesBuffer, true);
            if (!result.isUnderflow()) {
                result.throwException();
            }
            result = encoder.flush(bytesBuffer);
            if (!result.isUnderflow()) {
                result.throwException();
            }
            byte[] output;
            if (bytes.length == bytesBuffer.position()) {
                output = bytes;
            } else {
                output = Arrays.copyOf(bytes, bytesBuffer.position());
                Arrays.fill(bytes, (byte) 0x00);
            }
            return output;
        } catch (CharacterCodingException e) {
            Arrays.fill(bytes, (byte) 0x00);
            throw new Jargon2Exception("Failed to encode value to " + encoding.displayName());
        }
    }

    interface Data {
        void wipe();
        byte[] toByteArray();
    }

    interface CharSeqData extends Data {
        CharSeqData withEncoding(Charset encoding);
        CharSeqData withNormalization(Normalization normalization);
    }

    interface ExtendedData<E> extends Data {
        int length();
        E copyAndWipe(int length);
    }

    static class ByteArrayData implements ExtendedData<ByteArrayData> {
        byte[] bytes;

        ByteArrayData(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int length() {
            return bytes.length;
        }

        @Override
        public ByteArrayData copyAndWipe(int length) {
            try {
                return new ByteArrayData(Arrays.copyOf(this.bytes, length));
            } finally {
                wipe();
            }
        }

        @Override
        public void wipe() {
            Arrays.fill(this.bytes, (byte) 0x00);
        }

        @Override
        public byte[] toByteArray() {
            return bytes;
        }
    }

    static class CharArrayData implements ExtendedData<CharArrayData>, CharSeqData {
        char[] chars;
        byte[] bytes;
        Charset encoding;
        Normalization normalization;

        CharArrayData(char[] chars, Charset encoding, Normalization normalization) {
            this.chars = chars;
            this.encoding = encoding;
            this.normalization = normalization;
        }

        @Override
        public int length() {
            return chars.length;
        }

        @Override
        public CharArrayData copyAndWipe(int length) {
            try {
                return new CharArrayData(Arrays.copyOf(this.chars, length), encoding, normalization);
            } finally {
                wipe();
            }
        }

        @Override
        public void wipe() {
            Arrays.fill(chars, (char) 0);
            if (bytes != null) {
                Arrays.fill(bytes, (byte) 0x00);
            }
        }

        @Override
        public byte[] toByteArray() {
            if (bytes == null) {
                char[] c = chars;
                if (normalization != null) {
                    c = Normalizer.normalize(CharBuffer.wrap(chars), Normalizer.Form.valueOf(normalization.name())).toCharArray();
                }
                bytes = encode(c, encoding);
            }
            return bytes;
        }

        @Override
        public CharSeqData withEncoding(Charset encoding) {
            return new CharArrayData(chars, encoding, normalization);
        }

        @Override
        public CharSeqData withNormalization(Normalization normalization) {
            return new CharArrayData(chars, encoding, normalization);
        }
    }

    abstract static class Consumer<T, E extends ExtendedData<E>> implements Data {
        T stream;
        int bufferSize;
        byte[] bytes;

        Consumer(T stream, int bufferSize) {
            this.stream = stream;
            this.bufferSize = bufferSize;
        }

        abstract int read(E target, int offset) throws IOException;
        abstract E create(int length);

        @Override
        public byte[] toByteArray() {
            if (bytes == null) {
                E data = create(bufferSize);
                int offset = 0;
                int total = 0;
                do {
                    E copy;
                    try {
                        int dataRead = read(data, offset);
                        if (dataRead != -1) {
                            total += dataRead;
                        }
                        if (dataRead < bufferSize) {
                            if (total > 0) {
                                copy = data.copyAndWipe(total);
                            } else {
                                copy = create(0);
                            }
                        } else {
                            copy = data.copyAndWipe(data.length() + bufferSize);
                            offset += bufferSize;
                        }
                    } catch (IOException e) {
                        data.wipe();
                        throw new Jargon2Exception("Could not consume stream");
                    }
                    data = copy;
                } while (data.length() != total);
                bytes = data.toByteArray();
            }
            return bytes;
        }

        @Override
        public void wipe() {
            if (bytes != null) {
                Arrays.fill(bytes, (byte) 0x00);
            }
        }
    }

    static class InputStreamConsumer extends Consumer<InputStream, ByteArrayData> {

        InputStreamConsumer(InputStream stream, int bufferSize) {
            super(stream, bufferSize);
        }

        @Override
        int read(ByteArrayData target, int offset) throws IOException {
            return stream.read(target.bytes, offset, bufferSize);
        }

        @Override
        ByteArrayData create(int length) {
            return new ByteArrayData(new byte[length]);
        }
    }

    static class ReaderConsumer extends Consumer<Reader, CharArrayData> implements CharSeqData {

        Charset encoding;
        Normalization normalization;

        ReaderConsumer(Reader stream, int bufferSize, Charset encoding, Normalization normalization) {
            super(stream, bufferSize);
            this.encoding = encoding;
            this.normalization = normalization;
        }

        @Override
        int read(CharArrayData target, int offset) throws IOException {
            return stream.read(target.chars, offset, bufferSize);
        }

        @Override
        CharArrayData create(int length) {
            return new CharArrayData(new char[length], encoding, normalization);
        }

        @Override
        public CharSeqData withEncoding(Charset encoding) {
            return new ReaderConsumer(stream, bufferSize, encoding, normalization);
        }

        @Override
        public CharSeqData withNormalization(Normalization normalization) {
            return new ReaderConsumer(stream, bufferSize, encoding, normalization);
        }
    }

    public static class CharSeqByteArrayImpl extends ByteArrayImpl implements Jargon2.CharSeqByteArray {

        public CharSeqByteArrayImpl(char[] value, Charset encoding) {
            super(new CharArrayData(value, encoding, null));
        }

        public CharSeqByteArrayImpl(String value, Charset encoding) {
            this(value.toCharArray(), encoding);
        }

        public CharSeqByteArrayImpl(Reader value, int bufferSize, Charset encoding) {
            super(new ReaderConsumer(value, bufferSize, encoding, null));
        }

        @Override
        public CharSeqByteArrayImpl encoding(String encoding) {
            return encoding(Charset.forName(encoding));
        }

        @Override
        public CharSeqByteArrayImpl encoding(Charset encoding) {
            this.data = ((CharSeqData) data).withEncoding(encoding);
            return this;
        }

        @Override
        public CharSeqByteArrayImpl normalize() {
            return normalize(DEFAULT_NORMALIZED_FORM);
        }

        @Override
        public CharSeqByteArrayImpl normalize(Normalization normalization) {
            this.data = ((CharSeqData) data).withNormalization(normalization);
            return this;
        }

        @Override
        public CharSeqByteArrayImpl finalizable() {
            return (CharSeqByteArrayImpl) super.finalizable();
        }
    }

    public static class ClearableSourceByteArrayImpl extends ByteArrayImpl implements Jargon2.ClearableSourceByteArray {

        boolean clearSource;
        byte[] bytes;

        public ClearableSourceByteArrayImpl(byte[] value) {
            super(new ByteArrayData(Arrays.copyOf(value, value.length)));
            this.bytes = value;
        }

        @Override
        public ClearableSourceByteArrayImpl clearSource() {
            return clearSource(true);
        }

        @Override
        public ClearableSourceByteArrayImpl clearSource(boolean clear) {
            this.clearSource = clear;
            return this;
        }

        @Override
        public void clear() {
            if (clearSource) {
                Arrays.fill(bytes, (byte) 0x00);
            }
            super.clear();
        }

        @Override
        public ClearableSourceByteArrayImpl finalizable() {
            return (ClearableSourceByteArrayImpl) super.finalizable();
        }
    }

    public static class ClearableSourceCharSeqByteArrayImpl extends CharSeqByteArrayImpl implements Jargon2.ClearableSourceCharSeqByteArray {
        boolean clearSource;
        char[] chars;

        public ClearableSourceCharSeqByteArrayImpl(char[] value, Charset encoding) {
            super(Arrays.copyOf(value, value.length), encoding);
            this.chars = value;
        }

        @Override
        public ClearableSourceCharSeqByteArrayImpl clearSource() {
            return clearSource(true);
        }

        @Override
        public ClearableSourceCharSeqByteArrayImpl clearSource(boolean clear) {
            this.clearSource = clear;
            return this;
        }

        @Override
        public void clear() {
            if (clearSource) {
                Arrays.fill(chars, (char) 0);
            }
            super.clear();
        }

        @Override
        public ClearableSourceCharSeqByteArrayImpl encoding(String encoding) {
            return (ClearableSourceCharSeqByteArrayImpl) super.encoding(encoding);
        }

        @Override
        public ClearableSourceCharSeqByteArrayImpl encoding(Charset encoding) {
            return (ClearableSourceCharSeqByteArrayImpl) super.encoding(encoding);
        }

        @Override
        public ClearableSourceCharSeqByteArrayImpl normalize() {
            return (ClearableSourceCharSeqByteArrayImpl) super.normalize();
        }

        @Override
        public ClearableSourceCharSeqByteArrayImpl normalize(Normalization normalization) {
            return (ClearableSourceCharSeqByteArrayImpl) super.normalize(normalization);
        }

        @Override
        public ClearableSourceCharSeqByteArrayImpl finalizable() {
            return (ClearableSourceCharSeqByteArrayImpl) super.finalizable();
        }
    }
}