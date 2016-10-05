package uk.co.optimisticpanda.configtaskchain;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import rx.Observable;
import rx.schedulers.Schedulers;

public enum TaskDecorators {
	;

	public static <T> Observable<T> task(Callable<T> callable) {
		return Observable.fromCallable(callable);
	}
	
	public static <T> Observable<T> inParrallel(Observable<T> obs) {
		return obs.subscribeOn(Schedulers.newThread());
	}
	
	public static <T> Observable<T> retry(int times, Observable<T> obs) {
		return obs.retry(times);
	}
	
	public static <T> Observable<T> ignoreAnyFailure(Observable<T> obs) {
		return obs.onExceptionResumeNext(Observable.empty());
	}

	public static <T, U, R> Observable<R> zip(Observable<T> obs, Observable<U> other, BiFunction<T, U, R> f) {
		return obs.zipWith(other, f::apply);
	}
	
}
