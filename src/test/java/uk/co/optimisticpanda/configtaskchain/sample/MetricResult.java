package uk.co.optimisticpanda.configtaskchain.sample;

import java.util.function.BiFunction;

import com.google.common.base.Objects;

public class MetricResult<T> {
	
	public enum MetricType {
		AREA, HEIGHT, WIDTH, DURATION, WEIGHT,
	}

	private final MetricType type;
	private final T value;

	public MetricResult(MetricType type, T value) {
		this.type = type;
		this.value = value;
	}

	public <S, U> MetricResult<U> mergeWith(MetricResult<S> other, MetricType type, BiFunction<T, S, U> f) {
		return new MetricResult<>(type, f.apply(value, other.value));
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(type, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetricResult<?> other = (MetricResult<?>) obj;
		return Objects.equal(type, other.type) 
				&& Objects.equal(value, other.value);
	}

	@Override
	public String toString() {
		return "MetricResult [type=" + type + ", value=" + value + "]";
	}
}
