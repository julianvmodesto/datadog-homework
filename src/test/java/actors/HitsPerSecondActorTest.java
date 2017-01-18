package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class HitsPerSecondActorTest {
    private ActorSystem system;
    private Materializer materializer;

    @Before
    public void setup() {
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);
    }

    @After
    public void tearDown() throws Exception {
        TestKit.shutdownActorSystem(
                system,
                FiniteDuration.create(10, TimeUnit.SECONDS),
                true);
        system = null;
        materializer = null;
    }

    @Test
    public void canAlertOnPassThreshold() throws Exception {
        double threshold = 2d;
        TestActorRef<HitsPerSecondActor> hitsPerSecondActorRef = TestActorRef.create(system,
                Props.create(HitsPerSecondActor.class, threshold));
        HitsPerSecondActor hitsPerSecondActor = hitsPerSecondActorRef.underlyingActor();

        // initially, there's no alert
        assertFalse(hitsPerSecondActor.alert);

        // simulate 1 hit/second
        hitsPerSecondActorRef.tell(new HitsPerSecondActor.AddHit(), ActorRef.noSender());
        hitsPerSecondActorRef.tell(new HitsPerSecondActor.CalculateHitsPerSecond(), ActorRef.noSender());
        assertFalse(hitsPerSecondActor.alert);

        // simulate 4 hits/second, which results in 2.5 hits/second on average
        hitsPerSecondActorRef.tell(new HitsPerSecondActor.AddHit(), ActorRef.noSender());
        hitsPerSecondActorRef.tell(new HitsPerSecondActor.AddHit(), ActorRef.noSender());
        hitsPerSecondActorRef.tell(new HitsPerSecondActor.AddHit(), ActorRef.noSender());
        hitsPerSecondActorRef.tell(new HitsPerSecondActor.AddHit(), ActorRef.noSender());
        hitsPerSecondActorRef.tell(new HitsPerSecondActor.CalculateHitsPerSecond(), ActorRef.noSender());
        assertTrue(hitsPerSecondActor.alert);
    }
}