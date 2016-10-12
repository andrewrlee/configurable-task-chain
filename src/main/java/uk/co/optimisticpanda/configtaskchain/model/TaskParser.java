package uk.co.optimisticpanda.configtaskchain.model;

import static java.util.stream.Collectors.toList;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.ignoreAnyFailure;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.retry;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.thenPerform;

import java.util.List;

import rx.Observable;
import uk.co.optimisticpanda.configtaskchain.Accumulator;
import uk.co.optimisticpanda.configtaskchain.TaskRunner;
import uk.co.optimisticpanda.configtaskchain.model.Node.Connector;
import uk.co.optimisticpanda.configtaskchain.model.Node.FollowOnTask;
import uk.co.optimisticpanda.configtaskchain.model.Node.Multiplexer;
import uk.co.optimisticpanda.configtaskchain.model.Node.StandaloneTask;
import uk.co.optimisticpanda.configtaskchain.model.Node.Task;

public class TaskParser {

	private TaskRegistry resolver;

	public TaskParser(TaskRegistry resolver) {
		this.resolver = resolver;
	}
	
	public <I, O> Observable<O> parse(Node task, final Observable<I> input) {
		if (task instanceof Connector) {
			return (Observable<O>) connector(input, Connector.class.cast(task));
		}
		if (task instanceof StandaloneTask) {
			return task(input, StandaloneTask.class.cast(task));
		}
		if (task instanceof FollowOnTask) {
			return dependentTask(input, FollowOnTask.class.cast(task));
		}
		if (task instanceof Multiplexer) {
			return (Observable<O>) multiplex(input, Multiplexer.class.cast(task));
		}
		throw new IllegalStateException("cannot handle task type: " + task.getClass());
	}
	
	private <T> Observable<T> connector(Observable<T> observable, Connector connector) {
		observable = addIgnoreErrors(connector, observable);
		observable = addRetries(connector, observable);
		return parse(connector.getTask(), observable);
	}

	private <T, R> Observable<Accumulator<Object>> multiplex(Observable<T> observable, Multiplexer multiplexer) {
		List<Observable<Object>> list = multiplexer.getChildren().stream().map(node -> parse(node, observable)).collect(toList());
		return new TaskRunner<>(list).asObservable();
	}
	
	private <I, O> Observable<O> task(Observable<I> observable, StandaloneTask<O> task) {
		Observable<O> ret = thenPerform(observable, result -> resolver.resolveAction(task));
		ret = addChild(task, ret);
		return ret;
	}

	private <I, O> Observable<O> dependentTask(Observable<I> observable, FollowOnTask<I, O> task) {
		Observable<O> ret = thenPerform(observable, result -> resolver.resolveDependentTask(task, result));
		ret = addChild(task, ret);
		return ret;
	}
	
	private <T> Observable<T> addRetries(Connector connector, Observable<T> observable) {
		return connector.getRetryCount()
				.map(count -> retry(count, observable))
				.orElse(observable);
	}

	private <T> Observable<T> addChild(Task task, Observable<T> observable) {
		return task.getNext()
				.map(connector -> connector(observable, connector))
				.orElse(observable);
	}
	
	private <T> Observable<T> addIgnoreErrors(Connector connector, Observable<T> observable) {
		return connector.isIgnoreIfFailed() ? ignoreAnyFailure(observable) : observable;
	}
}
