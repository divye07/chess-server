# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and project files
COPY . .

# Make sure mvnw is executable
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package

# Expose the port your app runs on (default 5000, can be overridden by PORT env)
EXPOSE 5000

# Run the Jetty WebSocket server
CMD ["java", "-cp", "target/classes:target/dependency/*", "com.chess.network.JettyWebSocketServer"] 