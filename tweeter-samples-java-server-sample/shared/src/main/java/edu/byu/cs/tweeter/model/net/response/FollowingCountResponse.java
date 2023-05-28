package edu.byu.cs.tweeter.model.net.response;

public class FollowingCountResponse extends Response{
    private int followingCount;

    public FollowingCountResponse(String message) {
        super(false, message);
    }

    public FollowingCountResponse(int count) {
        super(true, null);
        this.followingCount = count;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}
