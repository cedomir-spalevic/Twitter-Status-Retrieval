import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.json.simple.parser.*;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Retrieve public Twitter statuses with Twitter4j
 * 
 * @author Cedomir Spalevic
 */
public class Stream {

	static int count;
	static int minuteStart;
	static int minuteEnd;
	static String directory;
	static String originalLocation;
    static boolean wantsLocation;
    static double southwest_latitude;
    static double southwest_longitude;
    static double northeast_latitude;
    static double northeast_longitude;
    
    static String OAuthConsumerKey = "";
    static String OAuthConsumerSecret = "";
    static String OAuthAccessToken = "";
    static String OAuthAccessTokenSecret = "";
	static String GoogleMapsApiKey = "";
    
    public static void main(String[] args) {

		if(args.length > 1) {
			System.out.println("Usage: run.bat\n"
				+ "Usage: run.bat name_of_city_to_filter_by");
			System.exit(1);
		}
		
		if(args.length == 1) {
			//set wants location to true
			wantsLocation = true;
			//copy original location
			originalLocation = args[0];
		}
		else {
			//set wants location to false
			wantsLocation = false;
			//original location is sample
			originalLocation = "sample";
		}

		//set API tokens from credentials.json
		try {
			JSONParser parser = new JSONParser();
			JSONArray array = (JSONArray) parser.parse(new FileReader("credentials.json"));
			JSONObject json = (JSONObject) array.get(0);

			OAuthConsumerKey = (String) json.get("OAuthConsumerKey");
			OAuthConsumerSecret = (String) json.get("AuthConsumerSecret");
			OAuthAccessToken = (String) json.get("OAuthAccessToken");
			OAuthAccessTokenSecret = (String) json.get("OAuthAccessTokenSecret");
			GoogleMapsApiKey = (String) json.get("GoogleMapsApiKey");
		}
		catch(FileNotFoundException e) {
			System.out.println("Could not find credentials.json");
			System.exit(1);
		}
		catch(Exception e) {
			System.out.println("Could not read credentials from credentials.json");
			System.exit(1);
		}

		//the address attribute in the google maps HTTP get request does not accept spaces
		String location = args[0].replace(' ', '+');
		//Google Maps API to retrieve bounds of region
		String url_string = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&key=" + GoogleMapsApiKey;
		try {
			URL url = new URL(url_string);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			while((read = reader.read()) != -1)
				buffer.append((char) read);
			JSONObject json = new JSONObject(buffer.toString());
			JSONObject geometry = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("viewport");
			JSONObject southwest = geometry.getJSONObject("southwest");
			southwest_latitude = (double) southwest.get("lat");
			southwest_longitude = (double) southwest.get("lng");
			JSONObject northeast = geometry.getJSONObject("northeast");
			northeast_latitude = (double) northeast.get("lat");
			northeast_longitude = (double) northeast.get("lng");
		}
		catch(Exception e) {
			System.out.println("Could not find coordinates.");
			System.exit(1);
		}
	
		startListening();
    }
	
	public static void startListening() {
		System.out.println("Starting new Status Listener...");

		//setting current directory
		directory = System.getProperty("user.dir") + "/TwitterData/";
		File dir = new File(directory);
		if(!dir.exists()) {
			if(dir.mkdirs()) System.out.println("Directory is created!");
			else System.out.println("Directory not created.");
		}

		//creating directory strings
		Date date = new Date();
		SimpleDateFormat day = new SimpleDateFormat("YYYYMMdd_hmma");
		//the folder name will be the current date and time the program is run
		String foldername = day.format(date)+"-"+originalLocation;
		
		//creating folders to store tweets
		File folder = new File(directory+foldername);
		if(!folder.exists()) {
			if(folder.mkdirs()) System.out.println(foldername + " folder is created!");
			else System.out.println(foldername + " folder not created.");
		}

		//setting column names for tweets.csv file
		try {
			FileWriter fw = new FileWriter(new File(folder.toString() + "/tweets.csv"), true);
			String columns = "User ID\tUser name\tUser screen name\tDate user created account\t" +
						"Statuses posted\tAmount of followers\tAmount user is following\t" + 
						"User location\tUser timezone\tUser language\tStatus ID\tStatus language\t" +
						"Date created\tStatus location\tIs favorited\tAmount of favorites\t" +
						"Is retweeted\tAmount of retweets\tIs retweeted by me\tIs a retweet\t" +
						"Retweeted status ID\tIs possibly sensitive\tIs truncated\t" +
						"Place\tDescription\tText\n";
			fw.write(columns);
			fw.close();
		}
		catch(IOException e) {
			System.out.println("Could not write to .csv file");
		}

		//setting column names for tweets_with_coordiantes.csv file
		try {
			FileWriter fw = new FileWriter(new File(folder.toString() + "/tweets_with_coordinates.csv"), true);
			String columns = "User ID\tUser name\tUser screen name\tDate user created account\t" +
						"Statuses posted\tAmount of followers\tAmount user is following\t" + 
						"User location\tUser timezone\tUser language\tStatus ID\tStatus language\t" +
						"Date created\tStatus location\tIs favorited\tAmount of favorites\t" +
						"Is retweeted\tAmount of retweets\tIs retweeted by me\tIs a retweet\t" +
						"Retweeted status ID\tIs possibly sensitive\tIs truncated\t" +
						"Place\tDescription\tText\n";
			fw.write(columns);
			fw.close();
		}
		catch(IOException e) {
			System.out.println("Could not write to .csv file");
		}
		count = 0;
		minuteStart = 0;
		minuteEnd = Calendar.getInstance().get(Calendar.MINUTE) + 30;
		if(minuteEnd>=60) minuteEnd = minuteEnd%60;
		
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(OAuthConsumerKey)
                .setOAuthConsumerSecret(OAuthConsumerSecret)
                .setOAuthAccessToken(OAuthAccessToken)
                .setOAuthAccessTokenSecret(OAuthAccessTokenSecret);

		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		
		//Status Listener -- continuously listens for new tweets to come in
		StatusListener statusListener = new StatusListener() {

			@Override
			public void onStatus(twitter4j.Status status) {
				User user = status.getUser();
				boolean user_null = (user!=null?true:false);
				boolean status_location = (status.getGeoLocation()!=null?true:false);
				boolean status_place = (status.getPlace()!=null?true:false);
				
				//extra filter to get rid of any outliers
				boolean stay = false;
				if(status_location && 
					status.getGeoLocation().getLatitude() >= southwest_latitude &&
					status.getGeoLocation().getLatitude() <= northeast_latitude &&
					status.getGeoLocation().getLongitude() >= southwest_longitude &&
					status.getGeoLocation().getLongitude() <= northeast_longitude)
					stay = true;
				if(status_place &&
					status.getPlace().getBoundingBoxCoordinates()[0][0].getLatitude() >= southwest_latitude &&
					status.getPlace().getBoundingBoxCoordinates()[0][0].getLongitude() >= southwest_longitude &&
					status.getPlace().getBoundingBoxCoordinates()[0][1].getLatitude() <= northeast_latitude &&
					status.getPlace().getBoundingBoxCoordinates()[0][1].getLongitude() <= northeast_longitude)
					stay = true;
		
				if(stay) {
					count++;
					
					//for writing the serialized object to a stream
					try {
						FileOutputStream fos = new FileOutputStream(folder.toString() + "/tweets-" + count + ".ser");
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(status);
						oos.close();
						fos.close();
					}
					catch(Exception e) {
						System.out.println("Could not write tweet #" + count + " as a serialized object.");
					}
					
					//for writing the status and user object attributes to a .txt file	
					try {
						FileWriter fw = new FileWriter(new File(folder.toString() + "/tweets.txt"), true);
						fw.write("Tweet #" + count + ":\n");
						
						fw.write("User ID: " + (user_null?user.getId():"NULL_NULL_NULL") + "\n");
						fw.write("User name: " + (user_null?user.getName():"NULL_NULL_NULL") + "\n");
						fw.write("User screen name: " + (user_null?user.getScreenName():"NULL_NULL_NULL") + "\n");
						fw.write("Date user created account: " + (user_null?user.getCreatedAt().toString():"NULL_NULL_NULL") + "\n");
						fw.write("Statuses posted: " + (user_null?user.getStatusesCount():"NULL_NULL_NULL") + "\n");
						fw.write("Amount of followers: " + (user_null?user.getFollowersCount():"NULL_NULL_NULL") + "\n");
						fw.write("Amount user is following: " + (user_null?user.getFriendsCount():"NULL_NULL_NULL") + "\n");
						fw.write("User location: " + (user_null&&user.isGeoEnabled()?user.getLocation():"NULL_NULL_NULL") + "\n");
						fw.write("User timezone: " + (user_null?user.getTimeZone():"NULL_NULL_NULL") + "\n");
						fw.write("User language: " + (user_null?user.getLang():"NULL_NULL_NULL") + "\n");
						fw.write("Status ID: " + status.getId() + "\n");
						fw.write("Status language: " + status.getLang() + "\n");
						fw.write("Date created: " + status.getCreatedAt().toString() + "\n");
						fw.write("Status location: " + (status_location?status.getGeoLocation().getLatitude() + ", " + status.getGeoLocation().getLongitude():"NULL_NULL_NULL") + "\n");
						fw.write("Is favorited: " + status.isFavorited() + "\n");
						fw.write("Amount of favorites: " + status.getFavoriteCount() + "\n");
						fw.write("Is retweeted: " + status.isRetweeted() + "\n");
						fw.write("Amount of retweets: " + status.getRetweetCount() + "\n");
						fw.write("Is retweeted by me: " + status.isRetweetedByMe() + "\n");
						fw.write("Is a reweet: " + status.isRetweet() + "\n");
						Status t = status;
						while(t.isRetweet()) {
							fw.write("Retweeted status ID: " + t.getRetweetedStatus().getId() + "\n");
							t = t.getRetweetedStatus();
						}
						fw.write("Is possibly sensitive: " + status.isPossiblySensitive() + "\n");
						fw.write("Is truncated: " + status.isTruncated() + "\n");
						fw.write("Place: " + (status_place?status.getPlace().getFullName():"NULL_NULL_NULL") + "\n");
						fw.write("Description: " + (user_null?user.getDescription():"NULL_NULL_NULL") + "\n\n");
						fw.write(status.getText() + "\n\n-\n\n");
						fw.close();
					}
					catch(IOException e) {
						System.out.println("Could not write tweet #" + count + " to the text file.");
					}
					
					//for writing the status and user object attributes to a .csv file to be imported into Excel
					try {
						FileWriter fw = new FileWriter(new File(folder.toString() + "/tweets.csv"), true);
						
						fw.write((user_null?user.getId():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getName():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getScreenName():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getCreatedAt().toString():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getStatusesCount():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getFollowersCount():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getFriendsCount():"NULL_NULL_NULL") + "\t");
						fw.write((user_null&&user.isGeoEnabled()?user.getLocation():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getTimeZone():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getLang():"NULL_NULL_NULL") + "\t");
						fw.write(status.getId() + "\t");
						fw.write(status.getLang() + "\t");
						fw.write(status.getCreatedAt().toString() + "\t");
						fw.write((status_location?status.getGeoLocation().getLatitude() + ", " + status.getGeoLocation().getLongitude():"NULL_NULL_NULL") + "\t");
						fw.write(status.isFavorited() + "\t");
						fw.write(status.getFavoriteCount() + "\t");
						fw.write(status.isRetweeted() + "\t");
						fw.write(status.getRetweetCount() + "\t");
						fw.write(status.isRetweetedByMe() + "\t");
						fw.write( status.isRetweet() + "\t");
						Status t = status;
						if(t.isRetweet()) {
							while(t.isRetweet()) {
								fw.write(t.getRetweetedStatus().getId() + ", ");
								t = t.getRetweetedStatus();
							}
							fw.write("\t");
						}
						else fw.write("NULL_NULL_NULL\t");
						fw.write(status.isPossiblySensitive() + "\t");
						fw.write(status.isTruncated() + "\t");
						fw.write((status_place?status.getPlace().getFullName():"NULL_NULL_NULL") + "\t");
						fw.write((user_null?user.getDescription().replace("\t"," "):"NULL_NULL_NULL") + "\t");
						fw.write(status.getText().replace("\t"," ") + "\n");
						fw.close();
					}
					catch(IOException e) {
						System.out.println("Could not write tweet #" + count + " to the text file.");
					}
					
					//only write to this file if the status contains coordinates
					if(status_location) {
						//for writing the status and user object attributes to a .csv file to be imported into Excel (only statuses with coordinates)
						try {
							FileWriter fw = new FileWriter(new File(folder.toString() + "/tweets_with_coordinates.csv"), true);
							
							fw.write((user_null?user.getId():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getName():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getScreenName():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getCreatedAt().toString():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getStatusesCount():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getFollowersCount():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getFriendsCount():"NULL_NULL_NULL") + "\t");
							fw.write((user_null&&user.isGeoEnabled()?user.getLocation():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getTimeZone():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getLang():"NULL_NULL_NULL") + "\t");
							fw.write(status.getId() + "\t");
							fw.write(status.getLang() + "\t");
							fw.write(status.getCreatedAt().toString() + "\t");
							fw.write((status_location?status.getGeoLocation().getLatitude() + ", " + status.getGeoLocation().getLongitude():"NULL_NULL_NULL") + "\t");
							fw.write(status.isFavorited() + "\t");
							fw.write(status.getFavoriteCount() + "\t");
							fw.write(status.isRetweeted() + "\t");
							fw.write(status.getRetweetCount() + "\t");
							fw.write(status.isRetweetedByMe() + "\t");
							fw.write( status.isRetweet() + "\t");
							Status t = status;
							if(t.isRetweet()) {
								while(t.isRetweet()) {
									fw.write(t.getRetweetedStatus().getId() + ", ");
									t = t.getRetweetedStatus();
								}
								fw.write("\t");
							}
							else fw.write("NULL_NULL_NULL\t");
							fw.write(status.isPossiblySensitive() + "\t");
							fw.write(status.isTruncated() + "\t");
							fw.write((status_place?status.getPlace().getFullName():"NULL_NULL_NULL") + "\t");
							fw.write((user_null?user.getDescription().replace("\t"," "):"NULL_NULL_NULL") + "\t");
							fw.write(status.getText().replace("\t"," ") + "\n");
							fw.close();
						}
						catch(IOException e) {
							System.out.println("Could not write tweet #" + count + " to the text file.");
						}
					}
		
					//setting minuteStart to the current minute
					minuteStart = Calendar.getInstance().get(Calendar.MINUTE);
					//if it has been 30 minutes since program start, kill the program
					if(minuteStart == minuteEnd) {
						System.out.println("Killing current stream after receiving " + count + " tweets.\n");
						System.exit(1);
					}
				}
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice StatusDeletionNotice){}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses){}

			@Override
			public void onScrubGeo(long userId, long upToStatusId){}

			@Override
			public void onException(Exception e){}

			@Override
			public void onStallWarning(StallWarning warning){}
		};
        
		//Rate Limit Status Listener -- notifies you when you have reached a rate limit
		RateLimitStatusListener rateLimitStatusListener = new RateLimitStatusListener() {
			//Called when the account or IP address is hitting the rate limit.<br>
			//onRateLimitStatus will be also called before this event.
			@Override
			public void onRateLimitReached(RateLimitStatusEvent event) {
				System.out.println("Rate limit reached!\n" +
						"Is account rate limit status: " + event.isAccountRateLimitStatus() + "\n" +
						"Is IP rate limit status: " + event.isIPRateLimitStatus() + "\n" +
						"Limit: " + event.getRateLimitStatus().getLimit() + "\n" +
						"Remaining: " + event.getRateLimitStatus().getRemaining() + "\n" +
						"Reset time (in seconds): " +event.getRateLimitStatus().getResetTimeInSeconds() + "\n" +
						"Seconds until reset: " + event.getRateLimitStatus().getSecondsUntilReset() + "\n");
			}

			//Called when the response contains rate limit status
			@Override
			public void onRateLimitStatus(RateLimitStatusEvent event) {
				System.out.println("Rate limit status!\n" +
						"Is account rate limit status: " + event.isAccountRateLimitStatus() + "\n" +
						"Is IP rate limit status: " + event.isIPRateLimitStatus() + "\n" +
						"Limit: " + event.getRateLimitStatus().getLimit() + "\n" +
						"Remaining: " + event.getRateLimitStatus().getRemaining() + "\n" +
						"Reset time (in seconds): " +event.getRateLimitStatus().getResetTimeInSeconds() + "\n" +
						"Seconds until reset: " + event.getRateLimitStatus().getSecondsUntilReset() + "\n");
				System.out.println("Sleeping for " + event.getRateLimitStatus().getResetTimeInSeconds() + " seconds");
				try {
					TimeUnit.SECONDS.sleep(event.getRateLimitStatus().getResetTimeInSeconds());
					System.exit(1);
				}	
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

        //StatusListener - continuously listens for new tweets to come in
		twitterStream.addListener(statusListener);
		
		//RateLimitStatusListener - notifies you on a rate limit
		twitterStream.addRateLimitStatusListener(rateLimitStatusListener);
		
        //if wants location, add filter
        if(wantsLocation) {
        	FilterQuery filter = new FilterQuery();
            //filter by the location bounds found by Google Maps Geocoding API
            double[][] locations = {{southwest_longitude, southwest_latitude}, 
            		{northeast_longitude, northeast_latitude}};
            filter.locations(locations);
            twitterStream.filter(filter);
        }
        else twitterStream.sample();
	}
}