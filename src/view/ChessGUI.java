// ========================= src/view/ChessGUI.java =========================
package view;

import ai.MinimaxAI;
import controller.Game;
import model.board.Move;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class ChessGUI extends JFrame {

    private final Game game;
    private final MinimaxAI aiPlayer;
    private boolean isVsAI = true;

    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];

    private final JLabel status;
    private final JTextArea history;
    private final JScrollPane historyScroll;

    // Seleção atual e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Flag para anunciar fim de jogo apenas uma vez
    private boolean gameOverAnnounced = false;

    // Bordas para destacar seleção e destinos
    private static final Border BORDER_SELECTED = BorderFactory.createLineBorder(Color.BLUE, 3);
    private static final Border BORDER_LEGAL = BorderFactory.createLineBorder(new Color(0, 128, 0), 3);

    public ChessGUI() {
        super("ChessGame");
        this.game = new Game();
        this.aiPlayer = new MinimaxAI();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(Color.DARK_GRAY);

        // Cria botões das casas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f)); // fallback com Unicode
                b.addActionListener(e -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Vez: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Histórico
        history = new JTextArea(10, 20);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyScroll = new JScrollPane(history);

        // Layout principal: tabuleiro à esquerda, histórico à direita
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.add(new JLabel("Histórico de lances:"), BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);

        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Atualiza ícones conforme a janela/painel muda de tamanho
        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh(); // recarrega ícones ajustando o tamanho
            }
        });

        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setVisible(true);

        refresh();
    }

    /**
     * Lida com clique numa casa do tabuleiro.
     */
    private void handleClick(Position clicked) {
        // Se o jogo já acabou, ignora cliques
        if (game.isGameOver()) return;

        // Se for a vez da IA, não permite interação humana
        if (isVsAI && !game.whiteToMove()) return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            // Nada selecionado ainda: só seleciona se for peça da vez
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            // Já havia uma seleção
            if (game.legalMovesFrom(selected).contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                game.move(selected, clicked, promo);
                selected = null;
                legalForSelected.clear();
                // Após o lance do jogador, aciona a IA
                triggerAIMove();
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                // Troca a seleção para outra peça da vez
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            } else {
                // Clique inválido: limpa seleção
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    /**
     * Aciona a IA para fazer um movimento se for a vez dela.
     * Usa um SwingWorker para não travar a interface gráfica.
     */
    private void triggerAIMove() {
        if (isVsAI && !game.whiteToMove() && !game.isGameOver()) {
            // Desabilita o tabuleiro enquanto a IA pensa para evitar cliques
            boardPanel.setEnabled(false);
            status.setText("Vez: Pretas (pensando...)");

            // SwingWorker para rodar a IA em uma thread separada
            new SwingWorker<Move, Void>() {
                @Override
                protected Move doInBackground() throws Exception {
                    // Simula um pequeno atraso para a jogada não ser instantânea
                    Thread.sleep(1000);
                    return aiPlayer.findBestMove(game);
                }

                @Override
                protected void done() {
                    try {
                        Move aiMove = get();
                        if (aiMove != null) {
                            game.move(aiMove.getFrom(), aiMove.getTo(), aiMove.getPromotion());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // Reabilita o tabuleiro e atualiza a tela
                        boardPanel.setEnabled(true);
                        refresh();
                    }
                }
            }.execute();
        }
    }

    /**
     * Diálogo de escolha de peça para promoção.
     * Retorna 'Q','R','B','N' de acordo com a escolha.
     */
    private Character askPromotion() {
        String[] opts = {"Rainha", "Torre", "Bispo", "Cavalo"};
        int ch = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para promoção:",
                "Promoção",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opts,
                opts[0]
        );
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    /**
     * Atualiza cores, bordas, ícones das peças, status e histórico.
     * Ajusta o tamanho do ícone dinamicamente (quadrado do botão).
     */
    private void refresh() {
        // 1) Cores base e limpa bordas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? new Color(20, 77, 51) : new Color(220, 220, 220);
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
            }
        }

        // 2) Realce seleção e movimentos legais
        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        // 3) Ícones das peças (ou Unicode como fallback)
        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];

                if (p == null) {
                    b.setIcon(null);
                    b.setText("");
                    continue;
                }

                char sym = p.getSymbol().charAt(0); // "K","Q","R","B","N","P"
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon);
                    b.setText("");
                } else {
                    // Fallback: Unicode
                    b.setIcon(null);
                    b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }

        // 4) Status, histórico e estado de fim de jogo
        if (game.isGameOver()) {
            // Desabilita o tabuleiro inteiro
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    squares[r][c].setEnabled(false);
                }
            }
            String winner = game.winnerText();
            status.setText("Fim de jogo — Vencedor: " + (winner == null ? "" : winner));

            if (!gameOverAnnounced) {
                gameOverAnnounced = true;
                JOptionPane.showMessageDialog(
                        this,
                        "Rei capturado. Vencedor: " + (winner == null ? "" : winner),
                        "Fim de jogo",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } else {
            // Habilita casas (para caso inicie/novo jogo futuramente)
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    squares[r][c].setEnabled(true);
                }
            }
            String side = game.whiteToMove() ? "Brancas" : "Pretas";
            String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
            status.setText("Vez: " + side + chk);
        }

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0) sb.append((i / 2) + 1).append('.').append(' ');
            sb.append(hist.get(i)).append(' ');
            if (i % 2 == 1) sb.append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    /**
     * Converte símbolo da peça em caractere Unicode (fallback).
     */
    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    /**
     * Calcula o tamanho do ícone com base no tamanho atual das casas.
     * Usa o menor lado do primeiro botão como referência, aplicando uma pequena margem.
     */
    private int computeSquareIconSize() {
        // Pega um botão representante (0,0)
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1) {
            // Janela ainda não renderizou completamente → tamanho padrão
            return 64;
        }
        // pequena margem para não encostar na borda
        return Math.max(24, side - 6);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
