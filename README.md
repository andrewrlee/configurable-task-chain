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
    TaskTree tree = TaskTree.create()
            .connector()
                .ignoreFailures()
                .task("task1")
                    .connectedTo()
                        .task("task2")
                            .dependentOn(TypeToken.of(String.class))
                            .connectedTo()
                                .task("task3")
                                    .dependentOn(TypeToken.of(String.class))
                                    .end()
                                .end()
                            .end()
                        .end()
                    .end()
                .end();

    Node root = tree.getChildren().get(0);
    parser.parse(root).subscribe(subscriber);
    
    subscriber.awaitTerminalEvent(30, SECONDS);
    subscriber.assertValueCount(1);
    
    assertThat(subscriber.getOnNextEvents()).containsExactly("initial : then : then");
```