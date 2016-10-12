package uk.co.optimisticpanda.configtaskchain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.reflect.TypeToken;

public interface Node {

	public static class Root {
		private final List<Node> children = new ArrayList<>();

		Root(List<Node> nodes){
			this.children.addAll(children);
		}
		
		public List<Node> getChildren() {
			return children;
		}
	}

	public static interface Task extends Node {
		Optional<Connector> getNext();
	}
	
	public static class Connector implements Node {
		private final boolean ignoreIfFailed;
		private final Integer retryCount;
		private final Task task;

		Connector(boolean ignoreIfFailed, Integer retryCount, Task task) {
			super();
			this.ignoreIfFailed = ignoreIfFailed;
			this.retryCount = retryCount;
			this.task = task;
		}

		public Optional<Integer> getRetryCount() {
			return Optional.ofNullable(retryCount);
		}

		public boolean isIgnoreIfFailed() {
			return ignoreIfFailed;
		}
		
		public Task getTask() {
			return task;
		}
	}
	
	public static class StandaloneTask<O> implements Task {
		private StandAloneTaskKey<O> standAloneTaskKey;
		private final Connector next;
		
		StandaloneTask(StandAloneTaskKey<O> standAloneTaskKey, Connector next) {
			this.standAloneTaskKey = standAloneTaskKey;
			this.next = next;
		}

		public StandAloneTaskKey<O> getStandAloneTaskKey() {
			return standAloneTaskKey;
		}
		
		@Override
		public Optional<Connector> getNext() {
			return Optional.ofNullable(next);
		}
	}
	
	public static class FollowOnTask<I, O> implements Task {
		private DependentTaskKey<I, O> dependentTaskKey;
		private Connector next;
		
		FollowOnTask(DependentTaskKey<I, O> dependentTaskKey, Connector next) {
			this.dependentTaskKey = dependentTaskKey;
			this.next = next;
		}

		public DependentTaskKey<I, O> getDependentTaskKey() {
			return dependentTaskKey;
		}
		
		public Optional<Connector> getNext() {
			return Optional.ofNullable(next);
		}
	}

	public static class Multiplexer implements Task {
		private TypeToken<?> input;
		private List<Node> children;

		Multiplexer(TypeToken<?> input, List<Node> children) {
			this.input = input;
			this.children = children;
		}
		
		public TypeToken<?> getIntput() {
			return input;
		}

		public List<Node> getChildren() {
			return children;
		}

		@Override
		public Optional<Connector> getNext() {
			return children.stream().filter(Connector.class::isInstance).map(Connector.class::cast).findFirst();
		}

	}

}
