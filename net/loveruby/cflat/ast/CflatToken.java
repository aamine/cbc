package net.loveruby.cflat.ast;
import net.loveruby.cflat.parser.Token;
import net.loveruby.cflat.parser.ParserConstants;
import net.loveruby.cflat.utils.TextUtils;
import java.util.*;

/**
 * Token wrapper.
 */
public class CflatToken implements Iterable<CflatToken> {
    protected Token token;
    protected boolean isSpecial;

    public CflatToken(Token token) {
        this(token, false);
    }

    public CflatToken(Token token, boolean isSpecial) {
        this.token = token;
        this.isSpecial = isSpecial;
    }

    public String toString() {
       return token.image;
    }

    public boolean isSpecial() {
        return this.isSpecial;
    }

    public int kindID() {
        return token.kind;
    }

    public String kindName() {
        return ParserConstants.tokenImage[token.kind];
    }

    public int lineno() {
        return token.beginLine;
    }

    public int column() {
        return token.beginColumn;
    }

    public String image() {
        return token.image;
    }

    public String dumpedImage() {
        return TextUtils.dumpString(token.image);
    }

    public Iterator<CflatToken> iterator() {
        return buildTokenList(token, false).iterator();
    }

    protected List<CflatToken> tokensWithoutFirstSpecials() {
        return buildTokenList(token, true);
    }

    protected List<CflatToken> buildTokenList(Token first, boolean rejectFirstSpecials) {
        List<CflatToken> result = new ArrayList<CflatToken>();
        boolean rejectSpecials = rejectFirstSpecials;
        for (Token t = first; t != null; t = t.next) {
            if (t.specialToken != null && !rejectSpecials) {
                Token s = specialTokenHead(t.specialToken);
                for (; s != null; s = s.next) {
                    result.add(new CflatToken(s));
                }
            }
            result.add(new CflatToken(t));
            rejectSpecials = false;
        }
        return result;
    }

    protected Token specialTokenHead(Token firstSpecial) {
        Token s = firstSpecial;
        while (s.specialToken != null) {
            s = s.specialToken;
        }
        return s;
    }

    public String includedLine() {
        StringBuffer buf = new StringBuffer();
        for (CflatToken t : tokensWithoutFirstSpecials()) {
            int idx = t.image().indexOf("\n");
            if (idx >= 0) {
                buf.append(t.image().substring(0, idx));
                break;
            }
            buf.append(t.image());
        }
        return buf.toString();
    }
}
