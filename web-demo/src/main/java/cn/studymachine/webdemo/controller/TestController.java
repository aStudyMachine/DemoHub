package cn.studymachine.webdemo.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.system.SystemUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author wukun
 * @since 2022/7/28
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /*---------------------------------------------- Fields ~ ----------------------------------------------*/



    /*---------------------------------------------- Methods ~ ----------------------------------------------*/


    @PostMapping("/download")
    public void download(HttpServletResponse response) throws FileNotFoundException {
        String url1 = "https://cbftest.oss-cn-guangzhou.aliyuncs.com/vcd/attachment/2022-07-26/20220726134141857_正本(1).jpg";
        String url2 = "https://cbftest.oss-cn-guangzhou.aliyuncs.com/vcd/attachment/2022-07-26/20220726134137829_副本(1).jpg";
        //
        // File zipFile = new File("/test.zip");


        //
        // byte[] bytes1 = HttpUtil.downloadBytes(url1);
        // byte[] bytes2 = HttpUtil.downloadBytes(url2);

        // String dest = SystemUtil.get(SystemUtil.USER_HOME);
        // System.out.println("dest = " + dest);
        // String dest1 = SystemUtil.get(SystemUtil.USER_DIR);
        // System.out.println("dest1 = " + dest1);
        // String currentDir = SystemUtil.getUserInfo().getCurrentDir();
        // System.out.println("currentDir = " + currentDir);

        int i = url1.lastIndexOf("/");
        String fileName1 = url1.substring(i + 1);
        System.out.println("fileName1 = " + fileName1);

        String desc1 = SystemUtil.getUserInfo().getCurrentDir() + "/" + fileName1;

        HttpUtil.downloadFile(url1, desc1);

        File file = new File(desc1);
    }

    public static void main(String[] args) {
        String url1 = "https://cbftest.oss-cn-guangzhou.aliyuncs.com/vcd/attachment/2022-07-26/20220726134141857_正本(1).jpg";
        int i = url1.lastIndexOf("/");
        String fileName1 = url1.substring(i + 1);
        System.out.println("fileName1 = " + fileName1);


        String url2 = "https://cbftest.oss-cn-guangzhou.aliyuncs.com/vcd/attachment/2022-07-26/20220726134137829_副本(1).jpg";

    }

    @PostMapping("/download2")
    public void batchDownload(HttpServletResponse response) {
        String zipName = "通行证" + ".zip";
        response.setHeader("content-type", "application/octet-stream");
        response.setHeader("Content-disposition", "attachment;filename=" + URLUtil.encode(zipName));
        response.setCharacterEncoding("utf-8");

        List<String> urls = CollUtil.newArrayList(
                "https://cbftest.oss-cn-guangzhou.aliyuncs.com/vcd/attachment/2022-07-26/20220726134141857_正本(1).jpg",
                "https://cbftest.oss-cn-guangzhou.aliyuncs.com/vcd/attachment/2022-07-26/20220726134137829_副本(1).jpg"
        );

        // 创建zip包输出流
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            for (String pictureUrl : urls) {

                // 文件名 (带后缀)
                String fileName = pictureUrl.substring(pictureUrl.lastIndexOf("/") + 1);

                // 开启写入当前 zip包 文件
                zipOutputStream.putNextEntry(new ZipEntry(fileName));

                URL url = new URL(pictureUrl);
                InputStream inputStream = new DataInputStream(url.openStream());

                IoUtil.copy(inputStream, zipOutputStream);
                
                // 关闭当前 zip包条目的写入
                zipOutputStream.closeEntry();

                // 关闭当前文件的 input流
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
