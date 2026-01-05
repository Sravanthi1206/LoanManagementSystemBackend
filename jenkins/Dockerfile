FROM jenkins/jenkins:lts
USER root
# Install Docker CLI to allow Jenkins to run docker commands (talking to host socket)
RUN apt-get update && \
    apt-get install -y docker.io && \
    rm -rf /var/lib/apt/lists/*
# Add jenkins user to docker group (optional, usually socket permissions handle this with 'user: root' in compose, but good practice)
# But simpler: run container as root in Compose or set socket permissions. 
# For this demo, we will run the container entrypoint as root in compose for simplicity.
USER jenkins
