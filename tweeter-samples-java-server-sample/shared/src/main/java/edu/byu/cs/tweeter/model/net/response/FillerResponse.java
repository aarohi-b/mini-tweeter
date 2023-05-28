package edu.byu.cs.tweeter.model.net.response;

public class FillerResponse extends Response{
    public FillerResponse(String message) {
        super(false, message);
    }


    public FillerResponse() {
        super(true, null);
    }
}
