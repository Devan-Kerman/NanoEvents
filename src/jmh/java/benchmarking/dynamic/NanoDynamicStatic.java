package benchmarking.dynamic;

import benchmarking.nano.NanoListeners;

public class NanoDynamicStatic {
	public static NanoDynamicStatic instance;

	public static int invoker(int sum) {
		// the invoker method would be transformed to be this, so that the instance could be replaced at any time
		return instance.invoke(sum);
	}

	public int invoke(int sum) {
		sum += NanoListeners.three(sum);
		sum += NanoListeners.two(sum);
		sum += NanoListeners.one(sum);
		return sum;
	}
}
