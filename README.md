# NanoEvents
no overhead event based programming for the fabric minecraft modding toolchain

Powered by [🍫](https://github.com/Chocohead/Fabric-ASM)

## Gradle
```groovy
repositories {
    ...
    maven {
        url = 'https://raw.githubusercontent.com/Devan-Kerman/Devan-Repo/master/'
    }
    maven { 
        url 'https://jitpack.io' 
    }
}

dependencies {
    // nanoevents 1.3 and beyond now JiJ fabric-asm
    modImplementation 'net.devtech:NanoEvents:1.3'
    // if you intend on JIJing NanoEvents yourself
    include 'com.github.Chocohead:Fabric-ASM:v2.0.1'
}
```

## Docs
https://github.com/Devan-Kerman/NanoEvents/wiki

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

Here's a benchmark with just nanoevents (with dynamic registry), direct invocation, and fabric callbacks
```
Benchmark                           Mode  Cnt          Score          Error  Units
Bench.direct                       thrpt    5  535897233.207 �  4521146.041  ops/s
Bench.fabric_callbacks             thrpt    5   64520488.648 �  8075224.643  ops/s
Bench.nanoevents_dynamic_instance  thrpt    5  505367722.699 �  1908906.878  ops/s
Bench.nanoevents_dynamic_static    thrpt    5  502780134.882 � 16932542.119  ops/s
Bench.nanoevents_static            thrpt    5  535231948.958 �  1673582.059  ops/s
```
You'll notice that direct and all nanoevent methods nearly equal in overhead. As u can see, static registry and direct invocation
are quite literally equal, the difference is around <.01%. dynamic_instance means lambdas were put in `private static final` fields
and invoked via object invocation, and dynamic_static means static methods were invoked via object invocation. The difference between
static registry and dynamic registry is miniscule, and quite literally a micro-optimization. However there's one aspect of static
registries that set it apart, it's ability to disable mixins ahead of time, this sole advantage allows nanoevents to be theoretically
faster than direct invocation!

tl;dr Static registries are faster and better at the cost of usability, dynamic registries are far more common and easier to use.

*Keep an eye out for dynamic registries*

# Other notes about event apis
## Priorities
Priorities are terrible, *never* have a priority system in an event api
 - no guarentee of the outcome of an event, without post-events, there's no way of telling if the event will be cancelled by some other listener down the line
 - priority within priorities is not guarenteed, this means u cant solve the above problen by just registering your listener at the highest priority
 ## Event Objects
 I'm sure you've used an event system that uses event objects in the past, they're ubiquitus, and they suck.
 event objects are things like ThingEvent, where the data for the event is stored inside an object.
  - object allocation needed, slowing it down
  - it doesn't actually add any usability, infact it simply makes your code more verbose, rather than just having the parameters of the function define the event, you need to getX, getY etc. for each of the objects, and constructing the object to throw it in the first place is a hassle too
  - if the event system respects inherited listeners, then you're totally dead, now you need reflection to find the super classes of an event, adding immense overhead to your event system
## Return Type Handling
This is critical to an event system, but almost none of them have this. Only fabric callbacks and nanoevents have this feature.
The ability for the user (thrower of the event) to determine for themselves how to deal with return types and values.
Take for example a cancellable event, most apis have the cancellable event hardcoded, whether it be an interface or something else. In fabric you write the cancellable event logic yourself, which would look something like this
```java
for(Listener listener : listeners) {
    if(listener.accept(...)) {
        return;
    }
}
```
Say you want to implement an event where there are 3 possible states, and one of them stops the execution like a cancellable one.
this is stupid to implement in other apis, you'd save a cancellable boolean in the event, and set it to true when a value that should
cancel execution is set. A hacky workaround for something that shouldn't exist. The equivalent nanoevents and fabric callback code would
be much cleaner, and doesn't require the same work arounds.

### Re-Iteration
## Dynamic Registries
Dynamic registries require iteration, which is slow, however a dynamic registry is comparably much easier to use than a static one, so I wouldn't use it as a point of critism ~~until I add dynamic registry to nanoevents~~

## Centralized Registry
Centralized registries are things like bukkit events (bukkit) or eventbus (forge), both of these are flawed and although the benchmark does show them performing poorly, in a real world scenario when there are typically multiple types of events, sometimes reaching tripple digits, the cost skyrockets. The problem here is you need a hashmap to go from the event type to a list of listeners of some sort. This adds a layer of unessesary overhead that becomes worse when reflection is used. Centralized registries have the same/less usability as decentralized ones. They're often limited in what they can do (see: return type handling), and in both cases you need to reference some class or similar to register your events, for example forge uses typetools (concern) to determine the listener type, but you still need to declare the event object as a parameter, so why not just handle the event in the event itself? Infact some registries actually are *less* usable than decentralized ones, like bukkit, where you must declare the type and register it seperately.

# Flaws
Everything has a trade-off, not everything is perfect, so what are the disadvantages of this system
1) increased startup times: the penalty isn't too bad, and nothing like jar scanning, but there is a slight load time penalty nontheless.
2) no dynamic registry: sometimes you need dynamic registry, but NanoEvents forbids it in the name of true 0 overhead. However that's not to say you can use NanoEvents **in conjunction** with dynamic events, you can just listen to the event on a statically declared
listener, and rethrow it in your event system of choice.

## Future features
dynamic registry - it wouldn't be *as* fast as you couldn't disable mixins ahead of time, but it's fast enough for most people. sacrifice performance for ease of use
 
# In conclusion
It's quite easy to tell what a perfect api is now isn't it?
 - no overhead (must be as fast as direct invocation, using the JIT to your advantage isn't really cheating)
 - negative overhead (being able to disable from un-used mixins ahead of time)
 - decentralized (for usability)
 - no startup cost (almost impossible)
 
However, accomplishing such a task is, as far as I'm aware, impossible.

However it is possible to get closer:
1) posted-mixined class caching would reduce the startup cost, and make it almost 0, at that stage there is better places to optimize
2) using instrumentation for events instead of mixin, the beuty of instrumentation is that u can dynamically transform classes, and remove/add bytecode on demand. It has it's limits though, it can only be used when run with a JDK, it's a hassle to setup, and can't add methods/fields at runtime.
3) decentralized events are already happening with fabric callbacks and nanoevents, and I hope other platforms adopt them
4) In my opinion asm is the path for performance, it's one of java's greatest advantages in my eyes, the fact that u can dynamically create code and have it still be optimized is ridiculously powerful.
5) ahead-of-time dynamic events. Jar scanning is not an option for startup costs, however the jar can be scanned at build time to determine what events the user uses. This allows for dynamic registration while still having negative overhead. However that doesn't come at a cost either, the user has to install a gradle plugin or external program to post-process their jar. If a system is officially added to loom however, this con dissapears.

Most if not all of these are out of the scope of nanoevents, and would require work in fabric to become a reality.

So no, a perfect event api *isn't* possible, but we can get very, very close.
