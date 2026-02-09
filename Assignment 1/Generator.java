import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Generator { 
    private final Random rng = new Random();

    private static final int GEN_JAVA_UTIL_RANDOM = 1;
    private static final int GEN_MATH_RANDOM = 2;
    private static final int GEN_THREAD_LOCAL_RANDOM = 3;

    
    public ArrayList<Double> populate(int n, int randNumGen) { 
        ArrayList<Double> values = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            double x;

            if (randNumGen == GEN_JAVA_UTIL_RANDOM) {
                x = rng.nextDouble();
            } else if (randNumGen == GEN_MATH_RANDOM) {
                x = Math.random();
            } else if (randNumGen == GEN_THREAD_LOCAL_RANDOM) {
                x = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
            } else {
                throw new IllegalArgumentException("Invalid randNumGen. Use 1, 2, or 3.");
            }

            values.add(x);
        }

        return values;
    }

  
    public ArrayList<Double> statistics(ArrayList<Double> randomValues) {
        int n = randomValues.size();
        if (n == 0) {
            throw new IllegalArgumentException("statistics() requires at least one value.");
        }

        double sum = 0.0;
        double min = randomValues.get(0);
        double max = randomValues.get(0);

        for (double v : randomValues) {
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        double mean = sum / n;

        double sqSum = 0.0;
        if (n > 1) {
            for (double v : randomValues) {
                double diff = v - mean;
                sqSum += diff * diff;
            }
        }

        double stddev = (n > 1) ? Math.sqrt(sqSum / (n - 1)) : 0.0;

        ArrayList<Double> results = new ArrayList<>(5);
        results.add((double) n);
        results.add(mean);
        results.add(stddev);
        results.add(min);
        results.add(max);

        return results;
    }

    
    public void display(ArrayList<Double> results, boolean headerOn) {
        if (results == null || results.size() != 5) {
            throw new IllegalArgumentException("display() expects results of size 5: [n, mean, stddev, min, max]");
        }

        if (headerOn) {
            System.out.printf("%-10s %-12s %-12s %-12s %-12s%n",
                    "n", "mean", "stddev", "min", "max");
            System.out.println("---------------------------------------------------------------");
        }

        System.out.printf("%-10.0f %-12.6f %-12.6f %-12.6f %-12.6f%n",
                results.get(0), results.get(1), results.get(2), results.get(3), results.get(4));
    }

    
    public void execute() {
        int[] sampleSizes = {10, 100, 10000};
        int[] generators = {GEN_JAVA_UTIL_RANDOM, GEN_MATH_RANDOM, GEN_THREAD_LOCAL_RANDOM};

        boolean printedHeader = false;

        for (int n : sampleSizes) {
            for (int gen : generators) {
                ArrayList<Double> data = populate(n, gen);
                ArrayList<Double> stats = statistics(data);

                if (!printedHeader) {
                    display(stats, true);
                    printedHeader = true;
                } else {
                    display(stats, false);
                }
            }
        }
    }

    public static void main(String[] args) {
        Generator g = new Generator();

        
        g.execute();
    }
}

