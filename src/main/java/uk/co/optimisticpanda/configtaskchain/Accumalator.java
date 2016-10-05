package uk.co.optimisticpanda.configtaskchain;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Accumalator<T> implements Iterable<T> {
	private final List<T> vals = new ArrayList<>();

	public Accumalator<T> add(T val) {
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
		Accumalator<?> other = (Accumalator<?>) obj;
		return Objects.equals(vals, other.vals);
	}

	@Override
	public String toString() {
		return "Gathered objects: " + vals.stream()
				.map(Object::toString)
				.collect(joining("\n\t* ", "\n\t* ", ""));
	}
}