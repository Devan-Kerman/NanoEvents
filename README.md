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

## Dynamic Registries
Dynamic registration is bad for a number of reasons.
1) iteration is needed to invoke all the listeners, rather than a flattened class. Technically you could just generate more and more classes to wrap the invoker though.
2) Inability to simply not call the event if there are no listeners attached, there are ways of optimizing this of course, and the costs are very very small, but still non-zero

# Flaws
Everything has a trade-off, not everything is perfect, so what are the disadvantages of this system
1) increased startup times: the penalty isn't too bad, and nothing like jar scanning, but there is a slight load time penalty nontheless.
2) no dynamic registry: sometimes you need dynamic registry, but NanoEvents forbids it in the name of true 0 overhead. However that's not to say you can use NanoEvents **in conjunction** with dynamic events, you can just listen to the event on a statically declared
listener, and rethrow it in your event system of choice.

# Can we go EVEN FASTER???
*Yes*, it is theoretically possible to go ***EVEN FASTER***. When I said zero overhead, I *technically* wasn't lying since if the event isn't listened to, there really isn't any overhead, but there is 2 possible optimizations you could do to make it even faster.

## Inlining
You could aggressivly inline the listener methods on the mixins that invoke it, and inline the listener methods as well. However I suspect the JVM will already do this, as the depth is only 3, and they're all static methods, so it should have no trouble inlining these methods.

## Initial Conditions
When there is only 1 listener for an event, there are cases where you will check an extra condition that did not need checking,
take the following standard implementation of a cancellable event as an example:
```java
public boolean invoker() {
  boolean isCancelled = false;
  Logic.start();
  if(!isCancelled) {
    isCancelled = invoker();
  }
  Logic.end();
}
```
This will be the transformed invoker when there is only 1 listener
```java
public boolean invoker() {
  boolean isCancelled = false;
  if(!isCancelled) {
    isCancelled = myListener();
  }
  return isCancelled;
}
```
You can tell what the problem is here, much of that code is redundant, and can be simplified into
```java
public boolean invoker() {
  return myListener();
}
```
There's a few ways of allowing this to happen, like add an extra optional invoker for when there's only one listener, but I didn't bother
