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

A partial builder has been implemented:

```java
@Test
public void runTree() {
    
    TaskTree tree = TaskTree.create()
                        .rootTask("task1", TypeToken.of(String.class))
                            .connectedTo()
                                .chainedTask("task2", TypeToken.of(String.class))
                                    .connectedTo()
                                        .chainedTask("task3", TypeToken.of(String.class))
                                        .end()
                                    .end()
                                .end()
                            .end()
                        .end();
    
    TaskParser parser = new TaskParser(new ActionResolver(){
        @Override
        public <U, T> Observable<T> resolveDependentTask(FollowOnTask task, U result) {
            return (Observable<T>) dependentTask(r -> r + " : " + "then " + task.id().orElse("")).build(result);
        }
    });
    
    TestSubscriber<Object> subscriber = new TestSubscriber<>();
    
    parser.parse(tree.getRoot(), Observable.just("initial"))
        .subscribe(subscriber);
    
    subscriber.awaitTerminalEvent(30, SECONDS);
    subscriber.assertValueCount(1);
    
    assertThat(subscriber.getOnNextEvents()).containsExactly("initial : then task1 : then task2 : then task3");
}

```