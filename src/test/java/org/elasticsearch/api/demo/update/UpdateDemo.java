package org.elasticsearch.api.demo.update;

import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.api.demo.BaseDemo;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Test;

/**
 * @see <a href='https://www.elastic.co/guide/en/elasticsearch/client/java-api/5.5/java-docs-update.html'></a>
 * @auhthor lei.fang@shijue.me
 * @since . 2017-07-08
 */
public class UpdateDemo extends BaseDemo {

    @Test
    public void testForClient() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("index");
        updateRequest.type("type");
        updateRequest.id("1");
        updateRequest.doc(XContentFactory.jsonBuilder()
                .startObject()
                .field("gender", "male")
                .endObject());
        client.update(updateRequest).get();
    }

    @Test
    public void testForPreClient() throws Exception {
//        client.prepareUpdate("ttl", "doc", "1")
//                .setScript(new Script("ctx._source.gender = \"male\""  , ScriptType.INLINE, null, null))
//                .get();
//
//        client.prepareUpdate("ttl", "doc", "1")
//                .setDoc(XContentFactory.jsonBuilder()
//                        .startObject()
//                        .field("gender", "male")
//                        .endObject())
//                .get();
    }

}
