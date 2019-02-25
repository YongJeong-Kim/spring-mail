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

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
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

    Multipart multiPart = (Multipart) message.getContent();
    for (int i = 0; i < multiPart.getCount(); i++) {
      MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
      if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
        System.out.println("attachment filename : " + part.getFileName());
      }
    }

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

  private String receivingHost;
  private String userName;
  private String password;

  @GetMapping("receive")
  public @ResponseBody void receive() {
    /*this will print subject of all messages in the inbox of sender@gmail.com*/

    /* gmail imap.gmail.com
     * hiworks pop3s.hiworks.com
     * username abc@gmail.com etc.
     * password mail account password
     * */
    this.receivingHost="pop3s.hiworks.com";//for imap protocol
    this.userName = "kyj@pangaeasol.com";
    this.password = "";
    Properties props2 = System.getProperties();

    /* gmail imaps
     * hiworks pop3
     * */
    props2.setProperty("mail.store.protocol", "pop3");
    // I used imaps protocol here

    Session session2 = Session.getDefaultInstance(props2, null);

    try {
      /* gmail imaps
      *  hiworks pop3
      * */
      Store store = session2.getStore("pop3");

      store.connect(this.receivingHost, this.userName, this.password);

      Folder folder = store.getFolder("INBOX");//get inbox

      folder.open(Folder.READ_ONLY);//open folder only to read

      Message message[]=folder.getMessages();
      List<Message> list = Arrays.asList(message);
      list.sort((l1, l2) -> {
        try {
          return l2.getSentDate().compareTo(l1.getSentDate());
        } catch (MessagingException e) {
          e.printStackTrace();
        }
        return 0;
      });

      list.forEach(l -> {
        try {
          System.out.println(l.getSentDate() + " : " + l.getSubject());
        } catch (MessagingException e) {
          e.printStackTrace();
        }
      });

      for (int i = 0; i < message.length; i++){
        //print subjects of all mails in the inbox
        System.out.println(message[i].getSentDate() + " : " + message[i].getSubject());
        //anything else you want
      }

      //close connections
      folder.close(true);
      store.close();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
}
