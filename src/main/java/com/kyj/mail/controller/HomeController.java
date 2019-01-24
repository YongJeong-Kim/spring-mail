package com.kyj.mail.controller;

import com.kyj.mail.dto.MailDTO;
import com.kyj.mail.pojo.FileInfo;
import com.kyj.mail.service.EmailService;
import com.kyj.mail.service.FileSystemStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.util.stream.Collectors;

@Controller
public class HomeController {
  private final EmailService emailService;
  private final FileSystemStorageService fileSystemStorageService;

  @Autowired
  public HomeController(EmailService emailService, FileSystemStorageService fileSystemStorageService) {
    this.emailService = emailService;
    this.fileSystemStorageService = fileSystemStorageService;
  }

  @GetMapping("/files/{filename:.+}")
  @ResponseBody
  public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
    Resource file = fileSystemStorageService.loadAsResource(filename);
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.getFilename() + "\"").body(file);
  }

  @GetMapping("/")
  public String home(Model model) throws IOException {
    model.addAttribute("files", fileSystemStorageService.loadAll().map(
            path -> MvcUriComponentsBuilder.fromMethodName(HomeController.class,
                    "serveFile", path.getFileName().toString()).build().toString())
            .collect(Collectors.toList()));
    return "index";
  }

  @PostMapping(value="/")
  public @ResponseBody void send(MailDTO mailDTO) {
    if (mailDTO.getFile() != null) {
      FileInfo fileInfo = fileSystemStorageService.store(mailDTO.getFile());
      emailService.sendEmail(fileInfo, mailDTO);
    } else
      emailService.sendEmail(null, mailDTO);
  }
}
