package net.loveruby.cflat.utils;

public class TextUtils {
    static final public char bell = 007;
    static final public char vtab = 013;

    static public String escapeString(String str) {
        StringBuffer buf = new StringBuffer();
        buf.append("\"");
        for (int n = 0; n < str.length(); n++) {
            char c = str.charAt(n);
            if (c == '"') buf.append("\\\"");
            else if (isPrintable(c)) buf.append(c);
            else if (c == '\0') buf.append("\\000");
            else if (c == bell) buf.append("\\007");
            else if (c == '\b') buf.append("\\b");
            else if (c == '\t') buf.append("\\t");
            else if (c == '\n') buf.append("\\n");
            else if (c == vtab) buf.append("\\v");
            else if (c == '\f') buf.append("\\f");
            else if (c == '\r') buf.append("\\r");
            else {
                buf.append("\\" + Integer.toOctalString((int)c));
            }
        }
        buf.append("\"");
        return buf.toString();
    }

    static public boolean isPrintable(char c) {
        return (' ' <= c) && (c <= '~');
    }
}
