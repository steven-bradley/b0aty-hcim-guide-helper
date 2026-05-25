FROM amazoncorretto:17

# Install essential build tools
RUN yum install -y \
    git \
    vim \
    curl \
    unzip \
    tar \
    gzip \
    && yum clean all

# Install Gradle
ENV GRADLE_VERSION=8.5
RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && rm /tmp/gradle.zip \
    && ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle

# Set the working directory
WORKDIR /workspace

# Set up volume for development
VOLUME /workspace

# Default command opens a shell for development
CMD ["/bin/bash"]
