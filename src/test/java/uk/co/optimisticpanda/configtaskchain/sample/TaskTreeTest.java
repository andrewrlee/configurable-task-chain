package uk.co.optimisticpanda.configtaskchain.sample;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.optimisticpanda.configtaskchain.TaskDecorators.dependentTask;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import uk.co.optimisticpanda.configtaskchain.TaskDecorators.DependentTask;
import uk.co.optimisticpanda.configtaskchain.model.DependentTaskKey;
import uk.co.optimisticpanda.configtaskchain.model.Node;
import uk.co.optimisticpanda.configtaskchain.model.Node.FollowOnTask;
import uk.co.optimisticpanda.configtaskchain.model.TaskRegistry;
import uk.co.optimisticpanda.configtaskchain.model.TaskParser;
import uk.co.optimisticpanda.configtaskchain.model.TaskTree;

import com.google.common.reflect.TypeToken;

public class TaskTreeTest {

	private static final TypeToken<String> STRING = TypeToken.of(String.class);

	@Test
	public void runTree() {
		
		DependentTaskKey<String, String> task1 = DependentTaskKey.unaryTaskKey("task1", STRING);
		DependentTaskKey<String, String> task2 = DependentTaskKey.unaryTaskKey("task2", STRING);
		DependentTaskKey<String, String> task3 = DependentTaskKey.unaryTaskKey("task3", STRING);
		
		TaskRegistry actionResolver = new TaskRegistry()
			.put(task1, dependentTask(input -> input + " : " + task1.getIdentifier()))
			.put(task2, dependentTask(input -> input + " : " + task2.getIdentifier()))
			.put(task3, dependentTask(input -> input + " : " + task3.getIdentifier()));
		
		TaskTree tree = TaskTree.create()
							.rootTask(task1)
								.connectedTo()
									.chainedTask(task2)
										.connectedTo()
											.chainedTask(task3)
											.end()
										.end()
									.end()
								.end()
							.end();
		
		TestSubscriber<Object> subscriber = new TestSubscriber<>();
		
		new TaskParser(actionResolver).parse(tree.getRoot(), Observable.just("initial"))
			.subscribe(subscriber);
		
		subscriber.awaitTerminalEvent(30, SECONDS);
		subscriber.assertValueCount(1);
		
		assertThat(subscriber.getOnNextEvents()).containsExactly("initial : task1 : task2 : task3");
	}
}
