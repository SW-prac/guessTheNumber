package game;

public class BestScore {
    private Integer best;

    public boolean update(int tries) {
        if (tries <= 0) throw new IllegalArgumentException("tries must be positive");
        if (best == null || tries < best) {
            best = tries;
            return true;
        }
        return false;
    }

    public boolean hasScore() { return best != null; }

    public Integer get() { return best; }
}
// CD 테스트용 주석
// test