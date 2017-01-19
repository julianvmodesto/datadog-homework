# Design
I'm pretty fluent in Java, so I started here. I also chose to use actors for concurrency
because I like the model and I'm comfortable with it. Unfortunately, creating Java cli utilities
isn't simple + straight forward, so I definitely have some things I'd
like to improve.

First, Java's standard library sucks. I imported an actor library for
concurrency, a cli flag parsing library, a test library, and even a collections
library with a bit nicer data structures (Guava). I would love to improve by using the
standard library further, or even better, trying this exercise with a language with a
great standard library and light-weight concurrency primitives i.e. Golang.

Second, I used some higher-level data structures to calculate average hits/second
and top seconds. With a bit more work, I could eliminate my dependency on the
Google Guava library.

For average hits/second, I wanted a data structure which would store 120 values
to represent hits/second across 120 seconds. Instead of using a
`com.google.common.collect.EvictingQueue` I could use a normal
`java.util.LinkedList` by popping off the first element once reaching 120
capacity. Further, instead of calculating the average for this Queue every time
([see here](https://github.com/julianvmodesto/httplogmon/blob/master/src/main/java/actors/HitsPerSecondActor.java#L40-L44)),
I could keep an updated running sum and divide by the Queue size
[to calculate the simple moving average](https://en.wikipedia.org/wiki/Moving_average#Simple_moving_average).

![simple moving average](https://wikimedia.org/api/rest_v1/media/math/render/svg/5e1a8ec9b813571be2b12dfa518c8f3b368b3184)

For top section counts, instead of using
`com.google.common.collect.TreeMultiset`
([see here](https://github.com/julianvmodesto/httplogmon/blob/master/src/main/java/actors/HitsPerSectionActor.java#L34-L36)),
I could use both a
`java.util.HashMap<String, Integer>` (to keep section counts) & a
PriorityQueue of the top N sections (that's updated with each section count update).

