## Getting your data

Once the `C2monServiceGateway` is initialized you can access the `TagService` to retrieve data from C2MON.
To find and subscribe your data you have two possibilities.
Either you know the unique Tag id or you provide the Tag name.

> **Please note!**

>In addition to the unique ID every Tag in C2MON provides also a unique Tag name.
>The Tag name format is free to choose and can contain any kind of UTF-8 characters.

>A well chosen naming convention strategy will enable the users to make powerful wildcard expression searches.


### Subscription by Tag name

The `TagService` offers multiple possibilities to get data by name from the server.

You can:

- give the explicit Tag name or a list of names.
- pass one or multiple wildcard expressions and the server will return all matching Tags

A search is *always case insensitive*.

The following special characters are supported in wildcard expressions:

- ? - match any one single character
- \* - match any multiple character(s) (including zero)

The supported wildcard characters can be escaped with a backslash `\`, and a literal backslash can be included with '//'

> **Be careful**

>Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches


**Examples to get the latest Tag value received by the server**
```java
TagService tagService = C2monServiceGateway.getTagService();

// Get the latest value of Tag with name "my.tags.foo1"
Tag value = tagService.findByName("my.tags.foo1");

// Get the latest values for a list of Tags
Set<String> tagNames = new HashSet<>();
tagNames.add("my.tags.foo1");
tagNames.add("my.tags.foo2");
Collection<Tag> values = tagService.findByName(tagNames);


// Get all Tag values where the Tag name starts with "my.tags."
values = tagService.findByName("my.tags.*");

// Get all Tag values where the Tag name matches at least one of the provided wildcards expressions.
Set<String> regexList = new HashSet<>();
regexList.add("my.tags.foo?");
regexList.add("*.tags.*");
values = tagService.findByName(regexList);
```

It is also possible to subscribe with a listener to a list of tags in order to receive instantly new updates.

**Example for subscribing to Tags**
```java
public class SubscriptionExample1 {

  public SubscriptionExample1() {
    C2monServiceGateway.startC2monClientSynchronous();
  }

  /**
   * Subscribe to all Tags where the Tag name starts with "my.tags."
   */
  public void subscribeTags() {

    TagService tagService = C2monServiceGateway.getTagService();
    tagService.subscribeByName("my.tags.*", new TagUpdateListener());
  }
}

/**
 * Hello World example for a tag listener
 */
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
   * Once called during subscription to pass the initial values
   */
  @Override
  public void onInitialUpdate(final Collection<Tag> initialValues) {

    for (Tag tag : initialValues) {
      System.out.println(String.format("Initial value for tag %s (%d): %s",
                          tag.getName(), tag.getId(), tag.getValue()));
    }

    System.out.println(String.format("\nFound %d matching tags", initialValues.size()));

  }
}
```

### Subscription by Tag ID

Every Tag has a unique number called _tag ID_.
In certain use cases you may prefer to use the tag ID directly instead of the tag name, in particular if you have already a listener subscribed to a given tag.
In that case the API does not have to contact the server as for a wildcard search and can directly use the local cache, which is of course significantly faster.

**Examples to get the latest Tag value with the tag ID**
```java
TagService tagService = C2monServiceGateway.getTagService();

// Get the latest value of Tag with ID 1234
Tag value = tagService.get(1234L);

// Get the latest values for a list of Tags
Set<Long> tagIds = new HashSet<>();
tagIds.add(1234L);
tagIds.add(2345L);
Collection<Tag> values = tagService.get(tagIds);
```

Subscribing to Tags by tag ID is very similar to the subscription by tag name.
Again, you have to register an implementation of the `TagListener` interface.

**Example for subscribing to Tags by tag ID**
```java
public class SubscriptionExample2 {

  public SubscriptionExample2() {
    C2monServiceGateway.startC2monClientSynchronous();
  }

  /**
   * Subscribing a new listener to all Tags of the given list
   */
  public void subscribeToListOfTags(final Set<Long> tagIds) {

    TagService tagService = C2monServiceGateway.getTagService();
    tagService.subscribe(tagIds, new TagUpdateListener());
  }
}

/**
 * Hello World example for a tag listener
 */
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
   * Once called during subscription to pass the initial values
   */
  @Override
  public void onInitialUpdate(final Collection<Tag> initialValues) {

    for (Tag tag : initialValues) {
      System.out.println(String.format("Initial value for tag %s (%d): %s",
                          tag.getName(), tag.getId(), tag.getValue()));
    }

    System.out.println(String.format("\nFound %d matching tags", initialValues.size()));

  }
}
```
