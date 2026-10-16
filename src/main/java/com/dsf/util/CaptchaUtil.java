 package com.dsf.util;

 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.util.Base64;
 import java.util.Random;

 public class CaptchaUtil {
     private static final int W = 130, H = 46;
     private static final String SRC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
     private static final Random R = new Random();

     public static String genCode() {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < 4; i++) sb.append(SRC.charAt(R.nextInt(SRC.length())));
         return sb.toString();
     }

     public static String toBase64(String code) {
         BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
         Graphics2D g = img.createGraphics();
         // 背景渐变
         GradientPaint gp = new GradientPaint(0, 0, new Color(230, 240, 255), W, H, new Color(200, 220, 250));
         g.setPaint(gp);
         g.fillRect(0, 0, W, H);
         // 干扰线
         g.setColor(new Color(160, 180, 210, 100));
         for (int i = 0; i < 8; i++) {
             int x1 = R.nextInt(W), y1 = R.nextInt(H), x2 = R.nextInt(W), y2 = R.nextInt(H);
             g.setStroke(new BasicStroke(R.nextFloat() + 0.5f));
             g.drawLine(x1, y1, x2, y2);
         }
         // 噪点
         for (int i = 0; i < 100; i++) {
             int x = R.nextInt(W), y = R.nextInt(H);
             g.setColor(new Color(R.nextInt(100) + 100, R.nextInt(100) + 100, R.nextInt(100) + 100));
             g.fillRect(x, y, 1, 1);
         }
         // 绘制字符
         g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 26));
         for (int i = 0; i < code.length(); i++) {
             AffineTransform orig = g.getTransform();
             double angle = (R.nextDouble() - 0.5) * 0.6;
             g.rotate(angle, 20 + i * 28, H / 2);
             g.setColor(new Color(R.nextInt(60), R.nextInt(80), R.nextInt(140) + 60));
             g.drawString(String.valueOf(code.charAt(i)), 12 + i * 28, 32);
             g.setTransform(orig);
         }
         g.dispose();
         try {
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ImageIO.write(img, "png", bos);
             return Base64.getEncoder().encodeToString(bos.toByteArray());
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
