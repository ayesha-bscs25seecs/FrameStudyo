package com.framestudyo.framestudyo;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


;

public class SessionTracker {

    // ── Singleton ──────────────────────────────────────────────────────────

    private static SessionTracker instance;

    public static SessionTracker getInstance() {
        if (instance == null) instance = new SessionTracker();
        return instance;
    }

    private SessionTracker() {}

    // ── State ──────────────────────────────────────────────────────────────

    private final List<ActionEntry> history = new ArrayList<>();
    private LocalDateTime sessionStart;
    private long accumulatedPausedMs = 0; // total ms spent paused
    private long pauseStartMs = -1;       // wall-clock ms when current pause began
  //  private boolean running = false;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ── Session control ────────────────────────────────────────────────────

    public void startSession() {
        sessionStart        = LocalDateTime.now();
        accumulatedPausedMs = 0;
        pauseStartMs        = -1;
        //running             = true;
        history.clear();
        logAction("Session started");
    }

   /* public void pauseSession() {
       // if (running) {
            pauseStartMs = System.currentTimeMillis();
            running = false;
        }
    }*/

   /* public void resumeSession() {
        if (!running && pauseStartMs >= 0) {
            accumulatedPausedMs += System.currentTimeMillis() - pauseStartMs;
            pauseStartMs = -1;
            running = true;
            logAction("Returned to canvas");
        }
    }
*/
    /**
     * Returns total ACTIVE milliseconds (wall time minus all paused time).
     * Works correctly whether the session is currently running or paused.
     */
    public long elapsedMs() {

        if (sessionStart == null)
            return 0;

        return Duration.between(
                sessionStart,
                LocalDateTime.now()
        ).toMillis();
    }

    // ── Action logging ─────────────────────────────────────────────────────

    public void logAction(String description) {
        history.add(new ActionEntry(LocalDateTime.now(), description));
    }

    /** Returns the full action log (oldest first). */
    public List<ActionEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** Returns the last n actions (oldest first). */
    public List<ActionEntry> getRecentHistory(int n) {
        int from = Math.max(0, history.size() - n);
        return Collections.unmodifiableList(history.subList(from, history.size()));
    }

    // ── Statistics ─────────────────────────────────────────────────────────

    public SessionStats computeStats(DesignCanvas designCanvas) {
        List<DesignElement> elements = designCanvas.getElements();

        // Shape type counts
        Map<String, Long> shapeCounts = elements.stream()
                .collect(Collectors.groupingBy(
                        el -> el.getClass().getSimpleName().replace("Element", ""),
                        Collectors.counting()
                ));

        // Color frequency
        Map<String, Long> colorCounts = elements.stream()
                .filter(el -> el.getFillColor() != null
                        && !el.getFillColor().isEmpty()
                        && !el.getFillColor().equalsIgnoreCase("transparent"))
                .collect(Collectors.groupingBy(
                        el -> el.getFillColor().toUpperCase(),
                        Collectors.counting()
                ));

        String mostUsedColor = colorCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // Canvas coverage
        double totalArea = elements.stream()
                .mapToDouble(el -> el.getWidth() * el.getHeight())
                .sum();
        double coveragePct = Math.min(100.0, (totalArea / (860.0 * 600.0)) * 100.0);

        // Format elapsed time
        long elapsed = elapsedMs();
        long minutes = elapsed / 60000;
        long seconds = (elapsed % 60000) / 1000;
        String timeStr = minutes + "m " + seconds + "s";

        String startStr = (sessionStart != null)
                ? sessionStart.format(DATE_FMT) : "Not started";

        return new SessionStats(
                elements.size(),
                shapeCounts,
                colorCounts,
                mostUsedColor,
                coveragePct,
                history.size(),
                timeStr,
                startStr
        );
    }

    // ── Inner classes ──────────────────────────────────────────────────────

    public static class ActionEntry {
        private final LocalDateTime timestamp;
        private final String description;

        public ActionEntry(LocalDateTime timestamp, String description) {
            this.timestamp   = timestamp;
            this.description = description;
        }

        public String getDescription()      { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "[" + timestamp.format(FMT) + "]  " + description;
        }
    }

    public static class SessionStats {
        public final int totalElements;
        public final Map<String, Long> shapeCounts;
        public final Map<String, Long> colorCounts;
        public final String mostUsedColor;
        public final double coveragePercent;
        public final int totalActions;
        public final String sessionTime;
        public final String sessionStart;

        public SessionStats(int totalElements,
                            Map<String, Long> shapeCounts,
                            Map<String, Long> colorCounts,
                            String mostUsedColor,
                            double coveragePercent,
                            int totalActions,
                            String sessionTime,
                            String sessionStart) {
            this.totalElements   = totalElements;
            this.shapeCounts     = shapeCounts;
            this.colorCounts     = colorCounts;
            this.mostUsedColor   = mostUsedColor;
            this.coveragePercent = coveragePercent;
            this.totalActions    = totalActions;
            this.sessionTime     = sessionTime;
            this.sessionStart    = sessionStart;
        }
    }
}