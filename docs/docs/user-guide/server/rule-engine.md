---
layout:   post
title:    Rule Engine
summary:  An introduction to the C2MON RuleTag grammar.
---

C2MON provides a simple Domain Specific Language (DSL) that allows expressing complex rules on top of the acquired data.
The rules are stored as simple Strings and loaded as [RuleTags](/overview/tags), which are then interpreted at runtime.

The rule evaluation on the C2MON server layer is fully supported within a clustered setup.
Thanks to its rich rule grammar the C2MON Rule Engine can play a central role for real time analytics.

# The Rule Grammar

The rule engine provides a set of operations, that allows expressing complex computations, comparisons and conditions.
Thereby it is possible to reference the values of Data Tags, Control Tags or other registered Rule Tags.
A tag value is addressed through a hash (#) symbol followed by the tag id (see examples below).

A pre-requirement of the Rule Engine is that the data value format of the referenced tag is one of the following basic data types:
String, Integer, Long, Float, Double, Boolean.
However, it is possible to combine different data value types in one rule as long as it make sense within the rule logic.

The following operators are supported by the Rule Engine:

| Arithmetic Operator | Description |
--------------- | -----------------
| + | Addition |
| - | Subtraction |
| * | Multiplication |
| / | Division |
| ^ | Power by |
| && | Bitwise AND |
| &#124;&#124; | Bitwise OR |



Using the comparison operator will return a BOOLEAN result.

| Comparison Operator	| Description |
--------------- | -----------------
| > | Greater than |
| < | Less than |
| = | Equals |
| >= | Greater or equals |
| <= | Less or equals |


The conditional operators allow you to link several BOOLEAN results to one joined result.

|Conditional |Operator	Description |
--------------- | -----------------
|AND _or_ & | AND Condition |
|OR _or_ &#124; | OR Condition |


> **Please note!** <br>
It is also allowed to use parenthesis to clearly separate conditions or calculations

# The Rule Syntax

We distinguish two types of rules expressions that is "Simple Rule Expression" and "Conditioned Rule Expression".


## Simple Rule Expression

Simple rules perform arithmetic calculations and/or comparisons of tag values and constants.
The Rule Engine allows using brackets to make logical combinations of sub-results.

**Examples**
```java
// adding two tags
#1234 + #2345

// multiplication of a numeric tag with a constant value
#1234 * 10

// Comparison of two numeric tags, after tag #1234 has been multiplied with 10.
// The end result is a BOOLEAN
(#1234 * 10) > #2345
```



## Conditioned Rule Expression

Conditioned Rule Expressions are composed by at least two Simple Rules that must result in a BOOLEAN value.
The returning values are specified between square brackets.

```
(SIMPLE_RULE1) [RESULT1], ... , (SIMPLE_RULEn) [RESULTn], true [DEFAULT_RESULT]
```

The rules are evaluated from top to down and are in fact very similar to `if ... else if ... else` cases.
It is good practice to always add a default result to the end.
However the default result is not mandatory and can be removed, if the logic exclude the case that all conditions result in FALSE.

**Examples**
```java
// if the value of tag 1234 is greater than tag 2345, then the rule result is 0. Otherwise 1
(#1234 > #2345) [0], true [1]

// Here we suppose that all tag values are Booleans otherwise the rule evaluation would result in an exception
(#1234 & #2345) | (#2345 & #3456) [OK], true [ERROR]

// It is also Possible to put longer Strings between the square brackets.
(#1234 < 10) [OK],
(#1234 >= 10) & (#1234 <= 15.5) [Please call support],
(#1234 > 15.5) [Please shut down the system!]

// It is also possible to compare two Strings
(#3334 = "OK") [true], true [false]
```



## Multiple return values in IF-statements


Up to now every IF-statement had one return value.
Of course, many IF-statements could be added one after the other, each one returning a different value.

**Example for if... else if ... else statement**
```java
#44178 = 2 [result1],
true [result2]
```

Another possibility is to have one IF-statement with more than one return value.

**Example for multiple return values in one IF-statement**
```java
#44178 = 2 [result1] OR true [result2]
```

**What is the difference?**

At a first look the two cases seems to be equal, but there are not if you take into account that a Tag can be invalid.

In case that #44178 is invalid:

- In the first example, the rule result would be INVALID (first statement is invalid and we cannot continue evaluating the second statement, since we cannot determine the value of the first one).
- In the **'OR'** case example, the value would be "result2" (the second condition is true and we don't care about the first one).

## Checking for Invalid Tags

The rule engine does always a best effort evaluation.
In case of an invalid input tags you have to keep in mind the following behaviour of the Rule Engine:

- Conditions that are concatenated by the AND (&) operator cannot be evaluated, if one of the input tags has an invalid quality.
- OR (|) concatenated conditions can be evaluated, if at least one input tag is valid.

It is also possible to directly check for invalidation by making use of the `$INVALID` keyword.

**Example**
```java
// if Tag 44178 is invalid the rule result is 'WARNING', otherwise 'OK'
#44178 = $INVALID [WARNING], true [OK]
```


## Common Pitfalls

Even if a rule looks syntactical correct, there are cases which makes it impossible to evaluate.
The Rule Tag is then invalidated and you should check the quality description.

Common problems are:

- Comparison Operators are applied to non Boolean type values
- Arithmetic Operators are applied to Boolean or String types
- One of the tags addressed by the rule is not initialised


# Configuration of a RuleTags

Configuring a `RuleTag` is very similar to the configuration of a `DataTag`, which is described in the [Configuration API](/user-guide/client-api/configuration/#configuring-ruletags) section.
