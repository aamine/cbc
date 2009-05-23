package net.loveruby.cflat.utils;
import net.loveruby.cflat.parser.Parser;
import java.io.*;

abstract public class TextUtils {
    static final private byte vtab = 013;

    static public String dumpString(String str) {
        try {
            return dumpString(str, Parser.SOURCE_ENCODING);
        }
        catch (UnsupportedEncodingException ex) {
            throw new Error("UTF-8 is not supported??: " + ex.getMessage());
        }
    }

    static public String dumpString(String string, String encoding)
            throws UnsupportedEncodingException {
        byte[] src = string.getBytes(encoding);
        StringBuffer buf = new StringBuffer();
        buf.append("\"");
        for (int n = 0; n < src.length; n++) {
            int c = toUnsigned(src[n]);
            if (c == '"') buf.append("\\\"");
            else if (isPrintable(c)) buf.append((char)c);
            else if (c == '\b') buf.append("\\b");
            else if (c == '\t') buf.append("\\t");
            else if (c == '\n') buf.append("\\n");
            else if (c == vtab) buf.append("\\v");
            else if (c == '\f') buf.append("\\f");
            else if (c == '\r') buf.append("\\r");
            else {
                buf.append("\\" + Integer.toOctalString(c));
            }
        }
        buf.append("\"");
        return buf.toString();
    }

    static private int toUnsigned(byte b) {
        return b >= 0 ? b : 256 + b;
    }

    static public boolean isPrintable(int c) {
        return (' ' <= c) && (c <= '~');
    }
}
