package net.loveruby.cflat.utils;
import net.loveruby.cflat.exception.IPCException;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

abstract public class CommandUtils {
    static public void invoke(List<CommandArg> cmdArgs,
            ErrorHandler errorHandler, boolean debug) throws IPCException {
        if (debug) {
            dumpCommand(cmdArgs);
        }
        try {
            String[] cmd = getStrings(cmdArgs);
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
            errorHandler.error("gcc interrupted: " + ex.getMessage());
            throw new IPCException("compile error");
        }
        catch (IOException ex) {
            errorHandler.error(ex.getMessage());
            throw new IPCException("compile error");
        }
    }

    static private String[] getStrings(List<CommandArg> list) {
        String[] result = new String[list.size()];
        int idx = 0;
        for (CommandArg arg : list) {
            result[idx++] = arg.toString();
        }
        return result;
    }

    static private void dumpCommand(List<CommandArg> args) {
        String sep = "";
        for (CommandArg arg : args) {
            System.out.print(sep); sep = " ";
            System.out.print(arg.toString());
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
