@echo off

echo > out.txt

aws lambda update-function-code --function-name login --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name RegisterHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name FollowHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name UnfollowHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name GetFollowersCountHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name GetFollowingCountHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name IsFollowerHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name LogoutHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name GetFeedHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name GetStoryHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name GetFollowingHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name GetFollowersHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name GetUserHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name PostStatusHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name PostUpdateFeedMessagesHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt
aws lambda update-function-code --function-name UpdateFeedsHandler --zip-file fileb://server/build/libs/server-all.jar >> out.txt

