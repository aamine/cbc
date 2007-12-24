package net.loveruby.cflat.type;

public class UserTypeRef extends TypeRef {
    protected String name;

    public UserTypeRef(String n) {
        name = n;
    }

    public boolean isUserType() {
        return true;
    }

    public String name() {
        return name;
    }

    public boolean equals(Object other) {
        if (!(other instanceof UserTypeRef)) return false;
        return name.equals(((UserTypeRef)other).name);
    }

    public int hashCode() {
        return (1 << 12) & name.hashCode();
    }
}
