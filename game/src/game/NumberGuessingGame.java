package game;

import java.util.Random;

public class NumberGuessingGame {
    private final int min;
    private final int max;
    private final int maxTries;
    private final int secret;
    private int attempts;
    private boolean over;

    public NumberGuessingGame(int min, int max, int maxTries, int secret) {
        if (min > max) throw new IllegalArgumentException("min must be <= max");
        if (maxTries <= 0) throw new IllegalArgumentException("maxTries must be positive");
        if (secret < min || secret > max) throw new IllegalArgumentException("secret out of range");
        this.min = min;
        this.max = max;
        this.maxTries = maxTries;
        this.secret = secret;
        this.attempts = 0;
        this.over = false;
    }

    public static NumberGuessingGame forDifficulty(Difficulty d, Random random) {
        int secret = random.nextInt(d.getMax() - d.getMin() + 1) + d.getMin();
        return new NumberGuessingGame(d.getMin(), d.getMax(), d.getMaxTries(), secret);
    }

    public GuessResult guess(int value) {
        if (over) throw new IllegalStateException("game is already over");
        if (value < min || value > max) {
            return GuessResult.INVALID;
        }
        attempts++;
        if (value == secret) {
            over = true;
            return GuessResult.WIN;
        }
        if (attempts >= maxTries) {
            over = true;
            return GuessResult.LOSE;
        }
        return value < secret ? GuessResult.TOO_LOW : GuessResult.TOO_HIGH;
    }

    public int getAttempts() { return attempts; }
    public int getRemaining() { return maxTries - attempts; }
    public boolean isOver() { return over; }
    public int getMaxTries() { return maxTries; }
    public int getMin() { return min; }
    public int getMax() { return max; }
    public int getSecret() { return secret; }
}
