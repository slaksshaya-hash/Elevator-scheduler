# Intelligent Elevator Scheduler — Web Edition

This edition keeps the original `ElevatorScheduler.java` algorithms and replaces the Swing-only interface with a browser dashboard. The Java HTTP server serves the frontend and exposes JSON endpoints for scheduling and comparison.

## Run locally (Windows PowerShell)

Install a JDK (Java 21 or newer), then open PowerShell in this folder and run:

```powershell
javac ElevatorScheduler.java ElevatorWebServer.java
java ElevatorWebServer
```

Open http://localhost:8080 in your browser. Stop the server with Ctrl+C.

## API

- `POST /api/schedule` body: `{"startFloor":0,"algorithm":"SSTF","requests":[2,8,5]}`
- `POST /api/compare` body: `{"startFloor":0,"requests":[2,8,5]}`

The web app and API are served from the same origin, so no separate frontend host or CORS setup is required.

## Deploy online with Render (Docker)

1. Create a GitHub repository and upload the contents of this folder (not the ZIP file itself).
2. In Render, choose **New → Web Service** and connect that repository.
3. Choose **Docker** as the runtime/environment. Render will build using the included `Dockerfile`.
4. Create the service. The app reads Render's `PORT` environment variable automatically.
5. Once deployed, open the public `https://<your-service>.onrender.com` URL and test scheduling and comparison.

The public URL is shareable with your professor. On Render's free instance, the service may sleep when idle and take a short time to wake up. Do not put secrets in this public project.
