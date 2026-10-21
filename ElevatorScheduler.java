
import java.util.*;

public class ElevatorScheduler {

    public static final int TOTAL_FLOORS = 10;

    public static class Result {
        public final List<Integer> stops;
        public final int totalMovement;
        public final double averageWaitingTime;
        public final double averageMovementBeforeStop;
        public final int requestsServed;

        public Result(List<Integer> stops, int startFloor) {
            this.stops = new ArrayList<>(stops);

            int position = startFloor;
            int movement = 0;
            int totalCumulativeMovement = 0;

            for (int floor : stops) {
                movement += Math.abs(floor - position);
                totalCumulativeMovement += movement;
                position = floor;
            }

            totalMovement = movement;
            requestsServed = stops.size();

            averageMovementBeforeStop = stops.isEmpty()
                    ? 0.0
                    : (double) totalCumulativeMovement / stops.size();

            // Kept for compatibility with older GUI code.
            averageWaitingTime = averageMovementBeforeStop;
        }
    }

    public Result schedule(int startFloor, List<Integer> requests,
                           String algorithm) {

        validateFloor(startFloor);

        if (requests == null) {
            throw new IllegalArgumentException(
                    "Requests cannot be null.");
        }

        if (algorithm == null || algorithm.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Choose a scheduling algorithm.");
        }

        List<Integer> pending = new ArrayList<>(requests);

        for (Integer floor : pending) {
            if (floor == null) {
                throw new IllegalArgumentException(
                        "A requested floor cannot be null.");
            }

            validateFloor(floor);
        }

        // Requests for the current floor need no movement.
        pending.removeIf(floor -> floor == startFloor);

        List<Integer> stops = new ArrayList<>();
        int current = startFloor;

        String selected = algorithm.trim().toUpperCase(Locale.ROOT);

        switch (selected) {

            case "FCFS":
                stops.addAll(pending);
                break;

            case "SSTF":
                while (!pending.isEmpty()) {
                    int nearest = pending.get(0);

                    for (int floor : pending) {
                        if (Math.abs(floor - current)
                                < Math.abs(nearest - current)) {
                            nearest = floor;
                        }
                    }

                    stops.add(nearest);
                    pending.remove(Integer.valueOf(nearest));
                    current = nearest;
                }
                break;

            case "SCAN":
                pending.sort(Integer::compareTo);

                boolean hasLowerRequest = false;

                for (int floor : pending) {
                    if (floor < current) {
                        hasLowerRequest = true;
                    }

                    if (floor >= current) {
                        stops.add(floor);
                    }
                }

                // Travel to the top boundary before reversing,
                // but do not add floor 9 twice.
                if (hasLowerRequest
                        && current < TOTAL_FLOORS - 1
                        && (stops.isEmpty()
                        || stops.get(stops.size() - 1)
                        != TOTAL_FLOORS - 1)) {
                    stops.add(TOTAL_FLOORS - 1);
                }

                for (int i = pending.size() - 1; i >= 0; i--) {
                    int floor = pending.get(i);

                    if (floor < current) {
                        stops.add(floor);
                    }
                }
                break;

            case "LOOK":
                pending.sort(Integer::compareTo);

                for (int floor : pending) {
                    if (floor >= current) {
                        stops.add(floor);
                    }
                }

                for (int i = pending.size() - 1; i >= 0; i--) {
                    int floor = pending.get(i);

                    if (floor < current) {
                        stops.add(floor);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown scheduling algorithm: " + algorithm);
        }

        return new Result(stops, startFloor);
    }

    private static void validateFloor(int floor) {
        if (floor < 0 || floor >= TOTAL_FLOORS) {
            throw new IllegalArgumentException(
                    "Floor must be between 0 and "
                            + (TOTAL_FLOORS - 1) + ".");
        }
    }
}
