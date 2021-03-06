package com.funstill.netty.chat.server;


import com.funstill.netty.chat.protobuf.ProtoMsg;
import com.funstill.netty.chat.server.observer.ProtoMsgObservable;
import com.funstill.netty.chat.server.observer.ProtoMsgObserver;
import com.funstill.netty.chat.server.processor.OnlineProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyServerHandler extends SimpleChannelInboundHandler<ProtoMsg.Content> implements InitializingBean {
    @Autowired
    private ProtoMsgObserver protoMsgObserver;
    Logger logger = LogManager.getLogger(NettyServerHandler.class);
    public static ProtoMsgObservable msgObservable = new ProtoMsgObservable();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoMsg.Content msg) throws Exception {
        Channel ch = ctx.channel();
        msgObservable.handleMsg(ch, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接已建立,客户端ip={},channelId={}", ctx.channel().remoteAddress(), ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        String uniqueIdentity = OnlineProcessor.getInstance().getUniqueIdentityFromChannel(ch);
        if (uniqueIdentity != null) {
            OnlineProcessor.getInstance().removeUser(uniqueIdentity);
        }
        logger.info("连接已中断,客户端ip={},channelId={}", ch.remoteAddress(), ch.id());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {//服务端不需要回复心跳包了(浪费资源)
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                if(event.isFirst()){
                    logger.debug("5 秒没有接收到客户端的信息了");
                }else {
                    logger.error("第二次没有接收到客户端的信息了,关闭这个不活跃的channel,uniqueIdentity={}",OnlineProcessor.getUniqueIdentityFromChannel(ctx.channel()));
                    ctx.channel().close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        msgObservable.addObserver(protoMsgObserver);
    }
}
