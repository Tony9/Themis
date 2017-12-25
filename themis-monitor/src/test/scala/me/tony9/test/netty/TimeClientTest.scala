package me.tony9.test.netty

import java.util.Date

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer, ChannelOption}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

/**
  * Created by Tony on 2017/12/25.
  */
object TimeClientTest {

  def main(args: Array[String]) {

    val timeClient = new TimeClient(9999)
    timeClient.getTime()

  }

  class TimeClient(port: Integer) {
    def getTime(): Unit = {

      val host = "127.0.0.1"
      val port = 9999
      val workerGroup = new NioEventLoopGroup();

      try {
        val b = new Bootstrap()

        b.group(workerGroup)
          .channel(classOf[NioSocketChannel])
//          .option[Object](ChannelOption.SO_KEEPALIVE, true)
          .handler(new ChildChannelHander());

        // 启动客户端
        val f = b.connect(host, port).sync();

        // 等待连接关闭
        f.channel().closeFuture().sync();
      } finally {
        workerGroup.shutdownGracefully();
      }
    }

    class ChildChannelHander extends ChannelInitializer[SocketChannel] {
      override def initChannel(socketChannel: SocketChannel): Unit = {
        socketChannel.pipeline().addLast(new TimeClientHandler());
      }
    }

    class TimeClientHandler extends ChannelInboundHandlerAdapter {
      override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
        val m = msg.asInstanceOf[ByteBuf]
        try {
          val currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
          System.out.println(new Date(currentTimeMillis));
          ctx.close();
        } finally {
          m.release();
        }
      }

      override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace();
        ctx.close();
      }
    }

  }

}
