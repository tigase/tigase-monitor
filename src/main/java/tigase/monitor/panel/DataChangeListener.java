/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 */
package tigase.monitor.panel;

import tigase.stats.JMXProxyListenerOpt;
import tigase.stats.JavaJMXProxyOpt;

/**
 * @author Artur Hefczyc Created May 28, 2011
 */
public interface DataChangeListener extends JMXProxyListenerOpt {

	public static final String CPU_USAGE = "message-router/CPU usage [%][F]";
	public static final String HEAP_USAGE = "message-router/HEAP usage [%][F]";
	public static final String NONHEAP_USAGE = "message-router/NONHEAP usage [%][F]";
	public static final String SM_TRAFFIC_R = "sess-man/Packets received[L]";
	public static final String SM_TRAFFIC_S = "sess-man/Packets sent[L]";
	public static final String SM_QUEUE_WAIT = "sess-man/Total queues wait[I]";
	public static final String QUEUE_WAIT = "total/Total queues wait[I]";
	public static final String QUEUE_OVERFLOW = "total/Total queues overflow[L]";
	public static final String BOSH_CONNECTIONS = "bosh/Open connections[I]";
	public static final String C2S_CONNECTIONS = "c2s/Open connections[I]";
	public static final String S2S_CONNECTIONS = "s2s/Open connections[I]";
	public static final String CL_TRAFFIC_R = "cl-comp/Packets received[L]";
	public static final String CL_TRAFFIC_S = "cl-comp/Packets sent[L]";
	public static final String CL_CACHE_SIZE = "cl-caching-strat/Cached JIDs[I]";
	public static final String CL_QUEUE_WAIT = "cl-comp/Total queues wait[I]";
	public static final String CL_IO_QUEUE_WAIT = "cl-comp/Waiting to send[I]";

	void update(String id, JavaJMXProxyOpt bean);

}
