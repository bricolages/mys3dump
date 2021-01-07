# mys3dump

Dumps MySQL table to S3 parallely.

## Prerequisites

- Java 11

## Usage

```
java -jar mys3dump.jar <options>
```

Options:
```
-b,--bucket <arg>                           S3 bucket name.
-c,--partition-column <arg>                 [optional] Partition column name.
-C,--compress                               [optional] Compresses S3 objects with gzip.
-D,--database <arg>                         MySQL database name.
-d,--delete-object                          [optional] Delete object(s) with specified prefix.
                                            If this option is not enabled and any object already
                                            exists, mys3dump stops with error result.  (Default: false)
-f,--format <arg>                           [optinoal] Output format (json or csv). (Default: json)
-h,--host <arg>                             MySQL host address.
-H,--help                                   Print this message.
-k,--object-key-delimiter                   [optional] Delimiter for object key.  Used to prevent unintended
                                            deletion of object in different hierarchy. (Default: /)
-n,--partition-number <arg>                 [optional] Partition number. (Default: 4)
-o,--connection-property <property=value>   [optional] MySQL connection property.
-p,--password <arg>                         MySQL password.  Use MYS3DUMP_PASSWORD environment variable instead.
-P,--port <arg>                             [optional] MySQL port. (Default: 3306)
-q,--query <arg>                            MySQL Query. Optional.
-r,--object-size <arg>                      [optional] Preferred max S3 object bytes. (Default: 67108864)
-S,--src-zone-offset <arg>                  [optional] Convert timezone from src-zone-offset to
                                            dst-zone-offset. (Default: +00:00)
-t,--table <arg>                            Target table name.
-T,--dst-zone-offset <arg>                  Convert timezone from src-zone-offset to dst-zone-offset.
                                            No timezone conversion will happen if not specified.
-u,--username <arg>                         MySQL username
-w,--write-concurrency <arg>                [optional] Write concurrency. (Default: 4)
-x,--prefix <arg>                           S3 object prefix
```

Environment:

- MYS3DUMP_PASSWORD: MySQL password.

## Build & Test

```
% ./script/gradle build
```
This generates an executable jar `mys3dump-X.X.X-all.jar` in the build/libs/ directory.
