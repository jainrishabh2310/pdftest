package com.example.pdfgen.pdfgen;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFSegmentation {
    public static void segmentPDF(String inputFilePath, String outputDirectory) {
        double gapThreshold = 15.0; // Define a threshold for gap (in points)

        try {
            PDDocument document = PDDocument.load(new File(inputFilePath));
            CustomPDFTextStripper pdfStripper = new CustomPDFTextStripper();
            pdfStripper.setSortByPosition(true);
            pdfStripper.getText(document);

            List<Paragraph> paragraphs = pdfStripper.getParagraphs();
            PDDocument currentDoc = new PDDocument();
            StringBuilder str = new StringBuilder();

            for (int i = 0; i < paragraphs.size(); i++) {
                Paragraph currentParagraph = paragraphs.get(i);
                boolean shouldSplit = false;

                // Check for gap with the next paragraph
                if (i < paragraphs.size() - 1) {
                    Paragraph nextParagraph = paragraphs.get(i + 1);
                    double gap = nextParagraph.getY() - currentParagraph.getBottomY();

                    // if the gap is greater than threshold so it spilit
                    if (gap > gapThreshold) {
                        shouldSplit = true; // Set flag to split if gap is greater than threshold
                    }
                }

                // Accumulate paragraph text, replacing newlines with spaces
                str.append(currentParagraph.getText().replace("\n", " ")).append("\n"); // Preserve paragraph breaks

                // If gap is indicate so now we need to create pdf for this paragraph
                if (shouldSplit || i == paragraphs.size() - 1) { // Save last paragraph if it's the end
                    PDPage page = new PDPage(PDRectangle.A4);
                    currentDoc.addPage(page);
                    try (PDPageContentStream contentStream = new PDPageContentStream(currentDoc, page)) {
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, 700); // Set initial text position

                        // Split the text into lines and write each line separately
                        String[] lines = str.toString().split("\n");
                        float yPosition = 200; // Initial Y position
                        for (String line : lines) {
                            contentStream.showText(line); // Show each line
                            yPosition -= 15; // Adjust Y position for next line (15 points down)
                            contentStream.newLineAtOffset(0, -15); // Move cursor down
                        }

                        contentStream.endText();
                    }

                    saveDocument(currentDoc, outputDirectory, "output_file" + (i + 1));
                    currentDoc = new PDDocument(); // Start a new document for the next paragraph
                    str.setLength(0); // Clear the string for the next accumulation
                }
            }

            // Save any remaining document
            if (currentDoc.getNumberOfPages() > 0) {
                saveDocument(currentDoc, outputDirectory, "output_file" + paragraphs.size());
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveDocument(PDDocument document, String outputDirectory, String fileName) {
        try {
            String outputFilePath = outputDirectory + fileName + ".pdf";
            document.save(outputFilePath);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
