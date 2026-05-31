package game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BestScoreTest {
    @Test
    void firstScoreIsAlwaysBest() {
        BestScore best = new BestScore();
        assertFalse(best.hasScore());
        assertTrue(best.update(5));
        assertEquals(5, best.get());
    }

    @Test
    void lowerScoreReplaces() {
        BestScore best = new BestScore();
        best.update(5);
        assertTrue(best.update(3));
        assertEquals(3, best.get());
    }

    @Test
    void higherScoreIgnored() {
        BestScore best = new BestScore();
        best.update(3);
        assertFalse(best.update(8));
        assertEquals(3, best.get());
    }
}
