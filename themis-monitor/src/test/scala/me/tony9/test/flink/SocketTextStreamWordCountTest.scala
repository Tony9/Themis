package me.tony9.test.flink

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.apache.flink.streaming.api.scala._

/**
  * This example shows an implementation of WordCount with data from a text socket.
  * To run the example make sure that the service providing the text data is already up and running.
  *
  * To start an example socket text stream on your local machine run netcat from a command line,
  * where the parameter specifies the port number:
  *
  * {{{
  *   nc -lk 9999
  * }}}
  *
  * Usage:
  * {{{
  *   SocketTextStreamWordCount <hostname> <port> <output path>
  * }}}
  *
  * This example shows how to:
  *
  *   - use StreamExecutionEnvironment.socketTextStream
  *   - write a simple Flink Streaming program in scala.
  *   - write and use user-defined functions.
  */
object SocketTextStreamWordCountTest {

  def main(args: Array[String]) {

    val wordServer = new WordServer(9999)
    wordServer.start()

  }

  def streamWordCount() : Unit = {
    val hostName = "127.0.0.1"
    val port = 9999

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val text = env.socketTextStream(hostName, port)
    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map { (_, 1) }
      .keyBy(0)
      .sum(1)

    counts print

    env.execute("Scala SocketTextStreamWordCount Example")
  }


  class WordServer(port: Integer) {

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
