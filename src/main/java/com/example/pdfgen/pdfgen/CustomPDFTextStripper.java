package com.example.pdfgen.pdfgen;


import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CustomPDFTextStripper extends PDFTextStripper {
    private final List<Paragraph> paragraphs = new ArrayList<>();

    public CustomPDFTextStripper() throws IOException {
        super();
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        super.processTextPosition(text);
        if (text.getUnicode().trim().length() > 0) {

            // Capture paragraph data along with its position
            double x = text.getX();
            double y = text.getY();
            double width = text.getWidth();
            double height = text.getHeight();
            String textStr = text.getUnicode();

            // Store paragraph with its position
            if (paragraphs.isEmpty() || paragraphs.get(paragraphs.size() - 1).getY() != y) {
                paragraphs.add(new Paragraph(textStr, y, height));
            } else {
                paragraphs.get(paragraphs.size() - 1).addText(textStr);
            }
        }
    }

    public List<Paragraph> getParagraphs() {
        // Return the list of each line in paragraph
        return paragraphs;
    }
}

class Paragraph {
    private String text;
    private final double y; // Y position of the paragraph
    private final double height;

    public Paragraph(String text, double y, double height) {
        this.text = text;
        this.y = y;
        this.height = height;
    }

    public void addText(String newText) {
        text += " " + newText;
    }

    public String getText() {
        return text;
    }

    public double getY() {
        return y;
    }

    public double getHeight() {
        return height;
    }

    public double getBottomY() {
        return y + height; // Bottom Y position
    }
}