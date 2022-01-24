package com.sanri.tools.modules.codepatch.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;

import java.io.File;

public class CustomSshSessionFactory extends JschConfigSessionFactory {
    private File sshKeyFile;

    public CustomSshSessionFactory(File sshKeyFile) {
        this.sshKeyFile = sshKeyFile;
    }

    @Override
    protected void configure(OpenSshConfig.Host hc, Session session) {
        session.setConfig("StrictHostKeyChecking", "no");
    }

    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        final JSch defaultJSch = super.createDefaultJSch(fs);
//        defaultJSch.addIdentity(sshKeyFile.getAbsolutePath(),"");
        defaultJSch.addIdentity(sshKeyFile.getAbsolutePath());
        return defaultJSch;
    }
}
