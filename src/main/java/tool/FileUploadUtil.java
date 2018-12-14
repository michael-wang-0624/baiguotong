package tool;

import java.io.InputStream;

import io.vertx.ext.web.FileUpload;

 
 

public class FileUploadUtil {
	private void save(FileUpload fileUpload) {
	 

        String[] fileAbsolutePath={};
        String fileName= fileUpload.fileName();
       
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
  
        
   /*     FastDFSFile file = new FastDFSFile(fileName, file_buff, ext);
        try {
            fileAbsolutePath = FastDFSClient.upload(file);  //upload to fastdfs
        } catch (Exception e) {
            logger.error("upload file Exception!",e);
        }
        if (fileAbsolutePath==null) {
            logger.error("upload file failed,please upload again!");
        }
        String path=FastDFSClient.getTrackerUrl()+fileAbsolutePath[0]+ "/"+fileAbsolutePath[1];
        return path;*/
    

	}
}
