package game;

public enum Difficulty {
    EASY(1, 50, 15),
    NORMAL(1, 100, 10),
    HARD(1, 200, 7);

    private final int min;
    private final int max;
    private final int maxTries;

    Difficulty(int min, int max, int maxTries) {
        this.min = min;
        this.max = max;
        this.maxTries = maxTries;
    }

    public int getMin() { return min; }
    public int getMax() { return max; }
    public int getMaxTries() { return maxTries; }
}
