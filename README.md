# configurable-task-chain
Config driver job runner based on rx-java 

Not yet config driven so currently just a thin wrapper around rx-java!

```java
    @Test
    public void check() {
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
```