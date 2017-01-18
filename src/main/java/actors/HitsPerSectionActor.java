package actors;

import akka.actor.UntypedActor;
import com.google.common.collect.Multisets;
import com.google.common.collect.TreeMultiset;

import java.time.LocalDateTime;
import java.util.List;

public class HitsPerSectionActor extends UntypedActor {

    // We'll add sections to this set, and count them later.
    private TreeMultiset<String> hitsPerSection = TreeMultiset.create();
    private static final int TOP_N = 5;

    // Actor Message Protocol
    public static class AddHit {
        String section;

        public AddHit(String section) {
            this.section = section;
        }
    }

    public static class CalculateTopNSections {
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof AddHit) {
            String section = ((AddHit) message).section;
            hitsPerSection.add(section);
        } else if (message instanceof CalculateTopNSections) {
            List<String> topSections = Multisets.copyHighestCountFirst(hitsPerSection)
                    .elementSet()
                    .asList();
            int n = topSections.size() >= TOP_N ? TOP_N : topSections.size();
            if (n > 0) {
                System.out.printf("[%s] Top Hits by Section:%n", LocalDateTime.now());
                topSections
                        .subList(0, n)
                        .forEach(section -> {
                            System.out.printf("%s\t%s%n", hitsPerSection.count(section), section);
                        });
                System.out.printf("[%s] Rarest Hits by Section:%n", LocalDateTime.now());
                topSections
                        .subList(topSections.size() - n, topSections.size())
                        .forEach(section -> {
                            System.out.printf("%s\t%s%n", hitsPerSection.count(section), section);
                        });
            } else {
                System.out.printf("[%s] No hits logged yet%n", LocalDateTime.now());
            }
        }
    }
}
