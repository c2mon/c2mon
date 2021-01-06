---
layout:   post
title:    Data subscription
summary:  Learn how to subscribe to your data and alarms with the C2MON Client API.
---


# Tag subscription

The section explains how to search and subscribe to tags with the `TagService` of the C2MON Client API

> **Please note!** <br>
A well-chosen naming convention will enable you to make searching for tags easier in the future.
We suggest using a _folder-like_ structure with `/` as separator.
Example: `serviceA/computer/mypc1/memory`


## Searching for tags by name

The `TagService` offers multiple possibilities to get data by name from the server. You can:

- Give the explicit tag name (or a list of names);
- Give a wildcard expression (or multiple expressions)

**Tag names are *always case insensitive*.**

The following special characters are supported in wildcard expressions:

- ? - match any one single character
- \* - match any multiple character(s) (including zero)

The supported wildcard characters can be escaped with a backslash `\`, and a literal backslash can be included with '\\'

!!! warning "Be careful!"
    Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches.

**Example:** Get the latest value of a tag by explicit name
```java
Tag tag = tagService.findByName("host1:cpu.avg");
```

**Example:** Get the latest value of the `cpu.avg` metric for all hosts:
```java
Collection<Tag> tags = tagService.findByName("host*:cpu.avg");
```




## Subscribing to tag updates

A near real-time stream of tag updates can be acquired through the use of a `TagListener`.

**Example:** Subscribe to a set of tags
```java
TagService tagService = C2monServiceGateway.getTagService();
tagService.subscribeByName("host*:cpu.avg", new TagUpdateListener());

...

public class TagUpdateListener implements TagListener {

  /**
   * Called every time a new value update is received
   */
  @Override
  public void onUpdate(final Tag tagUpdate) {
    System.out.println(String.format("Update for tag %s (%d): %s",
                        tagUpdate.getName(), tagUpdate.getId(), tagUpdate.getValue()));
  }

  /**
   * Called once during subscription to pass the initial values
   */
  @Override
  public void onInitialUpdate(final Collection<Tag> initialValues) {
    System.out.println(String.format("\nFound %d matching tags", initialValues.size()));

    for (Tag tag : initialValues) {
      System.out.println(String.format("Initial value for tag %s (%d): %s",
                          tag.getName(), tag.getId(), tag.getValue()));
    }
  }
}
```

## Subscription by tag ID

In addition to its name, each tag has a unique ID. In certain cases you may prefer to use the ID directly instead of the name, in particular if
you have already a listener subscribed to a given tag. In that case, the client does not have to contact the server as for a wildcard search and can
directly use the local cache, which is of course significantly faster.

**Example:** Get the latest value of a tag by ID:
```java
Tag tag = tagService.get(1234L);
```

Otherwise, it's very similar to subscribing by name with the difference that a list of tag IDs has to be provided.

**Example:** Subscribe to a set of tags
```java
TagService tagService = C2monServiceGateway.getTagService();
tagService.subscribe(listOfTagIDs, new TagUpdateListener());

...

public class TagUpdateListener implements TagListener {

  /**
   * Called every time a new value update is received
   */
  @Override
  public void onUpdate(final Tag tagUpdate) {
    System.out.println(String.format("Update for tag %s (%d): %s",
                        tagUpdate.getName(), tagUpdate.getId(), tagUpdate.getValue()));
  }

  /**
   * Called once during subscription to pass the initial values
   */
  @Override
  public void onInitialUpdate(final Collection<Tag> initialValues) {
    System.out.println(String.format("\nFound %d matching tags", initialValues.size()));

    for (Tag tag : initialValues) {
      System.out.println(String.format("Initial value for tag %s (%d): %s",
                          tag.getName(), tag.getId(), tag.getValue()));
    }
  }
}
```


# Alarm subscription

C2MON allows getting all active alarms from the server In-Memory cache, as well as  subscribing to all alarm value changes. Since information is coming from the cache the retrieval is very fast (below 1 second), even if you have houndreds of thousands alarms defined.

## Get all active alarms

The following call returns all alarms from the C2MON In-Memory cache that are currently in ACTIVE state.

```java
AlarmService alarmService = C2monServiceGateway.getAlarmService();
Collection<AlarmValue> activeAlarms = alarmService.getAllActiveAlarms();
```

## Subscribe to alarm updates

Alarm listeners are notified whenever an alarm changes state or whenever the alarm value information gets updated. 

The alarm lister gets notified with a tag updated which contains the alarm change. This makes it very easy to analyse the event that triggered the alarm update.

Please note that the `sourceTimestamp` will be identical to the tag timestamp

```java
AlarmService alarmService = C2monServiceGateway.getAlarmService();

alarmService.addAlarmListener(tagUpdate -> {
  tagUpdate.getAlarms().stream()
      .filter(alarm -> alarm.getSourceTimestamp().equals(tagUpdate.getTimestamp()))
      .forEach(alarm -> System.out.println(alarm.getId() + " changed!"));
});
```


[c2mon-client.properties]: https://github.com/c2mon/c2mon-web-ui/blob/master/src/dist/tar/conf/c2mon-client.properties