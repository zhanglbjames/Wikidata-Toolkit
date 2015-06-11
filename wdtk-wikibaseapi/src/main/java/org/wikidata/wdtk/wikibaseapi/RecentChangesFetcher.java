package org.wikidata.wdtk.wikibaseapi;

/*
 * #%L
 * Wikidata Toolkit Wikibase API
 * %%
 * Copyright (C) 2014 - 2015 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.util.WebResourceFetcher;
import org.wikidata.wdtk.util.WebResourceFetcherImpl;

/**
 * Simple class to fetch recent changes
 * 
 * @author Markus Damm
 *
 */
public class RecentChangesFetcher {

	static final Logger logger = LoggerFactory
			.getLogger(WikibaseDataFetcher.class);

	/**
	 * URL prefix for the recent changes feed of wikidata.org.
	 */
	static final String WIKIDATA_RSS_FEED_URL_PREFIX = "http://www.wikidata.org/w/api.php";

	/**
	 * URL suffix for RSS recent changes feed
	 */
	static final String RSS_FEED_URL_SUFFIX = "?action=feedrecentchanges&format=json&feedformat=rss";

	/**
	 * URL suffix for the parameter "from" in the RSS recent changes feed
	 */
	static final String URL_FROM_DATE_SUFFIX = "&from=";

	/**
	 * The URL where the recent changes feed can be found.
	 */
	final String rssUrl;

	/**
	 * Object used to make web requests. Package-private so that it can be
	 * overwritten with a mock object in tests.
	 */
	WebResourceFetcher webResourceFetcher = new WebResourceFetcherImpl();

	/**
	 * Creates an object to fetch recent changes of Wikidata
	 */
	public RecentChangesFetcher() {
		this(WIKIDATA_RSS_FEED_URL_PREFIX);
	}

	/**
	 * Creates an object to fetch recent changes
	 * 
	 * @param rdfUrlPrefix
	 *                Prefix of an URL of the RSS recent changes feed, e.g.
	 *                http://www.wikidata.org/ for wikidata, the suffix is
	 *                added in this constructor
	 */
	public RecentChangesFetcher(String rdfUrlPrefix) {
		this.rssUrl = rdfUrlPrefix + RSS_FEED_URL_SUFFIX;
	}

	/**
	 * Fetches IOStream from RSS feed and return a set of recent changes.
	 * 
	 * @return a set recent changes from the recent changes feed
	 */
	public Set<RecentChange> getRecentChanges() {
		Set<RecentChange> changes = new TreeSet<>();
		try {
			InputStream inputStream = this.webResourceFetcher
					.getInputStreamForUrl(rssUrl);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			String line = bufferedReader.readLine();
			while (line != null) {
				if (line.contains("<item>")) {
					changes.add(parseItem(bufferedReader,
							line));
				}
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			inputStream.close();
		} catch (IOException e) {
			logger.error("Could not retrieve data from " + rssUrl
					+ ". Error:\n" + e.toString());
		}
		return changes;
	}

	/**
	 * Builds an URL for the recent change feed which contains the from
	 * parameter
	 * 
	 * @param from
	 *                earliest Date for recent changes that are fetched
	 * @return URL for the RSS recent changes feed
	 */
	String buildUrl(Date from) {
		String urlDateString = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(from);
		return rssUrl + URL_FROM_DATE_SUFFIX + urlDateString;
	}

	/**
	 * Returns the InputStream for the recent changes feed for a given URL.
	 * 
	 * @param url
	 *                URL of the recent changes feed
	 * @return InputStream for the recent changes feed
	 */
	InputStream getInputStream(String url) {
		try {
			return this.webResourceFetcher
					.getInputStreamForUrl(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * parses the name of the property from the item string of the recent
	 * changes feed
	 * 
	 * @param itemString
	 *                substring for an item of the recent changes feed
	 * @return name of the property
	 */
	String parsePropertyNameFromItemString(String itemString) {
		String startString = "<title>";
		String endString = "</title>";
		int start = itemString.indexOf(startString)
				+ startString.length();
		int end = itemString.indexOf(endString);
		String propertyName = itemString.substring(start, end);
		return propertyName;
	}

	/**
	 * parses the date and time of the change of a property from the item
	 * string of the recent changes feed
	 * 
	 * @param itemString
	 *                substring for an item of the recent changes feed
	 * @return date and time for the recent change
	 */
	Date parseTimeFromItemString(String itemString) {
		int start = 17;
		int end = 41;
		String timeString = itemString.substring(start, end);
		Date date = null;
		try {
			date = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z",
					Locale.ENGLISH).parse(timeString);
		}

		catch (ParseException e) {
			logger.error("Could not parse date from string \""
					+ itemString + "\". Error:\n"
					+ e.toString());
		}
		return date;
	}

	/**
	 * parses the name or the IP address of the change of a property from
	 * the item string of the recent changes feed
	 * 
	 * @param itemString
	 *                substring for an item of the recent changes feed
	 * @return name of the author (if user is registered) or the IP address
	 *         (if user is not registered)
	 */
	String parseAuthorFromItemString(String itemString) {
		String endString = "</dc:creator>";
		int start = 66;
		int end = itemString.indexOf(endString);
		return itemString.substring(start, end);
	}
	
	/**
	 * Parses an item inside the <item>-tag.
	 * 
	 * @param bufferedReader
	 *                reader that reads the InputStream
	 * @param line
	 *                last line from the InputStream that has been read
	 * @return RecentChange that equals the item
	 */
	RecentChange parseItem(BufferedReader bufferedReader, String line) {
		String propertyName = null;
		String author = null;
		Date date = null;
		while (!line.contains("</item>")) {
			try {
				line = bufferedReader.readLine();

				if (line.contains("<title>")) {
					propertyName = parsePropertyNameFromItemString(line);
				}
				if (line.contains("<pubDate>")) {
					date = parseTimeFromItemString(line);
				}
				if (line.contains("<dc:creator>")) {
					author = parseAuthorFromItemString(line);
				}
			} catch (IOException e) {
				logger.error("Could not parse data from "
						+ rssUrl + ". Error:\n"
						+ e.toString());
			}
		}
		if (propertyName == null || author == null || date == null) {
			throw new NullPointerException();
		}
		return new RecentChange(propertyName, date, author);
	}
}