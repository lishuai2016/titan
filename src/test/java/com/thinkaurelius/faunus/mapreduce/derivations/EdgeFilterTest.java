package com.thinkaurelius.faunus.mapreduce.derivations;

import com.thinkaurelius.faunus.BaseTest;
import com.thinkaurelius.faunus.FaunusVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;

import java.io.IOException;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeFilterTest extends BaseTest {

    MapReduceDriver<NullWritable, FaunusVertex, NullWritable, FaunusVertex, NullWritable, FaunusVertex> mapReduceDriver;

    public void setUp() throws Exception {
        mapReduceDriver = new MapReduceDriver<NullWritable, FaunusVertex, NullWritable, FaunusVertex, NullWritable, FaunusVertex>();
        mapReduceDriver.setMapper(new EdgeFilter.Map());
        mapReduceDriver.setReducer(new Reducer<NullWritable, FaunusVertex, NullWritable, FaunusVertex>());
    }

    public void testLowWeightedEdgesFiltered() throws IOException {
        Configuration config = new Configuration();
        config.set(EdgeFilter.FUNCTION, "{it -> it.weight <= 0.5}");

        this.mapReduceDriver.withConfiguration(config);
        Map<Long, FaunusVertex> results = runWithToyGraph(BaseTest.ExampleGraph.TINKERGRAPH, this.mapReduceDriver);
        assertEquals(results.size(), 6);
        int numberOfEdges = 0;
        for (final FaunusVertex vertex : results.values()) {
            for (final Edge edge : vertex.getEdges(Direction.BOTH)) {
                //System.out.println(edge.getProperty("weight"));
                assertTrue(((Number) edge.getProperty("weight")).doubleValue() <= 0.5d);
                numberOfEdges++;
            }
        }

        assertEquals(numberOfEdges, 8l);
        assertEquals(4l, this.mapReduceDriver.getCounters().findCounter(EdgeFilter.Counters.EDGES_DROPPED).getValue());
        assertEquals(8l, this.mapReduceDriver.getCounters().findCounter(EdgeFilter.Counters.EDGES_KEPT).getValue());
    }
}
