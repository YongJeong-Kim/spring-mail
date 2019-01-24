package com.kyj.mail.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ExceptionAdvice {
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public void maxUploadSizeException(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {

  }
}
