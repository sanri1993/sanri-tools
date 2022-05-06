package com.sanri.tools.modules.fastdfs.service;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class FastDfsClient {
	private TrackerServer trackerServer;
	private TrackerClient trackerClient;
	private FastDfsConfig fastDfsConfig;

	public FastDfsClient(FastDfsConfig fastDfsConfig) {
		this.fastDfsConfig = fastDfsConfig;
	}

	/**
	 * 下载文件
	 * @param dfsId 文件地址
	 * @return
	 * @throws IOException
	 * @throws MyException
	 */
	public byte[] downloadStream(String dfsId) throws IOException, MyException {
		StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
		return storageClient1.download_file1(dfsId);
	}

	/**
	 * 获取文件信息
	 * @param dfsId
	 * @return
	 * @throws IOException
	 * @throws MyException
	 */
	public FileInfo fileInfo(String dfsId) throws IOException, MyException {
		StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
		final FileInfo fileInfo = storageClient1.get_file_info1(dfsId);
		return fileInfo;
	}

	/**
	 * 重新连接
	 */
	public void reConnect() throws MyException, IOException {
		final String trackerServer = fastDfsConfig.getTrackerServer();
		final String[] szTrackerServers = StringUtils.split(trackerServer, ',');
		InetSocketAddress[] tracker_servers = new InetSocketAddress[szTrackerServers.length];

		for(int i = 0; i < szTrackerServers.length; ++i) {
			String[] parts = szTrackerServers[i].split("\\:", 2);
			if (parts.length != 2) {
				throw new MyException("the value of item \"tracker_server\" is invalid, the correct format is host:port");
			}

			tracker_servers[i] = new InetSocketAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
		}

		TrackerGroup trackerGroup = new TrackerGroup(tracker_servers);
		this.trackerClient = new TrackerClient(trackerGroup);
		this.trackerServer = trackerClient.getConnection();
	}
}