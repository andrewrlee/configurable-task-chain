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
	
	public Observable<Accumulator<T>> asObservable() {
		return Observable.<T>merge(observables)
				.reduce(new Accumulator<T>(), Accumulator::add);
	}
	
	public Accumulator<T> run() {
		return asObservable().toBlocking().first();
	}
}
