# REQUIREMENTS:

Java 8 on system path.

# CONFIGURATION:

Configure contentDir in application.properties + any other properties you like.
Files and buckets are served from contentDir. If not configured the contentDir will be ${CWD}/content.
In any case please make sure that configured contentDir actually exists and is accessible by the application.

See https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html for a list of other possible properties.


# RUN:

java -jar filestore-server-<version>.jar