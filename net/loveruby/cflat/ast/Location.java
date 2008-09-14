package net.loveruby.cflat.ast;
import net.loveruby.cflat.parser.Token;
import net.loveruby.cflat.parser.ParserConstants;
import net.loveruby.cflat.utils.TextUtils;
import java.util.*;

public class Location {
    protected String sourceName;
    protected CflatToken token;

    public Location(String sourceName, Token token) {
        this(sourceName, new CflatToken(token));
    }

    public Location(String sourceName, CflatToken token) {
        this.sourceName = sourceName;
        this.token = token;
    }

    public String sourceName() {
        return sourceName;
    }

    public CflatToken token() {
        return token;
    }

    /** line number */
    public int lineno() {
        return token.lineno();
    }

    public int column() {
        return token.column();
    }

    public String line() {
        return token.includedLine();
    }

    public String numberedLine() {
        return "line " + token.lineno() + ": " + line();
    }

    public String toString() {
        return sourceName + ":" + token.lineno();
    }
}
