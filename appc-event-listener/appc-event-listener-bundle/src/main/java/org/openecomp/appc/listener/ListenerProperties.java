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

import java.util.Properties;

/**
 * A class for instantiating Listener objects. It is primarily used to hold properties that start with the given prefix.
 * It also holds a class that implements {@see Listener} and will be used by the controller to spawn a new listener
 * object.
 *
 * @since Apr 25, 2016
 * @version $Id$
 */
public class ListenerProperties {

    private String prefix;

    private Class<? extends Listener> listenerClass;

    private Properties props;

    /**
     * Creates a new listener object with the given prefix and properties. Any property starting with the prefix is
     * added to the internal properties object with the prefix removed. All other properties are ignored.
     * ListenerProperties constructor
     *
     * @param prefix
     *            The prefix of the properties to load
     * @param allProps
     *            The properties object to load from.
     */
    public ListenerProperties(String prefix, Properties allProps) {
        this.prefix = prefix;
        props = new Properties();

        String dottedPrefix = String.format("%s.", prefix);
        for (String key : allProps.stringPropertyNames()) {
            if (key.startsWith(dottedPrefix) && key.length() > dottedPrefix.length()) {
                props.put(key.substring(dottedPrefix.length()), allProps.get(key));
            }
        }
    }

    /**
     * @return The prefix of these properties
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the listener class. Will be used by {@see Controller} to instantiate the Listener thread for this object
     *
     * @param cls
     *            The class to be created. Implements {@see Listener}
     */
    public void setListenerClass(Class<? extends Listener> cls) {
        this.listenerClass = cls;
    }

    /**
     * @return The class that will be used by {@see Controller} to instantiate the Listener thread for this object
     */
    public Class<? extends Listener> getListenerClass() {
        return listenerClass;
    }

    /**
     * Returns a property matching a given KEYS
     * 
     * @param key
     *            The KEYS object who's value to return.
     * @return The value of the property or null if none exists
     */
    public String getProperty(KEYS key) {
        return getProperty(key, null);
    }

    /**
     * Returns a property matching a given string.
     * 
     * @param key
     *            The key who's value to return.
     * @return The value of the property or null if none exists
     */
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Returns a property matching a given KEYS
     * 
     * @param key
     *            The KEYS object who's value to return.
     * @param defaultValue
     *            The value to return if the property is not found
     * @return The value of the property or null if none exists
     */
    public String getProperty(KEYS key, String defaultValue) {
        return getProperty(key.getPropertySuffix(), defaultValue);
    }

    /**
     * Returns a property matching a given string.
     * 
     * @param key
     *            The key who's value to return.
     * @param defaultValue
     *            The value to return if the property is not found
     * @return The value of the property or null if none exists
     */
    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    /**
     * @return The properties object containing all properties
     */
    public Properties getProperties() {
        return props;
    }

    /**
     * Reads the <i>prefix</i>.disabled property to determine if the listener is disabled and should not be run by the
     * controller. Defaults to false if property not set or value cannot be parsed.
     *
     * @return true if the listener is disabled and should not be started. false if the listener should be start
     *         normally (default).
     */
    public boolean isDisabled() {
        return Boolean.valueOf(getProperty(KEYS.DISABLED, "false"));
    }

    @Override
    public String toString() {
        return String.format("%s", prefix);
    }


    /**
     * Set of common properties that will be used by most systems. Primarily relating to DMaaP and ThreadPools
     *
     * @since Apr 25, 2016
     * @version $Id$
     */
    public enum KEYS {
        /**
         * Property to determine if the listener should be disabled. If not set, defaults to false
         */
        DISABLED("disabled"),

        /**
         * Property for the message service type. Should be a lower case string. See MessageService.
         */
        MESSAGE_SERVICE("service"),

        /**
         * A hostname or comma separated list (no spaces) of hostnames of servers in a cluster. Can have ports included
         * as well.<br>
         * Examples:
         * <ul>
         * <li>server1.appc.openecomp.org</li>
         * <li>server1.appc.openecomp.org:3904</li>
         * <li>server1.appc.openecomp.org,server2.appc.openecomp.org</li>
         * </ul>
         */
        HOSTS("poolMembers"),

        /**
         * The topic that will be used for DMaaP read operations. Can only support a single topic.
         */
        TOPIC_READ("topic.read"),

        /**
         * The topic or topics that will be used to write to. If multiple topics are provided, should be in a comma
         * seperated list with no spaces.<br>
         * Examples:
         * <ul>
         * <li>TOPIC-1</li>
         * <li>TOPIC-1,TOPIC-2,ANOTHER-TOPIC</li>
         * </ul>
         */
        TOPIC_WRITE("topic.write"),

        /**
         * The highland park filter to use on read requests. If you are reading and writing to the same topic this must
         * be provided. Filter should be in JSON format (not url escaped).
         */
        TOPIC_READ_FILTER("topic.read.filter"),

        /**
         * The amount of time in seconds that the DMaaP polling connection should stay open for. Recommended to be set
         * high (around 60 seconds) as most clients will return immediately and not wait until the timeout is up to
         * return if they have data.
         */
        TOPIC_READ_TIMEOUT("topic.read.timeout"),

        /**
         * The name of the client to use. Should be unique to the application.
         */
        CLIENT_NAME("client.name"),

        /**
         * The id of the client to use. Should be unique for each instance of the application in an environment.
         */
        CLIENT_ID("client.name.id"),

        /**
         * The User (DMaaP) to use for authentication. If a user is provided, you must include the
         * domain name (e.g. example<b>@example.com</b>).
         */
        AUTH_USER_KEY("client.key"),

        /**
         * The password (DMaaP) to use for authentication.
         */
        AUTH_SECRET_KEY("client.secret"),

        /**
         * The minimum amount of size of the queue. A client should request new messages once the queue has dropped
         * below this size.
         */
        THREADS_MIN_QUEUE("threads.queuesize.min"),

        /**
         * The maximum size of the queue. A client will request no new messages once this maximum size has been reached.
         */
        THREADS_MAX_QUEUE("threads.queuesize.max"),

        /**
         * The minimum size of the worker threads pool. This is the pool each listener will use to launch longer running
         * operations.
         */
        THREADS_MIN_POOL("threads.poolsize.min"),

        /**
         * The maximum size of the worker threads pool. This is the pool each listener will use to launch longer running
         * operations.
         */
        THREADS_MAX_POOL("threads.poolsize.max");

        private String suffix;

        private KEYS(String val) {
            this.suffix = val;
        }

        /**
         * @param prefix
         *            The prefix to prepend
         * @return a fully property name that corroponds to what is used in the properties file. Format is PREFIX.KEY
         */
        public String getFullProp(String prefix) {
            return String.format("%s.%s", prefix, suffix);
        }

        public String getPropertySuffix() {
            return suffix;
        }
    }

}
