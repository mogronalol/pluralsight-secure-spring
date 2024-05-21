package pluralsight.m7.controller.mvc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageProcessingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private static Stream<String[]> injectionTestData() {
        return Stream.of(
                new String[]{"301", "300", "PNG"},
                new String[]{"300", "301", "PNG"},
                new String[]{"300", "300", "JPG"},
                new String[]{"300; echo hello", "300", "PNG"},
                new String[]{"300", "300; echo hello", "PNG"},
                new String[]{"300", "300", "PNG;echo hello"}
        );
    }

    @ParameterizedTest
    @MethodSource("injectionTestData")
    public void testSanitization(final String width,
                                 final String height,
                                 final String fileType)
            throws Exception {

        BufferedImage bufferedImage = new BufferedImage(100, 100,
                BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        final MockMultipartFile
                file =
                new MockMultipartFile("file", "test.png", "image/png", baos.toByteArray());

        mockMvc.perform(multipart("/")
                        .file(file)
                        .param("width", width)
                        .param("height", height)
                        .param("format", fileType))
                .andExpect(status().isBadRequest());
    }
}