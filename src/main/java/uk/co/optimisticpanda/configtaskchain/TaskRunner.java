package uk.co.optimisticpanda.configtaskchain;

import static java.util.Arrays.asList;

import java.util.List;

import rx.Observable;

public class TaskRunner<T> {

	private final List<Observable<T>> observables;
	
	public TaskRunner(List<Observable<T>> observables) {
		this.observables = observables;
	}

	@SafeVarargs
	public TaskRunner(Observable<T>... observables) {
		this(asList(observables));
	}
	
	public Observable<Accumalator<T>> asObservable() {
		return Observable.<T>merge(observables)
				.reduce(new Accumalator<T>(), Accumalator::add);
	}
	
	public Accumalator<T> run() {
		return asObservable().toBlocking().first();
	}
}
