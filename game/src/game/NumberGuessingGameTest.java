package game;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class NumberGuessingGameTest {

    @Test
    void correctGuessWins() {
        NumberGuessingGame game = new NumberGuessingGame(1, 100, 10, 42);
        assertEquals(GuessResult.WIN, game.guess(42));
        assertTrue(game.isOver());
        assertEquals(1, game.getAttempts());
    }

    @Test
    void lowGuessReturnsTooLow() {
        NumberGuessingGame game = new NumberGuessingGame(1, 100, 10, 42);
        assertEquals(GuessResult.TOO_LOW, game.guess(10));
        assertFalse(game.isOver());
    }

    @Test
    void highGuessReturnsTooHigh() {
        NumberGuessingGame game = new NumberGuessingGame(1, 100, 10, 42);
        assertEquals(GuessResult.TOO_HIGH, game.guess(90));
        assertFalse(game.isOver());
    }

    @Test
    void outOfRangeGuessIsInvalidAndDoesNotConsumeAttempt() {
        NumberGuessingGame game = new NumberGuessingGame(1, 100, 10, 42);
        assertEquals(GuessResult.INVALID, game.guess(0));
        assertEquals(GuessResult.INVALID, game.guess(101));
        assertEquals(0, game.getAttempts());
        assertFalse(game.isOver());
    }

    @Test
    void runningOutOfAttemptsLoses() {
        NumberGuessingGame game = new NumberGuessingGame(1, 100, 3, 42);
        assertEquals(GuessResult.TOO_LOW, game.guess(1));
        assertEquals(GuessResult.TOO_LOW, game.guess(2));
        assertEquals(GuessResult.LOSE, game.guess(3));
        assertTrue(game.isOver());
        assertEquals(0, game.getRemaining());
    }

    @Test
    void remainingDecrementsPerAttempt() {
        NumberGuessingGame game = new NumberGuessingGame(1, 100, 10, 42);
        game.guess(10);
        assertEquals(9, game.getRemaining());
        assertEquals(1, game.getAttempts());
    }

    @Test
    void guessAfterGameOverThrows() {
        NumberGuessingGame game = new NumberGuessingGame(1, 100, 10, 42);
        game.guess(42);
        assertThrows(IllegalStateException.class, () -> game.guess(1));
    }

    @Test
    void forDifficultyProducesSecretInRange() {
        NumberGuessingGame game = NumberGuessingGame.forDifficulty(Difficulty.HARD, new Random(123));
        assertTrue(game.getSecret() >= Difficulty.HARD.getMin());
        assertTrue(game.getSecret() <= Difficulty.HARD.getMax());
        assertEquals(Difficulty.HARD.getMaxTries(), game.getMaxTries());
    }
}
