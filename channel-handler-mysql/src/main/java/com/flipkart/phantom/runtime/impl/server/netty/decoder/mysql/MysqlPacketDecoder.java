/*
 * Copyright 2012-2015, the original author or authors.
 *
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
 */

package com.flipkart.phantom.runtime.impl.server.netty.decoder.mysql;

import com.github.mpjct.jmpjct.mysql.proto.Com_Query;
import com.github.mpjct.jmpjct.mysql.proto.Packet;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * <code>MysqlPacketDecoder</code> is an extension of the Netty {@link org.jboss.netty.handler.codec.frame.FrameDecoder} that ensures that all mysql protocol bytes have been received
 * before the {@link org.jboss.netty.channel.MessageEvent} is constructed for use by other upstream channel handlers.
 * This decoder attempts to read the Mysql message from the transport using the protocol. An unsuccessful read indicates that the bytes have not been fully
 * received. This decoder returns a null object in {@link #decode(ChannelHandlerContext, Channel, ChannelBuffer)} in such scenarios and the Netty
 * framework would then call it again when more bytes are received, eventually resulting in all required bytes becoming available.
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlPacketDecoder extends FrameDecoder {

    /**
     * Logger for this class
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlPacketDecoder.class);

    /**
     * Mysql header read size (length of compressed payload)
     * {@link http://dev.mysql.com/doc/internals/en/compressed-packet-header.html}
     */
     private static final int HEADER_READ_SIZE = 3;

    /**
     * Mysql header size (length of compressed payload + compressed sequence id)
     * {@link http://dev.mysql.com/doc/internals/en/compressed-packet-header.html}
     */
     private static final int HEADER_SIZE = 4;

    /**
     * Mysql packet OFFSET_MARKER. This is the index of first byte in the packet.
     */
     private static final int OFFSET_MARKER = 0;

    /**
     * Interface method implementation. Tries to read the Mysql protocol message. Returns null if unsuccessful, else returns the read byte array
     *
     * @see org.jboss.netty.handler.codec.frame.FrameDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, org.jboss.netty.buffer.ChannelBuffer)
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {


        InputStream in = new ChannelBufferInputStream(buf);

        int b = 0;
        int size = 0;
        byte[] packet = new byte[HEADER_READ_SIZE];
        int offset = OFFSET_MARKER;
        int target = HEADER_READ_SIZE;

        do {
            b = in.read(packet, offset, (target - offset));
            if (b == -1) {
                buf.resetReaderIndex();
                return null;

            }
            offset += b;
        } while (offset != target);

        size = Packet.getSize(packet);
        byte[] packet_tmp = new byte[size + HEADER_SIZE];
        System.arraycopy(packet, OFFSET_MARKER, packet_tmp, OFFSET_MARKER, HEADER_READ_SIZE);
        packet = packet_tmp;
        packet_tmp = null;

        target = packet.length;
        do {
            b = in.read(packet, offset, (target - offset));
            if (b == -1) {
                buf.resetReaderIndex();
                return null;
            }
            offset += b;
        } while (offset != target);
        //LOGGER.info("RECEIVED: MySQL packet (size) = "+size);
        ArrayList<byte[]> buffer = new ArrayList<byte[]>();
        buffer.add(packet);
        String query = Com_Query.loadFromPacket(packet).query;
        //LOGGER.info("Mysql query : "+query);
        return buffer;

    }
}
