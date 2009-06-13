package net.loveruby.cflat.utils;
import net.loveruby.cflat.exception.IPCException;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

abstract public class CommandUtils {
    static public void invoke(List<String> cmdArgs,
            ErrorHandler errorHandler, boolean verbose) throws IPCException {
        if (verbose) {
            dumpCommand(cmdArgs);
        }
        try {
            String[] cmd = cmdArgs.toArray(new String[] {});
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
            passThrough(proc.getInputStream());
            passThrough(proc.getErrorStream());
            if (proc.exitValue() != 0) {
                errorHandler.error(cmd[0] + " failed."
                        + " (status " + proc.exitValue() + ")");
                throw new IPCException("compile error");
            }
        }
        catch (InterruptedException ex) {
            errorHandler.error("external command interrupted: "
                    + cmdArgs.get(0) + ": " + ex.getMessage());
            throw new IPCException("compile error");
        }
        catch (IOException ex) {
            errorHandler.error(
                    "IO error in external command: " + ex.getMessage());
            throw new IPCException("compile error");
        }
    }

    static private void dumpCommand(List<String> args) {
        String sep = "";
        for (String arg : args) {
            System.out.print(sep); sep = " ";
            System.out.print(arg);
        }
        System.out.println("");
    }

    static private void passThrough(InputStream s) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(s));
        String line;
        while ((line = r.readLine()) != null) {
            System.err.println(line);
        }
    }
}
