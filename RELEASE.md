# Release Note

## version 1.2.0
- [CHANGE] Use MariaDB Connector/J instead of MySQL Connector/J,
  to avoid unexpected performance degration of MySQL Connector 8.

## version 1.1.0
- [CHANGE] Upgrades Java from 8 to 11.
- [CHANGE] Logging system is changed from Log4j 1.x to Slf4j + Logback.

Developer only changes:
- Build system is changed from Maven to Gradle.
- Introduces Lombok.
