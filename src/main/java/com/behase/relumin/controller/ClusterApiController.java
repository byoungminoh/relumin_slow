package com.behase.relumin.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.behase.relumin.exception.InvalidParameterException;
import com.behase.relumin.model.Cluster;
import com.behase.relumin.model.Notice;
import com.behase.relumin.service.ClusterService;
import com.behase.relumin.service.LoggingOperationService;
import com.behase.relumin.service.NodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class ClusterApiController {
	@Autowired
	ClusterService clusterService;

	@Autowired
	NodeService nodeService;

	@Autowired
	private LoggingOperationService loggingOperationService;

	@Autowired
	ObjectMapper mapper;

	@RequestMapping(value = "/clusters", method = RequestMethod.GET)
	public Object getClusterList(
			@RequestParam(defaultValue = "") String full
			) {
		Set<String> clusterNamesSet = clusterService.getClusters();
		List<String> clusterNames = Lists.newArrayList(clusterNamesSet);
		Collections.sort(clusterNames);

		if (StringUtils.equalsIgnoreCase(full, "true")) {
			List<Cluster> clusters = Lists.newArrayList();
			Map<String, Cluster> clustersMap = Maps.newHashMap();

			clusterNames.parallelStream().forEach(clusterName -> {
				try {
					clustersMap.put(clusterName, clusterService.getCluster(clusterName));
				} catch (Exception e) {
					log.error("Failed to get cluster. clusterName = {}", clusterName, e);
				}
			});
			clusterNames.forEach(clusterName -> {
				Cluster cluster = clustersMap.get(clusterName);
				if (cluster != null) {
					clusters.add(cluster);
				}
			});

			return clusters;
		} else {
			return clusterNames;
		}
	}

	@RequestMapping(value = "/cluster/{clusterName}", method = RequestMethod.GET)
	public Cluster getCluster(
			@PathVariable String clusterName
			) throws IOException {
		return clusterService.getCluster(clusterName);
	}

	@RequestMapping(value = "/cluster/{clusterName}", method = RequestMethod.POST)
	public Cluster setCluster(
			Authentication authentication,
			@PathVariable String clusterName,
			@RequestParam String hostAndPort
			) throws IOException {
		loggingOperationService.log("registCluster", authentication, "clusterName={}, hostAndPort={}.", clusterName, hostAndPort);

		if (clusterService.existsClusterName(clusterName)) {
			throw new InvalidParameterException(String.format("This clusterName(%s) already exists.", clusterName));
		}
		clusterService.setCluster(clusterName, hostAndPort);
		return clusterService.getCluster(clusterName);
	}

	@RequestMapping(value = "/cluster/{clusterName}/change-cluster-name", method = RequestMethod.POST)
	public Object changeClusterName(
			Authentication authentication,
			@PathVariable String clusterName,
			@RequestParam String newClusterName
	) throws IOException {
		loggingOperationService.log("changeClusterName", authentication, "clusterName={}, newClusterName={}.", clusterName, newClusterName);

		clusterService.changeClusterName(clusterName, newClusterName);

		return clusterService.getCluster(newClusterName);
	}

	@RequestMapping(value = "/cluster/{clusterName}/delete", method = RequestMethod.POST)
	public Map<String, Boolean> deleteClusterByPost(
			Authentication authentication,
			@PathVariable String clusterName
			) {
		loggingOperationService.log("deleteCluster", authentication, "clusterName={}.", clusterName);

		clusterService.deleteCluster(clusterName);

		Map<String, Boolean> result = Maps.newHashMap();
		result.put("isSuccess", true);
		return result;
	}

	@RequestMapping(value = "/cluster/{clusterName}/metrics", method = {RequestMethod.GET, RequestMethod.POST})
	public Object getMetrics(
			@PathVariable String clusterName,
			@RequestParam(defaultValue = "") String nodes,
			@RequestParam(defaultValue = "") String fields,
			@RequestParam(defaultValue = "") String start,
			@RequestParam(defaultValue = "") String end
			) {
		long startLong;
		long endLong;
		try {
			startLong = Long.valueOf(start);
		} catch (Exception e) {
			throw new InvalidParameterException("'start' is must be number.");
		}
		try {
			endLong = Long.valueOf(end);
		} catch (Exception e) {
			throw new InvalidParameterException("'end' is must be number.");
		}

		List<String> nodesList = Lists.newArrayList();
		if (StringUtils.isNotBlank(nodes)) {
			nodesList.addAll(Splitter.on(",").splitToList(nodes));
		}
		if (nodesList.isEmpty()) {
			throw new InvalidParameterException("'nodes' is empty.");
		}

		List<String> fieldsList = Lists.newArrayList();
		if (StringUtils.isNotBlank(fields)) {
			fieldsList.addAll(Splitter.on(",").splitToList(fields));
		}
		if (fieldsList.isEmpty()) {
			throw new InvalidParameterException("'fields' is empty.");
		}

		return clusterService.getClusterStaticsInfoHistory(clusterName, nodesList, fieldsList, startLong, endLong);
	}

	@RequestMapping(value = "/cluster/{clusterName}/notice", method = RequestMethod.GET)
	public Object getClusterNotice(
			@PathVariable String clusterName
			) throws IOException {
		Notice notice = clusterService.getClusterNotice(clusterName);
		if (notice == null) {
			return new Notice();
		}
		return notice;
	}

	@RequestMapping(value = "/cluster/{clusterName}/notice", method = RequestMethod.POST)
	public Object setClusterNotice(
			Authentication authentication,
			@PathVariable String clusterName,
			@RequestParam(defaultValue = "") String notice
			) throws IOException {
		loggingOperationService.log("addNotice", authentication, "clusterName={}, notice={}.", clusterName, notice);

		if (StringUtils.isBlank(notice)) {
			throw new InvalidParameterException("'notice' is blank.");
		}

		Notice noticeObj;
		try {
			noticeObj = mapper.readValue(notice, Notice.class);
		} catch (Exception e) {
			throw new InvalidParameterException("'notice' is invalid format.");
		}

		clusterService.setClusterNotice(clusterName, noticeObj);
		return clusterService.getClusterNotice(clusterName);
	}

    @RequestMapping(value = "/cluster/{clusterName}/slowlogs", method = {RequestMethod.GET, RequestMethod.POST})
    public Object getSlowLogs(
            @PathVariable String clusterName,
            @RequestParam(defaultValue = "") String nodes,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end
    ) {

        long startLong;
        long endLong;
        try {
            startLong = Long.valueOf(start);
        } catch (Exception e) {
            throw new InvalidParameterException("'start' is must be number.");
        }
        try {
            endLong = Long.valueOf(end);
        } catch (Exception e) {
            throw new InvalidParameterException("'end' is must be number.");
        }

        List<String> nodesList = Lists.newArrayList();
        if (StringUtils.isNotBlank(nodes)) {
            nodesList.addAll(Splitter.on(",").splitToList(nodes));
        }
        if (nodesList.isEmpty()) {
            throw new InvalidParameterException("'nodes' is empty.");
        }

        return clusterService.getClusterSlowLogHistory(clusterName, nodesList, startLong, endLong);
    }
}
