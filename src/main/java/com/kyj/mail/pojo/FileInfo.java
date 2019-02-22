package com.kyj.mail.pojo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileInfo {
  private String filename;
  private String randomFilename;
  private String extension;
  private String path;
  private long size; // byte
}
