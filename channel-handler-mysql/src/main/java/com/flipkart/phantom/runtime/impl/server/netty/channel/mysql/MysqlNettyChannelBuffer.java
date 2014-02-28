package com.flipkart.phantom.runtime.impl.server.netty.channel.mysql;

import com.github.jmpjct.mysql.proto.ColCount;
import com.github.jmpjct.mysql.proto.EOF;
import com.github.jmpjct.mysql.proto.Flags;
import com.github.jmpjct.mysql.proto.Packet;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * <code>MysqlNettyChannelBuffer</code> is a Mysql transport based on JBoss Netty's ChannelBuffers
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlNettyChannelBuffer extends Packet {

    @Override
    public ArrayList<byte[]> getPayload() {
        return null;
    }

    /**
     *  read complete result set from mysql server all the input bytes
     */
    public static ArrayList<byte[]> readFullResultSet(InputStream in, MessageEvent messageEvent, ArrayList<byte[]> buffer, boolean bufferResultSet) throws IOException {
        // Assume we have the start of a result set already

        byte[] packet = buffer.get((buffer.size()-1));
        long colCount = ColCount.loadFromPacket(packet).colCount;

        // Read the columns and the EOF field
        for (int i = 0; i < (colCount+1); i++) {
            packet = Packet.read_packet(in);
            if (packet == null) {
                throw new IOException();
            }
            buffer.add(packet);

            if (!bufferResultSet) {
                MysqlNettyChannelBuffer.write(messageEvent.getChannel(), buffer);
                buffer.clear();
            }
        }

        int packedPacketSize = 65535;
        byte[] packedPacket = new byte[packedPacketSize];
        int position = 0;

        while (true) {
            packet = Packet.read_packet(in);

            if (packet == null) {
                throw new IOException();
            }

            int packetType = Packet.getType(packet);

            if (packetType == Flags.EOF || packetType == Flags.ERR) {
                byte[] newPackedPacket = new byte[position];
                System.arraycopy(packedPacket, 0, newPackedPacket, 0, position);
                buffer.add(newPackedPacket);
                packedPacket = packet;
                break;
            }

            if (position+packet.length > packedPacketSize) {
                int subsize = packedPacketSize - position;
                System.arraycopy(packet, 0, packedPacket, position, subsize);
                buffer.add(packedPacket);

                if (!bufferResultSet) {
                    MysqlNettyChannelBuffer.write(messageEvent.getChannel(), buffer);
                    buffer.clear();
                }

                packedPacket = new byte[packedPacketSize];
                position = 0;
                System.arraycopy(packet, subsize, packedPacket, position, packet.length-subsize);
                position += packet.length-subsize;
            }
            else {
                System.arraycopy(packet, 0, packedPacket, position, packet.length);
                position += packet.length;
            }
        }
        buffer.add(packedPacket);

        if (!bufferResultSet) {
            MysqlNettyChannelBuffer.write(messageEvent.getChannel(), buffer);
            buffer.clear();
        }

        if (Packet.getType(packet) == Flags.ERR) {
            return buffer;
        }

        if (EOF.loadFromPacket(packet).hasStatusFlag(Flags.SERVER_MORE_RESULTS_EXISTS)) {
            buffer.add(Packet.read_packet(in));
            buffer = MysqlNettyChannelBuffer.readFullResultSet(in, messageEvent, buffer, bufferResultSet);
        }
        return buffer;
    }

    /**
     *  Writes all the output bytes into the output ChannelBuffer
     *
     */
    public static void write(Channel channel, ArrayList<byte[]> buffer) {

        for (byte[] packet: buffer) {
            ChannelBuffer cb = ChannelBuffers.copiedBuffer(packet);
            channel.write(cb);
        }
    }

}
