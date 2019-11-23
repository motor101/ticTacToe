import javafx.util.Pair;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

enum GameState {
    NOT_OVER,
    DRAW,
    FIRST_PLAYER_WINS,
    SECOND_PLAYER_WINS
}

final class Move {
    final int row;
    final int column;

    Move(int row, int column) {
        this.row = row;
        this.column = column;
    }
}

enum Placeholder {
    EMPTY,
    FIRST_PLAYER,
    SECOND_PLAYER;

    @Override
    public String toString() {
        if (this == EMPTY) {
            return " ";
        } else if (this == FIRST_PLAYER) {
            return "X";
        } else {
            return "O";
        }
    }
}

public class Game {
    private static Scanner input = new Scanner(System.in);

    private Set<Move> movesRemaining;
    private Placeholder[][] board;
    private int playedMovesCount = 0;
    private final int boardSize;

    private Game(int boardSize) {
        this.boardSize = boardSize;
        movesRemaining = new HashSet<>();
        board = new Placeholder[boardSize][boardSize];

        for (int row = 0; row < boardSize; row++) {
            for (int column = 0; column < boardSize; column++) {
                board[row][column] = Placeholder.EMPTY;
                movesRemaining.add(new Move(row, column));
            }
        }
    }

    private void printBoard() {
        for (int i = 0; i < boardSize; i++) {

            for (int j = 0; j < 2 * boardSize; j++) {
                System.out.print("-");
            }
            System.out.println();

            for (int j = 0; j < boardSize; j++) {
                System.out.print("|");
                if (board[i][j] == Placeholder.EMPTY) {
                    System.out.print(" ");
                }
            }
            System.out.println();

        }
    }

    private boolean playMove(Move move, Placeholder placeholder) {
        if (!movesRemaining.contains(move)) {
            return false;
        }

        movesRemaining.remove(move);
        exploreMove(move, placeholder);
        return true;
    }

    private void exploreMove(Move move, Placeholder placeholder) {
        board[move.row][move.column] = placeholder;
        ++playedMovesCount;
    }

    private void undoMove(Move move) {
        board[move.row][move.column] = Placeholder.EMPTY;
        --playedMovesCount;
    }

    private boolean rowWins(Placeholder placeholder) {
        int column;
        for (int row = 0; row < boardSize; row++) {
            for (column = 0; column < boardSize; column++) {
                if (board[row][column] != placeholder) {
                    break;
                }
            }
            if (column == boardSize) {
                return true;
            }
        }
        return false;
    }

    private boolean columnWins(Placeholder placeholder) {
        int row;
        for (int column = 0; column < boardSize; column++) {
            for (row = 0; row < boardSize; row++) {
                if (board[row][column] != placeholder) {
                    break;
                }
            }
            if (row == boardSize) {
                return true;
            }
        }
        return false;
    }

    private boolean diagonalWins(Placeholder placeholder) {
        int i;
        for (i = 0; i < boardSize; i++) {
            if (board[i][i] != placeholder) {
                break;
            }
        }
        if (i == boardSize) {
            return true;
        }

        for (i = 0; i < boardSize; i++) {
            if (board[i][(boardSize - 1) - i] != placeholder) {
                break;
            }
        }
        if (i == boardSize) {
            return true;
        }

        return false;
    }

    private GameState getGameState() {
        // There is a minimal number of moves that need to be played for the game to be over
        if (playedMovesCount < ((boardSize * 2) - 1)) {
            return GameState.NOT_OVER;
        }

        if (columnWins(Placeholder.FIRST_PLAYER) || rowWins(Placeholder.FIRST_PLAYER)
                || diagonalWins(Placeholder.FIRST_PLAYER)) {
            return GameState.FIRST_PLAYER_WINS;
        }

        if (columnWins(Placeholder.SECOND_PLAYER) || rowWins(Placeholder.SECOND_PLAYER)
                || diagonalWins(Placeholder.SECOND_PLAYER)) {
            return GameState.SECOND_PLAYER_WINS;
        }

        if (playedMovesCount == boardSize * boardSize) {
            return GameState.DRAW;
        }

        return GameState.NOT_OVER;
    }

    private Move enterMove() {
        int row = input.nextInt();
        int column = input.nextInt();

        return new Move(row, column);
    }

    private Move getBestMove(boolean maximizingPlayer) {
        // you must use the minmax algorithm here
        Pair<Integer, Move> move = minMax(movesRemaining, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, maximizingPlayer);

        return move.getValue();
    }

    private Pair<Integer, Move> minMax(Set<Move> movesRemaining, int depth, int alpha, int beta, boolean maximizingPlayer) {
        GameState gameState = getGameState();
        if (gameState.equals(GameState.DRAW)) {
            return new Pair<>((0 - depth), null);
        } else if (gameState.equals(GameState.FIRST_PLAYER_WINS)) {
            return new Pair<>((1000 - depth), null);
        } else if (gameState.equals(GameState.SECOND_PLAYER_WINS)) {
            return new Pair<>((-1000 - depth), null);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            Move bestMove = null;
            for (Move move : movesRemaining) {
                Set<Move> subset = new HashSet<>(movesRemaining);
                subset.remove(move);
                exploreMove(move, Placeholder.FIRST_PLAYER);
                Pair<Integer, Move> result = minMax(subset, depth + 1, alpha, beta, false);
                int eval = result.getKey();
                undoMove(move);
                if (maxEval < eval) {
                    maxEval = eval;
                    bestMove = move;
                }
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    break;
                }
            }

            return new Pair<Integer, Move>(maxEval, bestMove);
        } else {
            int minEval = Integer.MAX_VALUE;
            Move bestMove = null;
            for (Move move : movesRemaining) {
                Set<Move> subset = new HashSet<>(movesRemaining);
                subset.remove(move);
                exploreMove(move, Placeholder.SECOND_PLAYER);
                Pair<Integer, Move> result = minMax(subset, depth + 1, alpha, beta, true);
                int eval = result.getKey();
                undoMove(move);
                if (minEval > eval) {
                    minEval = eval;
                    bestMove = move;
                }
                beta = Math.min(beta, eval);
                if (alpha >= beta) {
                    break;
                }
            }

            return new Pair<Integer, Move>(minEval, bestMove);
        }
    }

    private void play(boolean personIsOnTheMove) {
        Move move = null;
        Placeholder placeholder = null;
        boolean firstPlayer = true;

        while (true) {
            if (personIsOnTheMove) {
                printBoard();
                personIsOnTheMove = false;
                move = enterMove();
            } else {
                personIsOnTheMove = true;
                move = getBestMove(firstPlayer);
            }
            if (firstPlayer) {
                placeholder = Placeholder.FIRST_PLAYER;
            } else {
                placeholder = Placeholder.SECOND_PLAYER;
            }
            playMove(move, placeholder);

            GameState gameState = getGameState();
            if (gameState.equals(GameState.DRAW)) {
                System.out.println("Result is draw");
            } else if (gameState.equals(GameState.FIRST_PLAYER_WINS)) {
                System.out.println("Crosses win");
            } else if (gameState.equals(GameState.SECOND_PLAYER_WINS)) {
                System.out.println("Circles win");
            }
            if (!gameState.equals(GameState.NOT_OVER)) {
                printBoard();
                break;
            }
            firstPlayer = !firstPlayer;
        }
    }

    public static void main(String[] args) {
        Game game = new Game(3);
        System.out.println("Are you first?(y/n)");
        String answer = input.nextLine();
        boolean personIsOnTheMove = false;
        if (answer.equals("y")) {
            personIsOnTheMove = true;
        }
        game.play(personIsOnTheMove);
    }
}
