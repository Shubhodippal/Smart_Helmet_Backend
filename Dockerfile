# Use latest Eclipse Temurin JDK 21 base image (ARM64 compatible)
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR file into the image
COPY target/helmet-0.0.1-SNAPSHOT.jar app.jar

# Install Tesseract OCR (uncomment if needed)
#RUN apt-get update && apt-get install -y tesseract-ocr && rm -rf /var/lib/apt/lists/*

# Optimized for Raspberry Pi 5 (4 cores, 4GB RAM)
ENV JAVA_OPTS="-Xms512m -Xmx3g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -XX:ParallelGCThreads=4 -XX:ConcGCThreads=2 -XX:G1ConcRefinementThreads=4"

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
