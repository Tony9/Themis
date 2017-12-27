package me.tony9.test.flink

import java.io.File

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
  * Created by Tony on 2017/12/25.
  */
object WordServerTest {

  def main(args: Array[String]) {

    new WordServer("/Volumes/USB/data/bible/bible.txt", 9999).start()
  }

  class WordServer(filepath: String, port: Integer) {

    def start() {
      val bossGroup = new NioEventLoopGroup()
      val workerGroup = new NioEventLoopGroup()
      try {

        val b = new ServerBootstrap()
        b.group(bossGroup, workerGroup)
          .channel(classOf[NioServerSocketChannel])
          .option[Integer](ChannelOption.SO_BACKLOG, 1024)
          .childHandler(new ChannelInitializer[SocketChannel] {
            override def initChannel(socketChannel: SocketChannel): Unit = {
              socketChannel.pipeline().addLast(new WordServerHandler(filepath));
            }
          })

        val f = b.bind(port).sync()
        f.channel().closeFuture().sync()

      } finally {

        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()

      }
    }
  }


  class WordServerHandler(filepath: String) extends ChannelInboundHandlerAdapter {

    private var ws:WordFileSource = new WordFileSource(filepath)

    override def channelActive(ctx:ChannelHandlerContext): Unit = {

      while (true) {
        val buf = ctx.alloc().buffer(256)
        buf.writeBytes(ws.nextLine().getBytes)
        ctx.writeAndFlush(buf)

        Thread.sleep(1000)
      }

    }

    override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {

      val buf = msg.asInstanceOf[ByteBuf]
      val req = new Array[Byte](buf.readableBytes())
      buf.readBytes(req)

      if (req.size == 5 && "FF-F4-FF-FD-06".endsWith(req.map("%02X" format _).mkString("-"))) {
        ctx.channel().close()
      }
    }


    override def exceptionCaught(ctx:ChannelHandlerContext, cause:Throwable):Unit = {
      cause.printStackTrace();
      ctx.close();
    }

  }

  trait WordSource {
    def nextLine(): String;
  }

  class WordFileSource(filepath: String) extends WordSource {

    var iterator = initIterator
    val lineSeparator = System.getProperty("line.separator")

    private def initIterator(): Iterator[String] = {
      return scala.io.Source.fromFile(filepath).getLines()
    }

    def nextLine(): String = {

      if (!iterator.hasNext) {
        iterator = initIterator
      }

      return iterator.next()+lineSeparator
    }

  }

}
