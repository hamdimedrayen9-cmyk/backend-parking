package com.parking.service;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.parking.entity.Reservation;
import com.parking.entity.Statistique;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

@Service
public class ExportService {

    // ===== EXCEL EXPORTS =====

    public void exportReservationsToExcel(HttpServletResponse response, List<Reservation> reservations) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Réservations");

        // Header Style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        // Header Row
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Client", "Parking", "Place", "Véhicule", "Date Début", "Date Fin", "Statut"};
        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data Rows
        int rowIdx = 1;
        for (Reservation res : reservations) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(res.getId());
            row.createCell(1).setCellValue(res.getClient().getNom());
            row.createCell(2).setCellValue(res.getPlace().getParking().getNom());
            row.createCell(3).setCellValue(res.getPlace().getNumero());
            row.createCell(4).setCellValue(res.getVehicule().getImmatriculation());
            row.createCell(5).setCellValue(res.getDateDebut().toString());
            row.createCell(6).setCellValue(res.getDateFin().toString());
            row.createCell(7).setCellValue(res.getStatut());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportStatsToExcel(HttpServletResponse response, List<Statistique> stats) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Statistiques");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        headerStyle.setFont(font);

        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        String[] columns = {"Parking", "Total Réservations", "Revenus Totaux (DT)"};
        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Statistique stat : stats) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(stat.getParking().getNom());
            row.createCell(1).setCellValue(stat.getNombreReservations());
            row.createCell(2).setCellValue(stat.getRevenus());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ===== PDF EXPORTS =====

    public void exportReservationsToPdf(HttpServletResponse response, List<Reservation> reservations) throws IOException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(Color.BLUE);

        Paragraph p = new Paragraph("Rapport des Réservations", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10);

        writeReservationPdfHeader(table);
        writeReservationPdfData(table, reservations);

        document.add(table);
        document.close();
    }

    private void writeReservationPdfHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);

        com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        String[] headers = {"ID", "Client", "Parking", "Place", "Véhicule", "Début", "Fin", "Statut"};
        for (String header : headers) {
            cell.setPhrase(new Phrase(header, font));
            table.addCell(cell);
        }
    }

    private void writeReservationPdfData(PdfPTable table, List<Reservation> reservations) {
        for (Reservation res : reservations) {
            table.addCell(String.valueOf(res.getId()));
            table.addCell(res.getClient().getNom());
            table.addCell(res.getPlace().getParking().getNom());
            table.addCell(String.valueOf(res.getPlace().getNumero()));
            table.addCell(res.getVehicule().getImmatriculation());
            table.addCell(res.getDateDebut().toString());
            table.addCell(res.getDateFin().toString());
            table.addCell(res.getStatut());
        }
    }

    public void exportStatsToPdf(HttpServletResponse response, List<Statistique> stats) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(new Color(0, 128, 128));

        Paragraph p = new Paragraph("Rapport Statistique et Revenus", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10);

        writeStatsPdfHeader(table);
        writeStatsPdfData(table, stats);

        document.add(table);
        document.close();
    }

    private void writeStatsPdfHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(0, 128, 128));
        cell.setPadding(5);

        com.lowagie.text.Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        String[] headers = {"Parking", "Réservations", "Revenus (DT)"};
        for (String header : headers) {
            cell.setPhrase(new Phrase(header, font));
            table.addCell(cell);
        }
    }

    private void writeStatsPdfData(PdfPTable table, List<Statistique> stats) {
        for (Statistique stat : stats) {
            table.addCell(stat.getParking().getNom());
            table.addCell(String.valueOf(stat.getNombreReservations()));
            table.addCell(String.valueOf(stat.getRevenus()) + " DT");
        }
    }
}

