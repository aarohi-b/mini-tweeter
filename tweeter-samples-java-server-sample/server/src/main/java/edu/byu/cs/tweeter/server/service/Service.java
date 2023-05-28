package edu.byu.cs.tweeter.server.service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.server.dao.factory.IDAOAbstractFactory;

public class Service {
    public IDAOAbstractFactory factory;
    public Service(IDAOAbstractFactory factory){
        this.factory = factory;
    }
    public static String getSecurePassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "FAILED TO HASH PASSWORD";
    }

    public void follow(String userAlias, String followeeAlias){
        factory.getFollowDAO().follow(userAlias, followeeAlias);
        factory.getUserDAO().updateFollowerCount(followeeAlias, 1);
        factory.getUserDAO().updateFolloweeCount(userAlias,1);
    }
}
