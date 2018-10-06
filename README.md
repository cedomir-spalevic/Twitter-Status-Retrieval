# Twitter Data Retrieval

## history
> used to get tweets by a specific username

## stream
> used to get tweets by a specific location or around the globe

<br/><br/>

#### Please read the following prior to running
This program will automatically create a folder named TwitterData. Inside this folder, another folder will be created with the format > YYYYMMDD_HHMM corresponding to the time it ran.

There will be four formats of all tweets within this folder:
1. tweets.txt - a human readable format of all tweets
2. tweets.csv - a file that can be imported into Excel that provides all the same information as tweets.txt (Tab as the delimiter)
3. tweets_with_coordinates.csv - a file that is specifically for tweets that have coordinates that can be imported into Excel that provides all the same information as tweets.txt (Tab as the delimiter)
4. serialized objects of each tweet

You will need to edit run.bat and provide an argument before running.Change the value of location on the second line, to the location > you want. For example, Nashville, TN.

#### Note
You are now required to apply for a developer account on Twitter. You will need to do that in order to obtain the API Keys

