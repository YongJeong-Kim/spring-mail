package com.kyj.mail.service;

import com.kyj.mail.bean.StorageProps;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Service
public class EmailService {
  private static Logger logger = LoggerFactory.getLogger(EmailService.class);
  private final JavaMailSender mailSender;
  private final StorageProps storageProps;
  private final String toUser;

  @Autowired
  public EmailService(JavaMailSender mailSender,
                      StorageProps storageProps,
                      @Value("${test.mail.to}") String toUser) {
    this.mailSender = mailSender;
    this.storageProps = storageProps;
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
        String convertText = mailDTO.getText().replace("\n", "<br>");
        message.setText(convertText, true);

        if (fileInfo != null) {
          String filename = fileInfo.getPath() + "\\" + fileInfo.getRandomFilename() + "." + fileInfo.getExtension();
          FileSystemResource fsr = new FileSystemResource(filename);
          message.addAttachment(fileInfo.getFilename(), fsr);
        }
        File file = new File(storageProps.getLocation() + "\\aa.eml");
        OutputStream out = new FileOutputStream(file);
        try {
          mimeMessage.writeTo(out);
        } finally {
          if (out != null) {
            out.flush();
            out.close();
          }
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
