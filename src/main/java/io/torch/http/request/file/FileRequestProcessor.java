package io.torch.http.request.file;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.torch.file.MimeTypeDetector;
import io.torch.http.header.HeaderVariable;
import io.torch.http.request.RequestProcessor;
import io.torch.http.request.TorchHttpRequest;
import io.torch.http.response.TorchHttpResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

public class FileRequestProcessor extends RequestProcessor {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    private static final MimeTypeDetector mimeDetector = new MimeTypeDetector();

    private static final File PUBLIC_FOLDER = new File("public");
    private static final String PUBLIC_PATH = PUBLIC_FOLDER.getAbsolutePath();

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);

    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, TorchHttpRequest torchRequest, TorchHttpResponse torchResponse) {
        File file = new File(PUBLIC_FOLDER, torchRequest.getUri());

        if (!this.validatePath(file) || !file.exists() || !file.isFile()) {
            sendErrorResponse(ctx, HttpResponseStatus.NOT_FOUND, torchRequest);

            return;
        }

        try {
            // Cache Validation
            HeaderVariable ifModifiedSince = torchRequest.getHeaderData().getHeader(HttpHeaders.Names.IF_MODIFIED_SINCE);

            if (ifModifiedSince != null) {
                try {
                    Date lastModificationDate = dateFormatter.parse(ifModifiedSince.getValue());

                    if (this.isFileModified(lastModificationDate, file)) {
                        this.sendNotModified(ctx);

                        return;
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(FileRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            this.writeHeaders(ctx, file, torchRequest);
            this.writeData(ctx, file, torchRequest);
        } catch (IOException ex) {
            Logger.getLogger(FileRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);

            sendErrorResponse(ctx, HttpResponseStatus.NOT_FOUND, torchRequest);
        }
    }

    private boolean validatePath(File file) {
        try {
            String requestPath = file.getCanonicalPath();

            if (!requestPath.startsWith(PUBLIC_PATH)) {
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(FileRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    private void writeHeaders(ChannelHandlerContext ctx, File file, TorchHttpRequest torchRequest) throws IOException {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        this.setHeaders(response, file, torchRequest);

        ctx.write(response);
    }

    private void writeData(ChannelHandlerContext ctx, File file, TorchHttpRequest torchRequest) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, raf.length()), ctx.newProgressivePromise());

        // Write the end marker
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        if (!torchRequest.isKeepAlive()) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void setHeaders(HttpResponse response, File file, TorchHttpRequest torchRequest) throws IOException {
        HttpHeaders.setContentLength(response, file.length());
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);

        if (torchRequest.isKeepAlive()) {
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a
     * "304 Not Modified"
     *
     * @param ctx Context
     */
    private void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);

        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(new Date(System.currentTimeMillis())));

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response HTTP response
     * @param fileToCache file to extract content type
     */
    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(new Date(System.currentTimeMillis())));
        response.headers().set(HttpHeaders.Names.EXPIRES, dateFormatter.format(new Date(System.currentTimeMillis() + (HTTP_CACHE_SECONDS * 1000))));
        response.headers().set(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file file to extract content type
     */
    private static void setContentTypeHeader(HttpResponse response, File file) throws IOException {
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeDetector.getMimeByExtension(FilenameUtils.getExtension(file.getName())));
    }

    private boolean isFileModified(Date lastModified, File file) {
        return lastModified.getTime() / 1000 == file.lastModified() / 1000;
    }

}
