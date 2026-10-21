# Intelligent Elevator Scheduler

A browser-based Operating Systems simulation that visualizes and compares elevator scheduling algorithms in real time.

**Live Demo:** https://elevator-scheduler.onrender.com/  
**GitHub Repository:** https://github.com/slaksshaya-hash/Elevator-scheduler

## Overview

The Intelligent Elevator Scheduler demonstrates how different scheduling algorithms handle elevator floor requests. It provides an interactive web dashboard with an animated elevator, live movement statistics, a scheduled route, an activity log, and an algorithm comparison table.

The project uses the original Java scheduling logic with a browser-based interface served by a lightweight Java HTTP server.

## Features

- **Interactive dashboard:** Select a starting floor and add floor requests.
- **Four scheduling algorithms:** FCFS, SSTF, SCAN, and LOOK.
- **Live elevator animation:** Visualize the elevator moving between floors.
- **Movement statistics:** Track total movement, requests served, and average movement per stop.
- **Algorithm comparison:** Compare routes and movement totals for the same set of requests.
- **Activity log:** View timestamped simulation events.
- **Reset controls:** Reset the simulation and test new scenarios.

## Technologies Used

- Java
- Java HTTP Server
- HTML, CSS, and JavaScript
- JSON-based HTTP API
- Docker
- Render for deployment
- GitHub for version control

## Scheduling Algorithms

| Algorithm | Description |
|---|---|
| FCFS | Serves requests in the order they arrive. |
| SSTF | Chooses the nearest pending request. |
| SCAN | Moves in one direction before reversing at the configured boundary. |
| LOOK | Moves in one direction before reversing at the last request in that direction. |

## Run Locally

Requirements: JDK 21 or newer.

Open PowerShell in the project directory and run:

```powershell
javac ElevatorScheduler.java ElevatorWebServer.java
java ElevatorWebServer
```

Then open http://localhost:8080 in your browser.

Press `Ctrl+C` in the terminal to stop the server.

## API Endpoints

### Schedule requests

`POST /api/schedule`

Example request body:

```json
{
  "startFloor": 0,
  "algorithm": "SSTF",
  "requests": [2, 8, 5]
}
```

### Compare algorithms

`POST /api/compare`

Example request body:

```json
{
  "startFloor": 0,
  "requests": [2, 8, 5]
}
```

Both endpoints are served by the Java backend.

## Deployment

The application is containerized with Docker and deployed on Render.

**Live application:** https://elevator-scheduler.onrender.com/

The free hosting instance may take a short time to wake up after a period of inactivity.

## Project Purpose

This project demonstrates Operating Systems scheduling concepts through an interactive simulation, making it easier to understand and compare the movement efficiency of different algorithms.

---

Developed as an Operating Systems simulation project.
