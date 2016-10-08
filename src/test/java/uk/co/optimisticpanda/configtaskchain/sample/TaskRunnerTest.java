package uk.co.optimisticpanda.configtaskchain.sample;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.ignoreAnyFailure;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.inParrallel;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.retry;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.task;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.then;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.thenPerform;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.zip;
import static uk.co.optimisticpanda.configtaskchain.sample.MetricResult.MetricType.AREA;
import static uk.co.optimisticpanda.configtaskchain.sample.MetricResult.MetricType.DURATION;
import static uk.co.optimisticpanda.configtaskchain.sample.MetricResult.MetricType.HEIGHT;
import static uk.co.optimisticpanda.configtaskchain.sample.MetricResult.MetricType.WIDTH;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import uk.co.optimisticpanda.configtaskchain.Accumulator;
import uk.co.optimisticpanda.configtaskchain.TaskRunner;

public class TaskRunnerTest {

	private final TestService testService = new TestService();
	private TaskRunner<MetricResult<?>> runner; 
	
	@Before
	public void setup() {
		runner = new TaskRunner<>(
					inParrallel(task(testService::duration)), 
					inParrallel(task(testService::height)),
					retry(2, inParrallel(task(testService::widthOnlySucceedsEvery3rdAttempt))),
					ignoreAnyFailure(task(testService::weightAlwaysFails)));
	}

	@Test
	public void checkRun() {
		Accumulator<MetricResult<?>> accumalator = runner.run();

		verifyAccumalatedResults(accumalator);
	}

	@Test
	public void checkAsObservable() {
		TestSubscriber<Accumulator<MetricResult<?>>> subscriber = new TestSubscriber<>();
		
		runner.asObservable().subscribe(subscriber);
		
		subscriber.awaitTerminalEvent(30, SECONDS);
		subscriber.assertValueCount(1);
		
		verifyAccumalatedResults(subscriber.getOnNextEvents().get(0));
	}

	@Test
	public void canRunTwice() {
		verifyAccumalatedResults(runner.run());
		verifyAccumalatedResults(runner.run());
	}

	@Test
	public void checkFailsIfRunoutOfRetries() {
		runner = new TaskRunner<>(retry(1, task(testService::widthOnlySucceedsEvery3rdAttempt)));

		assertThatThrownBy(() -> runner.run())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("error!");
	}
	
	@Test
	public void checkZip() {
		runner = new TaskRunner<>(
				zip(retry(2, task(testService::widthOnlySucceedsEvery3rdAttempt)),
					task(testService::height), 
					(width, height) -> width.mergeWith(height, AREA, (h, w) -> h * w)));

		assertThat(runner.run())
			.containsExactlyInAnyOrder(
				new MetricResult<>(AREA, 6));
	}

	@Test
	public void checkThen() {
		runner = new TaskRunner<>(
				retry(3, then( task(testService::height), testService::calculateAreaFromHeight)));
		assertThat(runner.run())
			.containsExactlyInAnyOrder(
				new MetricResult<>(AREA, 6));
	}

	@Test
	public void checkThenSimpleTypes() {
		UnaryOperator<Integer> square = i -> i * i;
		
		TaskRunner<Integer> intTaskRunner = new TaskRunner<>(
				then(then(then(task(() -> 2), square ), square), square));
		
		assertThat(intTaskRunner.run()).containsExactly(256);
	}
	
	@Test
	public void checkThenPerform() {
		runner = new TaskRunner<>(
				thenPerform(task(testService::height), 
						height -> retry(3, task(() -> testService.calculateAreaFromHeight(height)))));
		assertThat(runner.run())
			.containsExactlyInAnyOrder(
				new MetricResult<>(AREA, 6));
	}
	
	@Test
	public void checkThenPerformSimpleTask() {
		Function<Integer, Observable<Integer>> square = i -> task(() -> i * i);
		
		TaskRunner<Integer> squarer = new TaskRunner<>(
				thenPerform(thenPerform(thenPerform(task(() -> 2), square ), square), square));
		
		assertThat(squarer.run()).containsExactly(256);
	}
	
	@Test
	public void checkThenPerformSimpleReduction() {
		Function<Integer, Function<List<Integer>, Observable<List<Integer>>>> conser = i -> {
			return list -> task(() -> {
				list.add(i);
				return list;
			});
		};
		Function<List<Integer>, Observable<List<Integer>>> first = conser.apply(1);
		Function<List<Integer>, Observable<List<Integer>>> second = conser.apply(2);
		Function<List<Integer>, Observable<List<Integer>>> third = conser.apply(3);
		
		TaskRunner<List<Integer>> reducer = new TaskRunner<>(
				thenPerform(thenPerform(thenPerform(task(() -> new ArrayList<>()), first), second), third));
		
		assertThat(reducer.run()).flatExtracting(i -> i).containsExactly(1, 2 , 3);
	}
	
	@Test
	public void checkThenSimpleReduction() {
		Function<Integer, Function<List<Integer>, List<Integer>>> conser = i -> {
			return list -> {
				list.add(i);
				return list;
			};
		};
		Function<List<Integer>, List<Integer>> first = conser.apply(1);
		Function<List<Integer>, List<Integer>> second = conser.apply(2);
		Function<List<Integer>, List<Integer>> third = conser.apply(3);
		
		TaskRunner<List<Integer>> reducer = new TaskRunner<>(
				then(then(then(task(() -> new ArrayList<>()), first), second), third));
		
		assertThat(reducer.run()).flatExtracting(i -> i).containsExactly(1, 2 , 3);
	
		reducer = new TaskRunner<>(
				then(task(() -> new ArrayList<>()), first.andThen(second).andThen(third)));
		
		assertThat(reducer.run()).flatExtracting(i -> i).containsExactly(1, 2 , 3);
	}
	
	@Test
	public void example() {
		runner = new TaskRunner<>(
				inParrallel(task(testService::duration)), 
				inParrallel(task(testService::height)),
				ignoreAnyFailure(task(testService::weightAlwaysFails)),
				zip(
					retry(2, task(testService::widthOnlySucceedsEvery3rdAttempt)),
					task(testService::height), 
					(width, height) -> width.mergeWith(height, AREA, (h, w) -> h * w)));
	
		Accumulator<MetricResult<?>> accumulator = runner.run();
		
		assertThat(accumulator)
			.containsExactlyInAnyOrder(
				new MetricResult<>(HEIGHT, 2),
				new MetricResult<>(AREA, 6),
				new MetricResult<>(DURATION, Duration.ofMinutes(4)));
	}
	
	
	private void verifyAccumalatedResults(Accumulator<MetricResult<?>> accumalator) {
		assertThat(accumalator)
			.containsExactlyInAnyOrder(
					new MetricResult<>(HEIGHT, 2),
					new MetricResult<>(WIDTH, 3),
					new MetricResult<>(DURATION, Duration.ofMinutes(4)));
	}

}
