package com.behase.relumin.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ClusterTest {
	private Cluster cluster;

	@Before
	public void init() {
		cluster = new Cluster();
	}

	@Test
	public void getNodeByHostAndPort() {
		cluster.setNodes(Lists.newArrayList(
				ClusterNode.builder().hostAndPort("localhost:10000").build(),
				ClusterNode.builder().hostAndPort("localhost:10001").build()
		));

		assertThat(cluster.getNodeByHostAndPort("localhost:10000"), is(notNullValue()));
		assertThat(cluster.getNodeByHostAndPort("localhost:10003"), is(nullValue()));
	}

	@Test
	public void getNodeByNodeId() {
		cluster.setNodes(Lists.newArrayList(
				ClusterNode.builder().nodeId("nodeId1").build(),
				ClusterNode.builder().nodeId("nodeId2").build()
		));

		assertThat(cluster.getNodeByNodeId("nodeId1"), is(notNullValue()));
		assertThat(cluster.getNodeByNodeId("nodeId3"), is(nullValue()));
	}

	@Test
	public void getStatus() {
		cluster.setStatus("status");
		assertThat(cluster.getStatus(), is("status"));

		cluster.setStatus(null);
		cluster.setInfo(ImmutableMap.of("cluster_state", "fail"));
		assertThat(cluster.getStatus(), is("fail"));

		cluster.setStatus(null);
		cluster.setInfo(ImmutableMap.of());
		cluster.setNodes(Lists.newArrayList(
				ClusterNode.builder().flags(Sets.newHashSet("fail")).build()
		));
		assertThat(cluster.getStatus(), is("warn"));

		cluster.setStatus(null);
		cluster.setInfo(ImmutableMap.of());
		cluster.setNodes(Lists.newArrayList(
		));
		assertThat(cluster.getStatus(), is("ok"));
	}
}
