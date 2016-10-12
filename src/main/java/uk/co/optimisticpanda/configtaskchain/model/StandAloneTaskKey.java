package uk.co.optimisticpanda.configtaskchain.model;

import java.util.Objects;

import com.google.common.reflect.TypeToken;

public class StandAloneTaskKey<OUTPUT> {

	private final String identifier;
	private final TypeToken<OUTPUT> output;

	public StandAloneTaskKey(String identifier, TypeToken<OUTPUT> output) {
		this.identifier = identifier;
		this.output = output;
	}

	public String getIdentifier() {
		return identifier;
	}

	public TypeToken<OUTPUT> getOutput() {
		return output;
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, output);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandAloneTaskKey<?> other = (StandAloneTaskKey<?>) obj;
		return Objects.equals(identifier, other.identifier)
				& Objects.equals(output, other.output);
	}
}