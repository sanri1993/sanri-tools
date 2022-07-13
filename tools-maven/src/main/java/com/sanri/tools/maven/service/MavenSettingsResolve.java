package com.sanri.tools.maven.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.OnlyPath;

import lombok.extern.slf4j.Slf4j;

/**
 * settings 文件解析, 需要获取本地仓库路径
 */
@Service
@Slf4j
public class MavenSettingsResolve {

    @Autowired
    private ConnectService connectService;
    @Autowired
    private FileManager fileManager;

    private SettingsXpp3Reader settingsXpp3Reader = new SettingsXpp3Reader();

    public static final String MODULE = "maven";

    /**
     * settings 文件解析
     * @param settingsName
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public Settings parseSettings(String settingsName) throws IOException, XmlPullParserException {
        final File settingsFile = connectService.connectFile(MODULE, settingsName);
        try(final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(settingsFile), StandardCharsets.UTF_8);){
            return settingsXpp3Reader.read(inputStreamReader);
        }
    }

    /**
     * 获取用户 maven 设置文件
     * @param settingsName
     * @return
     */
    public File settingsFile(String settingsName){
        return connectService.connectFile(MODULE, settingsName);
    }

    /**
     * 获取本地仓库地址
     * @param settingsName
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public File getLocalRepository(String settingsName) throws IOException, XmlPullParserException {
        final Settings settings = parseSettings(settingsName);
        final String localRepository = settings.getLocalRepository();
        final OnlyPath onlyPath = new OnlyPath(localRepository);
        return onlyPath.resolveFile(fileManager.getDataBase());
    }

    /**
     * 获取远程仓库列表 <br/>
     * 先找 active-profile, 然后查找 mirrors
     * @param settingsName settings 配置文件
     */
    public List<Repository> getRemoteRepositorys(String settingsName) throws IOException, XmlPullParserException {
        final Settings settings = parseSettings(settingsName);
        final List<Profile> profiles = settings.getProfiles();
        final List<String> activeProfiles = settings.getActiveProfiles();

        List<Repository> repositories = new ArrayList<>();

        for (Profile profile : profiles) {
            final String id = profile.getId();
            if (activeProfiles.contains(id)){
                repositories.addAll(profile.getRepositories());
            }
        }

        if (CollectionUtils.isEmpty(repositories)){
            // 如果在 profile 中没有找到仓库信息, 则去 mirrors 中查找
            for (Mirror mirror : settings.getMirrors()) {
                final Repository repository = new Repository();
                repository.setUrl(mirror.getUrl());
                repository.setId(mirror.getId());
                repository.setName(mirror.getName());
                repositories.add(repository);
            }
        }

        return repositories;
    }

    public List<RemoteRepository> getRemoteRepositorys2(String settingsName) throws IOException, XmlPullParserException {
        final List<Repository> remoteRepositorys = getRemoteRepositorys(settingsName);
        final List<RemoteRepository> remoteRepositories = new ArrayList<>();
        for (Repository repository : remoteRepositorys) {
//            final RepositoryPolicy snapshots = new RepositoryPolicy(repository.getSnapshots().isEnabled(), repository.getSnapshots().getUpdatePolicy(), repository.getSnapshots().getChecksumPolicy());
//            final RepositoryPolicy releases = new RepositoryPolicy(repository.getReleases().isEnabled(), repository.getReleases().getUpdatePolicy(), repository.getReleases().getChecksumPolicy());
            RemoteRepository remoteRepository = new RemoteRepository.Builder(repository.getId(), "default", repository.getUrl())
                    .build();
            remoteRepositories.add(remoteRepository);
        }
        return remoteRepositories;
    }

    /**
     * 获取仓库系统
     * @param connName
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public RepositorySystem repositorySystem(boolean offline) throws IOException, XmlPullParserException {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        if (!offline) {
            locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        }
        return locator.getService(RepositorySystem.class);
    }

    /**
     * 获取 session
     * @param settingsName
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public RepositorySystemSession repositorySystemSession(String settingsName,RepositorySystem repositorySystem) throws IOException, XmlPullParserException {
        final Settings settings = parseSettings(settingsName);

        LocalRepository localRepo = new LocalRepository(settings.getLocalRepository());
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
        session.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));
        return session;
    }
}
