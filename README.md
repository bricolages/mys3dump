# mys3dump

Dumps MySQL table to S3 parallely.

## Usage

```
Usage: java -jar mys3dump.jar <options>
Options:
 -b,--bucket <arg>                           S3 bucket name
 -c,--partition-column <arg>                 Partition column name.
                                             Optional.
 -C,--compress                               Gzip output.
 -D,--database <arg>                         MySQL database name
 -d,--delete-object                          Delete object(s) with
                                             specified prefix. Optional.
                                             Default: false (error when
                                             already exists)
 -f,--format <arg>                           Output format(json or csv).
                                             Optional. Default: json
 -h,--host <arg>                             MySQL host address
 -H,--help                                   Print Usage
 -k,--object-key-delimiter                   Delimiter for object key.
                                             Used to prevent unintended
                                             deletion of object in
                                             different hierarchy.
                                             Optional. Default: /
 -n,--partition-number <arg>                 Partition number. Optional.
                                             Default: 4
 -o,--connection-property <property=value>   MySQL connection property.
                                             Optional.
 -p,--password <arg>                         MySQL password
 -P,--port <arg>                             MySQL port. Optional.
                                             Default: 3306
 -q,--query <arg>                            MySQL Query. Optional.
 -r,--object-size <arg>                      Preferred S3 object size
                                             (byte). Optional. Default:
                                             67108864
 -S,--src-zone-offset <arg>                  Convert timezone from
                                             src-zone-offset to
                                             dst-zone-offset. Optional.
                                             Default: +00:00
 -t,--table <arg>                            MySQL table name
 -T,--dst-zone-offset <arg>                  Convert timezone from
                                             src-zone-offset to
                                             dst-zone-offset. Optional. No
                                             timezone conversion will
                                             happen if not specified.
 -u,--username <arg>                         MySQL username
 -w,--write-concurrency <arg>                Write concurrency. Optional.
                                             Default: 4
 -x,--prefix <arg>                           S3 object prefix
```

## Prerequisites

- JDK 8

## Build & Test

```
% ./script/gradle build
```

## Packaging

```
% ./script/gradle shadowJar
```
This generates mys3dump-X.X.X-all.jar in the build/libs/ directory.
