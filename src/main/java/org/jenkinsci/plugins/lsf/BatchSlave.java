/*
 * The MIT License
 *
 * Copyright 2015 Laisvydas Skurevicius.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.lsf;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.Slave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.util.Secret;
import jenkins.model.Jenkins;

/**
 *
 * @author Laisvydas Skurevicius
 */
public class BatchSlave extends Slave {

    private static final Logger LOGGER = Logger.getLogger(BatchSlave.class
            .getName());

    private BatchSlave(String name,
                       int numExecutors,
                       String label,
                       ComputerLauncher launcher) throws Descriptor.FormException, IOException {
        super(name,
                "",
                "jenkins",
                numExecutors,
                Node.Mode.NORMAL,
                label,
                launcher,
                new BatchRetentionStrategy(1),
                Collections.<NodeProperty<?>>emptyList());
        LOGGER.log(Level.INFO, "Constructing LSF slave {0}", name);
   }

    public BatchSlave(String name,
            String label,
            int numExecutors,
            String hostName,
            int port,
            StandardUsernameCredentials credentials) throws Descriptor.FormException, IOException {
        this(name,
             numExecutors,
             label,
                new SSHLauncher(hostName, port, credentials, "",
                                null, null, null, null, null, null));
   }

    @Deprecated
    public BatchSlave(String name,
            String label,
            int numExecutors,
            String hostName,
            int port,
            String userName,
            Secret password) throws Descriptor.FormException, IOException {
        this(name,
             numExecutors,
             label,
             new SSHLauncher(hostName, port, userName,
                             Secret.toString(password), "", ""));
    }

    /**
     * terminates the slave
     */
    public void terminate() {
        LOGGER.log(Level.INFO, "Terminating slave {0}", getNodeName());
        try {
            Jenkins.getInstance().removeNode(this);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to terminate LSF instance: "
                    + getInstanceId(), e);
        }
    }

    StandardUsernameCredentials getCredentials() {
        return ((SSHLauncher) getLauncher()).getCredentials();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    private String getInstanceId() {
        return getNodeName();
    }

    @Extension
    public static class DescriptorImpl extends SlaveDescriptor {

        @Override
        public String getDisplayName() {
            return "LSF Slave";
        }
    }

}
