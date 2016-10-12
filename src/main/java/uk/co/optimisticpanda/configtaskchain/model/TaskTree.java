package uk.co.optimisticpanda.configtaskchain.model;

import java.util.List;

import uk.co.optimisticpanda.configtaskchain.model.Node.Connector;
import uk.co.optimisticpanda.configtaskchain.model.Node.Task;

import com.google.common.reflect.TypeToken;

public class TaskTree extends Parent<TaskTree, Node> {

	private Node root;
	
	public static TaskTree create() {
		return new TaskTree();
	}

	public Node getRoot() {
		return root;
	}
	
	@Override
	protected TaskTree add(Node node) {
		this.root = node;
		return this;
	}
	
	public TaskBuilder<TaskTree> rootTask(DependentTaskKey<?, ?> key) {
		return new TaskBuilder<>(this, key);
	}
	
	public static class ConnectorBuilder<PARENT extends Parent<PARENT, Node>> extends Parent<ConnectorBuilder<PARENT>, Node> {
		
		private final PARENT parent;
		private boolean ignoreIfFailed;
		private Integer retryCount;
		private Task task;
		private TypeToken<?> input;

		private ConnectorBuilder(PARENT parent, TypeToken<?> input) {
			this.parent = parent;
			this.input = input;
		}

		public ConnectorBuilder<PARENT> ignoreFailures() {
			this.ignoreIfFailed = true;
			return this;
		}
		
		public ConnectorBuilder<PARENT> retry(int times) {
			this.retryCount = times;
			return this;
		}

		public TaskBuilder<ConnectorBuilder<PARENT>> chainedTask(DependentTaskKey<?, ?> key) {
			return new TaskBuilder<>(this, key);
		}

		public TaskBuilder<ConnectorBuilder<PARENT>> task(StandAloneTaskKey<?> key) {
			return new TaskBuilder<>(this, key);
		}
		
		public MultiplexerTaskBuilder<ConnectorBuilder<PARENT>> with() {
			return new MultiplexerTaskBuilder<>(this, input);
		}
		
		@Override
		protected ConnectorBuilder<PARENT> add(Node node) {
			task = (Task) node; // (remove cast)
			return this;
		}
		
		public PARENT end() {
			return parent.add(new Node.Connector(ignoreIfFailed, retryCount, task));
		}
	}
		
	public static class TaskBuilder<PARENT extends Parent<PARENT, Node>> extends Parent<TaskBuilder<PARENT>, Node> {
		
		private final PARENT parent;
		private Connector child;
		private DependentTaskKey<?, ?> dependentTaskKey;
		private StandAloneTaskKey<?> standAloneTaskKey;
		private TypeToken<?> output; 
		
		private TaskBuilder(PARENT parent, StandAloneTaskKey<?> standAloneTaskKey) {
			this.parent = parent;
			this.standAloneTaskKey = standAloneTaskKey;
			this.output = standAloneTaskKey.getOutput();
		}
		
		private TaskBuilder(PARENT parent, DependentTaskKey<?, ?> dependentTaskKey) {
			this.parent = parent;
			this.dependentTaskKey = dependentTaskKey;
			this.output = dependentTaskKey.getOutput();
		}

		public ConnectorBuilder<TaskBuilder<PARENT>> connectedTo() {
			return new ConnectorBuilder<>(this, output);
		}

		@Override
		protected TaskBuilder<PARENT> add(Node node) {
			child = (Connector) node; // (remove cast)
			return this;
		}
		
		public PARENT end() {
			return dependentTaskKey == null 
					? parent.add(new Node.StandaloneTask<>(standAloneTaskKey, child))
					: parent.add(new Node.FollowOnTask<>(dependentTaskKey, child));
		}
	}
	
	public static class MultiplexerTaskBuilder<PARENT extends Parent<PARENT, Node>> extends Parent<MultiplexerTaskBuilder<PARENT>, Node> {
		
		private final PARENT parent;
		private List<Node> children;
		private TypeToken<?> input;
		
		private MultiplexerTaskBuilder(PARENT parent, TypeToken<?> input) {
			this.parent = parent;
			this.input = input;
		}

		public ConnectorBuilder<MultiplexerTaskBuilder<PARENT>> withConnection() {
			return new ConnectorBuilder<>(this, input);
		}

		public TaskBuilder<MultiplexerTaskBuilder<PARENT>> addChainedTask(DependentTaskKey<?, ?> key) {
			return new TaskBuilder<>(this, key);
		}

		public TaskBuilder<MultiplexerTaskBuilder<PARENT>> addTask(StandAloneTaskKey<?> key) {
			return new TaskBuilder<>(this, key);
		}

		public MultiplexerTaskBuilder<MultiplexerTaskBuilder<PARENT>> with() {
			return new MultiplexerTaskBuilder<>(this, input);
		}
		
		@Override
		protected MultiplexerTaskBuilder<PARENT> add(Node node) {
			children.add(node); // (remove cast)
			return this;
		}
		
		public PARENT end() {
			return parent.add(new Node.Multiplexer(input, children));
		}
	}
}

abstract class Parent<THIS, NODETYPE> {
	protected abstract THIS add(NODETYPE node);
}