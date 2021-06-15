package com.cdq.product.controller;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-20 10:27
 **/
@RestController
@CrossOrigin
public class TestFdfsController {
    public static void main(String[] args) throws IOException, MyException {
        //通过类加载器，可以获得根目录下面的所有类
        String path = TestFdfsController.class.getClassLoader().getResource("tracker.conf").getPath();

        System.out.println(path);

        // 初始化fdfs的全局配置
        ClientGlobal.init(path);// 读取配置文件中的配置信息

        // 获得一个tracker链接
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();

        // 获取一个storage
        StorageClient storageClient = new StorageClient(connection,null);

        // 上传文件 第一个参数是图片地址 第二个参数是上传格式 第三个参数是原数据列表
        String[] urls = storageClient.upload_file("f:/1.jpg", "jpg", null);

        String imgUrl = "";
        for (String url : urls) {
            imgUrl = imgUrl + "/"+url;
        }

        System.out.println(imgUrl);
    }
}
