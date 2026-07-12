# Document Engine

## Phase 5 - Document Intelligence (implemented)

PDF import, image import, OCR, document indexing, and a receipt vault
- see `03-domain-model.md` (`Document` entity), `05-current-state.md`
(build/test detail), `14-search-engine.md` (how `extractedText` becomes
searchable).

`Document` metadata (filename, storage path, MIME type, document type,
extracted text, optional linked transaction) lives in the encrypted
SQLite database; the raw file bytes never do. `DocumentStoragePort`
(`:domain`) owns bytes-on-disk and is implemented per platform:
`DesktopDocumentStorage`/`AndroidDocumentStorage` write to a
platform-appropriate app-data directory, namespaced by the document's
id so two imports of the same filename never collide.

Text extraction is the other platform-scoped port,
`DocumentTextExtractorPort`, and never throws - an extraction failure
returns `""` so import always succeeds and the document is simply not
text-searchable, rather than blocking the receipt-vault workflow on an
OCR failure.

- **Desktop** (`DesktopDocumentTextExtractor`): a digital PDF's
  embedded text is read directly via Apache PDFBox 3.0.8 (no OCR
  needed - fast, exact). Images and PDFs with no embedded text fall
  back to Tesseract OCR via `tess4j` 5.19.0, wrapped in a small
  `TesseractEngine` helper.
- **Android** (`AndroidDocumentTextExtractor`): Android has no bundled
  PDF-text-extraction API, so every PDF page is rendered to a bitmap
  via `android.graphics.pdf.PdfRenderer` and OCR'd. Images are OCR'd
  directly. Both paths use ML Kit's standalone on-device text
  recognizer (`com.google.mlkit:text-recognition` 16.0.1) - the model
  ships inside the app, so recognition never makes a network call,
  preserving the app's fully-offline guarantee. Multi-page PDFs are
  processed with a plain `for` loop (not `joinToString`'s lambda,
  which is a non-suspend function type and cannot call a `suspend`
  function like `recognizeText` from inside it) and joined afterward.

Import flow (`ImportDocumentUseCase`, taking one `ImportDocumentRequest`
to stay under detekt's `LongParameterList` threshold): the picked
file's bytes are saved via `DocumentStoragePort.save()`, text is
extracted via `DocumentTextExtractorPort.extractText()`, and the
resulting `Document` (with `storagePath`/`extractedText` filled in) is
persisted via `DocumentRepository.upsert()` - which also fires the
`document_search_ai` trigger folding `fileName`/`extractedText` into
`search_index` (Phase 4's FTS5 table), so a newly imported receipt is
globally searchable immediately.

The receipt-vault UI (`composeApp/.../document/DocumentVaultScreen`) is
a third bottom-nav destination ("Vault") alongside Dashboard and
Search: an import button launches the OS file picker
(`DocumentPicker` `expect`/`actual` - `java.awt.FileDialog` on Desktop,
`ActivityResultContracts.GetContent()` on Android), and each imported
document can optionally be linked to a `Transaction`
(`LinkDocumentToTransactionUseCase`) or deleted
(`DeleteDocumentUseCase`, which also deletes the on-disk file via
`DocumentStoragePort.delete()`).

## Known gaps

- OCR/PDF-extraction and both `DocumentPicker` actuals are unverified
  against real files/devices beyond the fixture-based
  `DesktopDocumentTextExtractorTest` - no Android runtime available in
  this environment.
- No dedicated `SchemaMigrationTest` case for v4->v5 (the `document`
  table), matching the gap noted in `05-current-state.md`.
