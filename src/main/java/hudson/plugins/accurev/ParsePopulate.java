package hudson.plugins.accurev;

import hudson.plugins.accurev.AccurevLauncher.ICmdOutputParser;
import hudson.plugins.accurev.AccurevLauncher.UnhandledAccurevCommandOutput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Filters the output of the populate command and just shows a summary of the
 * output. Helps prevent build logs being clogged up with the checkout.
 */
final class ParsePopulate implements ICmdOutputParser<Boolean, OutputStream> {
    public Boolean parse(InputStream cmdOutput, OutputStream streamToCopyOutputTo)
            throws UnhandledAccurevCommandOutput, IOException {
        final String lineStartDirectory = "Creating dir:";
        final String lineStartElement = "Populating element";
        int countOfDirectories = 0;
        int countOfElements = 0;
        final Reader stringReader = new InputStreamReader(cmdOutput);
        final Writer stringWriter = new OutputStreamWriter(streamToCopyOutputTo);
        final BufferedReader lineReader = new BufferedReader(stringReader);
        final BufferedWriter lineWriter = new BufferedWriter(stringWriter);
        String line;
        try {
            line = lineReader.readLine();
            while (line != null) {
                if (line.startsWith(lineStartElement)) {
                    countOfElements++;
                } else if (line.startsWith(lineStartDirectory)) {
                    countOfDirectories++;
                } else {
                    lineWriter.write(line);
                    lineWriter.newLine();
                }
                line = lineReader.readLine();
            }
            final String msg = "Populated " + countOfElements + " elements in " + countOfDirectories + " directories.";
            streamToCopyOutputTo.write(msg.getBytes());
        } finally {
            lineReader.close();
            lineWriter.flush();
        }
        return Boolean.TRUE;
    }
}
