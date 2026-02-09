# Random Number Generation and Descriptive Statistics (Java)

## Project Overview
This project implements a Java program that generates random double values in the range **[0, 1)** using multiple built-in Java random number generators and analyzes the generated data using **descriptive statistics**.

The main goal of this assignment is to:
- Compare different Java random number generation techniques
- Observe statistical behavior as sample size increases
- Apply object-oriented analysis and design principles in Java

The program demonstrates how statistical measures converge to their theoretical values as the number of samples grows.

---

## Random Number Generators Used
The program generates random values using the following approaches:

1. **`java.util.Random`**  
   A standard pseudo-random number generator widely used in Java applications.

2. **`Math.random()`**  
   A static utility method that produces uniformly distributed random values.

3. **`java.util.concurrent.ThreadLocalRandom`**  
   A modern random number generator optimized for concurrent and multithreaded programs.

All generators produce values uniformly distributed in the interval **[0, 1)**.

---

## Sample Sizes
For each random number generator, the program evaluates the following sample sizes:

- **n = 10**
- **n = 100**
- **n = 10,000**

This results in a total of **9 experiments** (3 sample sizes × 3 generators).

---

## Descriptive Statistics Calculated
For each dataset, the following statistics are computed:

- **n** – number of generated values  
- **mean** – arithmetic average  
- **sample standard deviation** – variability of the data  
- **minimum** – smallest generated value  
- **maximum** – largest generated value  

The statistics are returned and displayed in the following order:

[n, mean, stddev, min, max]



---

## Expected Statistical Behavior
Since all values are generated from a **Uniform(0,1)** distribution, the expected theoretical values are:

- Mean → **0.5**
- Standard deviation → **≈ 0.288675**
- Minimum → **0**
- Maximum → **1**

As the sample size increases, the computed statistics converge toward these values, which confirms the correctness of the implementation.

---

## Program Structure
The project consists of a single Java class:

### `Generator`
This class contains all functionality required to generate random values, compute statistics, and display results.

---

## Method Descriptions

### `populate(int n, int randNumGen)`
- Generates **n** random double values
- Selects the random number generator based on `randNumGen`
- Stores values in an `ArrayList<Double>`
- Returns the populated list

---

### `statistics(ArrayList<Double> randomValues)`
- Computes:
  - number of elements
  - mean
  - sample standard deviation
  - minimum
  - maximum
- Returns results in the required order as an `ArrayList<Double>`

---

### `display(ArrayList<Double> results, boolean headerOn)`
- Displays the statistics in a formatted table
- Optionally prints a header row
- Ensures readable console output

---

### `execute()`
- Controls the full execution of the program
- Iterates over all sample sizes and random generators
- Calls `populate()`, `statistics()`, and `display()`
- Produces exactly **nine output rows**, as required

---

### `main(String[] args)`
- Minimal by design, as required by the assignment
- Creates an instance of the `Generator` class
- Calls the `execute()` method

---

## Object-Oriented Concepts Demonstrated
The code explicitly highlights the following object-oriented concepts using comments:

- Class definition
- Method definition
- Class attribute
- Object instantiation
- Accessibility modifiers (`public`, `private`)

