/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2016 "Tigase, Inc." <office@tigase.com>
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
 * If not, see http://www.gnu.org/licenses/.
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
	public static final String HEAP_REGION_NAME = "message-router/Heap region name[S]";
	public static final String HEAP_REGION_MAX = "message-router/Max Heap mem[S]";
	public static final String HEAP_REGION_USED = "message-router/Used Heap[S]";
	public static final String SM_TRAFFIC_R = "sess-man/Packets received[L]";
	public static final String SM_TRAFFIC_S = "sess-man/Packets sent[L]";
	public static final String SM_QUEUE_WAIT = "sess-man/Total queues wait[I]";
	public static final String QUEUE_WAIT = "total/Total queues wait[I]";
	public static final String QUEUE_OVERFLOW = "total/Total queues overflow[L]";
	public static final String BOSH_CONNECTIONS = "bosh/Open connections[I]";
	public static final String WS2S_CONNECTIONS = "ws2s/Open connections[I]";
	public static final String C2S_CONNECTIONS = "c2s/Open connections[I]";
	public static final String S2S_CONNECTIONS = "s2s/Open connections[I]";
	public static final String CL_TRAFFIC_R = "cl-comp/Packets received[L]";
	public static final String CL_TRAFFIC_S = "cl-comp/Packets sent[L]";
	public static final String CL_CACHE_SIZE = "cl-caching-strat/Cached JIDs[I]";
	public static final String CL_QUEUE_WAIT = "cl-comp/Total queues wait[I]";
	public static final String CL_IO_QUEUE_WAIT = "cl-comp/Waiting to send[I]";

	public static final String HEAP_TOTAL_USAGE = "message-router/JVM/HEAP Total Usage [%][F]";
	public static final String HEAP_TOTAL_USAGE_USED = "message-router/JVM/HEAP Total Used [KB][L]";
	public static final String HEAP_TOTAL_USAGE_MAX = "message-router/JVM/HEAP Total Max [KB][L]";

	static final String JVM_KEY =  "message-router/JVM/";
	static final String HEAP_MEMORY_KEY =  JVM_KEY + "MemoryPools/HeapMemory/";
	static final String EDEN_KEY =  "eden/";
	static final String NAME_KEY =  "Name[S]";
	static final String SURVIVOR_KEY =  "survivor/";
	static final String OLD_KEY =  "old/";
	static final String USAGE_KEY =  "Usage/";
	static final String PEAK_USAGE_KEY =  "Peak Usage/";
	static final String COLLECTION_USAGE_KEY =  "Collection Usage/";
	static final String USED_KEY =  "Used [KB][L]";
	static final String MAX_KEY =  "Max [KB][L]";


	public static final String HEAP_EDEN_NAME = HEAP_MEMORY_KEY + EDEN_KEY + NAME_KEY;
	public static final String HEAP_EDEN_USAGE_USED = HEAP_MEMORY_KEY + EDEN_KEY + USAGE_KEY + USED_KEY;
	public static final String HEAP_EDEN_USAGE_MAX = HEAP_MEMORY_KEY + EDEN_KEY + USAGE_KEY + MAX_KEY;
	public static final String HEAP_EDEN_PEAK_USAGE_USED = HEAP_MEMORY_KEY + EDEN_KEY + PEAK_USAGE_KEY + USED_KEY;
	public static final String HEAP_EDEN_PEAK_USAGE_MAX = HEAP_MEMORY_KEY + EDEN_KEY + PEAK_USAGE_KEY + MAX_KEY;
	public static final String HEAP_EDEN_COLLECTION_USAGE_USED = HEAP_MEMORY_KEY + EDEN_KEY + COLLECTION_USAGE_KEY + USED_KEY;
	public static final String HEAP_EDEN_COLLECTION_USAGE_MAX = HEAP_MEMORY_KEY + EDEN_KEY + COLLECTION_USAGE_KEY + MAX_KEY;


	public static final String HEAP_SURVIVOR_NAME = HEAP_MEMORY_KEY + SURVIVOR_KEY + NAME_KEY;
	public static final String HEAP_SURVIVOR_USAGE_USED = HEAP_MEMORY_KEY + SURVIVOR_KEY + USAGE_KEY + USED_KEY;
	public static final String HEAP_SURVIVOR_USAGE_MAX = HEAP_MEMORY_KEY + SURVIVOR_KEY + USAGE_KEY + MAX_KEY;
	public static final String HEAP_SURVIVOR_PEAK_USAGE_USED = HEAP_MEMORY_KEY + SURVIVOR_KEY + PEAK_USAGE_KEY + USED_KEY;
	public static final String HEAP_SURVIVOR_PEAK_USAGE_MAX = HEAP_MEMORY_KEY + SURVIVOR_KEY + PEAK_USAGE_KEY + MAX_KEY;
	public static final String HEAP_SURVIVOR_COLLECTION_USAGE_USED = HEAP_MEMORY_KEY + SURVIVOR_KEY + COLLECTION_USAGE_KEY + USED_KEY;
	public static final String HEAP_SURVIVOR_COLLECTION_USAGE_MAX = HEAP_MEMORY_KEY + SURVIVOR_KEY + COLLECTION_USAGE_KEY + MAX_KEY;

	public static final String HEAP_OLD_NAME = HEAP_MEMORY_KEY + OLD_KEY + NAME_KEY;
	public static final String HEAP_OLD_USAGE_USED = HEAP_MEMORY_KEY + OLD_KEY + USAGE_KEY + USED_KEY;
	public static final String HEAP_OLD_USAGE_MAX = HEAP_MEMORY_KEY + OLD_KEY + USAGE_KEY + MAX_KEY;
	public static final String HEAP_OLD_PEAK_USAGE_USED = HEAP_MEMORY_KEY + OLD_KEY + PEAK_USAGE_KEY + USED_KEY;
	public static final String HEAP_OLD_PEAK_USAGE_MAX = HEAP_MEMORY_KEY + OLD_KEY + PEAK_USAGE_KEY + MAX_KEY;
	public static final String HEAP_OLD_COLLECTION_USAGE_USED = HEAP_MEMORY_KEY + OLD_KEY + COLLECTION_USAGE_KEY + USED_KEY;
	public static final String HEAP_OLD_COLLECTION_USAGE_MAX = HEAP_MEMORY_KEY + OLD_KEY + COLLECTION_USAGE_KEY + MAX_KEY;


	public static final String GC_STATISTICS = "message-router/JVM/GC-statistics";

	void update(String id, JavaJMXProxyOpt bean);

}
