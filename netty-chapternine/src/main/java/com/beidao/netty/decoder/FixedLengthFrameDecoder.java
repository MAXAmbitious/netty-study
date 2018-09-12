package com.beidao.netty.decoder;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author 0200759
 *
 */
public class FixedLengthFrameDecoder extends ByteToMessageDecoder {
	private final int frameLength;

	public FixedLengthFrameDecoder(int frameLength) {
		if (frameLength <= 0) {
			throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);
		}
		this.frameLength = frameLength;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//Checks if enough bytes can be read to produce the next frame
		while (in.readableBytes() >= frameLength) {
			ByteBuf buf = in.readBytes(frameLength);
			out.add(buf);
		}
	}
}
