/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.listener;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * EventHandler defines a class that wraps DMaaP operations (most notably Get Message and Post Message) to make them
 * easier to use.
 *
 */
public interface EventHandler {

    /**
     * Gets a list of messages as Strings on the read topic.
     * 
     * @return A list of String messages. Never returns null.
     */
    public List<String> getIncomingEvents();

    /**
     * Gets a list of messages as String on the read topic.
     * 
     * @param limit
     *            The maximum amount of entries to return
     * @return A list of String messages. Never returns null.
     */
    public List<String> getIncomingEvents(int limit);

    /**
     * Gets a list of messages Mapped to the given Class. If a message cannot be mapped to that class, it is discarded.
     *
     * @param cls
     *            The class to map the message to.
     * @return A list of objects of the provided class. Never returns null.
     */
    public <T> List<T> getIncomingEvents(Class<T> cls);

    /**
     * Gets a list of messages Mapped to the given Class. If a message cannot be mapped to that class, it is discarded.
     *
     * @param cls
     *            The class to map the message to.
     * @param limit
     *            The maximum amount of entries to return
     * @return A list of objects of the provided class. Never returns null.
     */
    public <T> List<T> getIncomingEvents(Class<T> cls, int limit);

    /**
     * Posts the String message to the write topic(s).
     * 
     * @param event
     *            The String to post.
     */
    public void postStatus(String event);

    /**
     * Posts the String message to the write topic(s) on the specified partition. Partitions are only used to guarantee
     * ordering and do not impact if data is retreived.
     *
     * @param partition
     *            The partition to post to or null if no partition should be used.
     * @param event
     *            The String to post.
     */
    public void postStatus(String partition, String event);

    /**
     * @return The client/group id used to read messages
     */
    public String getClientId();

    /**
     * Set the client/group id used to read messages
     * 
     * @param clientId
     *            The new clientId to use
     */
    public void setClientId(String clientId);

    /**
     * @return The client/group name to use.
     */
    public String getClientName();

    /**
     * Set the client/group name used to read messages.
     * 
     * @param clientName
     *            The new clientName to use
     */
    public void setClientName(String clientName);

    /**
     * @return The name of the topic to read from
     */
    public String getReadTopic();

    /**
     * Set the name of the topic to read from.
     * 
     * @param topic
     *            The new topic to read from
     */
    public void setReadTopic(String topic);

    /**
     * @return The name of the topic to write to
     */
    public Set<String> getWriteTopics();

    /**
     * Set the name of the topic to write to
     * 
     * @param topic
     *            The new topic to write to
     */
    public void setWriteTopics(Set<String> topic);

    /**
     * Adds a DMaaP host to the host pool
     * 
     * @param host
     *            The host to add to the pool in &lt;host&gt;:&lt;port&gt; format
     */
    public void addToPool(String host);

    /**
     * Remove the host name from the pool if it exists
     * 
     * @param host
     *            The host to add to the pool in &lt;host&gt;:&lt;port&gt; format
     */
    public void removeFromPool(String host);

    /**
     * Get all of the hosts in the DMaaP pool
     * 
     * @return A collection of host in &lt;host&gt;:&lt;port&gt; format
     */
    public Collection<String> getPool();

    /**
     * Clear any provided api credentials and make future requests as an unauthenticated user
     */
    public void clearCredentials();

    /**
     * Set the api credentials and make future requests as an authenticated user
     * 
     * @param access
     *            The access portion of the credentials (either user name or api key)
     * @param secret
     *            The secret portion of the credentials (either password or api secret)
     */
    public void setCredentials(String access, String secret);
    
    /**
     * Close consumer/producer DMaaP clients
     */
    public void closeClients();

}
