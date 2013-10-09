package io.torch;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.torch.pipeline.HttpRequestValidator;
import io.torch.pipeline.ServingFileHandler;
import io.torch.pipeline.ServingWebpageHandler;

/**
 * Initialize the server pipeline.
 */
class ServerInitializer extends ChannelInitializer<SocketChannel> {
    
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast("encoder", new HttpResponseEncoder()); 
        pipeline.addLast("streamer", new ChunkedWriteHandler());
        pipeline.addLast("validator", new HttpRequestValidator());
        pipeline.addLast("webpage", new ServingWebpageHandler());
        pipeline.addLast("file", new ServingFileHandler());
    }
}
