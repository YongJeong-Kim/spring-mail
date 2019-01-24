package com.kyj.mail.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class MailDTO {
  private String customerEmail;
  private String subject;
  private String text;
  private MultipartFile file;
}
