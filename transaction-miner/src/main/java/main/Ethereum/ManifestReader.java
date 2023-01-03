package main.Ethereum;
import java.io.*;
import java.math.BigInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManifestReader {
    private String path;
    private String contractAddress;
    private BigInteger startBlock;
    private BigInteger endBlock;

    private String node;
    private static final Logger LOGGER = Logger.getLogger(ManifestReader.class.getName());

    public ManifestReader(String path) {
        this.path = path;
        this.extractParameters();
    }

    InputStream createFileStream() throws IOException {
        if (this.path == null) {
            throw new FileNotFoundException("Filepath not found.");
        }
        final File file = new File(this.path);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new java.io.IOException(String.format("Invalid file path: '%s'.", this.path), ex);
        }
    }

    private void extractParameters() {
        Pattern patter;
        Matcher matcher;
        try {
            InputStream fs = this.createFileStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
            while(reader.ready()) {
                String line = reader.readLine();
                if (this.contractAddress == null && line.contains("LOG ENTRIES")) {
                    patter = Pattern.compile("(?<=\\()(\\S+)(?=\\))");
                    matcher = patter.matcher(line);
                    if(matcher.find()) {
                        this.contractAddress = matcher.group(0);
                    }
                } else if(this.getStartBlock() == null && this.getEndBlock() == null && line.contains("BLOCKS")) {
                    patter = Pattern.compile("(\\d+)+");
                    matcher = patter.matcher(line);
                    if(matcher.find()) {
                        this.startBlock  = new BigInteger(matcher.group(0));
                        if(matcher.find()) {
                            this.endBlock = new BigInteger(matcher.group(0));
                        }
                    }
                } else if (this.node == null && line.toLowerCase().contains("connect")) {
                    patter = Pattern.compile("(?<=\\\")(\\S+)(?=\\\")");
                    matcher = patter.matcher(line);
                    if(matcher.find()) {
                        this.node = matcher.group(0);
                    }
                }
            }
            if(this.contractAddress == null) {
                throw new IOException("contractAddress not defined");
            }
            if(this.startBlock == null || this.endBlock == null ) {
                throw new IOException("startblock or endblock not defined");
            }
            LOGGER.info(this.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BigInteger getStartBlock() {
        return startBlock;
    }

    public BigInteger getEndBlock() {
        return endBlock;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getNode() {
        return node;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ManifestReader{" +
                "path='" + path + '\'' +
                "node='" + node + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", startBlock=" + startBlock +
                ", endBlock=" + endBlock +
                '}';
    }
}
