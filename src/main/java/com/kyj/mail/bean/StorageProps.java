package com.kyj.mail.bean;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class StorageProps {
  private final String location = "upload-dir";
}
