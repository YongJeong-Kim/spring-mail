package com.kyj.mail.service;

import com.kyj.mail.StorageException;
import com.kyj.mail.bean.StorageProps;
import com.kyj.mail.pojo.FileInfo;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService {
  private final Path rootLocation;
  private static Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
  @Autowired
  public FileSystemStorageService(StorageProps storageProps) {
    this.rootLocation = Paths.get(storageProps.getLocation());
  }

  public void init() {
    try {
      Files.createDirectories(rootLocation);
    }
    catch (IOException e) {
      throw new StorageException("Could not initialize storage", e);
    }
  }

  public void deleteAll() {
    FileSystemUtils.deleteRecursively(rootLocation.toFile());
  }

  public Path load(String filename) {
    return rootLocation.resolve(filename);
  }

  public Resource loadAsResource(String filename) {
    try {
      Path file = load(filename);
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() || resource.isReadable()) {
        return resource;
      }
      else {
        throw new StorageException(
                "Could not read file: " + filename);
      }
    }
    catch (MalformedURLException e) {
      throw new StorageException("Could not read file: " + filename, e);
    }
  }

  public Stream<Path> loadAll() {
    checkDir();
    try {
      return Files.walk(this.rootLocation, 1)
              .filter(path -> !path.equals(this.rootLocation))
              .map(this.rootLocation::relativize);
    }
    catch (IOException e) {
      throw new StorageException("Failed to read stored files", e);
    }
  }

  private void checkDir() {
    String absolutePath = rootLocation.toAbsolutePath().toString();
    String path = rootLocation.normalize().toString();
    File checkDir = new File(path);
    logger.info(path);
    logger.info(absolutePath);
    if (!checkDir.isDirectory())
      checkDir.mkdirs();
  }
  public FileInfo store(MultipartFile file) {
    checkDir();
    String filename = StringUtils.cleanPath(file.getOriginalFilename());
    try {
      if (file.isEmpty()) {
        throw new StorageException("Failed to store empty file " + filename);
      }
      if (filename.contains("..")) {
        // This is a security check
        throw new StorageException(
                "Cannot store file with relative path outside current directory "
                        + filename);
      }
      try (InputStream inputStream = file.getInputStream()) {
        String randomFilename = UUID.randomUUID().toString();
        String extension = FilenameUtils.getExtension(filename);
        Files.copy(inputStream,
                this.rootLocation.resolve(randomFilename + "." + extension),
                StandardCopyOption.REPLACE_EXISTING);

        return FileInfo.builder()
                .filename(filename)
                .randomFilename(randomFilename)
                .extension(extension)
                .path(rootLocation.toAbsolutePath().toString())
                .size(file.getSize())
                .build();
      }
    }
    catch (IOException e) {
      throw new StorageException("Failed to store file " + filename, e);
    }
  }
}
