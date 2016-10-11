package uk.co.optimisticpanda.configtaskchain;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Accumulator<T> implements Iterable<T> {
	private final List<T> vals = new ArrayList<>();

	public Accumulator<T> add(T val) {
		if (val instanceof Accumulator) {
			vals.addAll(((Accumulator<T>) val).getVals());
		}
		vals.add(val);
		return this;
	}

	public List<T> getVals() {
		return vals;
	}

	@Override
	public Iterator<T> iterator() {
		return vals.iterator();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(vals);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Accumulator<?> other = (Accumulator<?>) obj;
		return Objects.equals(vals, other.vals);
	}

	@Override
	public String toString() {
		return "Gathered objects: " + vals.stream()
				.map(Object::toString)
				.collect(joining("\n\t* ", "\n\t* ", ""));
	}
}