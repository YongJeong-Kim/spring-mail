package com.kyj.mail.service;

import com.kyj.mail.dto.MailDTO;
import com.kyj.mail.pojo.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;

@Service
public class EmailService {
  private static Logger logger = LoggerFactory.getLogger(EmailService.class);
  private final JavaMailSender mailSender;
  private final String toUser;

  @Autowired
  public EmailService(JavaMailSender mailSender,
                      @Value("${test.mail.to}") String toUser) {
    this.mailSender = mailSender;
    this.toUser = toUser;
  }

  public void sendEmail(FileInfo fileInfo, MailDTO mailDTO) {
    MimeMessagePreparator mailMessage = mimeMessage -> {
      MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      try {
//        message.setFrom(mailDTO.getCustomerEmail(), "ddd");
        message.setFrom(mailDTO.getCustomerEmail());
        message.setTo(new InternetAddress(toUser));
        message.setSubject(mailDTO.getSubject());
        message.setText(mailDTO.getText(), false);
        if (fileInfo != null) {
          String filename = fileInfo.getPath() + "\\" + fileInfo.getRandomFilename() + "." + fileInfo.getExtension();
          FileSystemResource fsr = new FileSystemResource(filename);
          message.addAttachment(fileInfo.getFilename(), fsr);
        }
      } catch (Exception e) {
        e.printStackTrace();
        logger.info("message preparator fail !!", e);
      }
    };
    mailSender.send(mailMessage);
 /*   if (fileInfo != null) {
      String filename = fileInfo.getPath() + "\\" + fileInfo.getRandomFilename() + "." + fileInfo.getExtension();
      File file = new File(filename);
      file.delete();
    }*/
  }
}
