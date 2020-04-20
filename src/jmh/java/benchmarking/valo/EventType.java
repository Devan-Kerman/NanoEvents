package benchmarking.valo;

import com.google.common.collect.Lists;
import net.devtech.nanoevents.util.Id;
import java.util.List;

public abstract class EventType<T> {
	private final Id name;
	private final List<T> toAdd = Lists.newArrayList();
	private boolean flag = false;

	public EventType(String name) {
		this(new Id(name));
	}

	public final List<T> subscribers = Lists.newArrayList();

	public EventType(Id name) {
		this.name = name;

		EventsImpl.register(name, this);
	}

	/**
	 * Call this at the beginning of your post method
	 */
	protected void updateEventSubscribers() {
		if (this.flag) {
			this.subscribers.addAll(this.toAdd);
			this.toAdd.clear();
			this.flag = false;
		}
	}

	public void addEventSubscriber(T subscriber) {
		this.flag = true;
		this.toAdd.add(subscriber);
	}

	public Id getId() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name.toString();
	}

	public static EventType<?> getTypeForId(Id id) {
		return EventsImpl.getEventType(id);
	}
}