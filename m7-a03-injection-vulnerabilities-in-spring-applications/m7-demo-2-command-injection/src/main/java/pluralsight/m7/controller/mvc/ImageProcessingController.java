package pluralsight.m7.controller.mvc;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import lombok.SneakyThrows;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
public class ImageProcessingController {

    private static byte[] unsafeConversion(final MultipartFile file, final String width,
                                           final String height, final String format)
            throws IOException {
        String command = "convert - -resize " + width + "x" + height + " " + format + ":-";
        // Insecure due to shell execution
        //ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        // Transfer the file's InputStream to the process's OutputStream (standard input)
        try (OutputStream os = process.getOutputStream();
             InputStream is = file.getInputStream()) {
            IOUtils.copyLarge(is, os);  // Use Apache Commons IO to copy
            os.close();  // Ensure the output stream is closed to signify end of input
        }

        // Assuming process outputs an image, read this back into a byte array
        byte[] outputData = IOUtils.toByteArray(process.getInputStream());
        return outputData;
    }

    public static String changeFileExtension(String originalFilename, String newExtension) {
        // Check if the original filename contains a dot
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex != -1) {
            // Replace the part after the dot with the new extension
            return originalFilename.substring(0, dotIndex) + "." + newExtension;
        } else {
            // If no dot was found, just append the new extension
            return originalFilename + "." + newExtension;
        }
    }

    @GetMapping("/")
    public String resize() {
        return "resize";
    }

    @PostMapping("/")
    public ResponseEntity<byte[]> resizeImageAndConvert(
            @RequestParam("file") MultipartFile file,
// Insecure as strings
//            @RequestParam("width") String width,
//            @RequestParam("height") String height,
//            @RequestParam("width") String width,
//            @RequestParam("height") String height,
            @Max(300) @RequestParam("width") int width,
            @Max(300) @RequestParam("height") int height,
            @Pattern(regexp = "^(?i)(GIF|PNG)$") @RequestParam("format") String format) throws IOException {

//        Unsafe in general as it executes an external commmand
//        final byte[] outputData = unsafeConversion(file, width, height, format);

        // Safe as it uses a Java library
        final byte[] outputData = safeConversion(file, width, height, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(changeFileExtension(file.getOriginalFilename(), format))
                        .build());
        headers.setContentType(
                org.springframework.http.MediaType.valueOf("image/" + format));

        return ResponseEntity.ok().headers(headers).body(outputData);
    }

    @SneakyThrows
    private byte[] safeConversion(final MultipartFile file,
                                  final int width,
                                  final int height,
                                  final String format) {
        BufferedImage srcImage = Imaging.getBufferedImage(file.getInputStream());

        // Resize the image
        BufferedImage scaledImage = new BufferedImage(width, height, srcImage.getType());
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.drawImage(srcImage, 0, 0, width, height, null);
        graphics2D.dispose();

        // Convert BufferedImage to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ImageFormats imageFormats = ImageFormats.valueOf(format.toUpperCase());
        Imaging.writeImage(scaledImage, baos, imageFormats);
        return baos.toByteArray();
    }
}
