package benchmarking.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface SumCallback {
	Event<SumCallback> EVENT = EventFactory.createArrayBacked(SumCallback.class, t -> i -> {
		int sum = 0;
		for (SumCallback callback : t) {
			sum += callback.invoke(sum);
		}
		return sum;
	});

	int invoke(int sum);
}
