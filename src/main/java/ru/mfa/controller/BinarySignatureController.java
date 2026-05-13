package ru.mfa.controller;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.mfa.binary.DataBinaryBuilder;
import ru.mfa.binary.DataBuildResult;
import ru.mfa.binary.ManifestBinaryBuilder;
import ru.mfa.model.MalwareSignature;
import ru.mfa.model.SignatureStatus;
import ru.mfa.repository.MalwareSignatureRepository;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary/signatures")
public class BinarySignatureController {

    private final MalwareSignatureRepository signatureRepository;
    private final DataBinaryBuilder dataBinaryBuilder;
    private final ManifestBinaryBuilder manifestBinaryBuilder;

    public BinarySignatureController(MalwareSignatureRepository signatureRepository,
                                     DataBinaryBuilder dataBinaryBuilder,
                                     ManifestBinaryBuilder manifestBinaryBuilder) {
        this.signatureRepository = signatureRepository;
        this.dataBinaryBuilder = dataBinaryBuilder;
        this.manifestBinaryBuilder = manifestBinaryBuilder;
    }

    @GetMapping("/full")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> full() throws Exception {
        List<MalwareSignature> signatures = signatureRepository.findAllByStatus(SignatureStatus.ACTUAL);
        return buildMultipartResponse(signatures, 1, -1L);
    }

    @GetMapping("/increment")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> increment(@RequestParam String since) throws Exception {
        Instant sinceInstant;
        try {
            sinceInstant = Instant.parse(since);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid 'since' format, expected ISO-8601 instant (e.g. 2024-01-01T00:00:00Z)");
        }
        List<MalwareSignature> signatures = signatureRepository.findAllByUpdatedAtAfter(sinceInstant);
        return buildMultipartResponse(signatures, 2, sinceInstant.toEpochMilli());
    }

    @PostMapping("/by-ids")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> byIds(@RequestBody ByIdsRequest request) throws Exception {
        List<MalwareSignature> signatures = signatureRepository.findAllByIdIn(request.getIds());
        return buildMultipartResponse(signatures, 3, -1L);
    }

    private ResponseEntity<MultiValueMap<String, Object>> buildMultipartResponse(
            List<MalwareSignature> signatures, int exportType, long sinceMillis) throws Exception {

        DataBuildResult result = dataBinaryBuilder.build(signatures);
        byte[] manifestBytes = manifestBinaryBuilder.build(
                result.getEntries(), result.getDataBytes(), exportType, sinceMillis);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        HttpHeaders manifestHeaders = new HttpHeaders();
        manifestHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        manifestHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"manifest.bin\"");
        body.add("manifest", new HttpEntity<>(manifestBytes, manifestHeaders));

        HttpHeaders dataHeaders = new HttpHeaders();
        dataHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        dataHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"data.bin\"");
        body.add("data", new HttpEntity<>(result.getDataBytes(), dataHeaders));

        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_MIXED)
                .body(body);
    }

    static class ByIdsRequest {
        private List<UUID> ids;

        public List<UUID> getIds() { return ids; }
        public void setIds(List<UUID> ids) { this.ids = ids; }
    }
}
