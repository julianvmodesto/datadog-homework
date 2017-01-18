package actors;

import akka.actor.UntypedActor;
import com.google.common.collect.EvictingQueue;

import java.time.LocalDateTime;
import java.util.Queue;


public class HitsPerSecondActor extends UntypedActor {

    private double hitsPerSecond = 0;

    // Here, we have a queue that's of fixed size of 120 elements.
    private Queue<Double> hitsPerSecondForLastTwoMinutes = EvictingQueue.create(60 * 2);
    // We'll take samples of hits/second every second, so when we add a new hits/second to our queue,
    // then the oldest is evicted and we can maintain our list of hits/second over exactly 2 minutes or 120 seconds.

    boolean alert = false;
    private double alertThreshold = 0d;

    public HitsPerSecondActor(double alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    // Actor Message Protocol
    public static class AddHit {}
    public static class CalculateHitsPerSecond {}

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof AddHit) {
            hitsPerSecond++;
        } else if (message instanceof CalculateHitsPerSecond) {
            // Every second, we'll calculate hits per second
            hitsPerSecondForLastTwoMinutes.add(hitsPerSecond);
            hitsPerSecond = 0d;

            // We'll then calculate average hits per second for the last two minutes
            double averageHitsPerTwoMinutes = hitsPerSecondForLastTwoMinutes
                    .stream()
                    .mapToDouble(x -> x)
                    .average()
                    .getAsDouble();

            if (!alert && averageHitsPerTwoMinutes > alertThreshold) {
                alert = true;
                System.out.printf("[%s] High traffic generated an alert - { hits = %s, triggered at %s}%n",
                        LocalDateTime.now(),
                        averageHitsPerTwoMinutes,
                        LocalDateTime.now());
            } else if (alert && averageHitsPerTwoMinutes < alertThreshold) {
                alert = false;
                System.out.printf("[%s] High traffic alert recovered - { hits = %s, recovered at %s}%n",
                        LocalDateTime.now(),
                        averageHitsPerTwoMinutes,
                        LocalDateTime.now().toString());
            }
        }
    }
}
