package benchmarking.valo;

import net.devtech.nanoevents.util.Id;
import java.util.HashMap;
import java.util.Map;

public final class EventsImpl {
	private static final Map<Id, EventType<?>> eventMap = new HashMap<>();

	public static void register(Id name, EventType<?> eventType) {
		eventMap.put(name, eventType);
	}

	public static EventType<?> getEventType(Id id) {
		return eventMap.get(id);
	}
}