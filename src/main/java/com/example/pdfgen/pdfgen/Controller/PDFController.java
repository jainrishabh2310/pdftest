package com.example.pdfgen.pdfgen.Controller;

import com.example.pdfgen.pdfgen.PDFSegmentation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class PDFController {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir"); // Temporary directory
    private static final String OUTPUT_DIR = TEMP_DIR + "output/"; // Output directory in temp


    // Call template where user can upload the pdf file
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // when user click on segement button this method is called
    @PostMapping("/segment")
    public void uploadPDF(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        try {

            // Ensure output directory is exists or not
            Path outputPath = Paths.get(OUTPUT_DIR);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // Save file which is upload by user to temporary location
            File uploadedFile = new File(TEMP_DIR, file.getOriginalFilename());
            file.transferTo(uploadedFile);

            // Segment the pdf
            PDFSegmentation.segmentPDF(uploadedFile.getAbsolutePath(), OUTPUT_DIR);

            // Create a ZIP file containing the segmented PDFs
            String zipFilePath = createZip(OUTPUT_DIR);

            // Set response headers for downloading the ZIP file
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + new File(zipFilePath).getName() + "\"");

            // Write the ZIP file to the response output stream
            try (FileInputStream in = new FileInputStream(zipFilePath); OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String createZip(String outputDirectory) throws IOException {
        String zipoutputFilePath = outputDirectory + "segmented_pdfs.zip";
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(zipoutputFilePath)))) {
            Files.list(Paths.get(outputDirectory)).forEach(file -> {
                try {
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return zipoutputFilePath; // Return the path to the created ZIP file
    }
}
