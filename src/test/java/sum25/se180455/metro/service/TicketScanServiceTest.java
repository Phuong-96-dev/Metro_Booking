package sum25.se180455.metro.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class TicketScanServiceTest {

    @Test
    void metroTicketOrderReferenceRoundTrips() {
        String orderRef = ScanTicketService.metroOrderRef(42L);

        assertThat(orderRef).isEqualTo("METRO-TICKET-42");
        assertThat(ScanTicketService.metroTicketId(orderRef)).isEqualTo(42L);
        assertThat(ScanTicketService.metroTicketId("VNP-42")).isNull();
    }

    @Test
    void generatedQrCanBeDecodedAgain() throws Exception {
        QrCodeService qrCodeService = new QrCodeService();
        String code = "TKABCDEFGHJKLMNPQR";

        byte[] image = qrCodeService.generateQr(code, 300);

        assertThat(qrCodeService.decode(new ByteArrayInputStream(image))).contains(code);
    }
}
