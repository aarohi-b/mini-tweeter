package edu.byu.cs.tweeter.server.lambda;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.JsonSerializer;
import edu.byu.cs.tweeter.server.dao.factory.DynamoFactory;
import edu.byu.cs.tweeter.server.service.StatusService;

public class PostStatusHandler implements RequestHandler<PostStatusRequest, PostStatusResponse> {
    @Override
    public PostStatusResponse handleRequest(PostStatusRequest input, Context context) {
        StatusService statusService = new StatusService(new DynamoFactory());
        PostStatusResponse postStatusResponse = statusService.postStatus(input);
        if (postStatusResponse.getMessage()==null) {
            String queueUrl = "https://sqs.us-west-2.amazonaws.com/876591137690/PostStatusQueue";
            try {
                SendMessageRequest sendMessageRequest = new SendMessageRequest().withQueueUrl(queueUrl)
                        .withMessageBody(JsonSerializer.serialize(input));
                AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
                sqs.sendMessage(sendMessageRequest);
            } catch (Exception e) {
                throw new RuntimeException("[Internal Error] Failed to put PostStatusRequest in PostStatus SQS Queue");
            }
        }
        return postStatusResponse;
    }
}