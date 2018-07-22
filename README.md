<h1>Twitter Status Retrieval</h1>

**history** is used to get tweets by a specific username<br/>
**stream** is used to get tweets by a specific location or around the globe

Please read the following prior to running this program.

This program will automatically create a folder named TwitterData. Inside this folder, another folder will be created with the format YYYYMMDD_HHMM corresponding to the time it ran.

There will be four formats of all tweets within this folder:

1. tweets.txt - a human readable format of all tweets

2. tweets.csv - a file that can be imported into Excel that provides all the same information as tweets.txt (Tab as the delimiter)

3. tweets_with_coordinates.csv - a file that is specifically for tweets that have coordinates that can be imported into Excel that provides all the same information as tweets.txt (Tab as the delimiter)

4. serialized objects of each tweet

You will need to edit run.bat and provide an argument before running.Change the value of location on the second line, to the location you want. For example, Nashville, TN.

You will need to get your Google Maps API Key. This will be changed in RetrieveTweets.java, but first you need to obtain it.Follow the steps below to get your Google Maps API Key:

1. Go to https://developers.google.com/maps/documentation/geocoding/get-api-key

2. Under Authentication for the standard API API Keys, click GET A KEY

3. Or alternatively, follow the below steps to get an API Key. Once you have a key, open RetrieveTweets.java

5. On line 34, place your new key in GoogleMapsApiKey

You will also need to change your Twitter authentication key. This will need to be changed in RetrieveTweets.java, but first you must obtain your authentication keys if you havent already. Follow the steps below to get your Twitter authentication keys or create a new one and place them into RetrieveTweets.java:

1. Go to https://apps.twitter.com

2. Sign in to your twitter account. Or if you do not have one, then please register.

3. If you are not already on https://apps.twitter.com, go back to this link

4. Click Create New App

5. Fill out the information, and a placeholder name under Website, temporarily. (This may need to be changed later)

6. Check the checkbox under Developer Agreement and click create your Twitter application

7. Click on the tab Keys and Access Tokens

8. You should already have a Consumer Key and a Consumer Secret. Now, you need Access Tokens. Scroll down and click Create my access token

9. Under Your Access Tokens, your Access Token and your Access Token Secret should now be there

10. Now open RetrieveTweets.java

11. Lines 30-33 are where you should place your new authentication keys

12. Place your:Consumer Key in OAuthConsumerKeyConsumer Secret in OAuthConsumerSecretAccess Token in OAuthAccessToken Access Token Secret in OAuthAccessTokenSecret

13. You should now be able to run run.bat!
