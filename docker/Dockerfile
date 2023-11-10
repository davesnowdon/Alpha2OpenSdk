FROM eclipse-temurin:8u382-b05-jdk-jammy

# Set user home dir so that gradle does not create directories named '?' in the project dir
ENV HOME /home/build
ENV GRADLE_USER_HOME $HOME
RUN mkdir -p ${GRADLE_USER_HOME} \
    && chmod 777 ${GRADLE_USER_HOME}

# android SDK location (and make it writable just in case we need to install more stuff)
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools
RUN mkdir -p ${ANDROID_HOME} \
    && chmod 777 ${ANDROID_HOME}

# Install system utilities
RUN apt-get update && apt-get install -y \
    unzip \
    wget

# Download and install Android SDK
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-6200805_latest.zip -O android-sdk.zip \
    && mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && unzip -d ${ANDROID_HOME}/cmdline-tools android-sdk.zip \
    && rm android-sdk.zip

# Move downloaded SDK tools to correct directory and set PATH
RUN mv ${ANDROID_HOME}/cmdline-tools/tools ${ANDROID_HOME}/cmdline-tools/latest

# Accept licenses before installing components
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses

# Install Android SDK components
RUN ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" \
    "platforms;android-22" \
    "platforms;android-25" \
    "build-tools;22.0.1" \
    "build-tools;30.0.2"

# Cleanup
RUN apt-get clean

# TODO download gradle

# TODO install android plugin

# Make bash our default shell
CMD ["/bin/bash"]

