package net.loveruby.cflat.sysdep.x86;

public interface ELFConstants {
    // Flags
    static public final String SectionFlag_allocatable = "a";
    static public final String SectionFlag_writable = "w";
    static public final String SectionFlag_executable = "x";
    static public final String SectionFlag_sectiongroup = "G";
    static public final String SectionFlag_strings = "S";
    static public final String SectionFlag_threadlocalstorage = "T";

    // argument of "G" flag
    static public final String Linkage_linkonce = "comdat";

    // Types
    static public final String SectionType_bits = "@progbits";
    static public final String SectionType_nobits = "@nobits";
    static public final String SectionType_note = "@note";

    static public final String SymbolType_function = "@function";
}
