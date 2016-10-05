package uk.co.optimisticpanda.configtaskchain.sample;

import static java.util.concurrent.TimeUnit.SECONDS;
import static uk.co.optimisticpanda.configtaskchain.sample.MetricResult.MetricType.DURATION;
import static uk.co.optimisticpanda.configtaskchain.sample.MetricResult.MetricType.HEIGHT;
import static uk.co.optimisticpanda.configtaskchain.sample.MetricResult.MetricType.WIDTH;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestService {
	private static final Logger L = LoggerFactory.getLogger(TestService.class);
	private final AtomicInteger count = new AtomicInteger();

	public MetricResult<Duration> duration() throws InterruptedException {
		SECONDS.sleep(1);
		return new MetricResult<>(DURATION, Duration.ofMinutes(4));	
	}
	
	public MetricResult<Integer> height() throws InterruptedException {
		SECONDS.sleep(1);
		return new MetricResult<>(HEIGHT, 2);	
	}
	
	public MetricResult<Integer> widthOnlySucceedsEvery3rdAttempt() throws InterruptedException {
		SECONDS.sleep(1);
		if (count.incrementAndGet() % 3 != 0) {
			L.warn("failed to get width on attempt: " + count.get());
			throw new IllegalStateException("error!");
		}
		L.info("succeeded to get width on attempt: " + count.get());
		return new MetricResult<>(WIDTH, 3);	
	}
	
	public MetricResult<Double> weightAlwaysFails() {
		L.error("failed to get weight on attempt: 1");
		throw new RuntimeException("error!");
	}	

	
}
