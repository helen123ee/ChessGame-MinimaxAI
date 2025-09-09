package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Utilitário para carregar e redimensionar imagens/ícones do projeto.
 * Procura primeiro no classpath (/resources/...), e depois na pasta local "resources/".
 * Mantém cache por (nomeArquivo, tamanho).
 */
public final class ImageUtil {

    private static final String CLASSPATH_PREFIX = "/resources/";
    private static final String FILE_PREFIX = "resources" + File.separator;

    // Cache de ícones escalados: chave = nomeArquivo + "|" + size
    private static final Map<String, ImageIcon> ICON_CACHE = new ConcurrentHashMap<>();

    private ImageUtil() { /* utilitário */ }

    /**
     * Retorna um ImageIcon da peça (K,Q,R,B,N,P) para a cor indicada.
     * Usa a convenção de nomes do seu resources: "wK.png", "bQ.png", etc.
     * Cria um placeholder (com letra) se a imagem não existir.
     *
     * @param isWhite true para branca, false para preta
     * @param pieceChar um de K,Q,R,B,N,P (case-insensitive)
     * @param size tamanho (largura=altura) em px
     */
    public static ImageIcon getPieceIcon(boolean isWhite, char pieceChar, int size) {
        char p = Character.toUpperCase(pieceChar);
        if ("KQRBNP".indexOf(p) < 0) {
            // caractere inválido → placeholder com '?'
            return placeholderIcon('?', isWhite, size);
        }
        String prefix = isWhite ? "w" : "b";
        String filename = prefix + p + ".png";
        ImageIcon icon = getIcon(filename, size);

        if (icon == null) {
            // Fallback para placeholder se a imagem estiver ausente (ex.: wB.png não listado)
            return placeholderIcon(p, isWhite, size);
        }
        return icon;
    }

    /**
     * Carrega um ImageIcon do resources, redimensionando para size x size.
     * Usa cache para evitar reprocessamento.
     *
     * @param filename nome do arquivo (ex.: "wK.png")
     * @param size tamanho desejado (px)
     */
    public static ImageIcon getIcon(String filename, int size) {
        String cacheKey = filename + "|" + size;
        ImageIcon cached = ICON_CACHE.get(cacheKey);
        if (cached != null) return cached;

        BufferedImage img = loadBuffered(filename);
        if (img == null) return null;

        Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);
        ICON_CACHE.put(cacheKey, icon);
        return icon;
    }

    /**
     * Tenta carregar a imagem como BufferedImage:
     * 1) do classpath: /resources/filename
     * 2) do disco: resources/filename
     */
    public static BufferedImage loadBuffered(String filename) {
        // 1) Classpath
        try {
            URL url = ImageUtil.class.getResource(CLASSPATH_PREFIX + filename);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (IOException ignored) { }

        // 2) Arquivo local
        try {
            File f = new File(FILE_PREFIX + filename);
            if (f.exists()) {
                return ImageIO.read(f);
            }
        } catch (IOException ignored) { }

        return null; // não encontrado
    }

    /**
     * Gera um ícone placeholder com fundo e letra (ex.: 'K', 'Q', ...).
     * Útil quando a imagem da peça não está disponível.
     */
    public static ImageIcon placeholderIcon(char pieceChar, boolean isWhite, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            // Fundo
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = isWhite ? new Color(245, 245, 245) : new Color(60, 60, 60);
            Color fg = isWhite ? new Color(25, 25, 25) : new Color(230, 230, 230);

            g.setColor(bg);
            g.fillRoundRect(0, 0, size, size, size / 6, size / 6);

            // Borda leve
            g.setColor(isWhite ? new Color(200, 200, 200) : new Color(40, 40, 40));
            g.drawRoundRect(0, 0, size - 1, size - 1, size / 6, size / 6);

            // Letra central
            g.setColor(fg);
            int fontSize = Math.max(12, (int)(size * 0.55));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
            FontMetrics fm = g.getFontMetrics();
            String s = String.valueOf(Character.toUpperCase(pieceChar));
            int textW = fm.stringWidth(s);
            int textH = fm.getAscent();

            int x = (size - textW) / 2;
            int y = (size + textH) / 2 - Math.max(2, size / 30);
            g.drawString(s, x, y);
        } finally {
            g.dispose();
        }
        return new ImageIcon(img);
    }
}
