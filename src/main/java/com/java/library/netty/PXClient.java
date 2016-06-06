package com.java.library.netty;

import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;

public class PXClient implements Runnable{
	private  int port;
	public static boolean isSSL;
	private Thread thread;
	private Channel ch;
	public void init(){
		thread = new Thread(this, "Thread-PXClient");
		int port = 8183;
		isSSL = false;
		this.port = port;
		thread.start();
	}
	public void destroy(){
		ch.close();
	}

	@Override
	public void run(){
		EventLoopGroup group = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup(50);
		try {
			 Bootstrap b = new Bootstrap();
			 b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new PXInitializer());
			// Start the connection attempt.
	        Channel ch = b.connect("192.168.0.158", port).sync().channel();
			
	        // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://mpstest.abusi.net:6583/mps2/mgr/login.html");
            request.headers().set("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, */*");
            request.headers().set("Accept-Language", "en-us,zh-cn;q=0.5");
            request.headers().set("Host", "mpstest.abusi.net:6583");
            request.headers().set("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            request.headers().set("Connection", "Keep-Alive");
            lastWriteFuture = ch.writeAndFlush(request);

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
		} catch(Exception e){
			e.printStackTrace();
		}finally {
			workerGroup.shutdownGracefully();
		}
	}
	
	public class PXInitializer extends ChannelInitializer<SocketChannel> {
	    @Override
	    public void initChannel(SocketChannel ch) throws Exception {
	    	System.out.println(ch == PXClient.this.ch);
	        ChannelPipeline pipeline = ch.pipeline();
	        SSLEngine engine = SecureChatSslContextFactory.getClientContext().createSSLEngine();
	        engine.setUseClientMode(true);
            //pipeline.addLast("ssl", new SslHandler(engine));
            pipeline.addLast("decoder", new HttpResponseDecoder());
	        pipeline.addLast("encoder", new HttpRequestEncoder());
	        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
	        //pipeline.addLast("deflater", new HttpContentCompressor());
	        pipeline.addLast("handler", new PXHandler());
	    }
	}
	
	public  class PXHandler extends SimpleChannelInboundHandler<HttpObject> {
	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	    }
	 
	    public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
	    	System.out.println(this);
	    	FullHttpResponse fullHttpRequest = (FullHttpResponse) msg;
	        ByteBuf buf = fullHttpRequest.content(); 
	        byte[] data = null; 
	        if(buf.readableBytes() > 0){
	        	data = new byte[buf.readableBytes()]; 
	        	buf.readBytes(data); 
	        }
	        System.out.println(String.format("[RESPONSE] [%s] [%s]", fullHttpRequest.getStatus(), fullHttpRequest.headers().entries()));
	    }
	 
	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	    	cause.printStackTrace();
	        ctx.channel().close();
	    }
	 
	    @Override
	    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
	        messageReceived(ctx, msg);
	    }
	}

	public static void main(String[] args) throws Exception {
		PXClient server = new PXClient();
		server.init();
		Thread.sleep(100000);
		//server.destroy();
	}

}
