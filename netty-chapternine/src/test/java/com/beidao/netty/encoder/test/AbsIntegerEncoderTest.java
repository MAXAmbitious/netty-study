package com.beidao.netty.encoder.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.beidao.netty.encoder.AbsIntegerEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class AbsIntegerEncoderTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testEncoded() {
		ByteBuf buf = Unpooled.buffer();
		for (int i = 1; i < 10; i++) {
			buf.writeInt(i * -1);
		}
		EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());
		assertTrue(channel.writeOutbound(buf));
		assertTrue(channel.finish());
		// read bytes
		for (int i = 1; i < 10; i++) {
			assertEquals(i, channel.readOutbound());
		}
		assertNull(channel.readOutbound());
	}

}
