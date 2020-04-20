# NanoEvents
no overhead event based programming for the fabric minecraft modding toolchain

Keep in mind this tool isn't here to be safe, it's here to be fast. There are all sorts of nonsense that comes with cutting and pasting
abitrary bytecode.

# Why?
Mostly a proof of concept, but it is ready for use, and I intend to use it in the future. And sometimes events are geniunly performance critical.

# How is this possible
There's a few things here that are unique to minecraft in a way, [Mixin](https://github.com/SpongePowered/Mixin) is a bytecode transformation framework that abstracts bytecode changes in a way that's simple and easy to understand, and by using ASM based hooks, we can enable and disable them at runtime, whether or not there are listeners for our events. Mixin is platform independant, so in theory it could run anywhere, it's just most widely used in minecraft. Secondly, we can generate classes ahead of time to **directly** call our listener methods, reducing overhead even further.

# Comparisons
[MicroEvents](https://github.com/WoolMC/MicroEvents) this is another library I made, however this one was designed with
dynamic registry in mind, it's not perfect, and having a centralised registry is a bad idea, but it is fairly quick, and I intend
on using this alongside NanoEvents when dynamic registry is needed. This has the same problem as all dynamic event registries. Centralised registries are apis where all events and listeners are registed to a single class, and when the event is called, the
manager finds all the listeners accociated with an event (often with a hashmap) and invokes them, it should be fairly obvious why this
is a bad idea.

[Fabric API Callbacks](https://github.com/FabricMC/fabric) This is similar to microevents, I'm not sure how much optimization exists in the backend as I haven't really looked into it, but it has the advantage of having non-centralised registries, which makes events significantly faster and easier to use. This also has the same problem as all dynamic event registries.

## Benchmarks
```
Benchmark           Mode  Cnt          Score           Error  Units
Bench.direct       thrpt    5  477800018.217 � 126553198.981  ops/s // directly calling and manual inlining (static)
Bench.fabric       thrpt    5   59083167.578 �   3819690.352  ops/s // fabric api callbacks (dynamic)
Bench.invoker      thrpt    5  528689833.588 �  73864223.779  ops/s // nano events (static)
Bench.microEvents  thrpt    5   12970110.503 �    394414.314  ops/s // micro events (dynamic)
Bench.shuttle      thrpt    5   41303416.170 �   1550479.125  ops/s // shuttle event api (dynamic)
Bench.valo         thrpt    5   23667336.627 �     94967.188  ops/s // mcalphadev events (dynamic)
```
Notes:
### Dynamic Registries
Dynamic registration is bad for a number of reasons.
1) iteration is needed to invoke all the listeners, rather than a flattened class. Technically you could just generate more and more classes to wrap the invoker though.
2) Inability to simply not call the event if there are no listeners attached, there are ways of optimizing this of course, and the costs are very very small, but still non-zero
### NanoEvents and Direct are roughly equal
This is because the JVM is able to inline nanoevent handlers. This is the real meaning of "no overhead", once the JVM gets to it
there is virtually no difference between direct invocation and nanoevent handlers
### MicroEvents is slow
yea, I made MicroEvents, it's slow because it uses reflection to search for subclass listeners of the event

# Flaws
Everything has a trade-off, not everything is perfect, so what are the disadvantages of this system
1) increased startup times: the penalty isn't too bad, and nothing like jar scanning, but there is a slight load time penalty nontheless.
2) no dynamic registry: sometimes you need dynamic registry, but NanoEvents forbids it in the name of true 0 overhead. However that's not to say you can use NanoEvents **in conjunction** with dynamic events, you can just listen to the event on a statically declared
listener, and rethrow it in your event system of choice.
