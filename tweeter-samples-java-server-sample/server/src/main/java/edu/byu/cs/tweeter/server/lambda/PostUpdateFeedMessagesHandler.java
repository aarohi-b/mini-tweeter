package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import java.util.List;

import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.UpdateFeedRequest;
import edu.byu.cs.tweeter.server.JsonSerializer;
import edu.byu.cs.tweeter.server.dao.factory.DynamoFactory;
import edu.byu.cs.tweeter.server.service.FollowService;
import edu.byu.cs.tweeter.util.Pair;

public class PostUpdateFeedMessagesHandler implements RequestHandler<SQSEvent, Void> {
    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        for (SQSEvent.SQSMessage msg : input.getRecords()) {
            PostStatusRequest request = JsonSerializer.deserialize(msg.getBody(), PostStatusRequest.class);

            FollowService followService = new FollowService(new DynamoFactory());

            Pair<List<String>, Boolean> response = followService.factory.
                    getFollowDAO().getFollowers(new FollowersRequest(
                            request.getAuthToken(), request.getStatus().getUser().getAlias(),
                            250, null));

            List<String> followers = response.getFirst();
            updateFeedQueue(new UpdateFeedRequest(followers, request.getStatus()));

            while(response.getSecond()){
                response = followService.factory.getFollowDAO().getFollowers(new FollowersRequest(
                                request.getAuthToken(), request.getStatus().getUser().getAlias(),
                                250, followers.get(followers.size()-1)));

                followers = response.getFirst();
                updateFeedQueue(new UpdateFeedRequest(followers, request.getStatus()));
            }

        }
        return null;
    }

    public void updateFeedQueue(UpdateFeedRequest request){
        String queueUrl = "https://sqs.us-west-2.amazonaws.com/876591137690/UpdateFeedQueue";

        try {
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(JsonSerializer.serialize(request));

            AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
            SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);

        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to put updateFeedRequest in UpdateFeed SQS Queue");
        }
    }
}
