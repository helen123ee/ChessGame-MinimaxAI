package ai;

import controller.Game;
import model.board.Board;
import model.board.Move;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Nível 3: Usa o algoritmo Minimax com poda Alfa-Beta para escolher o melhor movimento.
 */
public class MinimaxAI {

    // Profundidade da busca (quantos lances à frente a IA vai analisar)
    private static final int SEARCH_DEPTH = 3;

    // --- Valores das Peças para a Função de Avaliação ---
    private static final int PAWN_VALUE = 100;   // [cite: 209]
    private static final int KNIGHT_VALUE = 320; // [cite: 210]
    private static final int BISHOP_VALUE = 330; // [cite: 211]
    private static final int ROOK_VALUE = 500;   // [cite: 212]
    private static final int QUEEN_VALUE = 900;  // [cite: 213]
    private static final int KING_VALUE = 20000; // [cite: 214]

    /**
     * Ponto de entrada da IA. Encontra o melhor movimento possível.
     */
    public Move findBestMove(Game game) {
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE; // Começamos com o pior valor possível para o jogador maximizador (Pretas)

        for (Move move : getAllPossibleMoves(game)) {
            // Cria um "clone" do jogo para simular o movimento
            Game tempGame = game.snapshot();
            tempGame.move(move.getFrom(), move.getTo(), move.getPromotion());

            // Chama o minimax para avaliar a posição após este movimento
            int moveValue = minimax(tempGame, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

            // O jogador Preto (minimizador) quer o menor valor, mas como a primeira chamada é feita
            // a partir daqui, invertemos a lógica para encontrar o "melhor" movimento para as pretas.
            // Para simplificar, vamos pensar que as Pretas também querem maximizar seu resultado negativo.
            // No contexto do jogador atual (Pretas), um valor menor é melhor.
            // No entanto, a implementação do minimax abaixo assume que o jogador inicial é o MAX.
            // Para as pretas (MIN), queremos o menor valor. A lógica inicial precisa ser ajustada.
            // Vamos refazer a lógica do findBestMove para o jogador MIN (Pretas).
        }

        // Lógica correta para o jogador Minimizador (Pretas)
        int worstValue = Integer.MAX_VALUE;
        for (Move move : getAllPossibleMoves(game)) {
            Game tempGame = game.snapshot();
            tempGame.move(move.getFrom(), move.getTo(), move.getPromotion());

            // A vez agora é do jogador Maximizador (Brancas)
            int moveValue = minimax(tempGame, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

            if (moveValue < worstValue) {
                worstValue = moveValue;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Implementação do algoritmo Minimax com poda Alfa-Beta.
     * @param game O estado atual do jogo.
     * @param depth A profundidade restante da busca.
     * @param alpha O melhor valor para o maximizador até agora.
     * @param beta O melhor valor para o minimizador até agora.
     * @param isMaximizingPlayer True se for a vez das Brancas (maximizador), False para as Pretas (minimizador).
     * @return A avaliação da posição.
     */
    private int minimax(Game game, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        if (depth == 0 || game.isGameOver()) {
            return evaluateBoard(game.board());
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : getAllPossibleMoves(game)) {
                Game tempGame = game.snapshot();
                tempGame.move(move.getFrom(), move.getTo(), move.getPromotion());
                int eval = minimax(tempGame, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) { // Poda Alfa-Beta [cite: 435]
                    break;
                }
            }
            return maxEval;
        } else { // Minimizador
            int minEval = Integer.MAX_VALUE;
            for (Move move : getAllPossibleMoves(game)) {
                Game tempGame = game.snapshot();
                tempGame.move(move.getFrom(), move.getTo(), move.getPromotion());
                int eval = minimax(tempGame, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) { // Poda Alfa-Beta [cite: 445]
                    break;
                }
            }
            return minEval;
        }
    }

    /**
     * Avalia a posição atual do tabuleiro com base no material.
     * Pontuação positiva favorece as Brancas, negativa favorece as Pretas. [cite: 245]
     * @param board O tabuleiro a ser avaliado.
     * @return A pontuação da posição.
     */
    private int evaluateBoard(Board board) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.get(new Position(r, c));
                if (piece != null) {
                    score += getPieceValue(piece);
                }
            }
        }
        return score;
    }

    /**
     * Retorna o valor de uma peça. Peças brancas têm valor positivo, pretas têm negativo.
     */
    private int getPieceValue(Piece piece) {
        int value = 0;
        if (piece instanceof Pawn) value = PAWN_VALUE;
        else if (piece instanceof Knight) value = KNIGHT_VALUE;
        else if (piece instanceof Bishop) value = BISHOP_VALUE;
        else if (piece instanceof Rook) value = ROOK_VALUE;
        else if (piece instanceof Queen) value = QUEEN_VALUE;
        else if (piece instanceof King) value = KING_VALUE;

        return piece.isWhite() ? value : -value;
    }

    /**
     * Gera uma lista de todos os movimentos legais para o jogador da vez.
     */
    private List<Move> getAllPossibleMoves(Game game) {
        List<Move> allMoves = new ArrayList<>();
        Board board = game.board();
        boolean isWhiteTurn = game.whiteToMove();

        for (Piece piece : board.pieces(isWhiteTurn)) {
            Position from = piece.getPosition();
            for (Position to : game.legalMovesFrom(from)) {
                allMoves.add(new Move(from, to, piece, board.get(to), false, false, false, null));
            }
        }
        return allMoves;
    }
}