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
private static final TypeToken<String> STRING = TypeToken.of(String.class);

@Test
public void runTree() {
    
    DependentTaskKey<String, String> task1 = DependentTaskKey.unaryTaskKey("task1", STRING);
    DependentTaskKey<String, String> task2 = DependentTaskKey.unaryTaskKey("task2", STRING);
    DependentTaskKey<String, String> task3 = DependentTaskKey.unaryTaskKey("task3", STRING);
    
    ActionResolver actionResolver = new ActionResolver()
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
    
    assertThat(subscriber.getOnNextEvents())
        .containsExactly("initial : task1 : task2 : task3");
}
```