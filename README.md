dockerfile-maven-plugin
=======================

A Maven plugin to generate Dockerfiles based on templates.

= Usage =

== pom.xml ==

Add the following plugin declaration to your pom.xml:

```
  <build>
    [..]
    <plugin>
      <groupId>org.acmsl</groupId>
      <artifactId>dockerfile-maven-plugin</artifactId>
      <version>latest-SNAPSHOT</version>
      <configuration>
        <outputDir>${project.build.outputDirectory}/META-INF/</outputDir>
        <template>${project.basedir}/src/main/assembly/Dockerfile.stg</template>
      </configuration>
    </plugin>
    [..]
  </build>
```

== Dockerfile.stg ==

Define your Dockerfile template.
You can use the following to customize yours:
```
group Dockerfile;

source(C) ::= <<
<!
  Defines the rules to generate Dockerfile files.
  @param C the context.
!>
<dockerfile(C=C, pom=C.P)>
>>

dockerfile(C, pom) ::= <<
<!
  Generates a custom Dockerfile.
  @param C the context.
  @param pom the pom.
!>
FROM acmsl/tomcat:201410
MAINTAINER <rydnr@acm-sl.org>
ENV HOME /root
USER root
# Create user <pom.artifactId>
RUN groupadd <pom.artifactId>
RUN useradd -s /bin/bash -g <pom.artifactId> -G <pom.artifactId>,tomcat,sudo -m -c "User running /etc/init.d/<pom.artifactId>" artifactory

# java-user can run tomcat app
RUN sed -i '/%sudo   ALL=(ALL:ALL) ALL/a java-user localhost=NOPASSWD: /etc/init.d/<pom.artifactId> stop,/etc/init.d/<pom.artifactId> start,/etc/init.d/<pom.artifactId> restart,/sbin/services <pom.artifactId> stop,/sbin/services <pom.artifactId> start,/sbin/services <pom.artifactId> restart' /etc/sudoers

RUN wget -O /opt/tomcat/webapps/<pom.artifactId>-<pom.version>.<pom.packaging> http://my.artifactory/repo/<pom.groupId>/<pom.artifactId>/<pom.version>/<pom.artifactId>-<pom.version>.<pom.packaging>
RUN mv /opt/tomcat/webapps/ROOT /opt/tomcat/webapps/welcome
RUN cd /home/ && /usr/lib/jvm/java/bin/jar -xvf /opt/tomcat/webapps/<pom.artifactId>-<pom.version>.<pom.packaging>
RUN rm -rf /home/<pom.artifactId> && mv /home/<pom.artifactId>-<pom.version> /home/<pom.artifactId>
ADD context.xml /opt/tomcat/conf/Catalina/localhost/ROOT.xml
ADD .bashrc /home/<pom.artifactId>/.bashrc
RUN rm -rf /opt/tomcat/webapps/*
ADD config /etc/default/tomcat
RUN mkdir /etc/service/<pom.artifactId>
RUN cd /etc/service/<pom.artifactId> && ln -s ../tomcat/run .
RUN cd /etc/service/<pom.artifactId> && ln -s tomcat <pom.artifactId>
RUN cd /etc/init.d && ln -s tomcat <pom.artifactId>
RUN update-rc.d <pom.artifactId> defaults
RUN chown -R <pom.artifactId>:tomcat /home/<pom.artifactId> /opt/tomcat/.<pom.artifactId>
RUN chmod -R g+w /home/<pom.artifactId> /opt/tomcat/.<pom.artifactId>
ADD rc.local /etc/rc.local
RUN chmod +x /etc/rc.local

VOLUME /home/<pom.artifactId>

EXPOSE 8080
>>
```