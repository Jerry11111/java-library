package com.java.library.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLEngine;

import com.java.library.netty.HttpRequestHelper.SimpleHTTPResult;

public class PXServer implements Runnable{
	private  int port;
	public static boolean isSSL;
	private Thread thread;
	private Channel ch;
	public void init(){
		thread = new Thread(this, "Thread-PXServer");
		int port = 8183;
		isSSL = false;
		this.port = port;
		System.out.println(String.format("proxy server start at %d", port));
		thread.start();
	}
	public void destroy(){
		ch.close();
	}

	@Override
	public void run(){
		EventLoopGroup bossGroup = new NioEventLoopGroup(5);
		EventLoopGroup workerGroup = new NioEventLoopGroup(50);
		try {
			ServerBootstrap b = new ServerBootstrap();
			//SelfSignedCertificate cert = new SelfSignedCertificate();
			
//			File keyCertChainFile = new File("");
//			File keyFile = new File("");
//			SslContextBuilder sslContext = SslContextBuilder.forServer(keyCertChainFile, keyFile);
			//SSLContext sslContext = SslContextFactory.getServerContext();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new PXInitializer(null, false, false));
			Channel ch = b.bind(port).sync().channel();
			this.ch = ch;
			System.out.println(ch.pipeline());
			ch.closeFuture().sync();
			System.out.println("close");
		} catch(Exception e){
			e.printStackTrace();
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public class PXInitializer extends ChannelInitializer<SocketChannel> {
		private final SslContext context;
	    private final boolean startTls;

		public PXInitializer(SslContext context, boolean client, boolean startTls) {
			this.context = context;
			this.startTls = startTls;
		}
	    @Override
	    public void initChannel(SocketChannel ch) throws Exception {
	    	System.out.println(ch == PXServer.this.ch);
	        ChannelPipeline pipeline = ch.pipeline();
//	        SSLEngine engine = context.newEngine(ch.alloc());  //2
	        SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
            engine.setNeedClientAuth(true); //ssl双向认证
            engine.setUseClientMode(false);
            engine.setWantClientAuth(true);
            engine.setEnabledProtocols(new String[]{"SSLv3"});
            SslHandler sslHandler = new SslHandler(engine, true);
            //pipeline.addLast("ssl", sslHandler);
            sslHandler.handshakeFuture();
           // pipeline.addLast("codec", new HttpServerCodec());
	        //pipeline.addFirst("ssl", new SslHandler(engine, startTls));
	        pipeline.addLast("decoder", new HttpRequestDecoder());
	        pipeline.addLast("encoder", new HttpResponseEncoder());
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
	    	FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
	        ByteBuf buf = fullHttpRequest.content(); 
	        byte[] data = null; 
	        if(buf.readableBytes() > 0){
	        	data = new byte[buf.readableBytes()]; 
	        	buf.readBytes(data); 
	        }
	        System.out.println(String.format("[REQUEST] [%s] [%s] [%s]", fullHttpRequest.getMethod().name(), fullHttpRequest.getUri(), fullHttpRequest.headers().entries()));
	        String method = fullHttpRequest.getMethod().name();
	        if(method.equalsIgnoreCase("CONNECT")){
	        	FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "Connection established"));
	        	//FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED);
	        	ctx.channel().writeAndFlush(response);
	        	return;
	        }
	        String url = fullHttpRequest.getUri();
	        List<Entry<String, String>> headerList = fullHttpRequest.headers().entries();
	        Map<String, String> reqHeaders = new HashMap<String, String>();
	        for(Iterator<Entry<String, String>> it = headerList.iterator(); it.hasNext(); ){
	        	Entry<String, String> entry = it.next();
	        	reqHeaders.put(entry.getKey(), entry.getValue());
	        }
	        SimpleHTTPResult res = null;
	        res = HttpRequestHelper.simpleInvoke(method, url, data, reqHeaders, null, null);
	        if(res == null){
	        	return;
	        }
	        writeResponse(ctx.channel(), fullHttpRequest, res);
	    }
	 
	    private void writeResponse(Channel channel, FullHttpRequest fullHttpRequest, SimpleHTTPResult res) {
	    	
	        // Convert the response content to a ChannelBuffer.
	        //ByteBuf buf = copiedBuffer(res, CharsetUtil.UTF_8);
	 
	        // Decide whether to close the connection or not.
	        boolean close = fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
	                || fullHttpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
	                && !fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);
	 
	        // Build the response object.
	        FullHttpResponse response = null;
	        if(res.data == null){
	        	response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(res.code));
	        }else{
	        	ByteBuf buf = Unpooled.copiedBuffer(res.data);
	            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(res.code), buf);
	        }
	        
	        //response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
	 
	        if (!close) {
	            // There's no need to add 'Content-Length' header
	            // if this is the last response.
	            //response.headers().set(CONTENT_LENGTH, buf.readableBytes());
	        }
	        Map<String, List<String>> respHeaders = res.respHeaders;
	        for(Iterator<Map.Entry<String, List<String>>> it = respHeaders.entrySet().iterator(); it.hasNext();){
	        	Map.Entry<String, List<String>> entry = it.next();
	        	if(entry.getKey() == null){
	        		continue;
	        	}
	        	List<String> valueList = entry.getValue();
	        	for(Iterator<String> vit = valueList.iterator(); vit.hasNext(); ){
	        		String value = vit.next();
	        		response.headers().add(entry.getKey(), value);
	        	}
	        }
	        //ByteBuf bb = response.content();
	       // byte[] data = new byte[bb.readableBytes()]; 
//	        buf.readBytes(data); 
//	        System.out.println(String.format("[RESPONSE] [%s] [%s] [%s]", response.getStatus().toString(), response.headers().entries(), new String(data)));
	        System.err.println(String.format("[RESPONSE] [%s] [%s] [%s]", fullHttpRequest.getUri(), response.getStatus().toString(), response.headers().entries()));
	        // Write the response.
	        ChannelFuture future = channel.writeAndFlush(response);
	        // Close the connection after the write operation is done if necessary.
	        if (close) {
	            future.addListener(ChannelFutureListener.CLOSE);
	        }
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
		PXServer server = new PXServer();
		server.init();
		Thread.sleep(100);
		server.destroy();
	}

}
