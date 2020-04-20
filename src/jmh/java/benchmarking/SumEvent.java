package benchmarking;

import benchmarking.fabric.SumCallback;
import io.github.microevents.events.Event;
import tk.valoeghese.shuttle.api.event.Context;
import tk.valoeghese.shuttle.api.event.ShuttleEventListener;

public class SumEvent implements Event, Context<SumEvent.SumCallbackListener> {
	public interface SumCallbackListener extends ShuttleEventListener {
		int sum(int i);

		@Override
		default String pluginId() {return "urmom";}
	}

	private int sum;

	public SumEvent(int sum) {
		this.sum = sum;
	}

	public int getSum() {
		return this.sum;
	}

	public void setSum(int sum) {
		this.sum = sum;
	}
}
