package com.AITemplate.util;

import com.AITemplate.model.Portfolio;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PdfGenerator {

    private static final Pattern emojiPattern = Pattern.compile("[\uD83C-\uDBFF\uDC00-\uDFFF]+");
    private static final float MARGIN_LEFT = 50f;
    private static final float MARGIN_TOP = 700f;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float USABLE_WIDTH = PAGE_WIDTH - 2 * MARGIN_LEFT;
    private static final float FONT_SIZE = 12f;
    private static final float LINE_HEIGHT = 16f;
    private static final int MAX_LINES_PER_PAGE = (int) ((MARGIN_TOP - 50f) / LINE_HEIGHT);

    public static void generatePortfolioPdfZip(Portfolio portfolio, OutputStream outputStream) throws IOException {
        String clean = cleanText(portfolio.getContent());
        List<String> wrappedLines = wrapText(clean);

        List<ByteArrayOutputStream> pdfParts = new ArrayList<>();
        int totalLines = wrappedLines.size();

        for (int i = 0; i < totalLines; i += MAX_LINES_PER_PAGE) {
            int end = Math.min(i + MAX_LINES_PER_PAGE, totalLines);
            List<String> linesForPage = wrappedLines.subList(i, end);

            ByteArrayOutputStream partStream = new ByteArrayOutputStream();
            boolean includeImage = (i == 0);
            writePdfPart(portfolio, linesForPage, partStream, includeImage);
            pdfParts.add(partStream);
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            for (int i = 0; i < pdfParts.size(); i++) {
                String entryName = String.format("portfolio_%s_part%d.pdf", portfolio.getTitle(), i + 1);
                zipOut.putNextEntry(new ZipEntry(entryName));
                zipOut.write(pdfParts.get(i).toByteArray());
                zipOut.closeEntry();
            }
        }
    }
    public static void generatePortfolioPdf(Portfolio portfolio, OutputStream outputStream) throws IOException {
        String clean = cleanText(portfolio.getContent());
        List<String> wrappedLines = wrapText(clean);

        try (PDDocument document = new PDDocument()) {
            InputStream fontStream = PdfGenerator.class.getResourceAsStream("/fonts/NotoSansKR-Regular.ttf");
            if (fontStream == null) throw new IOException("폰트 파일을 찾을 수 없습니다.");
            PDType0Font font = PDType0Font.load(document, fontStream);

            int totalLines = wrappedLines.size();
            int startIndex = 0;

            while (startIndex < totalLines) {
                PDPage page = new PDPage();
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                float textStartY = MARGIN_TOP;

                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                contentStream.setLeading(LINE_HEIGHT);
                contentStream.newLineAtOffset(MARGIN_LEFT, textStartY);

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
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page);
                     InputStream imageStream = openImageStreamFromUrl(portfolio.getImageUrl())) {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(), "image");

                    float maxImageWidth = USABLE_WIDTH;
                    float scale = maxImageWidth / image.getWidth();
                    float imageWidth = image.getWidth() * scale;
                    float imageHeight = image.getHeight() * scale;

                    float x = MARGIN_LEFT;
                    float y = PDRectangle.LETTER.getHeight() - imageHeight - 80f;

                    contentStream.drawImage(image, x, y, imageWidth, imageHeight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            document.save(outputStream);
        }
    }
    private static void writePdfPart(Portfolio portfolio, List<String> lines, OutputStream outputStream, boolean includeImage) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            InputStream fontStream = PdfGenerator.class.getResourceAsStream("/fonts/NotoSansKR-Regular.ttf");
            if (fontStream == null) throw new IOException("폰트 파일을 찾을 수 없습니다.");

            PDType0Font font = PDType0Font.load(document, fontStream);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float textStartY = MARGIN_TOP;

            if (includeImage && portfolio.getImageUrl() != null && !portfolio.getImageUrl().isBlank()) {
                try (InputStream imageStream = openImageStreamFromUrl(portfolio.getImageUrl())) {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, imageStream.readAllBytes(), "image");

                    float maxImageWidth = USABLE_WIDTH;
                    float scale = maxImageWidth / image.getWidth();
                    float imageWidth = image.getWidth() * scale;
                    float imageHeight = image.getHeight() * scale;

                    float x = MARGIN_LEFT;
                    float y = PDRectangle.LETTER.getHeight() - imageHeight - 80f;

                    contentStream.drawImage(image, x, y, imageWidth, imageHeight);

                    textStartY = y - 20f;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            contentStream.beginText();
            contentStream.setFont(font, FONT_SIZE);
            contentStream.setLeading(LINE_HEIGHT);
            contentStream.newLineAtOffset(MARGIN_LEFT, textStartY);

            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLine();
            }

            contentStream.endText();
            contentStream.close();

            document.save(outputStream);
        }
    }

    private static InputStream openImageStreamFromUrl(String rawUrl) throws IOException {
        String decodedUrl = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8); // URL 먼저 디코딩
        return new URL(decodedUrl).openStream(); // 그대로 스트림으로 열기
    }

    private static String cleanText(String input) {
        if (input == null) return "";
        String noEmoji = emojiPattern.matcher(input).replaceAll("");
        return noEmoji.replace("*", "");
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
