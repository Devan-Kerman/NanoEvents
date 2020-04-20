package benchmarking;

public class SumEvent /*implements Event, Context<SumEvent.SumCallbackListener>*/ {
	/*public interface SumCallbackListener extends ShuttleEventListener {
		int sum(int i);

		@Override
		default String pluginId() {return "urmom";}
	}
*/
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
