package uk.co.optimisticpanda.configtaskchain.model;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import uk.co.optimisticpanda.configtaskchain.TaskDecorators.DependentTask;
import uk.co.optimisticpanda.configtaskchain.model.Node.StandaloneTask;

public class TaskRegistry {

	private final Map<DependentTaskKey<?,?>, DependentTask<?, ?>> dependentTasks = new HashMap<>();
	private final Map<StandAloneTaskKey<?>, Observable<?>> standaloneTasks = new HashMap<>();
	
	public <I, O, V> TaskRegistry put(DependentTaskKey<I, O> entry, DependentTask<I, O> f) {
		dependentTasks.put(entry, f);
		return this;
	} 
	
	public <O, V> TaskRegistry put(StandAloneTaskKey<O> entry, Observable<O> obs) {
		standaloneTasks.put(entry, obs);
		return this;
	} 
	
	private <I, O, V> DependentTask<I, O> getDependent(DependentTaskKey<I, O> entry) {
		return (DependentTask<I, O>) dependentTasks.get(entry);
	}
	
	private <O, V> Observable<O> getStandalone(StandAloneTaskKey<O> entry) {
		return (Observable<O>) standaloneTasks.get(entry);
	}
	
	public <O> Observable<O> resolveAction(StandaloneTask<O> task) {
		return getStandalone(task.getStandAloneTaskKey());
	}
	
	public <I, O> Observable<O> resolveDependentTask(Node.FollowOnTask<I, O> task, I result) {
		return getDependent(task.getDependentTaskKey()).build(result);
	}
	
	
	
	
}
