package com.java.library.netty.test;

import io.netty.channel.CombinedChannelDuplexHandler;

public class CombinedByteCharCodec extends CombinedChannelDuplexHandler<ByteToCharDecoder, CharToByteEncoder> {
	public CombinedByteCharCodec() {
		super(new ByteToCharDecoder(), new CharToByteEncoder());
	}
}
