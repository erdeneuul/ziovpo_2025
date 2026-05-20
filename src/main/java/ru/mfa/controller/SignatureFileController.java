package ru.mfa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mfa.dto.SignatureRequest;
import ru.mfa.minio.MinioService;
import ru.mfa.model.MalwareSignature;
import ru.mfa.repository.MalwareSignatureRepository;
import ru.mfa.service.MalwareSignatureService;

import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/api/admin/signatures/files")
@PreAuthorize("hasRole('ADMIN')")
public class SignatureFileController {

    private final MinioService minioService;
    private final MalwareSignatureRepository malwareSignatureRepository;
    private final MalwareSignatureService malwareSignatureService;

    public SignatureFileController(MinioService minioService,
                                   MalwareSignatureRepository malwareSignatureRepository,
                                   MalwareSignatureService malwareSignatureService) {
        this.minioService = minioService;
        this.malwareSignatureRepository = malwareSignatureRepository;
        this.malwareSignatureService = malwareSignatureService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";

        String threatName = originalFilename.contains(".")
                ? originalFilename.split("\\.")[0]
                : originalFilename;

        int firstBytesLen = Math.min(8, bytes.length);
        byte[] firstBytes = Arrays.copyOfRange(bytes, 0, firstBytesLen);
        String firstBytesHex = bytesToHex(firstBytes);

        int remainderStart = Math.min(8, bytes.length);
        byte[] remainderBytes = Arrays.copyOfRange(bytes, remainderStart, bytes.length);
        String remainderHashHex = bytesToHex(MessageDigest.getInstance("SHA-256").digest(remainderBytes));
        long remainderLength = remainderBytes.length;

        String fileType = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                : "unknown";

        long offsetStart = 0;
        long offsetEnd = Math.max(0, bytes.length - 1);

        SignatureRequest req = new SignatureRequest();
        req.setThreatName(threatName);
        req.setFirstBytesHex(firstBytesHex);
        req.setRemainderHashHex(remainderHashHex);
        req.setRemainderLength(remainderLength);
        req.setFileType(fileType);
        req.setOffsetStart(offsetStart);
        req.setOffsetEnd(offsetEnd);

        MalwareSignature saved = malwareSignatureService.create(req);

        String objectName = saved.getId().toString() + "/" + originalFilename;
        minioService.uploadFile(objectName, bytes, file.getContentType());

        saved.setFileObjectName(objectName);
        malwareSignatureRepository.save(saved);

        return ResponseEntity.ok(Map.of("signature", saved, "objectName", objectName));
    }

    @PostMapping("/presigned-urls")
    public ResponseEntity<Map<String, String>> presignedUrls(
            @RequestBody Map<String, List<String>> body) throws Exception {
        List<String> ids = body.get("ids");
        Map<String, String> result = new LinkedHashMap<>();
        if (ids == null) return ResponseEntity.ok(result);

        for (String idStr : ids) {
            UUID id;
            try {
                id = UUID.fromString(idStr);
            } catch (IllegalArgumentException e) {
                continue;
            }
            Optional<MalwareSignature> opt = malwareSignatureRepository.findById(id);
            if (opt.isEmpty()) continue;
            MalwareSignature sig = opt.get();
            if (sig.getFileObjectName() == null) continue;
            String url = minioService.getPresignedUrl(sig.getFileObjectName());
            result.put(id.toString(), url);
        }
        return ResponseEntity.ok(result);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
