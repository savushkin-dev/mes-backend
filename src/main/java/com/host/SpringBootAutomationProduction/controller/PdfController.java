package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.dto.PageDataDTO;
import com.host.SpringBootAutomationProduction.service.PdfService;
import com.lowagie.text.pdf.BaseFont;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.spring6.SpringTemplateEngine;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;


@Slf4j
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfService pdfService;

    @Autowired
    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(@RequestBody List<PageDataDTO> pages) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .body(pdfService.createPdf(pages));
    }

    //1 Посмотреть рендер на фронте, скорее всего придется переносить на бэк


    @PostMapping("/generate2")
    public ResponseEntity<byte[]> generatePdf2(@RequestBody List<PageDataDTO> pages) throws IOException {
        try {
            byte[] pdfContents = createPdf(pages);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/pdf");
            headers.add("Content-Disposition", "attachment; filename=output.pdf");
            return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] createPdf(List<PageDataDTO> pages) throws Exception {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html lang=\"ru\"><head>");
        htmlBuilder.append("<meta charset=\"UTF-8\"/>");

        for (PageDataDTO page : pages) {
            if (page.getStyles() != null) {
                htmlBuilder.append("<style>")
//                        .append("@font-face {")
//                        .append("    font-family: 'Arial';")
//                        .append("    src: local('Arial');") // Используем системный Arial
//                        .append("}")
//                        .append(" * { font-family: Arial; }/n")
                        .append(" @page { margin: 0px; }")
                        .append(" body {" +
                                "    font-family: Verdana, Arial, Times New Roman; " +
//                                "    font-family:\"Times New Roman\", Times, serif; " +
                                "    margin: 0px; " +
                                "    padding: 0px; " +
                                "} " +
                                " .page-container {" +
                                "                         page-break-after: always;" +
//                                "                         height: 100vh;" +
//                                "                         overflow: hidden;" +
                                "                    }")
                        .append(page.getStyles())
                        .append("</style>");
            }
        }

        String htmlString = "<!DOCTYPE html>\n" + "<html lang=\"ru\">\n" + "<head>\n"
                + "    <meta charset=\"UTF-8\"/>\n" + "    <meta http-equiv=\"Content-Type\" content=\"text/html\"/>\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n"
                + "    <style type='text/css'> "
                + "        * { font-family: Verdana; }/n"
                + "    </style>/n"
                + "</head>\n"
                + "<body>\n" + "    <h3>ПРЕДСТАВЛЕНИЕ</h3>\n" + "</body>\n" + "</html>";

        htmlBuilder.append("</head><body>");

        // Добавляем содержимое всех страниц
        for (PageDataDTO page : pages) {
            htmlBuilder.append("<div class='page-container'>").append(page.getContent()).append("</div><hr/>");
        }

        htmlBuilder.append("</body></html>");



        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            ITextFontResolver fontResolver = renderer.getFontResolver();
//            fontResolver.addFont("C:/Windows/Fonts/arial.ttf", true);
//            fontResolver.addFontDirectory("C:/Windows/Fonts/", true);

            renderer.getFontResolver().addFont("c:/windows/fonts/verdana.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            renderer.getFontResolver().addFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            renderer.getFontResolver().addFont("c:/windows/fonts/times.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

//            File fontDir = new File("C:/Windows/Fonts/");
//            for (File f : fontDir.listFiles()) {
//                if (f.getName().toLowerCase().endsWith(".ttf")) {
//                    try {
//                        fontResolver.addFont(f.getAbsolutePath(),
//                                BaseFont.IDENTITY_H,
//                                BaseFont.EMBEDDED);
//                        System.out.println("Added font: " + f.getName());
//                    } catch (Exception e) {
//                        System.out.println("Failed to add: " + f.getName());
//                    }
//                }
//            }

//            // Выведет список доступных шрифтов
//            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//            for (String font : ge.getAvailableFontFamilyNames()) {
//                System.out.println(font);
//            }

//            renderer.setDocumentFromString(htmlBuilder.toString());
            String testHtml = "<html><body><p>Привет мир!</p></body></html>";
            renderer.setDocumentFromString(htmlBuilder.toString());
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        }
    }

}
