package tn.esprit.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import tn.esprit.entities.Commande;
import tn.esprit.entities.CommandeProduit;
import tn.esprit.entities.User;
import tn.esprit.tools.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class FacturePDFService {

    private final User currentUser = SessionManager.getInstance().getCurrentUser();

    public byte[] generateFacturePDF(Commande commande) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            createPdfContent(document, commande);
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createPdfContent(PDDocument document, Commande commande) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Set up coordinates and styles
            float margin = 50;
            float yPosition = 700;
            float lineHeight = 15;
            contentStream.setLineWidth(1f);

            // Add title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("FACTURE");
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Add invoice details
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Facture N°: CMD-" + commande.getId());
            contentStream.endText();
            yPosition -= lineHeight;

            String dateStr = commande.getDateCreation() != null
                    ? commande.getDateCreation().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "[Date non spécifiée]";
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Date: " + dateStr);
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Client information
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Client: " + currentUser.getLastName() + " " + currentUser.getName());
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Draw the products table
            drawProductTable(contentStream, margin, yPosition, commande);
            yPosition -= (lineHeight * 2 * commande.getCommandeProduits().size());

            // Calculate totals
            float subtotal = calculateSubtotal(commande);
            float tva = subtotal * 0.19f;
            float total = subtotal + tva;

            // Add totals section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin + 280, yPosition);
            contentStream.showText("Sous-total: " + String.format("%.2f TND", subtotal));
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin + 280, yPosition);
            contentStream.showText("TVA (19%): " + String.format("%.2f TND", tva));
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(margin + 280, yPosition);
            contentStream.showText("Total TTC: " + String.format("%.2f TND", total));
            contentStream.endText();

            // Add footer
            yPosition -= lineHeight * 2;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Merci pour votre confiance !");
            contentStream.endText();
        }
    }

    private void drawProductTable(PDPageContentStream contentStream, float margin, float startY, Commande commande)
            throws IOException {
        float yPosition = startY;
        float lineHeight = 15;
        float[] columnWidths = {200, 80, 100, 100}; // Product, Quantity, Unit Price, Total

        // Table header
        contentStream.setNonStrokingColor(220, 220, 220); // Light gray background
        contentStream.addRect(margin, yPosition - 15,
                columnWidths[0] + columnWidths[1] + columnWidths[2] + columnWidths[3], 20);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0); // Black text

        // Header text
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText("Produit");
        contentStream.newLineAtOffset(columnWidths[0], 0);
        contentStream.showText("Quantité");
        contentStream.newLineAtOffset(columnWidths[1], 0);
        contentStream.showText("Prix Unitaire");
        contentStream.newLineAtOffset(columnWidths[2], 0);
        contentStream.showText("Total");
        contentStream.endText();
        yPosition -= lineHeight;

        // Table rows
        for (CommandeProduit cp : commande.getCommandeProduits()) {
            String productName = cp.getProduit() != null ? cp.getProduit().getNom() : "N/A";
            String quantity = String.valueOf(cp.getQuantite());
            String unitPrice = String.format("%.2f TND", cp.getPrixUnitaire());
            String total = String.format("%.2f TND", cp.getQuantite() * cp.getPrixUnitaire());

            // Draw row
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(productName);
            contentStream.newLineAtOffset(columnWidths[0], 0);
            contentStream.showText(quantity);
            contentStream.newLineAtOffset(columnWidths[1], 0);
            contentStream.showText(unitPrice);
            contentStream.newLineAtOffset(columnWidths[2], 0);
            contentStream.showText(total);
            contentStream.endText();

            // Draw line under row
            contentStream.moveTo(margin, yPosition - 5);
            contentStream.lineTo(margin + columnWidths[0] + columnWidths[1] + columnWidths[2] + columnWidths[3], yPosition - 5);
            contentStream.stroke();

            yPosition -= lineHeight;
        }
    }

    private float calculateSubtotal(Commande commande) {
        return commande.getCommandeProduits().stream()
                .map(cp -> cp.getQuantite() * cp.getPrixUnitaire())
                .reduce(0f, Float::sum);
    }
}