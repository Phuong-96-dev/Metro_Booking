# Metro_Booking

## Student ticket verification

Student ticket eligibility is granted only after an ADMIN approves a submitted
student-card image. OCR extracts text to assist review; it does not authenticate
a card.

Before using this flow:

1. Apply `src/main/resources/db/manual/student-verification.sql` to the Metro
   SQL Server database. The migration marks ticket types named `Vé sinh viên`;
   check the flag in ticket-type administration if your display name differs.
2. Install Tesseract OCR with Vietnamese and English language data, and ensure
   `tesseract --version` works for the application account.
3. Configure `student-verification.ocr-command`,
   `student-verification.ocr-languages`, and
   `student-verification.storage-directory` in `application.properties` or
   environment variables. Images are stored outside the public static
   directory and are available only to their owner or an ADMIN. Define and
   implement a retention period before production use.
4. Members can open **Xác minh sinh viên**, capture a card with the camera or
   upload a JPG/JPEG, PNG, or WEBP image (maximum 5 MB), review OCR, then submit
   it for ADMIN approval at `/admin/student-verifications`. Camera access
   requires HTTPS or localhost and is requested only after the member starts a
   scan.

The existing `/ticket/buy` flow is retained. The backend checks the
database-backed student-ticket flag and current approval before creating a
student ticket; hiding the purchase option in the page is not the security
control.
