import actors.HitsPerSecondActor;
import actors.HitsPerSectionActor;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.contrib.FileTailSource;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import org.apache.commons.cli.*;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class HttpLogMonitor {

    // Command line binary name
    private static final String CLI_BINARY_NAME = "httplogmon";

    public static void main(String[] args) {
        // Set up command line parser
        Options options = new Options();
        options.addOption("f", "file", true, "File path to the HTTP log");
        options.addOption("t", "threshold", true, "Alerting threshold average hits/second over 2 minutes");
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        // Parse args
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) {
            formatter.printHelp(CLI_BINARY_NAME, options);
            System.exit(2);
        }
        String filename = cmd.getOptionValue("file");
        String thresholdOption = cmd.getOptionValue("threshold");

        // Do some file checking
        if (filename == null) {
            formatter.printHelp(CLI_BINARY_NAME, options);
            System.exit(2);
        }
        Path filepath = Paths.get(filename);
        if (!Files.exists(filepath)) {
            System.out.printf("File doesn't exist: %s%n", filename);
            System.exit(2);
        } else if (!Files.isRegularFile(filepath)) {
            System.out.printf("Not a file: %s%n", filename);
            System.exit(2);
        } else if (!Files.isReadable(filepath)) {
            System.out.printf("Insufficient read privileges for: %s%n", filename);
            System.exit(2);
        }
        long filesize = 0;
        try {
            filesize = Files.size(filepath);
        } catch (IOException e) {
            System.out.println("Couldn't read file");
            System.exit(2);
        }

        double threshold = 0;
        if (thresholdOption == null) {
            System.out.printf("Using default alert threshold: %s%n", threshold);
        } else {
            try {
                threshold = Double.parseDouble(thresholdOption);
                System.out.printf("Using alert threshold: %s%n", threshold);
            } catch (NumberFormatException nfe) {
                System.out.println("Expected threshold to be a number");
                System.exit(2);
            }
        }

        // We'll use actors for our concurrency model, so let's set up Akka actors
        ActorSystem system = ActorSystem.create();
        Materializer materializer = ActorMaterializer.create(system);
        ActorRef hitsPerSecondActor = system.actorOf(Props.create(HitsPerSecondActor.class, threshold));
        ActorRef hitsPerSectionActor = system.actorOf(Props.create(HitsPerSectionActor.class));

        // Set up scheduled jobs.
        // Calculate hits per second every 1 second.
        system.scheduler().schedule(
                Duration.Zero(),
                Duration.create(1, TimeUnit.SECONDS),
                hitsPerSecondActor,
                new HitsPerSecondActor.CalculateHitsPerSecond(),
                system.dispatcher(),
                ActorRef.noSender());
        // Calculate hits per section every 10 seconds.
        system.scheduler().schedule(
                Duration.Zero(),
                Duration.create(10, TimeUnit.SECONDS),
                hitsPerSectionActor,
                new HitsPerSectionActor.CalculateTopNSections(),
                system.dispatcher(),
                ActorRef.noSender());

        // Start watching file
        System.out.printf("Watching file: %s%n", filename);
        Source<ByteString, NotUsed> source = FileTailSource.create(
                filepath,
                8192, // chunk size
                filesize, // starting position at end
                FiniteDuration.create(250, TimeUnit.MILLISECONDS)); // poll file every 250ms
        source.via(Framing.delimiter(ByteString.fromString("\n"), 8192))
                .map(ByteString::utf8String)
                .runForeach(line -> {
                    hitsPerSecondActor.tell(new HitsPerSecondActor.AddHit(), ActorRef.noSender());

                    HttpLogMatcher.getSection(line)
                            .ifPresent(section -> hitsPerSectionActor.tell(
                                    new HitsPerSectionActor.AddHit(section), ActorRef.noSender()));
                }, materializer);

    }
}
