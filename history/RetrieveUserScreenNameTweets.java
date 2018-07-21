package history;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;
 
public class RetrieveUserScreenNameTweets 
{
    final static String OAuthConsumerKey = "zZZ9lLXVclwV4jvJ0h9uMkL6h";
    final static String OAuthConsumerSecret = "tfKHA8jBGNhtPD3sbOHLgeXLQmqB14Dbis5vyxRJBaLIU2AwTy";
    final static String OAuthAccessToken = "898555464-x7oZQrkdi2nHrNILqOYhaJBGfboVT4KOsoRaXy11";
    final static String OAuthAccessTokenSecret = "pEJEo2IGX2qfB8fYNjnnoORaD2P0hB5TjSOu04kzgjkoD";
     
    public static void main(String[] args) 
    {
        String user_screenname = "";
        Date ending_date = null;
        Date beginning_date = null;
        boolean wants_date = false;
         
        if(args.length != 1 || args.length > 3)
        {
            System.out.println("Usage: user_screenname or\n" +
                                "Usage: user_screenname ending_date beginning_date\n");
            System.exit(1);
        }
         
        //get arguments
        user_screenname = args[0];
        if(args.length == 3)
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                ending_date = sdf.parse(args[1]);
                beginning_date = sdf.parse(args[2]);
            }
            catch(ParseException e)
            {
                System.out.println("Usage for ending_date and beginning_date: mm/dd/yyyy\n");
                System.exit(1);
            }
            wants_date = true;
        }
         
        //setting current directory
        String directory = System.getProperty("user.dir") + "/TwitterData/";
        File dir = new File(directory);
        if(!dir.exists()) dir.mkdirs();
        //setting user_screenname folder
        File folder = new File(directory+user_screenname+"/");
        if(!folder.exists()) folder.mkdirs();
        //setting column names for .csv file
        try
        {
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
        catch(IOException e)
        {
            System.out.println("Could not write to .csv file");
        }
        //setting column names for tweets_with_coordiantes.csv file
        try
        {
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
        catch(IOException e)
        {
            System.out.println("Could not write to .csv file");
        }
         
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(OAuthConsumerKey)
                .setOAuthConsumerSecret(OAuthConsumerSecret)
                .setOAuthAccessToken(OAuthAccessToken)
                .setOAuthAccessTokenSecret(OAuthAccessTokenSecret);
         
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
         
        //get the number of total statuses posted by the user
        List<Status> temp;
        int statuses_posted = 0;
        try
        {
            temp = twitter.getUserTimeline(user_screenname);
            statuses_posted = temp.get(0).getUser().getStatusesCount();
        }
        catch(TwitterException e)
        {
        		e.printStackTrace();
            System.out.println("Could not receive users timeline");
            System.exit(1);
        }
         
        int pageno = 1;
        int total = 0;
        System.out.println("Receiving tweets...");
        //while loop to continuously receive approx. 3000 tweets from the user (or the total amount of tweets posted if less)
        while(true)
        {
            int per_page = 100;
            //if we received all the statuses from the user or if we reached 3000, then break
            if(statuses_posted-total <= 0 || total >= 3000) break;
             
            //if there are less than 100 statuses left, get the remaining statuses
            if(statuses_posted-total < 100) per_page = statuses_posted-total;
             
            //set the pagination
            Paging paging = new Paging(pageno, per_page);
            //get the statuses
            List<Status> statuses = null;
            try
            {
                statuses = twitter.getUserTimeline(user_screenname, paging);
            }
            catch(TwitterException e)
            {
                System.out.println("Could not receive users timeline");
                System.exit(1);
            }
             
            if(statuses.size() == 0)
            {
                System.out.println(user_screenname + " has 0 statuses");
                System.exit(1);
            }
             
            for(int i = 0; i < statuses.size(); i++)
            {   
                Status status = statuses.get(i);
                 
                //determine if the status is within the given time frame, if given
                if(wants_date)
                {
                    //if the status date is after the beginning date, continue
                    if(status.getCreatedAt().compareTo(beginning_date) > 0) continue;
                     
                    //or if the current status is before the ending_date, terminate
                    if(status.getCreatedAt().compareTo(ending_date) < 0)
                    {
                        System.out.println("Killing current stream after receiving " + total + " tweets.\n");
                        System.exit(1);
                    }
                }
                 
                User user = status.getUser();
                boolean user_null = (user!=null?true:false);
                boolean status_location = (status.getGeoLocation()!=null?true:false);
                boolean status_place = (status.getPlace()!=null?true:false);
                 
                //for writing the serialized object to a stream
                try
                {
                    FileOutputStream fos = new FileOutputStream(folder.toString() + "/tweets-" + total + ".ser");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(status);
                    oos.close();
                    fos.close();
                }
                catch(Exception e)
                {
                    System.out.println("Could not write tweet #" + total + " as a serialized object.");
                }
                 
                //for writing the status and user object attributes to a .txt file  
                try
                {
                    FileWriter fw = new FileWriter(new File(folder.toString() + "/tweets.txt"), true);
                    fw.write("Tweet #" + total + ":\n");
                     
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
                    while(t.isRetweet())
                    {
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
                catch(IOException e)
                {
                    System.out.println("Could not write tweet #" + total + " to the text file.");
                }
                 
                //for writing the status and user object attributes to a .csv file to be imported into Excel
                try
                {
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
                    if(t.isRetweet())
                    {
                        while(t.isRetweet())
                        {
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
                catch(IOException e)
                {
                    System.out.println("Could not write tweet #" + total + " to the text file.");
                }
                 
                //only write to this file if the status contains coordinates
                if(status_location)
                {
                    //for writing the status and user object attributes to a .csv file to be imported into Excel (only statuses with coordinates)
                    try
                    {
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
                        if(t.isRetweet())
                        {
                            while(t.isRetweet())
                            {
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
                    catch(IOException e)
                    {
                        System.out.println("Could not write tweet #" + total + " to the text file.");
                    }
                }
                 
                total++;
            }
            pageno++;
        }
        System.out.println("Killing current stream after receiving " + total + " tweets.\n");
    }
}