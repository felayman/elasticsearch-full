package org.elasticsearch.api.demo.delete;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

/**
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class DeleteDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        DeleteResponse response = client.prepareDelete("twitter", "tweet", "1").get();
    }
}
