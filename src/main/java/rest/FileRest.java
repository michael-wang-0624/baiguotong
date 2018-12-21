package rest;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import org.apache.log4j.Logger;
import tool.FastDFSFile;
import tool.FastDFSClient;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Set;

public class FileRest {
    private static final Logger logger = Logger.getLogger(FileRest.class);
    private Vertx vertx;

    public FileRest(Vertx vertx) {
        this.vertx = vertx;
    }

    public void savePhoto(RoutingContext routeContext) {
        logger.info("savePhoto");
        HttpServerResponse response = routeContext.response();
        response.setChunked(true);
        Set<FileUpload> fileUploads = routeContext.fileUploads();
        for (FileUpload fileUpload : fileUploads) {
            Buffer uploadedFile = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
            try {
                String fileName = URLDecoder.decode(fileUpload.fileName(), "UTF-8");
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                byte[] bytes = uploadedFile.getBytes();
                FastDFSFile file = new FastDFSFile(fileName, bytes, ext);
                String[] upload = FastDFSClient.upload(file);
                //Arrays.asList(upload).toString();
                String fullName = upload[0] + "/" + upload[1];
                JsonObject body = new JsonObject()
                        .put("url", fullName);

                JsonObject ll = new JsonObject();
                ll.put("body",body).put("statuCode",200);

                routeContext.response().putHeader("content-type", "application/json;charset=UTF-8")
                        .end(Json.encodePrettily(ll));

            }   catch (Exception e) {
                logger.error("upload Exception" + e.getCause());
            }
        }

    }



    public void uploadFile(RoutingContext routeContext) {
        logger.info("uploadFile");
        HttpServerResponse response = routeContext.response();
        response.setChunked(true);
        Set<FileUpload> fileUploads = routeContext.fileUploads();

        Iterator<FileUpload> iterator = fileUploads.iterator();

        try {
            while (iterator.hasNext()) {
                FileUpload fileUpload = iterator.next();
                Buffer uploadedFile = vertx.fileSystem().readFileBlocking(fileUpload.uploadedFileName());
                String fileName = URLDecoder.decode(fileUpload.fileName(), "UTF-8");
                String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
                byte[] bytes = uploadedFile.getBytes();
                FastDFSFile file = new FastDFSFile(fileName, bytes, ext);
                String[] upload = FastDFSClient.upload(file);
                String fullName = upload[0] + "/" + upload[1];
                response.write(fullName).end();

            }
        } catch (Exception e ) {
            logger.error("upload Exception" + e.getCause());
    }
    }
}