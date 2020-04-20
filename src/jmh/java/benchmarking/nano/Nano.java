package benchmarking.nano;

import net.devtech.nanoevents.api.Invoker;
import net.devtech.nanoevents.api.Logic;
import net.devtech.nanoevents.api.SingleInvoker;

public class Nano {
	@Invoker("nano:test")
	public static int invoke(int sum) {
		Logic.start();
		sum += invoke(sum);
		Logic.end();
		return sum;
	}

	@SingleInvoker("nano:test")
	private static int invokeSingle(int sum) {
		return invokeSingle(sum);
	}
}
