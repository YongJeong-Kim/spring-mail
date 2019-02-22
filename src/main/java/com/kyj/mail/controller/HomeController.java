package com.kyj.mail.controller;

import com.kyj.mail.dto.MailDTO;
import com.kyj.mail.pojo.FileInfo;
import com.kyj.mail.service.EmailService;
import com.kyj.mail.service.FileSystemStorageService;
import org.apache.commons.mail.util.MimeMessageParser;
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

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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

  @GetMapping("/test")
  public @ResponseBody void ddd() throws Exception {
    File emlFile = new File("upload-dir/aa.eml");
    Properties props = System.getProperties();
    props.put("mail.host", "smtp.dummydomain.com");
    props.put("mail.transport.protocol", "smtp");

    Session mailSession = Session.getDefaultInstance(new Properties(), null);
    InputStream source = new FileInputStream(emlFile);
    MimeMessage message = new MimeMessage(mailSession, source);

    MimeMessageParser parser = new MimeMessageParser(message);
    parser.parse();

    System.out.println("Subject : " + message.getSubject());
    System.out.println("From : " + message.getFrom()[0]);
    System.out.println("--------------");
    System.out.println("Body : " + message.getContent());
    System.out.println("Body HTML : " + parser.getHtmlContent());
    System.out.println("Body Plain : " + parser.getPlainContent());
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
