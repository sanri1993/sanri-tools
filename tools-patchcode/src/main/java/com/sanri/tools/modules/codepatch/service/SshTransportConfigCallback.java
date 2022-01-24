package com.sanri.tools.modules.codepatch.service;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;

public class SshTransportConfigCallback implements TransportConfigCallback {
    private SshSessionFactory sshSessionFactory;

    public SshTransportConfigCallback(SshSessionFactory sshSessionFactory) {
        this.sshSessionFactory = sshSessionFactory;
    }

    @Override
    public void configure(Transport transport) {
        SshTransport sshTransport = (SshTransport) transport;
        sshTransport.setSshSessionFactory(sshSessionFactory);
    }
}
