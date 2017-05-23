package org.bricolages.mys3dump;

import org.apache.commons.cli.*;

import java.time.ZoneOffset;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Created by shimpei-kodama on 2016/02/26.
 */
class Parameters {
    private final String MYSQL_PORT = "3306";
    private final String FORMAT = "json";
    private final String PARTITION_NUMBER = "4";
    private final String WRITE_CONCURRENCY = "4";
    private final String OBJECT_SIZE = "67108864";
    private final String OBJECT_KEY_DELIMITER = "/";
    private final String SRC_ZONE_OFFSET = "+00:00";
    private final CommandLine cl;
    private final String password;

    public Parameters(String args[]) throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("host").hasArg().required().desc("MySQL host address").build());
        options.addOption(Option.builder("u").longOpt("username").hasArg().required().desc("MySQL username").build());
        options.addOption(Option.builder("p").longOpt("password").hasArg().desc("MySQL password").build());
        options.addOption(Option.builder("D").longOpt("database").hasArg().required().desc("MySQL database name").build());
        options.addOption(Option.builder("b").longOpt("bucket").hasArg().required().desc("S3 bucket name").build());
        options.addOption(Option.builder("x").longOpt("prefix").hasArg().required().desc("S3 object prefix").build());
        options.addOption(Option.builder("t").longOpt("table").hasArg().required().desc("MySQL table name").build());
        options.addOption(Option.builder("o").longOpt("connection-property").hasArgs().argName("property=value").desc("MySQL connection property. Optional.").build());
        options.addOption(Option.builder("q").longOpt("query").hasArg().desc("MySQL Query. Optional.").build());
        options.addOption(Option.builder("c").longOpt("partition-column").hasArg().desc("Partition column name. Optional.").build());
        options.addOption(Option.builder("n").longOpt("partition-number").hasArg().desc("Partition number. Optional. Default: " + PARTITION_NUMBER).build());
        options.addOption(Option.builder("P").longOpt("port").hasArg().desc("MySQL port. Optional. Default: " + MYSQL_PORT).type(Integer.class).build());
        options.addOption(Option.builder("f").longOpt("format").hasArg().desc("Output format(json or csv). Optional. Default: " + FORMAT).build());
        options.addOption(Option.builder("C").longOpt("compress").desc("Gzip output.").build());
        options.addOption(Option.builder("w").longOpt("write-concurrency").hasArg().desc("Write concurrency. Optional. Default: " + WRITE_CONCURRENCY).build());
        options.addOption(Option.builder("r").longOpt("object-size").hasArg().desc("Preferred S3 object size (byte). Optional. Default: " + OBJECT_SIZE).build());
        options.addOption(Option.builder("d").longOpt("delete-object").desc("Delete object(s) with specified prefix. Optional. Default: false (error when already exists)").build());
        options.addOption(Option.builder("k").longOpt("object-key-delimiter").desc("Delimiter for object key. Used to prevent unintended deletion of object in different hierarchy. Optional. Default: " + OBJECT_KEY_DELIMITER).build());
        options.addOption(Option.builder("S").longOpt("src-zone-offset").hasArg().desc("Convert timezone from src-zone-offset to dst-zone-offset. Optional. Default: " + SRC_ZONE_OFFSET).build());
        options.addOption(Option.builder("T").longOpt("dst-zone-offset").hasArg().desc("Convert timezone from src-zone-offset to dst-zone-offset. Optional. No timezone conversion will happen if not specified.").build());
        options.addOption(Option.builder("H").longOpt("help").desc("Print Usage").build());
        try {
            cl = new DefaultParser().parse(options, args);
            if (cl.hasOption('H')) printHelp(options);
        } catch (ParseException e) {
            printHelp(options);
            throw e;
        }
        if (cl.hasOption('p')) {
            this.password = cl.getOptionValue('p');
        } else {
            this.password = System.getenv("MYS3DUMP_PASSWORD");
            if (this.password == null) {
                System.err.println("mys3dump: error: missing password");
                printHelp(options);
            }
        }
    }

    void printHelp(Options options) {
        new HelpFormatter().printHelp("mys3dump", options);
        System.exit(1);
    }

    String getHost() {
        return cl.getOptionValue('h');
    }

    String getUsername() {
        return cl.getOptionValue('u');
    }

    String getPassword() {
        return this.password;
    }

    String getDatabase() {
        return cl.getOptionValue('D');
    }

    String getBucket() {
        return cl.getOptionValue('b');
    }

    String getPrefix() {
        return cl.getOptionValue('x');
    }

    String getQuery() {
        return cl.getOptionValue('q', null);
    }

    String getTable() {
        return cl.getOptionValue('t');
    }

    String getConnectionProperty() {
        return cl.getOptionValue("o", null);
    }

    String getPartitionColumn() {
        return cl.getOptionValue('c', null);
    }

    Integer getPartitionNumber() {
        return Integer.valueOf(cl.getOptionValue('n', PARTITION_NUMBER));
    }

    Integer getPort() {
        return Integer.valueOf(cl.getOptionValue('P', MYSQL_PORT));
    }

    String getFormat() {
        return cl.getOptionValue('f', FORMAT);
    }

    Boolean getCompress() {
        return cl.hasOption('C');
    }

    Integer getWriteConcurrency() {
        return Integer.valueOf(cl.getOptionValue('w', WRITE_CONCURRENCY));
    }

    Integer getObjectSize() {
        return Integer.valueOf(cl.getOptionValue('r', OBJECT_SIZE));
    }

    boolean getDeleteObject() {
        return cl.hasOption('d');
    }

    String getObjectKeyDelimiter() {
        return cl.getOptionValue('k', OBJECT_KEY_DELIMITER);
    }

    ZoneOffset getSrcZoneOffset() {
        return ZoneOffset.of(cl.getOptionValue('S', SRC_ZONE_OFFSET));
    }

    ZoneOffset getDstZoneOffset() {
        return ZoneOffset.of(cl.getOptionValue('T', SRC_ZONE_OFFSET));
    }
}
