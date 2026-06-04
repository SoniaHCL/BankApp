FROM registry.access.redhat.com/ubi9

USER root

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Install Java 17, Maven, and required tooling
RUN dnf install -y \
        --allowerasing \
        --setopt=install_weak_deps=False \
        sudo \
        git \
        libicu \
        java-17-openjdk \
        java-17-openjdk-devel \
        maven \
        jq \
        python3 \
        gnupg2 \
        make \
        pinentry-curses \
        curl && \
    dnf clean all

# Install password-store
RUN git clone https://git.zx2c4.com/password-store.git /tmp/pass && \
    cd /tmp/pass && \
    make PREFIX=/usr/local install && \
    cd / && \
    rm -rf /tmp/pass

# Install Git Credential Manager
RUN curl -fsSL \
    https://github.com/git-ecosystem/git-credential-manager/releases/download/v2.6.1/gcm-linux_amd64.2.6.1.tar.gz \
    -o /tmp/gcm.tar.gz && \
    tar -xvf /tmp/gcm.tar.gz -C /usr/local/bin && \
    rm -f /tmp/gcm.tar.gz && \
    git-credential-manager configure

# Install code-server
RUN curl -fsSL https://code-server.dev/install.sh | sh -s -- --version=4.110.0

# Create non-root user
RUN useradd -u 1001 -m -d /usr/code codeuser

# Create required directories
RUN mkdir -p /usr/code /opt /tmp/junit-results && \
    chown -R 1001:1001 /usr/code /tmp/junit-results

# Enterprise certificate permissions
RUN chmod -R ugo+w \
    /etc/pki/ca-trust/source \
    /etc/pki/ca-trust/extracted \
    /etc/pki/tls/certs

# Add JUnit runner
COPY run-junit.sh /opt/run-junit.sh
RUN chmod +x /opt/run-junit.sh

USER 1001

CMD ["/bin/bash"]