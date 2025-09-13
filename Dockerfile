# Use a full-featured Ubuntu image as a base
FROM ubuntu:22.04

# Update packages and install all the necessary tools
# RUN apt-get update && apt-get install -y \
#     default-jdk \
#     maven \
#     gcc \
#     g++ \
#     python3 \
#     nodejs \
#     npm \
#     && rm -rf /var/lib/apt/lists/*

# Inside Dockerfile, change this line:
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
   
gcc \
    g++ \
    python3 \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /app

# Copy the entire project to the container
COPY .
/app

# Run Maven to build the application's JAR file and copy dependencies
RUN mvn clean package -DskipTests
RUN mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Define the command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "/app/target/javabackend-0.0.1-SNAPSHOT.jar"]