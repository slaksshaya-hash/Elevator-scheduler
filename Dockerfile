FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY ElevatorScheduler.java ElevatorWebServer.java ./
COPY public ./public
RUN javac ElevatorScheduler.java ElevatorWebServer.java
ENV PORT=8080
EXPOSE 8080
CMD ["java", "ElevatorWebServer"]
