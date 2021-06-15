package com.cdq.product.util;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-20 10:49
 **/

import com.cdq.common.util.Result;
import lombok.SneakyThrows;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("admin/product/")
@CrossOrigin
public class FileUploadUtil {
    /***
     * 上传图片
     */
    @SneakyThrows
    @RequestMapping("fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile multipartFile) throws IOException, MyException {

        String imgUrl = "http://192.168.43.147:8080";

        String path = FileUploadUtil.class.getClassLoader().getResource("tracker.conf").getPath();

        System.out.println(path);

        // 初始化fdfs的全局配置
        ClientGlobal.init(path);// 读取配置文件中的配置信息

        // 获得一个tracker链接
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();

        // 获取一个storage
        StorageClient storageClient = new StorageClient(connection,null);

        // 上传文件
        // 123.abc.t.png
        //multipartFile 参数
        //StringUtils.getFilenameExtension 获得最后一个点后面的数据
        String[] urls = storageClient.upload_file(multipartFile.getBytes(), StringUtils.getFilenameExtension(multipartFile.getOriginalFilename()), null);

        for (String url : urls) {
            imgUrl = imgUrl + "/"+url;
        }

        System.out.println(imgUrl);
        return Result.ok(imgUrl);

    }
}
