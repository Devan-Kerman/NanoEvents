package benchmarking.dynamic;

import java.util.function.IntUnaryOperator;

// an example dynamically generated class
public class NanoDynamicInstance {
	private static final IntUnaryOperator ONE = i -> i + 1;
	private static final IntUnaryOperator TWO = i -> i + 2;
	private static final IntUnaryOperator THREE = i -> i + 3;
	public static NanoDynamicInstance instance;

	public static int invoker(int sum) {
		// the invoker method would be transformed to be this, so that the instance could be replaced at any time
		return instance.invoke(sum);
	}

	public int invoke(int sum) {
		sum += ONE.applyAsInt(sum);
		sum += TWO.applyAsInt(sum);
		sum += THREE.applyAsInt(sum);
		return sum;
	}
}
