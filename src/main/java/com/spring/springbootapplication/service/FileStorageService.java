package com.spring.springbootapplication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.io.IOException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String S3_BASE_URL;

    // 画像ファイルの保存先
    private final Path fileStorageLocation = Paths.get("src/main/resources/static/images/profile").toAbsolutePath().normalize();

    // コンストラクタにBeanとプロパティを注入
    public FileStorageService(

        // AWS S3と通信するためのクライアント
        S3Client s3Client,
            
        // カスタムプロパティ（application.propertiesを参照）
        @Value("${app.aws.s3.bucket-name}") String bucketName,
        @Value("${spring.cloud.aws.region.static}") String region
        
        ) {
            
        this.s3Client = s3Client;
        this.bucketName = bucketName;

        //// 外部アクセス用のURL
        this.S3_BASE_URL = String.format("https://%s.s3.%s.amazonaws.com/profile/", bucketName, region);

        //エラー時の処理
        try {

            // ディレクトリが存在しない場合
            Files.createDirectories(this.fileStorageLocation);
 
        } catch (Exception ex) {

            throw new RuntimeException("アップロード用ディレクトリの作成に失敗しました。", ex);

        }
    }

    // 画像ファイルを保存・アップロード
    public String storeFile(MultipartFile file) throws IOException {

        // UUIDを付与
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isEmpty()) {

            // 処理を中断
            throw new IllegalArgumentException("アップロードされたファイルにファイル名がありません。");

        }
        // ファイル名と拡張子の処理
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // ファイル名の生成(ファイル名の一意確保のためランダムでのファイル名を付与)
        String s3Key = "profile/" + UUID.randomUUID().toString() + extension;

        try {
            // リクエストの処理
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            // アップロード処理
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return S3_BASE_URL + s3Key.substring("profile/".length());

        //エラー時の処理
        } catch (Exception e) {
            
        throw new RuntimeException("S3へのファイルアップロードに失敗しました。", e);
            
        }
    }
    
    // 画像ファイルの削除
    public void deleteFile(String imagePath) {

        // パスがnullや空なら処理を中止・デフォルト画像の表示
        if (imagePath == null || imagePath.isEmpty()|| imagePath.contains("default_profile.png")) {
            return;
        }

        try {
            
            // ファイル名の抽出
            String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String s3Key = "profile/" + fileName;

            // S3からファイルを削除
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);

        //エラー時の処理
        } catch (Exception e) {

            System.err.println("ファイル削除に失敗しました: " + imagePath);
            
        }
    }

    // 最終更新日時の取得（ブラウザ・キャッシュ制御の対策）
    public long getLastModifiedTime(String imagePath) {

        // パスがnullや空なら処理を中止（DB内の画像URLの確認）
        if (imagePath == null || imagePath.isEmpty()) {
            return 0L;
        }

        try {

            // ファイル名の抽出
            String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String s3Key = "profile/" + fileName;

            //S3オブジェクトのメタデータを取得
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            return s3Client.headObject(headObjectRequest)
                    .lastModified()
                    .toEpochMilli();

        //エラー時の処理
        } catch (NoSuchKeyException e) {
                
            return 0L;
                
        } catch (Exception e) {

            e.printStackTrace();
                
            return 0L;
                
        }
    }
}