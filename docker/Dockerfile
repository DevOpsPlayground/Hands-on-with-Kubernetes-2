FROM jenkins/jenkins:2.96

# Disable the installation wizard
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install the desired plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

ADD scripts /usr/share/jenkins/ref/init.groovy.d

# Install Maven
USER root
RUN apt-get update && apt-get install -y maven
USER jenkins
