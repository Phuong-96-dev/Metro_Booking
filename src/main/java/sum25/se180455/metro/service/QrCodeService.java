package sum25.se180455.metro.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QrCodeService {

    public byte[] generateQr(String content, int size) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        return render(content, BarcodeFormat.QR_CODE, size, size, hints);
    }

    public byte[] generateCode128(String content, int width, int height) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 10);
        return render(content, BarcodeFormat.CODE_128, width, height, hints);
    }

    private byte[] render(String content, BarcodeFormat format, int w, int h,
                          Map<EncodeHintType, Object> hints) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, format, w, h, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Không thể tạo mã " + format, e);
        }
    }

    public Optional<String> decode(InputStream imageStream) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        if (image == null) {
            return Optional.empty();
        }
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128));
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        try {
            return Optional.of(new MultiFormatReader().decode(bitmap, hints).getText());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
