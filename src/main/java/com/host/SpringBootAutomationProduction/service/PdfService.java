package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.dto.PageDataDTO;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PdfService {

    public byte[] createPdf(List<PageDataDTO> pages) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String html = """
                    <!DOCTYPE html>
                    <html lang="ru">
                    <head>
                        <meta charset="UTF-8" />
                        <style>
                            @page { 
                                size: A4;
                                margin: 0;
                            }
                            body, html {
                                font-family: 'Times New Roman', sans-serif;
                                margin: 0;
                                padding: 0;
                                left: 0;
                                right: 0;
                            }
                            .page-container {
                                 position: relative;
                                 page-break-after: always;
                                 height: 297mm;
                                 overflow: hidden;
                                 margin: 0;
                                 padding: 0;
                                 left: 0;
                                 right: 0;
                                 box-sizing: border-box;
                            }
                            %s
                        </style>
                    </head>
                    <body> 
                        %s
                    </body>
                    </html>
                    """.formatted(
                    pages.get(0).getStyles(),
                    pages.stream()
                            .map(page -> "<div class='page-container'>" + page.getContent() + "</div>")
                            .collect(Collectors.joining())
            );
            PdfRendererBuilder builder = getPdfRendererBuilder(html);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации PDF: " + e.getMessage(), e);
        }
    }

    private PdfRendererBuilder getPdfRendererBuilder(String html) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/arial.ttf"), "Arial");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/times.ttf"), "Times New Roman");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/comic.ttf"), "Comic Sans MS");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/comicbd.ttf"), "Comic Sans MS");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/verdana.ttf"), "Verdana");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/verdanab.ttf"), "Verdana");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/cour.ttf"), "Courier New");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/courbd.ttf"), "Courier New");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/trebuc.ttf"), "Trebuchet MS");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/trebucbd.ttf"), "Trebuchet MS");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/tahoma.ttf"), "Tahoma");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/impact.ttf"), "Impact");
        builder.useFont(() -> getClass().getResourceAsStream("/fonts/georgia.ttf"), "Georgia");
        builder.withHtmlContent(html, "/");
        return builder;
    }

}
