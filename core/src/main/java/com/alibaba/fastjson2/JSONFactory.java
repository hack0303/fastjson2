package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class JSONFactory {
    public static final String CREATOR;

    public static final String PROPERTY_DENY_PROPERTY = "fastjson2.parser.deny";
    public static final String PROPERTY_AUTO_TYPE_ACCEPT = "fastjson2.autoTypeAccept";
    public static final String PROPERTY_AUTO_TYPE_SUPPORT = "fastjson2.autoTypeSupport";
    public static final String PROPERTY_AUTO_TYPE_HANDLER = "fastjson2.autoTypeHandler";
    public static final String PROPERTY_AUTO_TYPE_BEFORE_HANDLER = "fastjson2.autoTypeBeforeHandler";

    public static String getProperty(String key) {
        return DEFAULT_PROPERTIES.getProperty(key);
    }

    static final class Utils {
        static volatile ToIntFunction<String> CODER_FUNCTION;
        static volatile Function<String, byte[]> VALUE_FUNCTION;
        static volatile boolean CODER_FUNCTION_ERROR;
        static BiFunction<char[], Boolean, String> STRING_CREATOR_JDK8;
        static Function<byte[], String> STRING_CREATOR_JDK11;
        static BiFunction<byte[], Charset, String> STRING_CREATOR_JDK17;
        static volatile boolean STRING_CREATOR_ERROR = false;
    }

    static final class UUIDUtils {
        private static final byte[] NIBBLES = new byte[]{
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            +0, +1, +2, +3, +4, +5, +6, +7, +8, +9, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
        };

        static long parse4Nibbles(byte[] name, int pos) {
            byte ch1 = name[pos];
            byte ch2 = name[pos + 1];
            byte ch3 = name[pos + 2];
            byte ch4 = name[pos + 3];
            return (ch1 | ch2 | ch3 | ch4) > 0xff ? -1 :
                NIBBLES[ch1] << 12 | NIBBLES[ch2] << 8 | NIBBLES[ch3] << 4 | NIBBLES[ch4];
        }

        static long parse4Nibbles(char[] name, int pos) {
            char ch1 = name[pos];
            char ch2 = name[pos + 1];
            char ch3 = name[pos + 2];
            char ch4 = name[pos + 3];
            return (ch1 | ch2 | ch3 | ch4) > 0xFF ? -1 :
                NIBBLES[ch1] << 12 | NIBBLES[ch2] << 8 | NIBBLES[ch3] << 4 | NIBBLES[ch4];
        }

        static long parse4Nibbles(String name, int pos) {
            char ch1 = name.charAt(pos);
            char ch2 = name.charAt(pos + 1);
            char ch3 = name.charAt(pos + 2);
            char ch4 = name.charAt(pos + 3);
            return (ch1 | ch2 | ch3 | ch4) > 0xFF ? -1 :
                NIBBLES[ch1] << 12 | NIBBLES[ch2] << 8 | NIBBLES[ch3] << 4 | NIBBLES[ch4];
        }
    }

    static final BigDecimal LOW = BigDecimal.valueOf(-9007199254740991L);
    static final BigDecimal HIGH = BigDecimal.valueOf(9007199254740991L);
    static final BigInteger LOW_BIGINT = BigInteger.valueOf(-9007199254740991L);
    static final BigInteger HIGH_BIGINT = BigInteger.valueOf(9007199254740991L);

    static final char[] CA = new char[]{
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };

    static final char[] DIGITS = {
        '0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'
    };

    static final int[] DIGITS2 = new int[]{
        +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
        +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
        +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
        +0, +1, +2, +3, +4, +5, +6, +7, +8, +9, +0, +0, +0, +0, +0, +0,
        +0, 10, 11, 12, 13, 14, 15, +0, +0, +0, +0, +0, +0, +0, +0, +0,
        +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
        +0, 10, 11, 12, 13, 14, 15
    };

    static {
        Properties properties = new Properties();

        InputStream inputStream = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            public InputStream run() {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();

                final String resourceFile = "fastjson2.properties";

                if (cl != null) {
                    return cl.getResourceAsStream(resourceFile);
                } else {
                    return ClassLoader.getSystemResourceAsStream(resourceFile);
                }
            }
        });
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (java.io.IOException ignored) {
            } finally {
                IOUtils.close(inputStream);
            }
        }
        DEFAULT_PROPERTIES = properties;

        String property = System.getProperty("fastjson2.creator");
        if (property != null) {
            property = property.trim();
        }

        CREATOR = property == null ? "asm" : property;
    }

    static final class Cache {
        volatile char[] chars;

        volatile byte[] bytes0;
        volatile byte[] bytes1;
        volatile byte[] bytes2;
        volatile byte[] bytes3;

        volatile byte[] valueBytes;
    }

    static final Cache CACHE = new Cache();

    static final int CACHE_THREAD = 1024 * 1024;

    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, char[]> CHARS_UPDATER = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, char[].class, "chars");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES0_UPDATER = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes0");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES1_UPDATER = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes1");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES2_UPDATER = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes2");
    static final AtomicReferenceFieldUpdater<JSONFactory.Cache, byte[]> BYTES3_UPDATER = AtomicReferenceFieldUpdater.newUpdater(JSONFactory.Cache.class, byte[].class, "bytes3");

    static final AtomicReferenceFieldUpdater<Cache, byte[]> VALUE_BYTES_UPDATER = AtomicReferenceFieldUpdater.newUpdater(Cache.class, byte[].class, "valueBytes");

    static final class SymbolTableImpl implements JSONB.SymbolTable {
        private final String[] names;
        private final long hashCode64;
        private final short[] mapping;

        private final long[] hashCodes;
        private final long[] hashCodesOrigin;

        SymbolTableImpl(String... input) {
            Set<String> set = new TreeSet<>();
            for (String name : input) {
                set.add(name);
            }
            names = new String[set.size()];
            Iterator<String> it = set.iterator();

            for (int i = 0; i < names.length; i++) {
                if (it.hasNext()) {
                    names[i] = it.next();
                }
            }

            long[] hashCodes = new long[names.length];
            for (int i = 0; i < names.length; i++) {
                long hashCode = Fnv.hashCode64(names[i]);
                hashCodes[i] = hashCode;
            }
            this.hashCodesOrigin = hashCodes;

            this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
            Arrays.sort(this.hashCodes);

            mapping = new short[this.hashCodes.length];
            for (int i = 0; i < hashCodes.length; i++) {
                long hashCode = hashCodes[i];
                int index = Arrays.binarySearch(this.hashCodes, hashCode);
                mapping[index] = (short) i;
            }

            long hashCode64 = Fnv.MAGIC_HASH_CODE;
            for (long hashCode : hashCodes) {
                hashCode64 ^= hashCode;
                hashCode64 *= Fnv.MAGIC_PRIME;
            }
            this.hashCode64 = hashCode64;
        }

        @Override
        public int size() {
            return names.length;
        }

        @Override
        public long hashCode64() {
            return hashCode64;
        }

        @Override
        public String getNameByHashCode(long hashCode) {
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return null;
            }

            int index = this.mapping[m];
            return names[index];
        }

        @Override
        public int getOrdinalByHashCode(long hashCode) {
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return -1;
            }

            return this.mapping[m] + 1;
        }

        @Override
        public int getOrdinal(String name) {
            long hashCode = Fnv.hashCode64(name);
            int m = Arrays.binarySearch(hashCodes, hashCode);
            if (m < 0) {
                return -1;
            }

            return this.mapping[m] + 1;
        }

        @Override
        public String getName(int ordinal) {
            return names[ordinal - 1];
        }

        @Override
        public long getHashCode(int ordinal) {
            return hashCodesOrigin[ordinal - 1];
        }
    }

    static final Properties DEFAULT_PROPERTIES;

    static ObjectWriterProvider defaultObjectWriterProvider = new ObjectWriterProvider();
    static ObjectReaderProvider defaultObjectReaderProvider = new ObjectReaderProvider();

    static final ThreadLocal<ObjectReaderCreator> readerCreatorLocal = new ThreadLocal<>();
    static final ThreadLocal<ObjectReaderProvider> readerProviderLocal = new ThreadLocal<>();
    static final ThreadLocal<ObjectWriterCreator> writerCreatorLocal = new ThreadLocal<>();

    public static JSONWriter.Context createWriteContext() {
        return new JSONWriter.Context(defaultObjectWriterProvider);
    }

    public static JSONWriter.Context createWriteContext(JSONWriter.Feature... features) {
        return new JSONWriter.Context(defaultObjectWriterProvider, features);
    }

    public static JSONReader.Context createReadContext() {
        return new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
    }

    public static ObjectWriterProvider getDefaultObjectWriterProvider() {
        return defaultObjectWriterProvider;
    }

    public static ObjectReaderProvider getDefaultObjectReaderProvider() {
        ObjectReaderProvider providerLocal = readerProviderLocal.get();
        if (providerLocal != null) {
            return providerLocal;
        }

        return defaultObjectReaderProvider;
    }

    public static void setContextReaderCreator(ObjectReaderCreator creator) {
        readerCreatorLocal.set(creator);
    }

    public static void setContextObjectReaderProvider(ObjectReaderProvider creator) {
        readerProviderLocal.set(creator);
    }

    public static ObjectReaderCreator getContextReaderCreator() {
        return readerCreatorLocal.get();
    }

    public static void setContextWriterCreator(ObjectWriterCreator creator) {
        writerCreatorLocal.set(creator);
    }

    public static ObjectWriterCreator getContextWriterCreator() {
        return writerCreatorLocal.get();
    }
}
