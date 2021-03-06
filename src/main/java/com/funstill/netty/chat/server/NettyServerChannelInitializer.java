package com.funstill.netty.chat.server;

import com.funstill.netty.chat.protobuf.ProtoMsg;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author liukaiyang
 * @date 2017/12/6 9:32
 */
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //idle,心跳包间隔为8秒,服务端冗余1秒--即9秒内收不到消息认为读超时
        socketChannel.pipeline().addLast(new IdleStateHandler(9, 0, 0));
        //接收解码
        socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
        socketChannel.pipeline().addLast(new ProtobufDecoder(ProtoMsg.Content.getDefaultInstance()));
        //发送编码
        socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
        socketChannel.pipeline().addLast(new ProtobufEncoder());
        socketChannel.pipeline().addLast(new NettyServerHandler());

    }
}
