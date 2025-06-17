package com.AITemplate.util;

import com.AITemplate.model.Portfolio;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PdfGenerator {

    private static final Pattern emojiPattern = Pattern.compile("[\uD83C-\uDBFF\uDC00-\uDFFF]+");
    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_TOP = 700f;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float USABLE_WIDTH = PAGE_WIDTH - 2 * MARGIN_LEFT;
    private static final float FONT_SIZE = 12f;
    private static final float LINE_HEIGHT = 16f;
    private static final int MAX_LINES_PER_PAGE = (int) ((MARGIN_TOP - 50f) / LINE_HEIGHT);

    public static void generatePortfolioPdf(Portfolio portfolio, OutputStream outputStream) throws IOException {
        String content = cleanText(portfolio.getContent());
        List<String> wrappedLines = wrapText(content);

        try (PDDocument document = new PDDocument()) {
            InputStream fontStream = PdfGenerator.class.getResourceAsStream("/fonts/NotoSansKR-Regular.ttf");
            if (fontStream == null) throw new IOException("폰트 파일을 찾을 수 없습니다.");
            PDType0Font font = PDType0Font.load(document, fontStream);

            int totalLines = wrappedLines.size();
            int startIndex = 0;

            PDPage lastPage = null;

            while (startIndex < totalLines) {
                PDPage page = new PDPage();
                document.addPage(page);
                lastPage = page;

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.setLeading(LINE_HEIGHT);
                contentStream.newLineAtOffset(MARGIN_LEFT, MARGIN_TOP);

                int endIndex = Math.min(startIndex + MAX_LINES_PER_PAGE, totalLines);
                for (int i = startIndex; i < endIndex; i++) {
                    contentStream.showText(wrappedLines.get(i));
                    contentStream.newLine();
                }

                contentStream.endText();
                contentStream.close();
                startIndex = endIndex;
            }

            if (portfolio.getImageUrl() != null && !portfolio.getImageUrl().isBlank()) {
                try (PDPageContentStream imageStream = new PDPageContentStream(document, lastPage, PDPageContentStream.AppendMode.APPEND, true, true);
                     InputStream imageInput = openImageStreamFromUrl(portfolio.getImageUrl())) {

                    PDImageXObject image = PDImageXObject.createFromByteArray(document, imageInput.readAllBytes(), "image");

                    float maxImageWidth = USABLE_WIDTH;
                    float scale = maxImageWidth / image.getWidth();
                    float imageWidth = image.getWidth() * scale;
                    float imageHeight = image.getHeight() * scale;

                    float x = MARGIN_LEFT;
                    float y = 50f;

                    if (imageHeight > (MARGIN_TOP - 50f)) {
                        PDPage newPage = new PDPage();
                        document.addPage(newPage);
                        try (PDPageContentStream newImageStream = new PDPageContentStream(document, newPage)) {
                            float y2 = PDRectangle.LETTER.getHeight() - imageHeight - 80f;
                            newImageStream.drawImage(image, MARGIN_LEFT, y2, imageWidth, imageHeight);
                        }
                    } else {
                        imageStream.drawImage(image, x, y, imageWidth, imageHeight);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            document.save(outputStream);
        }
    }

    private static InputStream openImageStreamFromUrl(String rawUrl) throws IOException {
        String decodedUrl = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8);
        return new URL(decodedUrl).openStream();
    }

    private static String cleanText(String input) {
        if (input == null) return "";
        String noEmoji = emojiPattern.matcher(input).replaceAll("");
        return noEmoji.replace("*", ""); // 필요 시 제거
    }

    private static List<String> wrapText(String content) {
        List<String> result = new ArrayList<>();
        try (PDDocument tempDoc = new PDDocument()) {
            InputStream fontStream = PdfGenerator.class.getResourceAsStream("/fonts/NotoSansKR-Regular.ttf");
            PDType0Font font = PDType0Font.load(tempDoc, fontStream);

            for (String line : content.split("\n")) {
                StringBuilder currentLine = new StringBuilder();
                for (String word : line.split(" ")) {
                    String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
                    float textWidth = font.getStringWidth(testLine) / 1000 * FONT_SIZE;

                    if (textWidth > USABLE_WIDTH) {
                        result.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        if (!currentLine.isEmpty()) currentLine.append(" ");
                        currentLine.append(word);
                    }
                }
                result.add(currentLine.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
