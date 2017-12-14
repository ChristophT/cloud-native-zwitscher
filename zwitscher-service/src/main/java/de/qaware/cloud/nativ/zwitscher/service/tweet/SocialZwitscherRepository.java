/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 QAware GmbH, Munich, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.qaware.cloud.nativ.zwitscher.service.tweet;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Repository;
import twitter4j.*;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toList;

/**
 * This implementation uses Spring Social Twitter API to access tweets
 * from twitter in order to map them to ZwitscherMessages.
 */
@Repository
@Slf4j
public class SocialZwitscherRepository implements ZwitscherRepository, HealthIndicator {

    private final Twitter twitter = TwitterFactory.getSingleton();

    @Override
    @HystrixCommand(fallbackMethod = "noResults")
    public Collection<ZwitscherMessage> search(String q, int pageSize) {
        Query query = new Query(q);
        query.setCount(pageSize);
        try {
            QueryResult result = twitter.search(query);

            return result.getTweets().stream()
                    .map(status -> new ZwitscherMessage(status.getText()))
                    .collect(toList());
        } catch (TwitterException e) {
            throw new RuntimeException("Could not read tweets");
        }
    }

    protected Collection<ZwitscherMessage> noResults(String q, int pageSize) {
        return Collections.emptyList();
    }

    @Override
    public Health health() {
        // maybe check the connection status here
        return Health.up().build();
    }
}
