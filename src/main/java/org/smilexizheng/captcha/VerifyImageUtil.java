package org.smilexizheng.captcha;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VerifyImageUtil {



    private final static Logger log = LogManager.getLogger(VerifyImageUtil.class);
    private static int BOLD = 5;
    private static final String IMG_FILE_TYPE = "jpg";
    private static final String TEMP_IMG_FILE_TYPE = "png";

    /**
     * 根据模板切图
     *
     * @param templateFile
     * @param targetFile
     * @return map
     * @throws Exception
     */
    public static Map<String, Object> pictureTemplatesCut(File templateFile, File targetFile) throws Exception {
        Map<String, Object> pictureMap = new HashMap<>();
        // 模板图
        BufferedImage imageTemplate = ImageIO.read(templateFile);
        int templateWidth = imageTemplate.getWidth();
        int templateHeight = imageTemplate.getHeight();

        // 原图
        BufferedImage oriImage = ImageIO.read(targetFile);
        int oriImageWidth = oriImage.getWidth();
        int oriImageHeight = oriImage.getHeight();
        //添加水印
        addWatermark(oriImage);

        //随机生成抠图坐标X,Y
        //X轴距离右端targetWidth  Y轴距离底部targetHeight以上
        Random random = new Random();
        int widthRandom = random.nextInt(oriImageWidth - 2 * templateWidth) + templateWidth;
        int heightRandom = 0;
        // 新建一个和模板一样大小的图像，TYPE_4BYTE_ABGR表示具有8位RGBA颜色分量的图像，正常取imageTemplate.getType()
        BufferedImage newImage = new BufferedImage(templateWidth, templateHeight, imageTemplate.getType());
        //得到画笔对象
        Graphics2D graphics = newImage.createGraphics();
        //如果需要生成RGB格式，需要做如下配置,Transparency 设置透明
        newImage = graphics.getDeviceConfiguration().createCompatibleImage(templateWidth, templateHeight, Transparency.TRANSLUCENT);

        // 新建的图像根据模板颜色赋值,源图生成遮罩
        cutByTemplate(oriImage, imageTemplate, newImage, widthRandom, heightRandom);

        // 设置“抗锯齿”的属性
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(BOLD, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        graphics.drawImage(newImage, 0, 0, null);
        graphics.dispose();
        //新建流。
        ByteArrayOutputStream newImageOs = new ByteArrayOutputStream();
        //利用ImageIO类提供的write方法，将bi以png图片的数据模式写入流。
        ImageIO.write(newImage, TEMP_IMG_FILE_TYPE, newImageOs);
        byte[] newImagebyte = newImageOs.toByteArray();
        //新建流。
        ByteArrayOutputStream oriImagesOs = new ByteArrayOutputStream();
        //利用ImageIO类提供的write方法，将bi以jpg图片的数据模式写入流。
        ImageIO.write(oriImage, IMG_FILE_TYPE, oriImagesOs);
        byte[] oriImageByte = oriImagesOs.toByteArray();

        pictureMap.put("smallImage", Base64Utils.encodeToString(newImagebyte));
        pictureMap.put("bigImage", Base64Utils.encodeToString(oriImageByte));
//        System.out.println("widthRandom:" + widthRandom);
        pictureMap.put("xWidth", widthRandom);
        pictureMap.put("yHeight", heightRandom);
        return pictureMap;
    }

    /**
     * 添加水印
     * @param oriImage
     */
    private static BufferedImage addWatermark(BufferedImage oriImage) throws IOException {
        Graphics2D graphics2D = oriImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 设置水印文字颜色
        graphics2D.setColor(Color.BLUE);
        // 设置水印文字Font
        graphics2D.setFont(new java.awt.Font("宋体", java.awt.Font.BOLD, 50));
        // 设置水印文字透明度
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
        // 第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y)
        graphics2D.drawString("我的水印", 400,300);
        graphics2D.dispose(); //释放
        return oriImage;
    }

    /**
     * @param oriImage      原图
     * @param templateImage 模板图
     * @param newImage      新抠出的小图
     * @param x             随机扣取坐标X
     * @param y             随机扣取坐标y
     * @throws Exception
     */
    private static void cutByTemplate(BufferedImage oriImage, BufferedImage templateImage, BufferedImage newImage, int x, int y) {
        //临时数组遍历用于高斯模糊存周边像素值
        int[][] martrix = new int[3][3];
        int[] values = new int[9];

        int xLength = templateImage.getWidth();
        int yLength = templateImage.getHeight();
        // 模板图像宽度
        for (int i = 0; i < xLength; i++) {
            // 模板图片高度
            for (int j = 0; j < yLength; j++) {
                // 如果模板图像当前像素点不是透明色 copy源文件信息到目标图片中
                int rgb = templateImage.getRGB(i, j);
                if (rgb < 0) {
                    //模板图 位置上色
                    newImage.setRGB(i, j, oriImage.getRGB(x + i, y + j));

                    //抠图区域高斯模糊
                    readPixel(oriImage, x + i, y + j, values);
                    fillMatrix(martrix, values);
                    oriImage.setRGB(x + i, y + j, avgMatrix(martrix));
                }

                //防止数组越界判断
                if (i == (xLength - 1) || j == (yLength - 1)) {
                    continue;
                }
                int rightRgb = templateImage.getRGB(i + 1, j);
                int downRgb = templateImage.getRGB(i, j + 1);
                //描边处理，,取带像素和无像素的界点，判断该点是不是临界轮廓点,如果是设置该坐标像素是白色
//                if ((rgb >= 0 && rightRgb < 0) || (rgb < 0 && rightRgb >= 0) || (rgb >= 0 && downRgb < 0) || (rgb < 0 && downRgb >= 0)) {
//                    newImage.setRGB(i, j, Color.white.getRGB());
//                    oriImage.setRGB(x + i, y + j, Color.white.getRGB());
//                }
            }
        }
    }

    private static void readPixel(BufferedImage img, int x, int y, int[] pixels) {
        int xStart = x - 1;
        int yStart = y - 1;
        int current = 0;
        for (int i = xStart; i < 3 + xStart; i++) {
            for (int j = yStart; j < 3 + yStart; j++) {
                int tx = i;
                if (tx < 0) {
                    tx = -tx;

                } else if (tx >= img.getWidth()) {
                    tx = x;
                }
                int ty = j;
                if (ty < 0) {
                    ty = -ty;
                } else if (ty >= img.getHeight()) {
                    ty = y;
                }
                pixels[current++] = img.getRGB(tx, ty);

            }
        }
    }

    private static void fillMatrix(int[][] matrix, int[] values) {
        int filled = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                x[j] = values[filled++];
            }
        }
    }

    private static int avgMatrix(int[][] matrix) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                if (j == 1) {
                    continue;
                }
                Color c = new Color(x[j]);
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        return new Color(r / 8, g / 8, b / 8).getRGB();
    }
}

