package game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DifficultyTest {
    @Test
    void easyConfig() {
        assertEquals(1, Difficulty.EASY.getMin());
        assertEquals(50, Difficulty.EASY.getMax());
        assertEquals(15, Difficulty.EASY.getMaxTries());
    }

    @Test
    void normalConfig() {
        assertEquals(1, Difficulty.NORMAL.getMin());
        assertEquals(100, Difficulty.NORMAL.getMax());
        assertEquals(10, Difficulty.NORMAL.getMaxTries());
    }

    @Test
    void hardConfig() {
        assertEquals(1, Difficulty.HARD.getMin());
        assertEquals(200, Difficulty.HARD.getMax());
        assertEquals(7, Difficulty.HARD.getMaxTries());
    }
}
