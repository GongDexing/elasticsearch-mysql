package cn.net.communion.sync.elasticsearch;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import cn.net.communion.sync.entity.Node;

public class Client {
    private static TransportClient client;
    private static Logger logger = Logger.getLogger(Client.class);

    private Client(String clusterName, Set<Node> nodes) {
        Settings settings = Settings.builder().put("client.transport.sniff", true)
                .put("cluster.name", clusterName).build();
        client = new PreBuiltTransportClient(settings);
        nodes.stream().forEach(node -> {
            try {
                client.addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName(node.getIp()), node.getPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });
    }

    public static boolean bulkIndex(String index, String type, List<Map<String, Object>> list) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        list.stream().forEach(map -> {
            bulkRequest.add(
                    client.prepareIndex(index, type, String.valueOf(map.get("id"))).setSource(map));
        });
        return !bulkRequest.get().hasFailures();
    }

    public static void searchAll() {
        SearchResponse response = client.prepareSearch().get();
        response.getHits().forEach(hit -> {
            logger.info(hit.getSource());
        });
    }

    public static void searchByIndexType(String index, String type) {
        SearchResponse response = client.prepareSearch(index).setTypes(type).get();
        logger.info(index + "-" + type);
        logger.info(response);
    }

    public static void shutdown() {
        client.close();
    }

}
