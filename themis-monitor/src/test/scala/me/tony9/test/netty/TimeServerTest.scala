package me.tony9.test.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
  * Created by Tony on 2017/12/25.
  */
object TimeServerTest {

  def main(args: Array[String]) {

    val timeServer = new TimeServer(9999)
    timeServer.start()

  }

  class TimeServer(port: Integer) {

    def start() {
      val bossGroup = new NioEventLoopGroup()
      val workerGroup = new NioEventLoopGroup()
      try {

        val b = new ServerBootstrap()
        b.group(bossGroup, workerGroup)
          .channel(classOf[NioServerSocketChannel])
          .option[Integer](ChannelOption.SO_BACKLOG, 1024)
          .childHandler(new ChildChannelHander())

        val f = b.bind(port).sync()
        f.channel().closeFuture().sync()

      } finally {

        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()

      }
    }

    class ChildChannelHander extends ChannelInitializer[SocketChannel] {
      override def initChannel(socketChannel: SocketChannel): Unit = {
        socketChannel.pipeline().addLast(new TimeServerHandler());
      }
    }

    class TimeServerHandler extends ChannelInboundHandlerAdapter {

      override def channelActive(ctx:ChannelHandlerContext): Unit = {
        val time = ctx.alloc().buffer(4);
        val timeInSeconds = (System.currentTimeMillis() / 1000L + 2208988800L).toInt
        time.writeInt(timeInSeconds)

        val f:ChannelFuture = ctx.writeAndFlush(time)
        f.addListener(new ChannelFutureListener() {
          override def operationComplete(future: ChannelFuture) : Unit = {
            ctx.close();
          }
        })
      }

      override def exceptionCaught(ctx:ChannelHandlerContext, cause:Throwable):Unit = {
        cause.printStackTrace();
        ctx.close();
      }

    }

  }


}
