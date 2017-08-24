package edu.uci.ics.textdb.exp.twitterfeed;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.twitter.hbc.core.endpoint.Location;
import edu.uci.ics.textdb.api.utils.Utils;

import static edu.uci.ics.textdb.exp.twitterfeed.TwitterUtils.twitterSchema.TWEET_COORDINATES;
import static org.mockito.Mockito.mock;

/**
 * Created by Chang on 7/13/17.
 */
public class TwitterFeedTestHelper {

    public static List<Tuple> getQueryResults(List<String> queryList, String locationList, List<String> languageList, int limit) throws TextDBException {
        TwitterFeedSourcePredicate predicate = new TwitterFeedSourcePredicate(10, queryList, locationList, languageList, null, null, null, null);
        TwitterFeedOperator twitterFeedOperator = new TwitterFeedOperator(predicate);
        twitterFeedOperator.setLimit(limit);
        twitterFeedOperator.setTimeout(20);
        Tuple tuple;
        List<Tuple> results = new ArrayList<>();

        twitterFeedOperator.open();
        while ((tuple = twitterFeedOperator.getNextTuple()) != null) {
            results.add(tuple);
        }
        twitterFeedOperator.close();
        return results;
    }

    public static boolean containsQuery(List<Tuple> resultList, List<String> queryList, List<String> attributeList) {
        if (resultList.isEmpty()) return false;
        for (Tuple tuple : resultList) {
            List<String> toMatch = new ArrayList<>();
            for (String attribute : attributeList) {
                toMatch.addAll(queryList.stream()
                        .filter(s -> tuple.getField(attribute).getValue().toString().toLowerCase().contains(s.toLowerCase()))
                        .collect(Collectors.toList()));
            }
            if (toMatch.isEmpty()) return false;
        }
        return true;
    }

    public static boolean checkKeywordInAttributes(List<Tuple> exactResult, List<String> queryList, List<String> attributeList) {
        List<String> toMatch = new ArrayList<>();
        for (Tuple tuple : exactResult) {
            for (String attribute : attributeList) {
                toMatch.addAll(queryList.stream()
                        .filter(s -> tuple.getField(attribute).getValue().toString().toLowerCase().contains(s.toLowerCase()))
                        .collect(Collectors.toList()));
            }

        }
        if (toMatch.isEmpty()) return false;
        return true;
    }

    public static boolean inLocation(List<Tuple> tupleList, String location) {
        List<String> boundingCoordinate = Arrays.asList(location.trim().split(","));
        List<Double> boundingBox = new ArrayList<>();
        boundingCoordinate.stream().forEach(s -> boundingBox.add(Double.parseDouble(s.trim())));
        Location geoBox = new Location(new Location.Coordinate(boundingBox.get(1), boundingBox.get(0)), new Location.Coordinate(boundingBox.get(3), boundingBox.get(2)));
        Location.Coordinate southwestCoordinate = geoBox.southwestCoordinate();
        Location.Coordinate northeastCoordinate = geoBox.northeastCoordinate();

        for (Tuple tuple : tupleList) {
            if (!tuple.getField(TWEET_COORDINATES).getValue().toString().equals("n/a")) {
                List<String> coordString = Arrays.asList(tuple.getField(TWEET_COORDINATES).getValue().toString().split(","));

                Location.Coordinate coordinate = new Location.Coordinate(Double.parseDouble(coordString.get(0).trim()), Double.parseDouble(coordString.get(1).trim()));
                if (!(coordinate.latitude() > southwestCoordinate.latitude() &&
                        coordinate.longitude() > southwestCoordinate.longitude() &&
                        coordinate.latitude() < northeastCoordinate.latitude() &&
                        coordinate.longitude() < northeastCoordinate.longitude())) {
                    return false;
                }

            }

        }

        return true;
    }

    public static boolean compareTuple(List<Tuple> outputTuple, Tuple expectedTuple) {
        for(Tuple t : outputTuple) {
            for (String attribute : expectedTuple.getSchema().getAttributeNames()) {
                if (!attribute.equals("_id")) {
                    if (! expectedTuple.getField(attribute).getValue().toString().equals(expectedTuple.getField(attribute).getValue().toString())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
