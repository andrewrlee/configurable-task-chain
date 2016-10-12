package uk.co.optimisticpanda.configtaskchain.model;

import java.util.Objects;

import com.google.common.reflect.TypeToken;

public class DependentTaskKey<INPUT, OUTPUT> {

	private final String identifier;
	private final TypeToken<INPUT> input;
	private final TypeToken<OUTPUT> output;

	public static <I, O> DependentTaskKey<I, O> of(String identifier, TypeToken<I> input, TypeToken<O> output){
		return new DependentTaskKey<I, O>(identifier, input, output);
	}
	
	public static <I> DependentTaskKey<I, I> unaryTaskKey(String identifier, TypeToken<I> inputAndOutput){
		return new DependentTaskKey<I, I>(identifier, inputAndOutput, inputAndOutput);
	}
	
	private DependentTaskKey(String identifier, TypeToken<INPUT> input, TypeToken<OUTPUT> output) {
		this.identifier = identifier;
		this.input = input;
		this.output = output;
	}

	public String getIdentifier() {
		return identifier;
	}

	public TypeToken<INPUT> getInput() {
		return input;
	}

	public TypeToken<OUTPUT> getOutput() {
		return output;
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, input, output);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DependentTaskKey<?,?> other = (DependentTaskKey<?,?>) obj;
		return Objects.equals(identifier, other.identifier)
				& Objects.equals(input, other.input)
				& Objects.equals(output, other.output);
	}
}