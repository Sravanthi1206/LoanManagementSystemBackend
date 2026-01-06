FROM jenkins/jenkins:lts

USER root

# Install Docker CLI and required tools
RUN apt-get update && \
    apt-get install -y docker.io wget unzip && \
    rm -rf /var/lib/apt/lists/*

# Install SonarQube Scanner
ENV SONAR_SCANNER_VERSION=5.0.1.3006
RUN wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip && \
    unzip -q sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip -d /opt && \
    rm sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip && \
    ln -s /opt/sonar-scanner-${SONAR_SCANNER_VERSION}-linux/bin/sonar-scanner /usr/local/bin/sonar-scanner

# Skip initial setup wizard and install suggested plugins
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install required Jenkins plugins
RUN jenkins-plugin-cli --plugins \
    git \
    workflow-aggregator \
    pipeline-stage-view \
    docker-workflow \
    docker-plugin \
    sonar \
    jacoco \
    junit \
    maven-plugin \
    credentials-binding \
    blueocean

USER jenkins
