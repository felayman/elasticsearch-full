package org.elasticsearch.api.demo.get;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.api.demo.BaseDemo;
import org.junit.Test;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class GetDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        GetResponse response = client.prepareGet("twitter", "tweet", "1").get();
    }
}
