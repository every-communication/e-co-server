package com.eco.ecoserver.domain.image;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;

public class URLMultipartFile implements MultipartFile {
    private final byte[] bytes;
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final boolean isEmpty;

    public URLMultipartFile(InputStream inputStream, String originalFilename, String contentType) throws IOException {
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.name = "file";

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            this.bytes = outputStream.toByteArray();
        }

        this.isEmpty = bytes.length == 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return bytes;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
            fileOutputStream.write(bytes);
        }
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        transferTo(dest.toFile());
    }
}
